package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位管控定时任务服务
 * <p>
 * 负责以下自动化处理：
 * 1. 超时未签到自动释放（标记为爽约）
 * 2. 暂离超时自动标记爽约
 * 3. 已结束预约自动完成
 * 4. 自动检查并加入黑名单
 * 5. 自动解除过期黑名单
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatScheduledService {

    private final ReservationMapper reservationMapper;
    private final NoShowRecordService noShowRecordService;
    private final BlacklistService blacklistService;

    /** 签到宽限时间（分钟），开始时间后多久判定爽约，默认50 */
    @Value("${booking.rules.checkin-grace-minutes:50}")
    private int checkinGraceMinutes;

    /** 签到截止时间（分钟），预约创建后必须在此时间内签到，默认10 */
    @Value("${booking.rules.checkin-deadline-minutes:10}")
    private int checkinDeadlineMinutes;

    /** 暂离保留时间（分钟），默认30 */
    @Value("${booking.rules.temporary-absence-minutes:30}")
    private int temporaryAbsenceMinutes;

    // ===================== 定时任务 =====================

    /**
     * 处理超时未签到的预约
     * <p>
     * 每分钟执行一次。
     * 提前预约（创建时间 < 开始时间）：开始时间+checkinGraceMinutes未签到 → 爽约
     * 开始后预约（创建时间 >= 开始时间）：创建时间+checkinGraceMinutes未签到 → 爽约
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processNoShowReservations() {
        LocalDateTime now = LocalDateTime.now();

        // 查询所有未签到的预约，在 Java 中按两种情况过滤
        List<Reservation> allReserved = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, "RESERVED")
        );

        List<Reservation> allExpired = new java.util.ArrayList<>();
        for (Reservation r : allReserved) {
            LocalDateTime deadline;
            if (r.getCreatedAt().isBefore(r.getStartTime())) {
                // 提前预约：爽约判定 = 开始时间 + checkinGraceMinutes
                deadline = r.getStartTime().plusMinutes(checkinGraceMinutes);
            } else {
                // 开始后预约：爽约判定 = 创建时间 + checkinGraceMinutes
                deadline = r.getCreatedAt().plusMinutes(checkinGraceMinutes);
            }
            if (now.isAfter(deadline)) {
                allExpired.add(r);
            }
        }

        for (Reservation reservation : allExpired) {
            // 创建爽约记录（物理删除前）
            noShowRecordService.createNoShowRecord(
                    reservation.getUserId(),
                    reservation.getId(),
                    "NO_CHECKIN"
            );

            // 标记为爽约状态，保留历史记录
            reservation.setStatus("NO_SHOW");
            reservationMapper.updateById(reservation);

            log.info("预约 {} 超时未签到，已标记为爽约。用户: {}, 座位: {}",
                    reservation.getId(), reservation.getUserId(), reservation.getSeatId());
        }
    }

    /**
     * 处理暂离超时
     * <p>
     * 每30秒执行一次（暂离时间敏感度高）。
     * 查找状态为 TEMPORARY_LEAVE 且暂离时间已超过限制的预约，
     * 将其标记为 NO_SHOW，创建爽约记录。
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processTemporaryLeaveTimeout() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(temporaryAbsenceMinutes);

        // 查找暂离超时的预约
        List<Reservation> timeoutReservations = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, "TEMPORARY_LEAVE")
                        .lt(Reservation::getTemporaryLeaveTime, deadline)
        );

        for (Reservation reservation : timeoutReservations) {
            // 创建爽约记录（物理删除前）
            noShowRecordService.createNoShowRecord(
                    reservation.getUserId(),
                    reservation.getId(),
                    "TEMPORARY_LEAVE_TIMEOUT"
            );

            // 标记为爽约状态，保留历史记录
            reservation.setStatus("NO_SHOW");
            reservationMapper.updateById(reservation);

            log.info("预约 {} 暂离超时（{}分钟），已标记为爽约。用户: {}",
                    reservation.getId(), temporaryAbsenceMinutes, reservation.getUserId());
        }
    }

    /**
     * 自动完成已结束的预约
     * <p>
     * 每2分钟执行一次。
     * 查找状态为 CHECKED_IN 或 TEMPORARY_LEAVE 且 end_time 已过的预约，
     * 自动将其标记为 COMPLETED。
     */
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void autoCompleteExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        // 查找已到结束时间但仍在使用中的预约
        List<Reservation> expiredReservations = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .in(Reservation::getStatus, "CHECKED_IN", "TEMPORARY_LEAVE")
                        .lt(Reservation::getEndTime, now)
        );

        for (Reservation reservation : expiredReservations) {
            // 标记为已完成状态，保留历史记录
            reservation.setStatus("COMPLETED");
            reservation.setCheckoutTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);

            log.info("预约 {} 已到结束时间，自动标记为已完成。用户: {}", reservation.getId(), reservation.getUserId());
        }
    }

    /**
     * 自动检查爽约并加入黑名单
     * <p>
     * 每5分钟执行一次。
     * 统计各用户近7天的爽约次数，达到阈值则自动加入黑名单。
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoBlacklistCheck() {
        log.debug("开始自动黑名单检查...");
        blacklistService.autoCheckAndAddToBlacklist();
    }

    /**
     * 自动解除过期黑名单
     * <p>
     * 每5分钟执行一次。
     * 查找已到结束时间的黑名单记录，自动解除。
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoReleaseBlacklist() {
        log.debug("开始自动解除过期黑名单...");
        blacklistService.autoReleaseExpiredBlacklist();
    }
}

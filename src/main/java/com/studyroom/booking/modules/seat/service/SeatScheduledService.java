package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    /** 签到提前宽限时间（分钟），默认50 */
    @Value("${booking.rules.checkin-grace-minutes:50}")
    private int checkinGraceMinutes;

    /** 签到截止期限（分钟），预约开始后/当场预约后多久未签到则爽约，默认10 */
    @Value("${booking.rules.checkin-deadline-minutes:10}")
    private int checkinDeadlineMinutes;

    /** 暂离保留时间（分钟），默认30 */
    @Value("${booking.rules.temporary-absence-minutes:30}")
    private int temporaryAbsenceMinutes;

    /** 预约开始前提醒时间（分钟），默认15分钟 */
    @Value("${booking.rules.booking-reminder-minutes:15}")
    private int bookingReminderMinutes;

    // ===================== 定时任务 =====================

    /**
     * 处理超时未签到的预约
     * <p>
     * 每分钟执行一次。
     * 提前预约（创建时间 < 开始时间）：开始时间+checkinDeadlineMinutes未签到 → 爽约
     * 开始后预约（创建时间 >= 开始时间）：创建时间+checkinDeadlineMinutes未签到 → 爽约
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processNoShowReservations() {
        LocalDateTime now = LocalDateTime.now();

        // SQL 层面过滤：只查询可能已经过期的 RESERVED 记录（startTime 在 deadline 之前）
        // 避免每次全表扫描所有 RESERVED 记录
        LocalDateTime latestPossibleStart = now.minusMinutes(checkinDeadlineMinutes + 1);
        List<Reservation> allReserved = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, "RESERVED")
                        .le(Reservation::getStartTime, latestPossibleStart)
        );

        List<Reservation> allExpired = new java.util.ArrayList<>();
        for (Reservation r : allReserved) {
            // 防护：createdAt 为空时跳过（数据异常，记录日志）
            if (r.getCreatedAt() == null || r.getStartTime() == null) {
                log.warn("预约 {} 的 createdAt 或 startTime 为空，跳过爽约检查", r.getId());
                continue;
            }
            LocalDateTime deadline;
            if (r.getCreatedAt().isBefore(r.getStartTime())) {
                // 提前预约：爽约判定 = 开始时间 + checkinDeadlineMinutes
                deadline = r.getStartTime().plusMinutes(checkinDeadlineMinutes);
            } else {
                // 开始后预约：爽约判定 = 创建时间 + checkinDeadlineMinutes
                deadline = r.getCreatedAt().plusMinutes(checkinDeadlineMinutes);
            }
            if (now.isAfter(deadline)) {
                allExpired.add(r);
            }
        }

        for (Reservation reservation : allExpired) {
            try {
                // 创建爽约记录
                noShowRecordService.createNoShowRecord(
                        reservation.getUserId(),
                        reservation.getId(),
                        "NO_CHECKIN"
                );

                // 标记为爽约状态，保留历史记录
                reservation.setStatus("NO_SHOW");
                reservationMapper.updateById(reservation);

                // 发送爽约通知
                try {
                    String content = String.format("很遗憾，您的预约已被记录为爽约（未签到）。请珍惜预约资源，多次爽约将被加入黑名单。");
                    notificationService.sendNotification(reservation.getUserId(), "NO_SHOW",
                            "爽约提醒", content,
                            String.format("{\"reservationId\":%d,\"reason\":\"NO_CHECKIN\"}", reservation.getId()));
                } catch (Exception e) {
                    log.warn("发送爽约通知失败，userId={}, reservationId={}",
                            reservation.getUserId(), reservation.getId(), e);
                }

                log.info("预约 {} 超时未签到，已标记为爽约。用户: {}, 座位: {}",
                        reservation.getId(), reservation.getUserId(), reservation.getSeatId());
            } catch (Exception e) {
                log.error("处理预约 {} 爽约时发生异常: {}", reservation.getId(), e.getMessage(), e);
            }
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
            try {
                // 创建爽约记录
                noShowRecordService.createNoShowRecord(
                        reservation.getUserId(),
                        reservation.getId(),
                        "TEMPORARY_LEAVE_TIMEOUT"
                );

                // 标记为爽约状态，保留历史记录
                reservation.setStatus("NO_SHOW");
                reservationMapper.updateById(reservation);

                // 发送暂离超时爽约通知
                try {
                    String content = String.format("很遗憾，您暂离座位已超过 %d 分钟，被记录为爽约。请珍惜预约资源，多次爽约将被加入黑名单。",
                            temporaryAbsenceMinutes);
                    notificationService.sendNotification(reservation.getUserId(), "NO_SHOW",
                            "爽约提醒", content,
                            String.format("{\"reservationId\":%d,\"reason\":\"TEMPORARY_LEAVE_TIMEOUT\"}", reservation.getId()));
                } catch (Exception e) {
                    log.warn("发送暂离超时爽约通知失败，userId={}, reservationId={}",
                            reservation.getUserId(), reservation.getId(), e);
                }

                log.info("预约 {} 暂离超时（{}分钟），已标记为爽约。用户: {}",
                        reservation.getId(), temporaryAbsenceMinutes, reservation.getUserId());
            } catch (Exception e) {
                log.error("处理预约 {} 暂离超时时发生异常: {}", reservation.getId(), e.getMessage(), e);
            }
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
     * 预约开始前提醒
     * <p>
     * 每5分钟执行一次。
     * 查找距离开始时间不足 bookingReminderMinutes 分钟且未签到的 RESERVED 预约，
     * 发送提醒通知（通过通知表去重，避免重复提醒）。
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remindWindowEnd = now.plusMinutes(bookingReminderMinutes);

        // 查询即将开始的 RESERVED 预约（startTime 在 now ~ now+reminderMinutes 之间）
        List<Reservation> upcomingReservations = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, "RESERVED")
                        .ge(Reservation::getStartTime, now)
                        .le(Reservation::getStartTime, remindWindowEnd)
        );

        for (Reservation reservation : upcomingReservations) {
            try {
                // 幂等检查：通知表中是否已有该预约的提醒通知
                boolean alreadyReminded = notificationService.existsByUserIdAndTypeAndDataContains(
                        reservation.getUserId(), "BOOKING_REMINDER",
                        "\"reservationId\":" + reservation.getId());
                if (alreadyReminded) {
                    continue;
                }

                // 发送预约提醒通知
                String content = String.format("您的预约即将开始，请及时前往签到。时间：%s ~ %s。",
                        reservation.getStartTime() != null ? reservation.getStartTime().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "",
                        reservation.getEndTime() != null ? reservation.getEndTime().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                notificationService.sendNotification(reservation.getUserId(), "BOOKING_REMINDER",
                        "预约即将开始", content,
                        String.format("{\"reservationId\":%d}", reservation.getId()));

                log.info("已发送预约开始提醒，预约ID: {}, 用户: {}",
                        reservation.getId(), reservation.getUserId());
            } catch (Exception e) {
                log.error("发送预约提醒失败，预约ID: {}", reservation.getId(), e);
            }
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

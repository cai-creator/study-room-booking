package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.notification.service.NotificationService;
import com.studyroom.booking.modules.seat.dto.CheckinVO;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.entity.SeatControl;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import com.studyroom.booking.modules.seat.mapper.SeatControlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 签到/签退/暂离/返回 服务
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final ReservationMapper reservationMapper;
    private final SeatControlMapper seatMapper;
    private final NotificationService notificationService;

    /** 签到提前宽限时间（分钟），预约开始前多久可以开始签到，默认50 */
    @Value("${booking.rules.checkin-grace-minutes:50}")
    private int checkinGraceMinutes;

    /** 签到截止期限（分钟），预约开始后/当场预约后多久必须签到，默认10 */
    @Value("${booking.rules.checkin-deadline-minutes:10}")
    private int checkinDeadlineMinutes;

    /** 暂离保留时间（分钟），默认30 */
    @Value("${booking.rules.temporary-absence-minutes:30}")
    private int temporaryAbsenceMinutes;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== 签到 =====================

    /**
     * 签到
     * <p>
     * 学生在预约时段开始前后的一定时间内到达自习室并签到。
     * 签到成功后，预约状态从 RESERVED 变为 CHECKED_IN。
     * 支持幂等性检查：如果已经签到，返回成功响应。
     *
     * @param seatCode 座位编号
     * @param roomId   自习室ID
     * @return 签到结果
     */
    @Transactional
    public CheckinVO checkin(String seatCode, Long roomId) {
        Long userId = UserContext.getUserId();

        // 1. 查找座位
        SeatControl seat = seatMapper.selectOne(
                new LambdaQueryWrapper<SeatControl>()
                        .eq(SeatControl::getSeatCode, seatCode)
                        .eq(SeatControl::getRoomId, roomId)
                        .eq(SeatControl::getStatus, 1)
        );
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }

        // 2. 查找该用户在该座位的预约（幂等性检查：RESERVED 或 CHECKED_IN）
        Reservation reservation = reservationMapper.selectOne(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, userId)
                        .eq(Reservation::getSeatId, seat.getId())
                        .in(Reservation::getStatus, "RESERVED", "CHECKED_IN")
                        .orderByDesc(Reservation::getStartTime)
                        .last("LIMIT 1")
        );

        if (reservation == null) {
            throw new BusinessException(ResultCode.CHECKIN_NOT_ALLOWED);
        }

        // 3. 幂等性检查：如果已经签到，直接返回成功
        if ("CHECKED_IN".equals(reservation.getStatus())) {
            log.info("用户 {} 重复签到（已签到），预约ID: {}, 座位: {}", userId, reservation.getId(), seatCode);
            return buildCheckinVO(reservation, seat);
        }

        // 4. 验证签到时间
        // 提前预约（创建时间 < 开始时间）：
        //   最早签到 = startTime - checkinGraceMinutes（可提前签到）
        //   最晚签到 = startTime + checkinDeadlineMinutes（开始后必须在这段时间内签到）
        // 当场预约（创建时间 >= 开始时间）：
        //   最早签到 = createdAt（创建后即可签到）
        //   最晚签到 = createdAt + checkinDeadlineMinutes
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestCheckin;
        LocalDateTime latestCheckin;

        if (reservation.getCreatedAt().isBefore(reservation.getStartTime())) {
            earliestCheckin = reservation.getStartTime().minusMinutes(checkinGraceMinutes);
            latestCheckin = reservation.getStartTime().plusMinutes(checkinDeadlineMinutes);
        } else {
            earliestCheckin = reservation.getCreatedAt();
            latestCheckin = reservation.getCreatedAt().plusMinutes(checkinDeadlineMinutes);
        }

        if (now.isBefore(earliestCheckin)) {
            throw new BusinessException(ResultCode.CHECKIN_TOO_EARLY);
        }

        if (now.isAfter(latestCheckin)) {
            throw new BusinessException(ResultCode.CHECKIN_TIMEOUT);
        }

        // 5. 更新预约状态为已签到
        reservation.setStatus("CHECKED_IN");
        reservation.setCheckinTime(now);
        reservation.setUpdatedAt(now);
        reservationMapper.updateById(reservation);

        log.info("用户 {} 签到成功，预约ID: {}, 座位: {}", userId, reservation.getId(), seatCode);

        // 发送签到成功通知
        try {
            String content = String.format("您已成功签到，座位：%s，时间：%s ~ %s。",
                    seatCode,
                    reservation.getStartTime() != null ? reservation.getStartTime().format(FORMATTER) : "",
                    reservation.getEndTime() != null ? reservation.getEndTime().format(FORMATTER) : "");
            notificationService.sendNotification(userId, "CHECKIN_SUCCESS",
                    "签到成功", content,
                    String.format("{\"reservationId\":%d,\"seatCode\":\"%s\"}", reservation.getId(), seatCode));
        } catch (Exception e) {
            log.warn("发送签到成功通知失败，userId={}, reservationId={}", userId, reservation.getId(), e);
        }

        // 6. 构建返回对象
        return buildCheckinVO(reservation, seat);
    }

    // ===================== 签退 =====================

    /**
     * 签退
     * <p>
     * 学生离开自习室时签退，物理删除预约记录以释放唯一约束。
     *
     * @param seatCode 座位编号
     * @param roomId   自习室ID
     * @return 签退结果
     */
    @Transactional
    public CheckinVO checkout(String seatCode, Long roomId) {
        Long userId = UserContext.getUserId();

        // 1. 获取当前活跃的预约（CHECKED_IN 或 TEMPORARY_LEAVE）
        Reservation reservation = getActiveReservation(userId, seatCode, roomId);

        if (!"CHECKED_IN".equals(reservation.getStatus()) && !"TEMPORARY_LEAVE".equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.CHECKOUT_NOT_ALLOWED);
        }

        // 2. 构建返回对象
        CheckinVO vo = buildCheckinVO(reservation, null);

        // 3. 状态变更为已完成，保留历史记录
        reservation.setStatus("COMPLETED");
        reservation.setCheckoutTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        log.info("用户 {} 签退成功，预约ID: {}", userId, reservation.getId());

        // 发送签退成功通知
        try {
            String content = String.format("您已成功签退，座位：%s，时长：%s ~ %s。",
                    seatCode,
                    reservation.getStartTime() != null ? reservation.getStartTime().format(FORMATTER) : "",
                    reservation.getCheckoutTime() != null ? reservation.getCheckoutTime().format(FORMATTER) : "");
            notificationService.sendNotification(userId, "CHECKOUT_SUCCESS",
                    "签退成功", content,
                    String.format("{\"reservationId\":%d,\"seatCode\":\"%s\"}", reservation.getId(), seatCode));
        } catch (Exception e) {
            log.warn("发送签退成功通知失败，userId={}, reservationId={}", userId, reservation.getId(), e);
        }

        return vo;
    }

    // ===================== 暂离 =====================

    /**
     * 暂离
     * <p>
     * 学生暂时离开座位（如去洗手间、短暂休息），
     * 预约状态从 CHECKED_IN 变为 TEMPORARY_LEAVE。
     * 暂离有时间限制，超时未返回将被记录为爽约。
     *
     * @param seatCode 座位编号
     * @param roomId   自习室ID
     * @return 暂离结果
     */
    @Transactional
    public CheckinVO temporaryLeave(String seatCode, Long roomId) {
        Long userId = UserContext.getUserId();

        // 1. 获取当前活跃的预约（必须是 CHECKED_IN）
        Reservation reservation = getActiveReservation(userId, seatCode, roomId);

        if (!"CHECKED_IN".equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.TEMPORARY_LEAVE_NOT_ALLOWED);
        }

        // 2. 更新为暂离状态
        reservation.setStatus("TEMPORARY_LEAVE");
        reservation.setTemporaryLeaveTime(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        log.info("用户 {} 暂离，预约ID: {}, 暂离截止: {}分钟后",
                userId, reservation.getId(), temporaryAbsenceMinutes);

        // 发送暂离通知
        try {
            String content = String.format("您已暂离座位 %s，请在 %d 分钟内返回，否则将被记录为爽约。",
                    seatCode, temporaryAbsenceMinutes);
            notificationService.sendNotification(userId, "TEMPORARY_LEAVE",
                    "已暂离", content,
                    String.format("{\"reservationId\":%d,\"seatCode\":\"%s\"}", reservation.getId(), seatCode));
        } catch (Exception e) {
            log.warn("发送暂离通知失败，userId={}, reservationId={}", userId, reservation.getId(), e);
        }

        return buildCheckinVO(reservation, null);
    }

    // ===================== 返回座位 =====================

    /**
     * 暂离后返回座位
     * <p>
     * 学生暂离后返回，预约状态从 TEMPORARY_LEAVE 恢复为 CHECKED_IN。
     *
     * @param seatCode 座位编号
     * @param roomId   自习室ID
     * @return 返回结果
     */
    @Transactional
    public CheckinVO returnSeat(String seatCode, Long roomId) {
        Long userId = UserContext.getUserId();

        // 1. 获取当前活跃的预约（必须是 TEMPORARY_LEAVE）
        Reservation reservation = getActiveReservation(userId, seatCode, roomId);

        if (!"TEMPORARY_LEAVE".equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.RETURN_SEAT_NOT_ALLOWED);
        }

        // 2. 检查暂离是否超时
        if (reservation.getTemporaryLeaveTime() != null) {
            LocalDateTime leaveDeadline = reservation.getTemporaryLeaveTime().plusMinutes(temporaryAbsenceMinutes);
            if (LocalDateTime.now().isAfter(leaveDeadline)) {
                throw new BusinessException(ResultCode.TEMPORARY_LEAVE_TIMEOUT);
            }
        }

        // 3. 恢复为已签到状态
        reservation.setStatus("CHECKED_IN");
        reservation.setTemporaryLeaveTime(null);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        log.info("用户 {} 返回座位，预约ID: {}", userId, reservation.getId());

        // 发送返回座位通知
        try {
            String content = String.format("您已返回座位 %s，继续您的学习吧。", seatCode);
            notificationService.sendNotification(userId, "RETURN_SEAT",
                    "已返回座位", content,
                    String.format("{\"reservationId\":%d,\"seatCode\":\"%s\"}", reservation.getId(), seatCode));
        } catch (Exception e) {
            log.warn("发送返回座位通知失败，userId={}, reservationId={}", userId, reservation.getId(), e);
        }

        return buildCheckinVO(reservation, null);
    }

    // ===================== 辅助方法 =====================

    /**
     * 获取用户当前活跃的预约记录
     */
    private Reservation getActiveReservation(Long userId, String seatCode, Long roomId) {
        // 先找座位
        SeatControl seat = seatMapper.selectOne(
                new LambdaQueryWrapper<SeatControl>()
                        .eq(SeatControl::getSeatCode, seatCode)
                        .eq(SeatControl::getRoomId, roomId)
                        .eq(SeatControl::getStatus, 1)
        );
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }

        // 找该座位的活跃预约
        Reservation reservation = reservationMapper.selectOne(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, userId)
                        .eq(Reservation::getSeatId, seat.getId())
                        .in(Reservation::getStatus, "CHECKED_IN", "TEMPORARY_LEAVE", "RESERVED")
                        .orderByDesc(Reservation::getStartTime)
                        .last("LIMIT 1")
        );

        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }

        return reservation;
    }

    /**
     * 构建返回VO
     */
    private CheckinVO buildCheckinVO(Reservation reservation, SeatControl seat) {
        CheckinVO vo = new CheckinVO();
        vo.setReservationId(reservation.getId());
        vo.setRoomId(reservation.getRoomId());
        vo.setStartTime(reservation.getStartTime() != null ? reservation.getStartTime().format(FORMATTER) : null);
        vo.setEndTime(reservation.getEndTime() != null ? reservation.getEndTime().format(FORMATTER) : null);
        vo.setCheckinTime(reservation.getCheckinTime() != null ? reservation.getCheckinTime().format(FORMATTER) : null);
        vo.setCheckoutTime(reservation.getCheckoutTime() != null ? reservation.getCheckoutTime().format(FORMATTER) : null);
        vo.setTemporaryLeaveTime(reservation.getTemporaryLeaveTime() != null
                ? reservation.getTemporaryLeaveTime().format(FORMATTER) : null);
        vo.setStatus(reservation.getStatus());
        if (seat != null) {
            vo.setSeatCode(seat.getSeatCode());
        }
        return vo;
    }
}

package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
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

    /** 签到宽限时间（分钟），开始时间后多久判定爽约，默认50 */
    @Value("${booking.rules.checkin-grace-minutes:50}")
    private int checkinGraceMinutes;

    /** 签到截止时间（分钟），预约创建后必须在此时间内签到，默认10 */
    @Value("${booking.rules.checkin-deadline-minutes:10}")
    private int checkinDeadlineMinutes;

    /** 暂离保留时间（分钟），默认30分钟 */
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
        // 提前预约（创建时间 < 开始时间）：签到窗口为开始时间 ~ 开始时间+checkinGraceMinutes
        // 开始后预约（创建时间 >= 开始时间）：签到窗口为创建时间 ~ 创建时间+checkinGraceMinutes
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestCheckin;
        LocalDateTime latestCheckin;

        if (reservation.getCreatedAt().isBefore(reservation.getStartTime())) {
            earliestCheckin = reservation.getStartTime();
            latestCheckin = reservation.getStartTime().plusMinutes(checkinGraceMinutes);
        } else {
            earliestCheckin = reservation.getCreatedAt();
            latestCheckin = reservation.getCreatedAt().plusMinutes(checkinGraceMinutes);
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

        // 2. 构建返回对象（物理删除前）
        CheckinVO vo = buildCheckinVO(reservation, null);

        // 3. 物理删除预约记录，释放唯一约束 (seat_id, start_time, end_time)
        reservationMapper.physicalDeleteById(reservation.getId());

        log.info("用户 {} 签退成功，预约ID: {}", userId, reservation.getId());

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

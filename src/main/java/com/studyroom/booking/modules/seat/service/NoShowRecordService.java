package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.modules.seat.dto.NoShowRecordVO;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.entity.SeatControl;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import com.studyroom.booking.modules.seat.mapper.SeatControlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 爽约记录管理服务
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoShowRecordService {

    private final NoShowRecordMapper noShowRecordMapper;
    private final ReservationMapper reservationMapper;
    private final SeatControlMapper seatMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_PAGE_SIZE = 100;

    // ===================== 查询 =====================

    /**
     * 分页查询爽约记录（管理员）
     */
    public Page<NoShowRecordVO> getNoShowRecordPage(Integer pageNum, Integer pageSize, Long userId, String startDate, String endDate) {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        Page<NoShowRecord> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<NoShowRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(NoShowRecord::getUserId, userId);
        }

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(NoShowRecord::getRecordDate, LocalDate.parse(startDate));
        }

        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(NoShowRecord::getRecordDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(NoShowRecord::getCreatedAt);

        Page<NoShowRecord> recordPage = noShowRecordMapper.selectPage(page, wrapper);

        // 批量查询关联数据，避免 N+1
        List<NoShowRecord> records = recordPage.getRecords();
        Map<Long, Reservation> reservationMap = Collections.emptyMap();
        Map<Long, SeatControl> seatMap = Collections.emptyMap();

        if (!records.isEmpty()) {
            List<Long> reservationIds = records.stream()
                    .map(NoShowRecord::getReservationId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!reservationIds.isEmpty()) {
                List<Reservation> reservations = reservationMapper.selectBatchIds(reservationIds);
                reservationMap = reservations.stream()
                        .collect(Collectors.toMap(Reservation::getId, r -> r, (a, b) -> a));

                List<Long> seatIds = reservations.stream()
                        .map(Reservation::getSeatId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                if (!seatIds.isEmpty()) {
                    List<SeatControl> seats = seatMapper.selectBatchIds(seatIds);
                    seatMap = seats.stream()
                            .collect(Collectors.toMap(SeatControl::getId, s -> s, (a, b) -> a));
                }
            }
        }

        // 转换为VO
        Page<NoShowRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        final Map<Long, Reservation> finalReservationMap = reservationMap;
        final Map<Long, SeatControl> finalSeatMap = seatMap;
        List<NoShowRecordVO> voList = records.stream()
                .map(record -> convertToVO(record, finalReservationMap, finalSeatMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询我的爽约记录（学生）
     */
    public Page<NoShowRecordVO> getMyNoShowRecords(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        return getNoShowRecordPage(pageNum, pageSize, userId, null, null);
    }

    // ===================== 创建（供定时任务和签到处调用） =====================

    /**
     * 创建爽约记录
     *
     * @param userId        用户ID
     * @param reservationId 预约ID
     * @param reason        爽约原因: NO_CHECKIN-未签到, TEMPORARY_LEAVE_TIMEOUT-暂离超时
     */
    @Transactional
    public void createNoShowRecord(Long userId, Long reservationId, String reason) {
        NoShowRecord record = new NoShowRecord();
        record.setUserId(userId);
        record.setReservationId(reservationId);
        record.setReason(reason);
        record.setRecordDate(LocalDate.now());
        noShowRecordMapper.insert(record);

        log.info("创建爽约记录: 用户={}, 预约={}, 原因={}", userId, reservationId, reason);
    }

    /**
     * 查询用户在一定天数内的爽约次数
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 爽约次数
     */
    public long getNoShowCountInDays(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return noShowRecordMapper.selectCount(
                new LambdaQueryWrapper<NoShowRecord>()
                        .eq(NoShowRecord::getUserId, userId)
                        .ge(NoShowRecord::getRecordDate, startDate)
        );
    }

    // ===================== 辅助方法 =====================

    private NoShowRecordVO convertToVO(NoShowRecord record, Map<Long, Reservation> reservationMap, Map<Long, SeatControl> seatMap) {
        NoShowRecordVO vo = new NoShowRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setReservationId(record.getReservationId());
        vo.setReason(record.getReason());
        vo.setRecordDate(record.getRecordDate() != null ? record.getRecordDate().toString() : null);
        vo.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().format(FORMATTER) : null);

        // 从批量查询的结果中获取预约和座位信息
        Reservation reservation = reservationMap.get(record.getReservationId());
        if (reservation != null) {
            SeatControl seat = seatMap.get(reservation.getSeatId());
            if (seat != null) {
                vo.setSeatCode(seat.getSeatCode());
            }
        }

        return vo;
    }
}

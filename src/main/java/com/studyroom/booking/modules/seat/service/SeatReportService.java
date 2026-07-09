package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 座位管控数据报表 Service
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatReportService {

    private final ReservationMapper reservationMapper;
    private final NoShowRecordMapper noShowRecordMapper;

    /**
     * 爽约率统计
     */
    public Map<String, Object> getNoShowRate(String startDate, String endDate, Long roomId) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.ge(Reservation::getStartTime, startDateTime);
        reservationWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            reservationWrapper.eq(Reservation::getRoomId, roomId);
        }
        long totalReservations = reservationMapper.selectCount(reservationWrapper);

        reservationWrapper.eq(Reservation::getStatus, "NO_SHOW");
        long noShowReservations = reservationMapper.selectCount(reservationWrapper);

        LambdaQueryWrapper<NoShowRecord> noShowWrapper = new LambdaQueryWrapper<>();
        noShowWrapper.ge(NoShowRecord::getRecordDate, start);
        noShowWrapper.le(NoShowRecord::getRecordDate, end);
        long totalNoShowRecords = noShowRecordMapper.selectCount(noShowWrapper);

        double noShowRate = totalReservations > 0
                ? (double) noShowReservations / totalReservations * 100
                : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalReservations", totalReservations);
        result.put("noShowReservations", noShowReservations);
        result.put("totalNoShowRecords", totalNoShowRecords);
        result.put("noShowRate", Math.round(noShowRate * 100.0) / 100.0);

        return result;
    }

    /**
     * 预约转化率统计
     */
    public Map<String, Object> getConversionRate(String startDate, String endDate, Long roomId) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Reservation> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.ge(Reservation::getStartTime, startDateTime);
        baseWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            baseWrapper.eq(Reservation::getRoomId, roomId);
        }
        long totalReservations = reservationMapper.selectCount(baseWrapper);

        LambdaQueryWrapper<Reservation> checkedInWrapper = new LambdaQueryWrapper<>();
        checkedInWrapper.ge(Reservation::getStartTime, startDateTime);
        checkedInWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            checkedInWrapper.eq(Reservation::getRoomId, roomId);
        }
        checkedInWrapper.in(Reservation::getStatus, "CHECKED_IN", "TEMPORARY_LEAVE", "COMPLETED");
        long checkedInCount = reservationMapper.selectCount(checkedInWrapper);

        LambdaQueryWrapper<Reservation> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.ge(Reservation::getStartTime, startDateTime);
        completedWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            completedWrapper.eq(Reservation::getRoomId, roomId);
        }
        completedWrapper.eq(Reservation::getStatus, "COMPLETED");
        long completedCount = reservationMapper.selectCount(completedWrapper);

        double checkinRate = totalReservations > 0
                ? (double) checkedInCount / totalReservations * 100
                : 0.0;
        double completionRate = checkedInCount > 0
                ? (double) completedCount / checkedInCount * 100
                : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalReservations", totalReservations);
        result.put("checkedInCount", checkedInCount);
        result.put("completedCount", completedCount);
        result.put("checkinRate", Math.round(checkinRate * 100.0) / 100.0);
        result.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        return result;
    }
}

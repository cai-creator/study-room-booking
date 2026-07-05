package com.studyroom.booking.modules.seat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 座位管控相关的数据报表接口
 * <p>
 * 成员D（邓祺然）负责提供爽约率和预约转化率两个报表接口。
 *
 * @author 邓祺然
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "数据报表-座位管控", description = "爽约率、预约转化率等数据统计接口（成员D提供）")
public class SeatReportController {

    private final ReservationMapper reservationMapper;
    private final NoShowRecordMapper noShowRecordMapper;

    @GetMapping("/no-show-rate")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "爽约率统计", description = "统计指定时间段内的爽约率")
    public Result<Map<String, Object>> getNoShowRate(
            @Parameter(description = "开始日期（yyyy-MM-dd）", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        // 查询时间段内的总预约数
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.ge(Reservation::getStartTime, startDateTime);
        reservationWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            reservationWrapper.eq(Reservation::getRoomId, roomId);
        }
        long totalReservations = reservationMapper.selectCount(reservationWrapper);

        // 查询时间段内的爽约数（NO_SHOW状态的预约）
        reservationWrapper.eq(Reservation::getStatus, "NO_SHOW");
        long noShowReservations = reservationMapper.selectCount(reservationWrapper);

        // 查询爽约记录数
        LambdaQueryWrapper<NoShowRecord> noShowWrapper = new LambdaQueryWrapper<>();
        noShowWrapper.ge(NoShowRecord::getRecordDate, start);
        noShowWrapper.le(NoShowRecord::getRecordDate, end);
        long totalNoShowRecords = noShowRecordMapper.selectCount(noShowWrapper);

        // 计算爽约率
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

        return Result.success(result);
    }

    @GetMapping("/conversion-rate")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "预约转化率", description = "统计预约→签到→完成的转化率")
    public Result<Map<String, Object>> getConversionRate(
            @Parameter(description = "开始日期（yyyy-MM-dd）", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        // 基础查询条件
        LambdaQueryWrapper<Reservation> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.ge(Reservation::getStartTime, startDateTime);
        baseWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            baseWrapper.eq(Reservation::getRoomId, roomId);
        }

        // 总预约数
        long totalReservations = reservationMapper.selectCount(baseWrapper);

        // 已签到数（CHECKED_IN, TEMPORARY_LEAVE, COMPLETED都算签到过的）
        LambdaQueryWrapper<Reservation> checkedInWrapper = new LambdaQueryWrapper<>();
        checkedInWrapper.ge(Reservation::getStartTime, startDateTime);
        checkedInWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            checkedInWrapper.eq(Reservation::getRoomId, roomId);
        }
        checkedInWrapper.in(Reservation::getStatus, "CHECKED_IN", "TEMPORARY_LEAVE", "COMPLETED");
        long checkedInCount = reservationMapper.selectCount(checkedInWrapper);

        // 已完成数
        LambdaQueryWrapper<Reservation> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.ge(Reservation::getStartTime, startDateTime);
        completedWrapper.le(Reservation::getStartTime, endDateTime);
        if (roomId != null) {
            completedWrapper.eq(Reservation::getRoomId, roomId);
        }
        completedWrapper.eq(Reservation::getStatus, "COMPLETED");
        long completedCount = reservationMapper.selectCount(completedWrapper);

        // 计算转化率
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

        return Result.success(result);
    }
}

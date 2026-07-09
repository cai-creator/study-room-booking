package com.studyroom.booking.modules.seat.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.seat.service.SeatReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 座位管控相关的数据报表接口
 *
 * @author 邓祺然
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "数据报表-座位管控", description = "爽约率、预约转化率等数据统计接口（成员D提供）")
public class SeatReportController {

    private final SeatReportService seatReportService;

    @GetMapping("/no-show-rate")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "爽约率统计", description = "统计指定时间段内的爽约率")
    public Result<Map<String, Object>> getNoShowRate(
            @Parameter(description = "开始日期（yyyy-MM-dd）", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {
        return Result.success(seatReportService.getNoShowRate(startDate, endDate, roomId));
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
        return Result.success(seatReportService.getConversionRate(startDate, endDate, roomId));
    }
}

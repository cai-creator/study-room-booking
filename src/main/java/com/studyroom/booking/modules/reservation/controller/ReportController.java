package com.studyroom.booking.modules.reservation.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.reservation.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 数据报表控制器（成员C负责：使用率、时段分布、热门时段、导出）
 *
 * @author 郭学威
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "数据报表", description = "使用率、时段分布、热门时段、导出报表接口")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/usage-rate")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "日均使用率统计", description = "统计时间范围内每天每个自习室的座位使用率")
    public Result<List<Map<String, Object>>> usageRate(
            @Parameter(description = "开始日期 (yyyy-MM-dd)", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {
        return Result.success(reportService.getUsageRate(startDate, endDate, roomId));
    }

    @GetMapping("/time-distribution")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "时段占用分布", description = "统计时间范围内每个小时段的预约数量（24小时分布）")
    public Result<List<Map<String, Object>>> timeDistribution(
            @Parameter(description = "开始日期 (yyyy-MM-dd)", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {
        return Result.success(reportService.getTimeDistribution(startDate, endDate, roomId));
    }

    @GetMapping("/hot-periods")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "热门时段TOP5", description = "统计时间范围内预约数最多的5个时段")
    public Result<List<Map<String, Object>>> hotPeriods(
            @Parameter(description = "开始日期 (yyyy-MM-dd)", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId) {
        return Result.success(reportService.getHotPeriods(startDate, endDate, roomId));
    }

    @GetMapping("/export")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "导出Excel报表", description = "导出时间范围内的预约数据为Excel文件")
    public void export(
            @Parameter(description = "开始日期 (yyyy-MM-dd)", required = true) @RequestParam String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)", required = true) @RequestParam String endDate,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId,
            @Parameter(description = "楼栋ID") @RequestParam(required = false) Long buildingId,
            @Parameter(description = "自习室ID") @RequestParam(required = false) Long roomId,
            HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = URLEncoder.encode("预约数据报表_" + startDate + "_" + endDate + ".xlsx",
                StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        reportService.exportReport(response.getOutputStream(), startDate, endDate, roomId);
    }
}

package com.studyroom.booking.modules.dashboard.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.modules.dashboard.dto.BuildingOverviewVO;
import com.studyroom.booking.modules.dashboard.dto.CampusOverviewVO;
import com.studyroom.booking.modules.dashboard.dto.RoomDetailVO;
import com.studyroom.booking.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实时看板 Controller
 * <p>
 * 提供校区、楼栋、自习室使用概览，均为公开接口。
 *
 * @author 郭学威
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "实时看板", description = "校区/楼栋/自习室使用概览与实时状态")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/campus-overview")
    @Operation(summary = "校区使用概览", description = "获取各校区自习室使用率概览")
    public Result<List<CampusOverviewVO>> campusOverview() {
        return Result.success(dashboardService.getCampusOverview());
    }

    @GetMapping("/building-overview")
    @Operation(summary = "楼栋使用概览", description = "获取各楼栋自习室使用率，可按校区过滤")
    public Result<List<BuildingOverviewVO>> buildingOverview(
            @Parameter(description = "校区ID（可选，不传则返回全部楼栋）")
            @RequestParam(required = false) Long campusId) {
        return Result.success(dashboardService.getBuildingOverview(campusId));
    }

    @GetMapping("/room-detail/{roomId}")
    @Operation(summary = "自习室使用详情", description = "获取单个自习室的座位实时状态")
    public Result<RoomDetailVO> roomDetail(
            @Parameter(description = "自习室ID", required = true)
            @PathVariable Long roomId) {
        return Result.success(dashboardService.getRoomDetail(roomId));
    }
}

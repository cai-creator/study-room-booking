package com.studyroom.booking.modules.space.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.space.dto.RoomSeatStatusVO;
import com.studyroom.booking.modules.space.dto.SeatGenerateRequest;
import com.studyroom.booking.modules.space.dto.SeatTagsUpdateRequest;
import com.studyroom.booking.modules.space.dto.SeatUpdateRequest;
import com.studyroom.booking.modules.space.entity.Seat;
import com.studyroom.booking.modules.space.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 座位管理 Controller
 *
 * @author 陈梦涵
 */
@RestController
@RequestMapping("")
@Tag(name = "座位管理", description = "座位的批量生成、管理、实时状态查询")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // ========== 座位基础管理 ==========

    @GetMapping("/seats")
    @Operation(summary = "获取自习室座位列表")
    public Result<List<Seat>> list(
            @Parameter(description = "自习室ID") @RequestParam Long roomId) {
        return Result.success(seatService.listByRoomId(roomId));
    }

    @GetMapping("/seats/{id}")
    @Operation(summary = "获取座位详情")
    public Result<Seat> getById(@PathVariable Long id) {
        return Result.success(seatService.getById(id));
    }

    @PutMapping("/seats/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "更新座位信息")
    public Result<Seat> update(@PathVariable Long id,
                               @Valid @RequestBody SeatUpdateRequest request) {
        return Result.success(seatService.updateSeat(id, request.getSeatCode(),
                request.getRowNumber(), request.getColNumber(),
                request.getTags(), request.getStatus()));
    }

    @DeleteMapping("/seats/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除座位")
    public Result<Void> delete(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return Result.success();
    }

    // ========== 座位批量操作 ==========

    @PostMapping("/rooms/{roomId}/seats/generate")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "批量生成座位（按行列网格）")
    public Result<List<Seat>> generateSeats(
            @PathVariable Long roomId,
            @Valid @RequestBody SeatGenerateRequest request) {
        return Result.success(seatService.generateSeats(roomId, request));
    }

    @PatchMapping("/rooms/{roomId}/seats/tags")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "批量更新座位标签")
    public Result<Void> batchUpdateTags(
            @PathVariable Long roomId,
            @Valid @RequestBody SeatTagsUpdateRequest request) {
        seatService.batchUpdateTags(roomId, request);
        return Result.success();
    }

    // ========== 座位状态查询 ==========

    @GetMapping("/rooms/{roomId}/seats/status")
    @Operation(summary = "获取自习室所有座位的实时状态")
    public Result<RoomSeatStatusVO> getSeatStatus(@PathVariable Long roomId) {
        return Result.success(seatService.getRoomSeatStatus(roomId));
    }
}

package com.studyroom.booking.modules.space.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.space.dto.SeatUnavailableRequest;
import com.studyroom.booking.modules.space.entity.SeatUnavailable;
import com.studyroom.booking.modules.space.service.SeatUnavailableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats/{seatId}/unavailable")
@Tag(name = "座位定时不可用管理", description = "座位定时不可用设置")
@RequiredArgsConstructor
public class SeatUnavailableController {

    private final SeatUnavailableService seatUnavailableService;

    @GetMapping
    @Operation(summary = "获取座位的定时不可用列表")
    public Result<List<SeatUnavailable>> list(@PathVariable Long seatId) {
        return Result.success(seatUnavailableService.getUnavailableBySeatId(seatId));
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "创建座位定时不可用记录")
    public Result<SeatUnavailable> create(@PathVariable Long seatId,
                                          @RequestBody SeatUnavailableRequest request) {
        return Result.success(seatUnavailableService.create(seatId,
                request.getRepeatType(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                request.getDayOfWeek(),
                request.getDayOfMonth(),
                request.getReason()));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "更新座位定时不可用记录")
    public Result<SeatUnavailable> update(@PathVariable Long seatId,
                                          @PathVariable Long id,
                                          @RequestBody SeatUnavailableRequest request) {
        return Result.success(seatUnavailableService.update(id,
                request.getRepeatType(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                request.getDayOfWeek(),
                request.getDayOfMonth(),
                request.getReason()));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除座位定时不可用记录")
    public Result<Void> delete(@PathVariable Long id) {
        seatUnavailableService.delete(id);
        return Result.success();
    }

    @GetMapping("/current")
    @Operation(summary = "检查座位当前是否不可用")
    public Result<Boolean> checkCurrent(@PathVariable Long seatId) {
        return Result.success(seatUnavailableService.isSeatCurrentlyUnavailable(seatId));
    }
}
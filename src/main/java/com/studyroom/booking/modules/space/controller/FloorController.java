package com.studyroom.booking.modules.space.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.space.dto.FloorRequest;
import com.studyroom.booking.modules.space.entity.Floor;
import com.studyroom.booking.modules.space.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼层管理 Controller
 *
 * @author 陈梦涵
 */
@RestController
@RequestMapping("/floors")
@Tag(name = "楼层管理", description = "楼层的增删改查")
@RequiredArgsConstructor
public class FloorController {

    private final FloorService floorService;

    @GetMapping
    @Operation(summary = "获取楼层列表")
    public Result<List<Floor>> list(
            @Parameter(description = "楼栋ID（必填）", required = true)
            @RequestParam Long buildingId) {
        return Result.success(floorService.listByBuildingId(buildingId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取楼层详情")
    public Result<Floor> getById(@PathVariable Long id) {
        return Result.success(floorService.getById(id));
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "新增楼层")
    public Result<Floor> create(@Valid @RequestBody FloorRequest request) {
        return Result.success(floorService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "更新楼层")
    public Result<Floor> update(@PathVariable Long id, @Valid @RequestBody FloorRequest request) {
        return Result.success(floorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除楼层")
    public Result<Void> delete(@PathVariable Long id) {
        floorService.delete(id);
        return Result.success();
    }
}

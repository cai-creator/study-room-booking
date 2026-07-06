package com.studyroom.booking.modules.space.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.space.dto.BuildingRequest;
import com.studyroom.booking.modules.space.entity.Building;
import com.studyroom.booking.modules.space.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼栋管理 Controller
 *
 * @author 陈梦涵
 */
@RestController
@RequestMapping("/api/buildings")
@Tag(name = "楼栋管理", description = "楼栋的增删改查")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @Operation(summary = "获取楼栋列表")
    public Result<List<Building>> list(
            @Parameter(description = "校区ID（可选筛选）") @RequestParam(required = false) Long campusId) {
        return Result.success(buildingService.listByCampusId(campusId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取楼栋详情")
    public Result<Building> getById(@PathVariable Long id) {
        return Result.success(buildingService.getById(id));
    }

    @PostMapping
    @RequireRole("SUPER_ADMIN")
    @Operation(summary = "新增楼栋")
    public Result<Building> create(@Valid @RequestBody BuildingRequest request) {
        return Result.success(buildingService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRole("SUPER_ADMIN")
    @Operation(summary = "更新楼栋")
    public Result<Building> update(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
        return Result.success(buildingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRole("SUPER_ADMIN")
    @Operation(summary = "删除楼栋")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success();
    }
}

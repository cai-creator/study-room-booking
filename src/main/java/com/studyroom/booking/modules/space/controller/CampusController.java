package com.studyroom.booking.modules.space.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.space.dto.CampusRequest;
import com.studyroom.booking.modules.space.entity.Campus;
import com.studyroom.booking.modules.space.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区管理 Controller
 *
 * @author 陈梦涵
 */
@RestController
@RequestMapping("/campuses")
@Tag(name = "校区管理", description = "校区的增删改查")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @Operation(summary = "获取校区列表")
    public Result<List<Campus>> list() {
        return Result.success(campusService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取校区详情")
    public Result<Campus> getById(@PathVariable Long id) {
        return Result.success(campusService.getById(id));
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "新增校区")
    public Result<Campus> create(@Valid @RequestBody CampusRequest request) {
        return Result.success(campusService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "更新校区")
    public Result<Campus> update(@PathVariable Long id, @Valid @RequestBody CampusRequest request) {
        return Result.success(campusService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除校区")
    public Result<Void> delete(@PathVariable Long id) {
        campusService.delete(id);
        return Result.success();
    }
}

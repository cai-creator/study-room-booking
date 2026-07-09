package com.studyroom.booking.modules.space.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.dto.RoomQueryRequest;
import com.studyroom.booking.modules.space.dto.RoomRequest;
import com.studyroom.booking.modules.space.entity.StudyRoom;
import com.studyroom.booking.modules.space.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自习室管理 Controller
 *
 * @author 陈梦涵
 */
@RestController
@RequestMapping("/rooms")
@Tag(name = "自习室管理", description = "自习室的增删改查、批量导入、状态管理")
@RequiredArgsConstructor
public class RoomController {

    private final StudyRoomService studyRoomService;

    @GetMapping
    @Operation(summary = "分页查询自习室列表")
    public Result<Page<StudyRoom>> list(@Valid RoomQueryRequest query) {
        return Result.success(studyRoomService.listWithFilters(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取自习室详情")
    public Result<StudyRoom> getById(@PathVariable Long id) {
        return Result.success(studyRoomService.getById(id));
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "新增自习室")
    public Result<StudyRoom> create(@Valid @RequestBody RoomRequest request) {
        return Result.success(studyRoomService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "更新自习室")
    public Result<StudyRoom> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return Result.success(studyRoomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除自习室")
    public Result<Void> delete(@PathVariable Long id) {
        studyRoomService.delete(id);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "修改自习室状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        studyRoomService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/import")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "Excel批量导入自习室")
    public Result<Map<String, Object>> importRooms(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "上传文件为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式的Excel文件");
        }
        return Result.success(studyRoomService.importFromExcel(file));
    }
}

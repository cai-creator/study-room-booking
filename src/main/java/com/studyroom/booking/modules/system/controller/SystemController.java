package com.studyroom.booking.modules.system.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/system")
@Tag(name = "系统管理", description = "系统级管理接口（预留扩展）")
@SecurityRequirement(name = "BearerAuth")
public class SystemController {

    @PostMapping("/backup")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "数据库备份", description = "触发数据库全量备份")
    public Result<Map<String, Object>> backup() {
        log.info("触发数据库备份");
        Map<String, Object> result = new HashMap<>();
        result.put("backupId", System.currentTimeMillis());
        result.put("backupTime", LocalDateTime.now());
        result.put("message", "数据库备份接口已预留");
        return Result.success(result);
    }

    @GetMapping("/backups")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "备份列表", description = "查询历史备份记录")
    public Result<Map<String, Object>> listBackups() {
        Map<String, Object> result = new HashMap<>();
        result.put("backups", new Object[]{});
        result.put("message", "备份列表接口已预留");
        return Result.success(result);
    }

    @PostMapping("/restore")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "数据恢复", description = "从指定备份恢复数据")
    public Result<Map<String, Object>> restore(@Parameter(description = "备份ID") @RequestParam String backupId) {
        log.info("从备份恢复: backupId={}", backupId);
        Map<String, Object> result = new HashMap<>();
        result.put("backupId", backupId);
        result.put("message", "数据恢复接口已预留");
        return Result.success(result);
    }

    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统版本与基础信息")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("appName", "study-room-booking");
        result.put("version", "1.0.0");
        result.put("buildTime", LocalDateTime.now());
        result.put("message", "系统信息接口已预留");
        return Result.success(result);
    }

    @GetMapping("/config")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "系统配置", description = "获取系统级配置项")
    public Result<Map<String, Object>> getSystemConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingMaxDaily", 3);
        result.put("bookingMaxDurationHours", 8);
        result.put("message", "系统配置接口已预留");
        return Result.success(result);
    }
}
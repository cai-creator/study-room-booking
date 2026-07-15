package com.studyroom.booking.modules.sync.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sync")
@Tag(name = "数据同步", description = "外部系统数据同步接口（预留扩展）")
@SecurityRequirement(name = "BearerAuth")
public class SyncController {

    @PostMapping("/cas/users")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "同步CAS用户", description = "从CAS服务器同步用户数据")
    public Result<Map<String, Object>> syncCasUsers() {
        log.info("从CAS同步用户");
        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", 0);
        result.put("createdCount", 0);
        result.put("updatedCount", 0);
        result.put("message", "CAS用户同步接口已预留，待后续对接CAS服务");
        return Result.success(result);
    }

    @PostMapping("/ldap/users")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "同步LDAP用户", description = "从LDAP目录同步用户数据")
    public Result<Map<String, Object>> syncLdapUsers() {
        log.info("从LDAP同步用户");
        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", 0);
        result.put("message", "LDAP用户同步接口已预留");
        return Result.success(result);
    }

    @PostMapping("/schedule/tasks")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "同步排课数据", description = "从教务系统同步排课信息")
    public Result<Map<String, Object>> syncScheduleTasks() {
        log.info("同步排课数据");
        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", 0);
        result.put("message", "排课同步接口已预留");
        return Result.success(result);
    }

    @GetMapping("/status")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "同步状态", description = "查询最近一次同步状态")
    public Result<Map<String, Object>> getSyncStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("lastSyncTime", null);
        result.put("status", "IDLE");
        result.put("message", "同步状态查询接口已预留");
        return Result.success(result);
    }
}
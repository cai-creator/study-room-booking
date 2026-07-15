package com.studyroom.booking.modules.monitor.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/monitor")
@Tag(name = "系统监控", description = "系统监控接口（预留扩展）")
@SecurityRequirement(name = "BearerAuth")
public class MonitorController {

    @GetMapping("/online-users")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "在线用户", description = "获取当前在线用户统计")
    public Result<Map<String, Object>> getOnlineUsers() {
        log.info("查询在线用户");
        Map<String, Object> result = new HashMap<>();
        result.put("totalOnline", 0);
        result.put("userList", new Object[]{});
        result.put("message", "在线用户监控接口已预留，待后续对接WebSocket SessionManager");
        return Result.success(result);
    }

    @GetMapping("/server")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "服务器状态", description = "获取服务器资源使用情况")
    public Result<Map<String, Object>> getServerStatus() {
        log.info("查询服务器状态");
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> result = new HashMap<>();
        result.put("jvmTotalMemory", runtime.totalMemory());
        result.put("jvmFreeMemory", runtime.freeMemory());
        result.put("jvmMaxMemory", runtime.maxMemory());
        result.put("availableProcessors", runtime.availableProcessors());
        result.put("message", "服务器监控接口已预留");
        return Result.success(result);
    }

    @GetMapping("/api-stats")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "API调用统计", description = "获取API调用频率统计")
    public Result<Map<String, Object>> getApiStats(
            @Parameter(description = "时间范围(小时)") @RequestParam(defaultValue = "24") Integer hours) {
        log.info("查询API调用统计: hours={}", hours);
        Map<String, Object> result = new HashMap<>();
        result.put("timeRange", hours);
        result.put("totalCalls", 0);
        result.put("topApis", new Object[]{});
        result.put("message", "API统计接口已预留");
        return Result.success(result);
    }
}
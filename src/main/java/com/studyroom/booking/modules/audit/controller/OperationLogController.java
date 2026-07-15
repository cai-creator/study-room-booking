package com.studyroom.booking.modules.audit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.audit.entity.OperationLog;
import com.studyroom.booking.modules.audit.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "操作审计日志查询接口")
@SecurityRequirement(name = "BearerAuth")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "操作日志列表", description = "分页查询操作日志")
    public Result<Page<OperationLog>> getOperationLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作关键词") @RequestParam(required = false) String operation,
            @Parameter(description = "结果: 1成功 0失败") @RequestParam(required = false) Integer result,
            @Parameter(description = "开始日期 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {
        return Result.success(operationLogService.getOperationLogs(pageNum, pageSize, userId, module,
                operation, result, startDate, endDate));
    }

    @GetMapping("/recent")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "最近操作", description = "获取最近的操作日志")
    public Result<List<OperationLog>> getRecentLogs(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(operationLogService.getRecentLogs(limit));
    }

    @GetMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "日志详情", description = "获取单条操作日志详情")
    public Result<OperationLog> getLogById(@Parameter(description = "日志ID") @PathVariable Long id) {
        OperationLog log = operationLogService.getById(id);
        if (log == null) {
            return Result.error("日志不存在");
        }
        return Result.success(log);
    }
}
package com.studyroom.booking.modules.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.notification.dto.SendNotificationRequest;
import com.studyroom.booking.modules.notification.entity.Notification;
import com.studyroom.booking.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "消息通知", description = "消息通知管理接口")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "我的通知", description = "分页查询当前用户的通知")
    public Result<Page<Notification>> getNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "读取状态: 0未读 1已读") @RequestParam(required = false) Integer readFlag,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return Result.success(notificationService.getUserNotifications(userId, pageNum, pageSize, readFlag));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取当前用户未读通知数量")
    public Result<Map<String, Long>> getUnreadCount(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", notificationService.getUnreadCount(userId));
        return Result.success(result);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记单条通知为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        notificationService.markAsRead(userId, id);
        return Result.success("已标记为已读", null);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "标记所有通知为已读")
    public Result<Void> markAllRead(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        notificationService.markAllRead(userId);
        return Result.success("已全部标记为已读", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除单条通知")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        Notification notification = notificationService.getById(id);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationService.removeById(id);
            return Result.success("删除成功", null);
        }
        return Result.error("通知不存在或无权操作");
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "发送通知", description = "管理员向指定用户发送通知")
    public Result<Void> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request.getUserId(), request.getType(),
                request.getTitle(), request.getContent(), request.getData());
        return Result.success("发送成功", null);
    }
}
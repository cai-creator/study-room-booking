package com.studyroom.booking.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("role")
    private String role;

    @TableField("module")
    private String module;

    @TableField("operation")
    private String operation;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_name")
    private String targetName;

    @TableField("action")
    private String action;

    @TableField("detail")
    private String detail;

    @TableField("result")
    private Integer result;

    @TableField("error_message")
    private String errorMessage;

    @TableField("ip")
    private String ip;

    @TableField("user_agent")
    private String userAgent;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_method")
    private String requestMethod;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
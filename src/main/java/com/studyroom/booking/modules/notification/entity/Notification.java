package com.studyroom.booking.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("type")
    private String type;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("data")
    private String data;

    @TableField("read_flag")
    private Integer readFlag;

    @TableField("sender_id")
    private Long senderId;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
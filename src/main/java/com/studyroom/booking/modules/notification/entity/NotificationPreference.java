package com.studyroom.booking.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification_preference")
public class NotificationPreference implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("booking_reminder")
    private Boolean bookingReminder;

    @TableField("checkin_reminder")
    private Boolean checkinReminder;

    @TableField("system_notice")
    private Boolean systemNotice;

    @TableField("blacklist_alert")
    private Boolean blacklistAlert;

    @TableField("campus_id")
    private Long campusId;

    @TableField("room_type")
    private String roomType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
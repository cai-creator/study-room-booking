package com.studyroom.booking.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_attempts")
public class LoginAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 失败次数 */
    private Integer failCount;

    /** 锁定截止时间（null表示未锁定） */
    private LocalDateTime lockedUntil;

    /** 最后尝试时间 */
    private LocalDateTime lastAttemptTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

package com.studyroom.booking.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refresh_token")
@Schema(description = "刷新令牌")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "刷新令牌")
    private String token;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "是否已使用")
    private Boolean used;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "逻辑删除")
    @TableLogic
    private Integer deleted;
}
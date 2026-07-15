package com.studyroom.booking.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名/学号", example = "2024001001")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "角色", example = "STUDENT")
    private String role;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-正常", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;

    @Schema(description = "初始密码（仅创建用户时返回）", example = "Z2024")
    private String initialPassword;
}
package com.studyroom.booking.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建用户请求（超级管理员专用，可指定角色）")
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度需在4-50之间")
    @Schema(description = "用户名/学号", example = "admin001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "密码（留空则由系统自动生成）", example = "admin123")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50字符")
    @Schema(description = "真实姓名", example = "李管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Size(max = 20, message = "手机号不能超过20字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "角色：STUDENT/ADMIN/SUPER_ADMIN", example = "ADMIN")
    private String role;
}
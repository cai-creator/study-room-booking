package com.studyroom.booking.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @Size(max = 50, message = "真实姓名不能超过50字符")
    @Schema(description = "真实姓名", example = "李四")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "lisi@example.com")
    private String email;

    @Size(max = 20, message = "手机号不能超过20字符")
    @Schema(description = "手机号", example = "13900139000")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-正常", example = "1")
    private Integer status;

    @Schema(description = "角色：STUDENT/ADMIN/SUPER_ADMIN（仅管理员可修改）", example = "ADMIN")
    private String role;
}
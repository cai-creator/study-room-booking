package com.studyroom.booking.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改密码请求（本人需填旧密码，超级管理员可直接重置）")
public class ChangePasswordRequest {

    @Schema(description = "旧密码（本人修改时必填）", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度需在6-100之间")
    @Schema(description = "新密码", example = "newPassword456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
package com.studyroom.booking.modules.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminVerifyRequest {

    @NotBlank(message = "验证类型不能为空")
    private String verifyType;

    private String password;

    private String smsCode;

    private String emailCode;

    private String totpCode;
}
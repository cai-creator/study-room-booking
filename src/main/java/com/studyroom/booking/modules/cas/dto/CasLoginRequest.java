package com.studyroom.booking.modules.cas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CasLoginRequest {

    @NotBlank(message = "ticket不能为空")
    private String ticket;

    private String service;
}
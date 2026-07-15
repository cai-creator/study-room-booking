package com.studyroom.booking.modules.cas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CasValidateRequest {

    @NotBlank(message = "ticket不能为空")
    private String ticket;

    private String service;
}
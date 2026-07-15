package com.studyroom.booking.modules.security.dto;

import lombok.Data;

@Data
public class SecurityConfigResponse {

    private Integer loginAttemptLimit;

    private Integer lockDurationMinutes;

    private Integer passwordMinLength;

    private Boolean enable2FA;

    private Boolean enableIpWhitelist;

    private Boolean enableLoginAlert;

    private Integer sessionTimeoutMinutes;

    private String message;
}
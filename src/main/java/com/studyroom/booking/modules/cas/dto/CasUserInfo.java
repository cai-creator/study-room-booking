package com.studyroom.booking.modules.cas.dto;

import lombok.Data;

@Data
public class CasUserInfo {

    private String ticket;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String message;
}
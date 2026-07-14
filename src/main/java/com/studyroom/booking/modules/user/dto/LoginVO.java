package com.studyroom.booking.modules.user.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private String refreshToken;

    private Long expireAt;

    private Long refreshExpireAt;

    private UserVO user;
}
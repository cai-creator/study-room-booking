package com.studyroom.booking.modules.user.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long expireAt;
    private UserVO user;
}
package com.studyroom.booking.modules.user.dto;

import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String createdAt;
}
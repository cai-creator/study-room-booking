package com.studyroom.booking.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "真实姓名不能超过50字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号不能超过20字符")
    private String phone;

    private String avatar;

    private Integer status;

    private String role;
}
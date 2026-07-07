package com.studyroom.booking.modules.user.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.modules.user.dto.LoginRequest;
import com.studyroom.booking.modules.user.dto.LoginVO;
import com.studyroom.booking.modules.user.dto.RegisterRequest;
import com.studyroom.booking.modules.user.dto.UserVO;
import com.studyroom.booking.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、认证相关接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT token")
    public Result<LoginVO> login(
            @Parameter(description = "用户名/学号", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return Result.success("登录成功", userService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，默认角色为STUDENT")
    public Result<UserVO> register(
            @Parameter(description = "用户名/学号", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password,
            @Parameter(description = "真实姓名", required = true) @RequestParam String realName,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRealName(realName);
        request.setEmail(email);
        request.setPhone(phone);
        return Result.success("注册成功", userService.register(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录（客户端清除token即可）")
    public Result<Void> logout() {
        return Result.success("登出成功", null);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    public Result<UserVO> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }
}
package com.studyroom.booking.modules.security.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.security.dto.AdminVerifyRequest;
import com.studyroom.booking.modules.security.dto.SecurityConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/security")
@Tag(name = "安全验证", description = "管理员登录安全验证接口（预留扩展）")
@SecurityRequirement(name = "BearerAuth")
public class SecurityController {

    @PostMapping("/admin/verify")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "管理员二次验证", description = "管理员执行敏感操作前的二次身份验证")
    public Result<Map<String, Object>> adminVerify(
            @Valid @RequestBody AdminVerifyRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(hidden = true) @RequestAttribute("role") String role) {
        log.info("管理员二次验证请求: userId={}, role={}, type={}", userId, role, request.getVerifyType());
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("role", role);
        result.put("verifyType", request.getVerifyType());
        result.put("message", "管理员二次验证接口已预留，支持密码验证/手机验证码/动态令牌等方式");
        return Result.success("接口预留成功", result);
    }

    @PostMapping("/admin/verify/sms")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "发送验证短信", description = "向管理员发送验证码短信")
    public Result<Map<String, Object>> sendVerifySms(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        log.info("发送验证短信请求: userId={}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("message", "短信验证码发送接口已预留，待后续对接短信服务");
        return Result.success("接口预留成功", result);
    }

    @PostMapping("/admin/verify/email")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "发送验证邮件", description = "向管理员发送验证邮件")
    public Result<Map<String, Object>> sendVerifyEmail(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        log.info("发送验证邮件请求: userId={}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("message", "验证邮件发送接口已预留，待后续对接邮件服务");
        return Result.success("接口预留成功", result);
    }

    @GetMapping("/config")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "安全配置", description = "获取当前安全配置")
    public Result<SecurityConfigResponse> getSecurityConfig() {
        SecurityConfigResponse config = new SecurityConfigResponse();
        config.setLoginAttemptLimit(5);
        config.setLockDurationMinutes(30);
        config.setPasswordMinLength(6);
        config.setEnable2FA(false);
        config.setEnableIpWhitelist(false);
        config.setEnableLoginAlert(false);
        config.setSessionTimeoutMinutes(120);
        config.setMessage("安全配置接口已预留，支持登录失败限制、双因素认证、IP白名单等配置");
        return Result.success(config);
    }

    @PutMapping("/config")
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "更新安全配置", description = "更新安全配置")
    public Result<SecurityConfigResponse> updateSecurityConfig(@RequestBody SecurityConfigResponse config) {
        log.info("更新安全配置: {}", config);
        config.setMessage("安全配置更新接口已预留");
        return Result.success("配置更新成功", config);
    }

    @GetMapping("/risk/detect")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "风险检测", description = "检测当前登录是否存在风险")
    public Result<Map<String, Object>> detectRisk(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "IP地址") @RequestParam(required = false) String ip) {
        log.info("风险检测请求: userId={}, ip={}", userId, ip);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("ip", ip);
        result.put("riskLevel", "LOW");
        result.put("riskFactors", new String[]{});
        result.put("message", "风险检测接口已预留，支持异常IP、异常时间、异地登录等检测");
        return Result.success(result);
    }
}
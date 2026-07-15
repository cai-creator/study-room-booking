package com.studyroom.booking.modules.cas.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.modules.cas.dto.CasLoginRequest;
import com.studyroom.booking.modules.cas.dto.CasValidateRequest;
import com.studyroom.booking.modules.cas.dto.CasUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cas")
@Tag(name = "CAS认证", description = "CAS单点登录接口（预留扩展）")
public class CasController {

    @PostMapping("/login")
    @Operation(summary = "CAS登录", description = "通过CAS ticket获取用户信息并登录系统")
    public Result<Map<String, Object>> casLogin(@Valid @RequestBody CasLoginRequest request) {
        log.info("CAS登录请求: ticket={}", request.getTicket());
        Map<String, Object> result = new HashMap<>();
        result.put("message", "CAS认证接口已预留，待后续对接CAS服务");
        result.put("ticket", request.getTicket());
        result.put("service", request.getService());
        return Result.success("接口预留成功", result);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证CAS票据", description = "验证CAS ticket的有效性")
    public Result<CasUserInfo> validateTicket(@Valid @RequestBody CasValidateRequest request) {
        log.info("CAS票据验证: ticket={}", request.getTicket());
        CasUserInfo userInfo = new CasUserInfo();
        userInfo.setMessage("CAS票据验证接口已预留");
        userInfo.setTicket(request.getTicket());
        return Result.success(userInfo);
    }

    @GetMapping("/service-url")
    @Operation(summary = "获取CAS服务URL", description = "获取CAS登录跳转地址")
    public Result<Map<String, String>> getServiceUrl(
            @Parameter(description = "回调地址") @RequestParam(required = false) String redirectUrl) {
        Map<String, String> result = new HashMap<>();
        result.put("casServerUrl", "https://cas.example.com/cas/login");
        result.put("serviceUrl", redirectUrl != null ? redirectUrl : "https://your-domain.com/cas/callback");
        result.put("message", "CAS服务URL配置接口已预留");
        return Result.success(result);
    }

    @GetMapping("/callback")
    @Operation(summary = "CAS回调", description = "CAS登录成功后的回调地址")
    public Result<Map<String, Object>> casCallback(
            @Parameter(description = "CAS票据") @RequestParam String ticket,
            @Parameter(description = "服务地址") @RequestParam(required = false) String service) {
        log.info("CAS回调: ticket={}, service={}", ticket, service);
        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("service", service);
        result.put("message", "CAS回调接口已预留，待后续对接");
        return Result.success(result);
    }
}
package com.studyroom.booking.modules.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.modules.audit.entity.OperationLog;
import com.studyroom.booking.modules.audit.mapper.OperationLogMapper;
import com.studyroom.booking.modules.user.entity.User;
import com.studyroom.booking.modules.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {

    private final UserMapper userMapper;

    public void log(String module, String operation, String targetType, Long targetId,
                    String targetName, String action, String detail, boolean success,
                    String errorMessage, HttpServletRequest request) {
        OperationLog opLog = new OperationLog();
        opLog.setModule(module);
        opLog.setOperation(operation);
        opLog.setTargetType(targetType);
        opLog.setTargetId(targetId);
        opLog.setTargetName(targetName);
        opLog.setAction(action);
        opLog.setDetail(detail);
        opLog.setResult(success ? 1 : 0);
        opLog.setErrorMessage(errorMessage);
        opLog.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            opLog.setIp(getClientIp(request));
            opLog.setUserAgent(request.getHeader("User-Agent"));
            opLog.setRequestUri(request.getRequestURI());
            opLog.setRequestMethod(request.getMethod());

            Long userId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");
            opLog.setUserId(userId);
            opLog.setRole(role);

            if (userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    opLog.setUsername(user.getUsername());
                }
            }
        }

        try {
            save(opLog);
        } catch (Exception e) {
            log.warn("保存操作日志失败: {}", e.getMessage());
        }
    }

    public void logSuccess(String module, String operation, String targetType,
                           Long targetId, String targetName, HttpServletRequest request) {
        log(module, operation, targetType, targetId, targetName, operation, "", true, null, request);
    }

    public void logFailure(String module, String operation, String targetType,
                           Long targetId, String targetName, String errorMessage, HttpServletRequest request) {
        log(module, operation, targetType, targetId, targetName, operation, "", false, errorMessage, request);
    }

    public Page<OperationLog> getOperationLogs(int pageNum, int pageSize, Long userId, String module,
                                               String operation, Integer result, String startDate, String endDate) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(OperationLog::getUserId, userId);
        if (module != null && !module.isEmpty()) wrapper.eq(OperationLog::getModule, module);
        if (operation != null && !operation.isEmpty()) wrapper.like(OperationLog::getOperation, operation);
        if (result != null) wrapper.eq(OperationLog::getResult, result);
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(startDate + " 00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(endDate + " 23:59:59"));
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<OperationLog> getRecentLogs(int limit) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
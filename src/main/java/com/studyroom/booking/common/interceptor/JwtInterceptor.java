package com.studyroom.booking.common.interceptor;

import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 调试：打印所有请求头
        System.out.println("=== JWT INTERCEPTOR DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        System.out.println("Authorization (lowercase): " + request.getHeader("authorization"));
        System.out.println("bearerAuth Header: " + request.getHeader("bearerAuth"));
        System.out.println("Content-Type: " + request.getContentType());
        // 打印所有请求头名称
        System.out.println("All Header Names:");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println("  " + name + ": " + request.getHeader(name));
        }
        System.out.println("==============================");

        // 从请求头获取token（兼容Knife4j的bearerauth头）
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerAuth");
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerauth");
        }

        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 去除Bearer前缀
        token = token.substring(7);

        // 验证token
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 将用户信息存入request属性
        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        String role = jwtUtils.getRole(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        // 同步到 UserContext ThreadLocal，供 Service 层使用
        com.studyroom.booking.common.context.UserContext.setRequest(request);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal，防止内存泄漏
        com.studyroom.booking.common.context.UserContext.clear();
    }
}
package com.studyroom.booking.common.interceptor;

import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/doc.html",
            "/swagger-ui.html",
            "/favicon.ico"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String servletPath = request.getServletPath();
        System.out.println("=== JWT INTERCEPTOR ===");
        System.out.println("Servlet Path: " + servletPath);
        System.out.println("Request URI: " + request.getRequestURI());

        if (WHITE_LIST.contains(servletPath)) {
            System.out.println("White list matched, skipping JWT validation");
            return true;
        }

        if (servletPath.startsWith("/swagger-ui/") ||
            servletPath.startsWith("/v3/api-docs/") ||
            servletPath.startsWith("/webjars/") ||
            servletPath.startsWith("/swagger-resources/")) {
            return true;
        }

        // ========== 调试：打印所有请求头 ==========
        System.out.println("--- JWT DEBUG: All Request Headers ---");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println("  " + name + ": " + request.getHeader(name));
        }
        System.out.println("--- JWT DEBUG END ---");

        // 从请求头获取token（兼容Knife4j的多种请求头名称：Authorization / bearerAuth / bearerauth）
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerAuth");
            System.out.println("Authorization header empty, trying bearerAuth: " + token);
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerauth");
            System.out.println("bearerAuth header empty, trying bearerauth: " + token);
        }

        if (token == null || token.isEmpty()) {
            System.out.println("ERROR: No valid Authorization token found in any request header!");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        System.out.println("Raw Authorization value: " + token);

        // 去除 "Bearer " 前缀（使用while循环处理Knife4j重复添加前缀的情况）
        // 例如：用户在Knife4j对话框中输入"Bearer xxx"，Knife4j自动添加Bearer前缀
        // 导致请求头为 "Bearer Bearer xxx"
        while (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 安全地截断打印token前20个字符（避免完整token泄露到日志）
        System.out.println("Token after stripping Bearer prefix: " +
                (token.length() > 20 ? token.substring(0, 20) + "..." : token));

        // 验证token
        if (!jwtUtils.validateToken(token)) {
            System.out.println("ERROR: Token validation failed (expired or invalid)!");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 将用户信息存入request属性
        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        String role = jwtUtils.getRole(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        UserContext.setRequest(request);

        System.out.println("JWT validation SUCCESS: userId=" + userId + ", username=" + username + ", role=" + role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
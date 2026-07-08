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
            "/auth/logout",
            "/doc.html",
            "/swagger-ui.html",
            "/favicon.ico"
    );

    /**
     * 公共查询接口的路径前缀（仅 GET 方法时放行）
     * <p>用于：实时看板、空间（校区/楼栋/楼层/自习室/座位）的查询。
     * <p>注意：写操作（POST/PUT/DELETE/PATCH）即使命中这些前缀，仍需 token 校验。
     */
    private static final List<String> PUBLIC_GET_PREFIXES = Arrays.asList(
            "/swagger-ui/",
            "/v3/api-docs/",
            "/webjars/",
            "/swagger-resources/",
            "/dashboard/",
            "/campuses",
            "/buildings",
            "/floors",
            "/rooms",
            "/seats"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String servletPath = request.getServletPath();
        String httpMethod = request.getMethod();

        if (WHITE_LIST.contains(servletPath)) {
            return true;
        }

        // 文档相关 & 公共 GET 查询接口按前缀放行
        for (String prefix : PUBLIC_GET_PREFIXES) {
            if (servletPath.startsWith(prefix)) {
                // /swagger-ui/ 等文档路径不限方法；空间/看板接口仅放行 GET
                boolean isDoc = prefix.startsWith("/swagger-ui")
                        || prefix.startsWith("/v3/api-docs")
                        || prefix.startsWith("/webjars")
                        || prefix.startsWith("/swagger-resources");
                if (isDoc || "GET".equalsIgnoreCase(httpMethod)) {
                    return true;
                }
                break;
            }
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
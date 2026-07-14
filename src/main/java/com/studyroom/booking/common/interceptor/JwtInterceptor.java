package com.studyroom.booking.common.interceptor;

import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.modules.user.service.TokenBlacklistService;
import com.studyroom.booking.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
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

        // 从请求头获取token（兼容Knife4j的多种请求头名称：Authorization / bearerAuth / bearerauth）
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerAuth");
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("bearerauth");
        }

        if (token == null || token.isEmpty()) {
            log.debug("未授权的请求: {} {} (无token)", httpMethod, servletPath);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 去除 "Bearer " 前缀（使用while循环处理Knife4j重复添加前缀的情况）
        while (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查token是否在黑名单中（已登出的token）
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Token已在黑名单中（已登出）: {} {}", httpMethod, servletPath);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 解析token一次，提取所有用户信息
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            log.debug("Token解析失败: {} {} (token已过期或格式错误)", httpMethod, servletPath);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 检查token是否过期
        if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
            log.debug("Token已过期: {} {}", httpMethod, servletPath);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 从Claims中提取用户信息（仅一次解析）
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        UserContext.setRequest(request);

        log.debug("JWT验证成功: userId={}, role={}, path={} {}", userId, role, httpMethod, servletPath);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
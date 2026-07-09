package com.studyroom.booking.common.interceptor;

import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取方法或类上的RequireRole注解
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果没有注解，放行
        if (annotation == null) {
            return true;
        }

        // 获取当前用户角色
        String currentRole = (String) request.getAttribute("role");
        if (currentRole == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 检查角色是否匹配
        String[] requiredRoles = annotation.value();
        boolean hasRole = Arrays.asList(requiredRoles).contains(currentRole);

        if (!hasRole) {
            log.warn("角色权限不足: 当前角色={}, 需要角色={}, 路径={}", currentRole, Arrays.toString(requiredRoles), request.getServletPath());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return true;
    }
}
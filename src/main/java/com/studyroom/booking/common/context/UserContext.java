package com.studyroom.booking.common.context;

import jakarta.servlet.http.HttpServletRequest;

public class UserContext {

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    public static void setRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public static void clear() {
        requestHolder.remove();
    }

    public static Long getUserId() {
        HttpServletRequest request = requestHolder.get();
        if (request != null) {
            return (Long) request.getAttribute("userId");
        }
        return null;
    }

    public static String getUsername() {
        HttpServletRequest request = requestHolder.get();
        if (request != null) {
            return (String) request.getAttribute("username");
        }
        return null;
    }

    public static String getRole() {
        HttpServletRequest request = requestHolder.get();
        if (request != null) {
            return (String) request.getAttribute("role");
        }
        return null;
    }
}
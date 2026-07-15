package com.studyroom.booking.integration.interceptor;

import com.studyroom.booking.common.interceptor.JwtInterceptor;
import com.studyroom.booking.common.interceptor.RoleInterceptor;
import com.studyroom.booking.modules.user.service.TokenBlacklistService;
import com.studyroom.booking.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT + Role 拦截器单元测试（纯 Mock，无 Spring 容器）
 */
@DisplayName("JWT/Role 拦截器单元测试")
class JwtInterceptorTest {

    private JwtUtils jwtUtils;
    private TokenBlacklistService tokenBlacklistService;
    private JwtInterceptor jwtInterceptor;
    private RoleInterceptor roleInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        setField(jwtUtils, "secret", "study-room-booking-secret-key-2024-very-long-secret-for-jwt-token");
        setField(jwtUtils, "expire", 86400000L);
        tokenBlacklistService = new TokenBlacklistService();
        jwtInterceptor = new JwtInterceptor(jwtUtils, tokenBlacklistService);
        roleInterceptor = new RoleInterceptor();
    }

    /** 创建 Mock 请求并正确设置 servletPath */
    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        req.setServletPath(path);
        return req;
    }

    // ==================== 白名单测试 ====================

    @Test
    @DisplayName("/auth/login 在白名单中")
    void whiteList_Login() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("POST", "/auth/login"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("/auth/register 在白名单中")
    void whiteList_Register() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("POST", "/auth/register"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("/auth/logout 在白名单中")
    void whiteList_Logout() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("POST", "/auth/logout"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("/favicon.ico 在白名单中")
    void whiteList_Favicon() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/favicon.ico"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("OPTIONS 请求直接放行")
    void optionsAlwaysAllowed() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("OPTIONS", "/api/reservations"), new MockHttpServletResponse(), null));
    }

    // ==================== 公共 GET 放行测试 ====================

    @Test
    @DisplayName("GET /campuses 无 Token 放行")
    void publicGet_Campuses() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/campuses"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /buildings 无 Token 放行")
    void publicGet_Buildings() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/buildings"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /rooms 无 Token 放行")
    void publicGet_Rooms() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/rooms"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /seats 无 Token 放行")
    void publicGet_Seats() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/seats"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /dashboard/campus-overview 无 Token 放行")
    void publicGet_Dashboard() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/dashboard/campus-overview"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /seats/1/available-slots 无 Token 放行（子路径）")
    void publicGet_AvailableSlots() throws Exception {
        assertTrue(jwtInterceptor.preHandle(request("GET", "/seats/1/available-slots"), new MockHttpServletResponse(), null));
    }

    // ==================== 写操作不放行 ====================

    @Test
    @DisplayName("POST /campuses 无 Token — 拦截")
    void blockPost_Campuses() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("POST", "/campuses"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("PUT /rooms/1 无 Token — 拦截")
    void blockPut_Room() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("PUT", "/rooms/1"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("DELETE /seats/1 无 Token — 拦截")
    void blockDelete_Seat() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("DELETE", "/seats/1"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("PATCH /rooms/1/seats/tags 无 Token — 拦截")
    void blockPatch_SeatTags() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("PATCH", "/rooms/1/seats/tags"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("POST /reservations 无 Token — 拦截（非空间路径）")
    void blockPost_Reservation() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("POST", "/reservations"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("GET /users 无 Token — 拦截（非公共路径）")
    void blockGet_Users() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("GET", "/users"), new MockHttpServletResponse(), null));
    }

    // ==================== Token 校验 ====================

    @Test
    @DisplayName("有效 Token — 通过并设置 request 属性")
    void validToken_SetsAttributes() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin", "SUPER_ADMIN");
        MockHttpServletRequest req = request("GET", "/auth/me");
        req.addHeader("Authorization", "Bearer " + token);

        assertTrue(jwtInterceptor.preHandle(req, new MockHttpServletResponse(), null));
        assertEquals(1L, req.getAttribute("userId"));
        assertEquals("admin", req.getAttribute("username"));
        assertEquals("SUPER_ADMIN", req.getAttribute("role"));
    }

    @Test
    @DisplayName("无 Authorization Header — 拦截")
    void noToken_Blocked() {
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(request("GET", "/auth/me"), new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("无效 Token — 拦截")
    void invalidToken_Blocked() {
        MockHttpServletRequest req = request("GET", "/auth/me");
        req.addHeader("Authorization", "Bearer invalid_token");
        assertThrows(Exception.class, () ->
                jwtInterceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("重复 Bearer 前缀（Knife4j兼容）")
    void doubleBearerPrefix_Works() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin", "SUPER_ADMIN");
        MockHttpServletRequest req = request("GET", "/auth/me");
        req.addHeader("Authorization", "Bearer Bearer " + token);

        assertTrue(jwtInterceptor.preHandle(req, new MockHttpServletResponse(), null));
        assertEquals("admin", req.getAttribute("username"));
    }

    @Test
    @DisplayName("bearerAuth Header 兼容")
    void bearerAuthHeader_Works() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin", "SUPER_ADMIN");
        MockHttpServletRequest req = request("GET", "/auth/me");
        req.addHeader("bearerAuth", "Bearer " + token);

        assertTrue(jwtInterceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    @Test
    @DisplayName("bearerauth Header 兼容（全小写）")
    void bearerauthHeader_Works() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin", "SUPER_ADMIN");
        MockHttpServletRequest req = request("GET", "/auth/me");
        req.addHeader("bearerauth", "Bearer " + token);

        assertTrue(jwtInterceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    // ==================== JwtUtils 单元测试 ====================

    @Test
    @DisplayName("生成 Token 并解析")
    void jwtUtils_GenerateAndParse() {
        String token = jwtUtils.generateToken(100L, "testuser", "STUDENT");
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(100L, jwtUtils.getUserId(token));
        assertEquals("testuser", jwtUtils.getUsername(token));
        assertEquals("STUDENT", jwtUtils.getRole(token));
    }

    @Test
    @DisplayName("无效 Token — validateToken 返回 false")
    void jwtUtils_InvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid"));
        assertFalse(jwtUtils.validateToken(""));
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    @DisplayName("空 Token — parseToken 返回 null")
    void jwtUtils_EmptyToken() {
        assertNull(jwtUtils.getUserId(""));
        assertNull(jwtUtils.getUsername(""));
    }

    @Test
    @DisplayName("getExpireTime 返回配置值")
    void jwtUtils_ExpireTime() {
        assertEquals(86400000L, jwtUtils.getExpireTime());
    }

    // ==================== RoleInterceptor 测试 ====================

    @Test
    @DisplayName("RoleInterceptor 非 HandlerMethod 放行")
    void roleInterceptor_NullHandler_Passes() throws Exception {
        MockHttpServletRequest req = request("GET", "/test");
        req.setAttribute("role", "STUDENT");
        assertTrue(roleInterceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    // ==================== 辅助 ====================

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}

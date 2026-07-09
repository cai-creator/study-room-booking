package com.studyroom.booking.integration;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.modules.reservation.dto.BookingVO;
import com.studyroom.booking.modules.reservation.dto.CreateBookingRequest;
import com.studyroom.booking.modules.reservation.service.BookingService;
import com.studyroom.booking.modules.user.dto.ChangePasswordRequest;
import com.studyroom.booking.modules.user.dto.CreateUserRequest;
import com.studyroom.booking.modules.user.dto.LoginRequest;
import com.studyroom.booking.modules.user.dto.LoginVO;
import com.studyroom.booking.modules.user.dto.RegisterRequest;
import com.studyroom.booking.modules.user.dto.UpdateUserRequest;
import com.studyroom.booking.modules.user.dto.UserVO;
import com.studyroom.booking.modules.user.service.UserService;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.common.ResultCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 核心业务逻辑集成测试 — 直接调用 Service 层（不经过 MockMvc/Interceptor）
 * 测试真实业务规则、数据库操作、异常分支
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
@DisplayName("业务逻辑集成测试")
class CoreBusinessIntegrationTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private void loginAs(Long userId, String username, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        UserContext.setRequest(request);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 用户登录/注册 ====================

    @Nested
    @DisplayName("用户认证")
    class UserAuthTests {

        @Test
        @DisplayName("AUTH-001: 注册后正常登录")
        void login_AfterRegister() {
            // 先注册一个用户（schema-h2.sql 中 admin 的 BCrypt 哈希可能不兼容 Hutool）
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("login_test_admin");
            regReq.setPassword("admin123");
            regReq.setRealName("测试管理员");
            userService.register(regReq);

            // 登录
            LoginRequest req = new LoginRequest();
            req.setUsername("login_test_admin");
            req.setPassword("admin123");

            LoginVO result = userService.login(req);
            assertNotNull(result);
            assertNotNull(result.getToken());
            assertEquals("login_test_admin", result.getUser().getUsername());
            assertEquals("STUDENT", result.getUser().getRole());
            assertNotNull(result.getExpireAt());
            assertTrue(result.getExpireAt() > System.currentTimeMillis());
        }

        @Test
        @DisplayName("AUTH-003: 用户不存在")
        void login_UserNotFound() {
            LoginRequest req = new LoginRequest();
            req.setUsername("no_such_user");
            req.setPassword("123456");

            BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(req));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("AUTH-004: 密码错误")
        void login_WrongPassword() {
            // 注册用户确保密码哈希正确
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("wrong_pwd_test");
            regReq.setPassword("correct_pass");
            regReq.setRealName("密码测试");
            userService.register(regReq);

            LoginRequest req = new LoginRequest();
            req.setUsername("wrong_pwd_test");
            req.setPassword("wrong_password");

            BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(req));
            assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("AUTH-009: 注册新用户")
        void register_Success() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("test_student_01");
            req.setPassword("password123");
            req.setRealName("测试学生");

            UserVO result = userService.register(req);
            assertNotNull(result);
            assertEquals("test_student_01", result.getUsername());
            assertEquals("STUDENT", result.getRole());
            assertEquals("测试学生", result.getRealName());
            // 密码应该是 BCrypt 哈希，不是明文
            assertNotEquals("password123", result.toString()); // UserVO 不包含 password
        }

        @Test
        @DisplayName("AUTH-010: 重复用户名注册")
        void register_Duplicate() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("admin"); // 已存在
            req.setPassword("pass123");
            req.setRealName("重复");

            BusinessException ex = assertThrows(BusinessException.class, () -> userService.register(req));
            assertEquals(ResultCode.USER_ALREADY_EXISTS.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("注册后可用新账号登录")
        void register_ThenLogin() {
            // 注册
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("cycle_user");
            regReq.setPassword("mypassword");
            regReq.setRealName("循环测试");
            UserVO regResult = userService.register(regReq);
            assertNotNull(regResult);

            // 登录
            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("cycle_user");
            loginReq.setPassword("mypassword");
            LoginVO loginResult = userService.login(loginReq);
            assertNotNull(loginResult.getToken());
            assertEquals("STUDENT", loginResult.getUser().getRole());
        }

        @Test
        @DisplayName("获取当前用户信息")
        void getCurrentUser() {
            UserVO user = userService.getCurrentUser(1L); // root, SUPER_ADMIN
            assertNotNull(user);
            assertEquals("root", user.getUsername());
            assertEquals("SUPER_ADMIN", user.getRole());
        }

        @Test
        @DisplayName("获取不存在的用户")
        void getCurrentUser_NotFound() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getCurrentUser(99999L));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ==================== 预约核心业务 ====================

    @Nested
    @DisplayName("预约业务")
    class BookingTests {

        @Test
        @DisplayName("RES-001: 正常创建预约")
        void createBooking_Success() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L); // A-01, 状态正常
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));

            BookingVO result = bookingService.createBooking(req);
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals("RESERVED", result.getStatus());
            assertNotNull(result.getSeatCode());
            assertNotNull(result.getCheckinCode());
            assertTrue(result.getCheckinCode().startsWith("QR"));
        }

        @Test
        @DisplayName("RES-002: 座位不存在")
        void createBooking_SeatNotFound() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(99999L);
            req.setStartTime("2026-07-10 09:00:00");
            req.setEndTime("2026-07-10 10:00:00");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(req));
            assertEquals(ResultCode.SEAT_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-005: 结束时间早于开始时间")
        void createBooking_TimeReversed() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            req.setStartTime("2026-07-10 10:00:00");
            req.setEndTime("2026-07-10 09:00:00");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(req));
            assertEquals(ResultCode.RESERVATION_TIME_INVALID.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-006: 预约时间过早（>24h）")
        void createBooking_TooEarly() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime tooFar = LocalDateTime.now().plusHours(48).withMinute(0).withSecond(0);
            req.setStartTime(tooFar.format(FORMATTER));
            req.setEndTime(tooFar.plusHours(1).format(FORMATTER));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(req));
            assertEquals(ResultCode.RESERVATION_TOO_EARLY.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-008: 预约时长超过8小时")
        void createBooking_DurationExceeded() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(9).format(FORMATTER));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(req));
            assertEquals(ResultCode.RESERVATION_DURATION_EXCEEDED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-011: 同一座位时间冲突")
        void createBooking_Conflict() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            // 先创建第一个预约
            CreateBookingRequest first = new CreateBookingRequest();
            first.setSeatId(1L);
            LocalDateTime slot = LocalDateTime.now().plusHours(3).withMinute(0).withSecond(0);
            first.setStartTime(slot.format(FORMATTER));
            first.setEndTime(slot.plusHours(2).format(FORMATTER));
            bookingService.createBooking(first);

            // 尝试创建重叠的预约
            CreateBookingRequest second = new CreateBookingRequest();
            second.setSeatId(1L);
            second.setStartTime(slot.plusHours(1).format(FORMATTER));
            second.setEndTime(slot.plusHours(3).format(FORMATTER));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(second));
            assertEquals(ResultCode.RESERVATION_CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-019: 时间格式错误")
        void createBooking_BadFormat() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            req.setStartTime("not-a-date");
            req.setEndTime("2026-07-10 10:00:00");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.createBooking(req));
            assertEquals(ResultCode.RESERVATION_TIME_INVALID.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("创建后查询我的预约")
        void createThenQuery() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(4).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req);

            var page = bookingService.getMyBookings(1, 10, null, null);
            assertTrue(page.getTotal() > 0);
            boolean found = page.getRecords().stream().anyMatch(b -> b.getId().equals(created.getId()));
            assertTrue(found);
        }
    }

    // ==================== 取消预约 ====================

    @Nested
    @DisplayName("取消预约")
    class CancelTests {

        @Test
        @DisplayName("RES-020: 正常取消")
        void cancel_Success() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req);

            bookingService.cancelBooking(created.getId(), 1L, "SUPER_ADMIN");
            BookingVO after = bookingService.getBookingById(created.getId());
            assertEquals("CANCELLED", after.getStatus());
        }

        @Test
        @DisplayName("RES-024: 取消不存在的预约")
        void cancel_NotFound() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.cancelBooking(99999L, 1L, "SUPER_ADMIN"));
            assertEquals(ResultCode.RESERVATION_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("RES-025: 非本人取消（学生）")
        void cancel_NoPermission() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.cancelBooking(created.getId(), 999L, "STUDENT"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }
    }

    // ==================== 可用时段 ====================

    @Nested
    @DisplayName("可用时段查询")
    class AvailableSlotsTests {

        @Test
        @DisplayName("查询空闲时段成功")
        void availableSlots_Success() {
            var slots = bookingService.getAvailableSlots(1L, "2026-07-10");
            assertNotNull(slots);
            assertFalse(slots.isEmpty(), "08:00-22:00 每小时一个时段，应有多个可用");
        }

        @Test
        @DisplayName("座位不存在")
        void availableSlots_SeatNotFound() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookingService.getAvailableSlots(99999L, "2026-07-10"));
            assertEquals(ResultCode.SEAT_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ==================== 用户管理 ====================

    @Nested
    @DisplayName("用户管理")
    class UserManagementTests {

        @Test
        @DisplayName("分页查询用户列表")
        void getUserList() {
            var page = userService.getUserList(1, 10, null, null, null);
            assertNotNull(page);
            assertTrue(page.getTotal() >= 1); // admin 用户
            assertFalse(page.getRecords().isEmpty());
        }

        @Test
        @DisplayName("按角色筛选用户")
        void getUserList_ByRole() {
            var page = userService.getUserList(1, 10, null, "SUPER_ADMIN", null);
            assertNotNull(page);
            page.getRecords().forEach(u -> assertEquals("SUPER_ADMIN", u.getRole()));
        }

        @Test
        @DisplayName("按关键词搜索用户")
        void getUserList_ByKeyword() {
            var page = userService.getUserList(1, 10, "admin", null, null);
            assertNotNull(page);
            assertTrue(page.getTotal() > 0);
        }

        @Test
        @DisplayName("SUPER_ADMIN 删除普通用户")
        void deleteUser_AsSuperAdmin() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("to_delete_user");
            regReq.setPassword("pass123");
            regReq.setRealName("待删除");
            UserVO created = userService.register(regReq);
            assertNotNull(created);

            // SUPER_ADMIN 删除该用户
            userService.deleteUser(created.getId(), 1L, "SUPER_ADMIN");

            // 确认已逻辑删除（查询不到）
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getCurrentUser(created.getId()));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("SUPER_ADMIN 修改用户状态")
        void updateStatus_AsSuperAdmin() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("status_test");
            regReq.setPassword("pass123");
            regReq.setRealName("状态测试");
            UserVO created = userService.register(regReq);

            // SUPER_ADMIN 禁用
            userService.updateStatus(created.getId(), 0, 1L, "SUPER_ADMIN");
            UserVO disabled = userService.getCurrentUser(created.getId());
            assertEquals(0, disabled.getStatus());

            // SUPER_ADMIN 启用
            userService.updateStatus(created.getId(), 1, 1L, "SUPER_ADMIN");
            UserVO enabled = userService.getCurrentUser(created.getId());
            assertEquals(1, enabled.getStatus());
        }

        @Test
        @DisplayName("禁用后无法登录")
        void disabledUserCannotLogin() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("disabled_login_test");
            regReq.setPassword("pass123");
            regReq.setRealName("禁用登录测试");
            UserVO created = userService.register(regReq);

            userService.updateStatus(created.getId(), 0, 1L, "SUPER_ADMIN");

            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("disabled_login_test");
            loginReq.setPassword("pass123");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.login(loginReq));
            assertEquals(ResultCode.USER_DISABLED.getCode(), ex.getCode());
        }
    }

    // ==================== ADMIN 权限边界测试 ====================

    @Nested
    @DisplayName("ADMIN 权限边界")
    class AdminPermissionTests {

        private Long studentId;

        @BeforeEach
        void setUp() {
            // 注册一个学生，供所有测试使用
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("perm_test_student");
            regReq.setPassword("student123");
            regReq.setRealName("权限测试学生");
            UserVO created = userService.register(regReq);
            studentId = created.getId();
        }

        // ---- createUser 权限 ----

        @Test
        @DisplayName("ADMIN 创建 STUDENT → 成功")
        void createUser_AdminCreatesStudent() {
            CreateUserRequest req = new CreateUserRequest();
            req.setUsername("new_student_01");
            req.setPassword("pass123456");
            req.setRealName("新学生");
            req.setRole("STUDENT");

            UserVO result = userService.createUser(req, "ADMIN");
            assertNotNull(result);
            assertEquals("STUDENT", result.getRole());
        }

        @Test
        @DisplayName("ADMIN 创建 ADMIN → FORBIDDEN")
        void createUser_AdminCreatesAdmin_Forbidden() {
            CreateUserRequest req = new CreateUserRequest();
            req.setUsername("new_admin_evil");
            req.setPassword("pass123456");
            req.setRealName("越权管理员");
            req.setRole("ADMIN");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.createUser(req, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("SUPER_ADMIN 创建 ADMIN → 成功")
        void createUser_SuperAdminCreatesAdmin() {
            CreateUserRequest req = new CreateUserRequest();
            req.setUsername("new_admin_02");
            req.setPassword("pass123456");
            req.setRealName("新管理员");
            req.setRole("ADMIN");

            UserVO result = userService.createUser(req, "SUPER_ADMIN");
            assertNotNull(result);
            assertEquals("ADMIN", result.getRole());
        }

        // ---- deleteUser 权限 ----

        @Test
        @DisplayName("ADMIN 删除 STUDENT → 成功")
        void deleteUser_AdminDeletesStudent() {
            userService.deleteUser(studentId, 2L, "ADMIN");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getCurrentUser(studentId));
            assertEquals(ResultCode.USER_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("ADMIN 删除 ADMIN → FORBIDDEN")
        void deleteUser_AdminDeletesAdmin_Forbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.deleteUser(2L, 2L, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("删除自己 → FORBIDDEN")
        void deleteUser_SelfDelete_Forbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.deleteUser(1L, 1L, "SUPER_ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        // ---- updateStatus 权限 ----

        @Test
        @DisplayName("ADMIN 禁用 STUDENT → 成功")
        void updateStatus_AdminDisablesStudent() {
            userService.updateStatus(studentId, 0, 2L, "ADMIN");
            UserVO result = userService.getCurrentUser(studentId);
            assertEquals(0, result.getStatus());
        }

        @Test
        @DisplayName("ADMIN 禁用 ADMIN → FORBIDDEN")
        void updateStatus_AdminDisablesAdmin_Forbidden() {
            // root(id=1) 是 SUPER_ADMIN，admin(id=2) 是 ADMIN
            // ADMIN 不能修改 ADMIN 或 SUPER_ADMIN 的状态
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.updateStatus(1L, 0, 2L, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        // ---- changePassword 权限 ----

        @Test
        @DisplayName("本人修改密码（正确旧密码）→ 成功")
        void changePassword_SelfCorrectOldPwd() {
            // 先注册一个用户并记录密码
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("pwd_self_test");
            regReq.setPassword("old_password");
            regReq.setRealName("密码测试");
            UserVO created = userService.register(regReq);

            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("old_password");
            req.setNewPassword("new_password");

            assertDoesNotThrow(() ->
                    userService.changePassword(created.getId(), req, created.getId(), "STUDENT"));

            // 验证新密码可登录
            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("pwd_self_test");
            loginReq.setPassword("new_password");
            LoginVO result = userService.login(loginReq);
            assertNotNull(result.getToken());
        }

        @Test
        @DisplayName("本人修改密码（错误旧密码）→ USER_PASSWORD_ERROR")
        void changePassword_SelfWrongOldPwd() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("pwd_wrong_test");
            regReq.setPassword("correct_pass");
            regReq.setRealName("密码错误测试");
            UserVO created = userService.register(regReq);

            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("wrong_old_pass");
            req.setNewPassword("new_password");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.changePassword(created.getId(), req, created.getId(), "STUDENT"));
            assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("ADMIN 重置 STUDENT 密码 → 成功")
        void changePassword_AdminResetsStudent() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setNewPassword("reset_by_admin");

            assertDoesNotThrow(() ->
                    userService.changePassword(studentId, req, 2L, "ADMIN"));

            // 验证新密码可登录
            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("perm_test_student");
            loginReq.setPassword("reset_by_admin");
            LoginVO result = userService.login(loginReq);
            assertNotNull(result.getToken());
        }

        @Test
        @DisplayName("ADMIN 重置 ADMIN 密码 → FORBIDDEN")
        void changePassword_AdminResetsAdmin_Forbidden() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setNewPassword("hacked");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.changePassword(1L, req, 2L, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("SUPER_ADMIN 重置任意密码 → 成功")
        void changePassword_SuperAdminResetsAny() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setNewPassword("reset_by_super");

            assertDoesNotThrow(() ->
                    userService.changePassword(studentId, req, 1L, "SUPER_ADMIN"));

            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("perm_test_student");
            loginReq.setPassword("reset_by_super");
            LoginVO result = userService.login(loginReq);
            assertNotNull(result.getToken());
        }

        // ---- getUserById 权限 ----

        @Test
        @DisplayName("ADMIN 查看 STUDENT → 成功")
        void getUserById_AdminViewsStudent() {
            UserVO result = userService.getUserById(studentId, 2L, "ADMIN");
            assertNotNull(result);
            assertEquals("perm_test_student", result.getUsername());
        }

        @Test
        @DisplayName("ADMIN 查看 SUPER_ADMIN → FORBIDDEN")
        void getUserById_AdminViewsSuperAdmin_Forbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getUserById(1L, 2L, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("STUDENT 查看其他用户 → FORBIDDEN")
        void getUserById_StudentViewsOther_Forbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.getUserById(1L, studentId, "STUDENT"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        // ---- updateUser 权限 ----

        @Test
        @DisplayName("ADMIN 更新 STUDENT 信息 → 成功")
        void updateUser_AdminUpdatesStudent() {
            UpdateUserRequest req = new UpdateUserRequest();
            req.setRealName("修改后的姓名");
            req.setEmail("newemail@test.com");

            UserVO result = userService.updateUser(studentId, req, 2L, "ADMIN");
            assertNotNull(result);
            assertEquals("修改后的姓名", result.getRealName());
            assertEquals("newemail@test.com", result.getEmail());
        }

        @Test
        @DisplayName("ADMIN 修改 STUDENT 的 role → role 不生效（只有 SUPER_ADMIN 能改）")
        void updateUser_AdminChangesStudentRole_Ignored() {
            UpdateUserRequest req = new UpdateUserRequest();
            req.setRole("ADMIN"); // ADMIN 试图提升学生为管理员

            UserVO result = userService.updateUser(studentId, req, 2L, "ADMIN");
            // role 应保持不变（只有 SUPER_ADMIN 能改）
            assertEquals("STUDENT", result.getRole());
        }

        @Test
        @DisplayName("ADMIN 更新 ADMIN 用户 → FORBIDDEN")
        void updateUser_AdminUpdatesAdmin_Forbidden() {
            UpdateUserRequest req = new UpdateUserRequest();
            req.setRealName("hacked");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> userService.updateUser(1L, req, 2L, "ADMIN"));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        // ---- getUserList 补充筛选 ----

        @Test
        @DisplayName("按状态筛选用户")
        void getUserList_ByStatus() {
            // 先禁用一个用户
            userService.updateStatus(studentId, 0, 1L, "SUPER_ADMIN");

            var disabled = userService.getUserList(1, 10, null, null, 0);
            assertTrue(disabled.getTotal() >= 1);
            disabled.getRecords().forEach(u -> assertEquals(0, u.getStatus()));

            var active = userService.getUserList(1, 10, null, null, 1);
            assertTrue(active.getTotal() >= 1);
            active.getRecords().forEach(u -> assertEquals(1, u.getStatus()));
        }
    }
}

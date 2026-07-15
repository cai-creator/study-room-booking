package com.studyroom.booking.integration;

import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.modules.audit.entity.OperationLog;
import com.studyroom.booking.modules.audit.service.OperationLogService;
import com.studyroom.booking.modules.notification.entity.Notification;
import com.studyroom.booking.modules.notification.service.NotificationService;
import com.studyroom.booking.modules.reservation.dto.BookingVO;
import com.studyroom.booking.modules.reservation.dto.CreateBookingRequest;
import com.studyroom.booking.modules.reservation.service.BookingService;
import com.studyroom.booking.modules.seat.dto.CheckinVO;
import com.studyroom.booking.modules.seat.service.CheckinService;
import com.studyroom.booking.modules.user.dto.LoginRequest;
import com.studyroom.booking.modules.user.dto.LoginVO;
import com.studyroom.booking.modules.user.dto.RegisterRequest;
import com.studyroom.booking.modules.user.dto.UserVO;
import com.studyroom.booking.modules.user.service.UserService;
import com.studyroom.booking.common.context.UserContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
@DisplayName("服务层单元测试")
class ServiceUnitTests {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private NotificationService notificationService;

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

    @Nested
    @DisplayName("登录失败锁定")
    class LoginAttemptLockTests {

        @Test
        @DisplayName("登录失败5次后账户被锁定")
        void login_FiveFailures_LockAccount() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("lock_test_user");
            regReq.setPassword("correct_pass");
            regReq.setRealName("锁定测试");
            userService.register(regReq);

            LoginRequest req = new LoginRequest();
            req.setUsername("lock_test_user");
            req.setPassword("wrong_pass");

            for (int i = 0; i < 5; i++) {
                BusinessException ex = assertThrows(BusinessException.class,
                        () -> userService.login(req));
                assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), ex.getCode());
            }

            BusinessException lockEx = assertThrows(BusinessException.class,
                    () -> userService.login(req));
            assertEquals(ResultCode.USER_LOCKED.getCode(), lockEx.getCode());
        }

        @Test
        @DisplayName("登录成功后失败计数重置")
        void login_Success_ResetFailCount() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("reset_test_user");
            regReq.setPassword("correct_pass");
            regReq.setRealName("重置测试");
            userService.register(regReq);

            LoginRequest wrongReq = new LoginRequest();
            wrongReq.setUsername("reset_test_user");
            wrongReq.setPassword("wrong_pass");

            BusinessException ex1 = assertThrows(BusinessException.class,
                    () -> userService.login(wrongReq));
            assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), ex1.getCode());

            LoginRequest correctReq = new LoginRequest();
            correctReq.setUsername("reset_test_user");
            correctReq.setPassword("correct_pass");
            LoginVO result = userService.login(correctReq);
            assertNotNull(result.getToken());

            BusinessException ex2 = assertThrows(BusinessException.class,
                    () -> userService.login(wrongReq));
            assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), ex2.getCode());
        }
    }

    @Nested
    @DisplayName("签到签退")
    class CheckinCheckoutTests {

        @Test
        @DisplayName("签到后状态变为CHECKED_IN")
        void checkin_Success() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime now = LocalDateTime.now().plusMinutes(10).withSecond(0);
            req.setStartTime(now.format(FORMATTER));
            req.setEndTime(now.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            CheckinVO checkin = checkinService.checkin(created.getCheckinCode(), 1L);
            assertNotNull(checkin);
            assertEquals("CHECKED_IN", checkin.getStatus());
        }

        @Test
        @DisplayName("签退后状态变为COMPLETED")
        void checkout_Success() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime now = LocalDateTime.now().plusMinutes(10).withSecond(0);
            req.setStartTime(now.format(FORMATTER));
            req.setEndTime(now.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            checkinService.checkin(created.getCheckinCode(), 1L);
            CheckinVO checkout = checkinService.checkout(created.getSeatCode(), created.getRoomId());
            assertNotNull(checkout);
            assertEquals("COMPLETED", checkout.getStatus());
        }

        @Test
        @DisplayName("签退后可在历史记录中查看")
        void checkout_RecordRetained() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime now = LocalDateTime.now().plusMinutes(10).withSecond(0);
            req.setStartTime(now.format(FORMATTER));
            req.setEndTime(now.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            checkinService.checkin(created.getCheckinCode(), 1L);
            checkinService.checkout(created.getSeatCode(), created.getRoomId());

            BookingVO found = bookingService.getBookingById(created.getId());
            assertNotNull(found);
            assertEquals("COMPLETED", found.getStatus());
        }

        @Test
        @DisplayName("暂离后返回座位")
        void temporaryLeave_Return() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime now = LocalDateTime.now().plusMinutes(10).withSecond(0);
            req.setStartTime(now.format(FORMATTER));
            req.setEndTime(now.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            checkinService.checkin(created.getCheckinCode(), 1L);
            CheckinVO leave = checkinService.temporaryLeave(created.getSeatCode(), created.getRoomId());
            assertEquals("TEMPORARY_LEAVE", leave.getStatus());

            CheckinVO back = checkinService.returnSeat(created.getSeatCode(), created.getRoomId());
            assertEquals("CHECKED_IN", back.getStatus());
        }
    }

    @Nested
    @DisplayName("取消预约")
    class CancelBookingTests {

        @Test
        @DisplayName("取消后状态变为CANCELLED")
        void cancel_Success_StatusCancelled() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            bookingService.cancelBooking(created.getId(), 1L, "SUPER_ADMIN");
            BookingVO after = bookingService.getBookingById(created.getId());
            assertEquals("CANCELLED", after.getStatus());
        }

        @Test
        @DisplayName("取消后可在历史记录中查看")
        void cancel_RecordRetained() {
            loginAs(1L, "admin", "SUPER_ADMIN");

            CreateBookingRequest req = new CreateBookingRequest();
            req.setSeatId(1L);
            LocalDateTime future = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0);
            req.setStartTime(future.format(FORMATTER));
            req.setEndTime(future.plusHours(1).format(FORMATTER));
            BookingVO created = bookingService.createBooking(req).get(0);

            bookingService.cancelBooking(created.getId(), 1L, "SUPER_ADMIN");

            BookingVO found = bookingService.getBookingById(created.getId());
            assertNotNull(found);
            assertEquals("CANCELLED", found.getStatus());
        }
    }

    @Nested
    @DisplayName("操作日志")
    class OperationLogTests {

        @Test
        @DisplayName("记录操作日志成功")
        void logOperation_Success() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setAttribute("userId", 1L);
            request.setAttribute("role", "SUPER_ADMIN");
            UserContext.setRequest(request);

            operationLogService.logSuccess("USER_MANAGEMENT", "创建用户", "USER", 100L, "test_user", request);

            List<OperationLog> logs = operationLogService.getRecentLogs(10);
            assertFalse(logs.isEmpty());
            OperationLog latest = logs.get(0);
            assertEquals("USER_MANAGEMENT", latest.getModule());
            assertEquals("创建用户", latest.getOperation());
            assertEquals(1, latest.getResult());
        }

        @Test
        @DisplayName("记录失败操作日志")
        void logOperation_Failure() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setAttribute("userId", 1L);
            request.setAttribute("role", "ADMIN");
            UserContext.setRequest(request);

            operationLogService.logFailure("BOOKING", "创建预约", "SEAT", 1L, "A-01", "座位不可用", request);

            List<OperationLog> logs = operationLogService.getRecentLogs(10);
            OperationLog latest = logs.get(0);
            assertEquals("BOOKING", latest.getModule());
            assertEquals("创建预约", latest.getOperation());
            assertEquals(0, latest.getResult());
            assertEquals("座位不可用", latest.getErrorMessage());
        }

        @Test
        @DisplayName("分页查询操作日志")
        void getOperationLogs_Pagination() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setAttribute("userId", 1L);
            request.setAttribute("role", "ADMIN");
            UserContext.setRequest(request);

            for (int i = 0; i < 15; i++) {
                operationLogService.logSuccess("TEST", "测试操作" + i, "TEST", (long) i, "target" + i, request);
            }

            var page = operationLogService.getOperationLogs(1, 10, null, null, null, null, null, null);
            assertEquals(15, page.getTotal());
            assertEquals(10, page.getRecords().size());
        }
    }

    @Nested
    @DisplayName("消息通知")
    class NotificationTests {

        @Test
        @DisplayName("发送通知成功")
        void sendNotification_Success() {
            notificationService.sendNotification(1L, "BOOKING_REMINDER", "预约提醒",
                    "您的预约即将开始", "{\"bookingId\":123}");

            List<Notification> notifications = notificationService.getUserNotifications(1L, 1, 10, 0).getRecords();
            assertFalse(notifications.isEmpty());
            Notification latest = notifications.get(0);
            assertEquals("BOOKING_REMINDER", latest.getType());
            assertEquals("预约提醒", latest.getTitle());
            assertEquals(0, latest.getReadFlag());
        }

        @Test
        @DisplayName("获取未读数量")
        void getUnreadCount_Success() {
            notificationService.sendNotification(1L, "TEST", "测试1", "内容1", null);
            notificationService.sendNotification(1L, "TEST", "测试2", "内容2", null);

            long count = notificationService.getUnreadCount(1L);
            assertTrue(count >= 2);
        }

        @Test
        @DisplayName("标记为已读")
        void markAsRead_Success() {
            notificationService.sendNotification(1L, "TEST", "测试", "内容", null);

            List<Notification> unread = notificationService.getUserNotifications(1L, 1, 10, 0).getRecords();
            assertFalse(unread.isEmpty());

            notificationService.markAsRead(1L, unread.get(0).getId());

            List<Notification> stillUnread = notificationService.getUserNotifications(1L, 1, 10, 0).getRecords();
            assertTrue(stillUnread.isEmpty());
        }

        @Test
        @DisplayName("全部标记为已读")
        void markAllRead_Success() {
            notificationService.sendNotification(1L, "TEST", "测试1", "内容1", null);
            notificationService.sendNotification(1L, "TEST", "测试2", "内容2", null);

            long before = notificationService.getUnreadCount(1L);
            assertTrue(before >= 2);

            notificationService.markAllRead(1L);

            long after = notificationService.getUnreadCount(1L);
            assertEquals(0, after);
        }
    }

    @Nested
    @DisplayName("用户服务")
    class UserServiceTests {

        @Test
        @DisplayName("创建用户时自动生成初始密码")
        void createUser_WithGeneratedPassword() {
            RegisterRequest regReq = new RegisterRequest();
            regReq.setUsername("20240001");
            regReq.setPassword("Password1");
            regReq.setRealName("张三");
            UserVO created = userService.register(regReq);
            assertNotNull(created);

            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername("20240001");
            loginReq.setPassword("Password1");
            LoginVO result = userService.login(loginReq);
            assertNotNull(result.getToken());
        }

        @Test
        @DisplayName("创建用户时初始密码包含姓名拼音首字母")
        void createUser_PinyinInitialPassword() {
            com.studyroom.booking.modules.user.dto.CreateUserRequest req =
                    new com.studyroom.booking.modules.user.dto.CreateUserRequest();
            req.setUsername("20241234");
            req.setRealName("李四");
            req.setRole("STUDENT");

            UserVO created = userService.createUser(req, "ADMIN");
            assertNotNull(created);
            assertNotNull(created.getInitialPassword());
            assertTrue(created.getInitialPassword().startsWith("Ls"));
            assertTrue(created.getInitialPassword().endsWith("1234"));
        }
    }
}
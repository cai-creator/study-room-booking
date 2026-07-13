package com.studyroom.booking.modules.reservation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.reservation.dto.AvailableSlotVO;
import com.studyroom.booking.modules.reservation.dto.BookingVO;
import com.studyroom.booking.modules.reservation.dto.CreateBookingRequest;
import com.studyroom.booking.modules.reservation.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约核心控制器
 *
 * @author 郭学威
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "预约核心", description = "预约创建、取消、查询相关接口")
public class BookingController {

    private final BookingService bookingService;

    // ==================== 创建预约 ====================

    @PostMapping("/reservations")
    @RequireRole("STUDENT")
    @Operation(summary = "创建预约", description = "学生选择座位和时间段创建预约，支持多时段，系统自动校验预约规则")
    public Result<List<BookingVO>> create(@Valid @RequestBody CreateBookingRequest request) {
        return Result.success("预约成功", bookingService.createBooking(request));
    }

    // ==================== 取消预约 ====================

    @PostMapping("/reservations/{id}/cancel")
    @RequireRole({"STUDENT", "ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "取消预约", description = "取消预约，仅RESERVED状态可取消，开始后不可取消")
    public Result<Void> cancel(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role) {
        bookingService.cancelBooking(id, userId, role);
        return Result.success("取消成功", null);
    }

    // ==================== 我的预约 ====================

    @GetMapping("/reservations/my")
    @RequireRole("STUDENT")
    @Operation(summary = "我的预约", description = "分页查询当前用户的预约记录")
    public Result<Page<BookingVO>> myReservations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "状态筛选: RESERVED/CHECKED_IN/CHECKED_OUT/CANCELLED/NO_SHOW")
            @RequestParam(required = false) String status,
            @Parameter(description = "日期筛选 (yyyy-MM-dd)") @RequestParam(required = false) String date) {
        return Result.success(bookingService.getMyBookings(pageNum, pageSize, status, date));
    }

    // ==================== 预约详情 ====================

    @GetMapping("/reservations/{id}")
    @RequireRole({"STUDENT", "ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "预约详情", description = "获取单条预约的详细信息")
    public Result<BookingVO> detail(@PathVariable Long id) {
        return Result.success(bookingService.getBookingById(id));
    }

    // ==================== 管理员预约列表 ====================

    @GetMapping("/reservations")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "预约列表（管理员）", description = "管理员分页查询所有预约记录")
    public Result<Page<BookingVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "自习室ID筛选") @RequestParam(required = false) Long roomId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {
        return Result.success(bookingService.getAllBookings(
                pageNum, pageSize, userId, roomId, status, startDate, endDate));
    }

    // ==================== 可用时段查询 ====================

    @GetMapping("/seats/{seatId}/available-slots")
    @Operation(summary = "座位可用时段", description = "查询指定座位在某一天的空闲时段，按1小时粒度划分")
    public Result<List<AvailableSlotVO>> availableSlots(
            @Parameter(description = "座位ID") @PathVariable Long seatId,
            @Parameter(description = "查询日期 (yyyy-MM-dd)，默认当天") @RequestParam String date) {
        return Result.success(bookingService.getAvailableSlots(seatId, date));
    }
}

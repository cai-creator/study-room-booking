package com.studyroom.booking.modules.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.reservation.dto.AvailableSlotVO;
import com.studyroom.booking.modules.reservation.dto.BookingVO;
import com.studyroom.booking.modules.reservation.dto.CreateBookingRequest;
import com.studyroom.booking.modules.reservation.entity.Booking;
import com.studyroom.booking.modules.reservation.mapper.BookingMapper;
import com.studyroom.booking.modules.seat.service.BlacklistService;
import com.studyroom.booking.modules.space.entity.*;
import com.studyroom.booking.modules.space.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预约核心服务
 *
 * @author 郭学威
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService extends ServiceImpl<BookingMapper, Booking> {

    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final FloorMapper floorMapper;
    private final BuildingMapper buildingMapper;
    private final CampusMapper campusMapper;
    private final BlacklistService blacklistService;

    @Value("${booking.rules.max-daily-reservations:3}")
    private int maxDailyReservations;

    @Value("${booking.rules.max-duration-hours:8}")
    private int maxDurationHours;

    @Value("${booking.rules.advance-booking-hours:24}")
    private int advanceBookingHours;

    @Value("${booking.rules.min-advance-minutes:15}")
    private int minAdvanceMinutes;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 创建预约 ====================

    @Transactional
    public List<BookingVO> createBooking(CreateBookingRequest request) {
        Long userId = UserContext.getUserId();

        // 黑名单检查
        if (blacklistService.isUserBlacklisted(userId)) {
            throw new BusinessException(ResultCode.BLACKLISTED);
        }

        // 座位存在性 + 可用性
        Seat seat = seatMapper.selectById(request.getSeatId());
        if (seat == null || seat.getDeleted() == 1) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        if (seat.getStatus() != 1) {
            throw new BusinessException(ResultCode.SEAT_NOT_AVAILABLE);
        }

        // 自习室存在性 + 开放状态
        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
        if (room == null || room.getDeleted() == 1) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        if (room.getStatus() != 1) {
            throw new BusinessException(ResultCode.ROOM_CLOSED);
        }

        // 构建时段列表并合并为一条预约
        List<LocalDateTime[]> slots = new ArrayList<>();
        if (request.getTimeSlots() != null && !request.getTimeSlots().isEmpty()) {
            for (CreateBookingRequest.TimeSlotDTO ts : request.getTimeSlots()) {
                slots.add(new LocalDateTime[]{parseDateTime(ts.getStartTime()), parseDateTime(ts.getEndTime())});
            }
        } else {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID);
            }
            slots.add(new LocalDateTime[]{parseDateTime(request.getStartTime()), parseDateTime(request.getEndTime())});
        }

        // 按开始时间排序
        slots.sort((a, b) -> a[0].compareTo(b[0]));

        // 验证时段合法性和相邻性，合并为整体时间范围
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime overallStart = slots.get(0)[0];
        LocalDateTime overallEnd = slots.get(slots.size() - 1)[1];

        for (int i = 0; i < slots.size(); i++) {
            LocalDateTime[] slot = slots.get(i);
            if (!slot[1].isAfter(slot[0])) {
                throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID);
            }
            if (i > 0 && !slot[0].equals(slots.get(i - 1)[1])) {
                throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID.getCode(), "多时段预约的时段必须相邻");
            }
        }

        // 对合并后的整体时间范围进行校验
        LocalDateTime startTime = overallStart;
        LocalDateTime endTime = overallEnd;

        // 预约窗口检查
        if (startTime.isAfter(now.plusHours(advanceBookingHours))) {
            throw new BusinessException(ResultCode.RESERVATION_TOO_EARLY);
        }
        if (startTime.isBefore(now.plusMinutes(minAdvanceMinutes))) {
            throw new BusinessException(ResultCode.RESERVATION_TOO_LATE);
        }

        // 自习室开放时间检查
        if (room.getOpenTime() != null && startTime.toLocalTime().isBefore(room.getOpenTime())) {
            throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID.getCode(), "预约开始时间早于自习室开放时间(" + room.getOpenTime() + ")");
        }
        if (room.getCloseTime() != null && endTime.toLocalTime().isAfter(room.getCloseTime())) {
            throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID.getCode(), "预约结束时间晚于自习室关闭时间(" + room.getCloseTime() + ")");
        }

        // 总时长检查
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (durationMinutes > (long) maxDurationHours * 60) {
            throw new BusinessException(ResultCode.RESERVATION_DURATION_EXCEEDED);
        }

        // 当日预约次数检查
        LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        long todayCount = baseMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId)
                .ge(Booking::getStartTime, dayStart)
                .lt(Booking::getStartTime, dayEnd)
                .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE"));
        if (todayCount >= maxDailyReservations) {
            throw new BusinessException(ResultCode.RESERVATION_DAILY_LIMIT_EXCEEDED);
        }

        // 用户时段冲突检查
        Long userConflictCount = baseMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId)
                .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE")
                .lt(Booking::getStartTime, endTime)
                .gt(Booking::getEndTime, startTime));
        if (userConflictCount > 0) {
            throw new BusinessException(ResultCode.RESERVATION_USER_CONFLICT);
        }

        // 座位时段冲突检查
        Long conflictCount = baseMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getSeatId, request.getSeatId())
                .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE")
                .lt(Booking::getStartTime, endTime)
                .gt(Booking::getEndTime, startTime));
        if (conflictCount > 0) {
            throw new BusinessException(ResultCode.RESERVATION_CONFLICT);
        }

        // 写入数据库（合并为单条预约）
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setSeatId(request.getSeatId());
        booking.setRoomId(seat.getRoomId());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus("RESERVED");
        booking.setVersion(0);
        baseMapper.insert(booking);

        log.info("用户 {} 创建预约成功，预约ID: {}, 座位: {}, 时间: {} ~ {}",
                userId, booking.getId(), seat.getSeatCode(), startTime, endTime);

        return Collections.singletonList(buildBookingVO(booking, seat, room));
    }

    // ==================== 取消预约 ====================

    @Transactional
    public void cancelBooking(Long bookingId, Long userId, String role) {
        Booking booking = baseMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }

        boolean isAdmin = "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (!"RESERVED".equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.RESERVATION_CANCEL_NOT_ALLOWED);
        }

        if (LocalDateTime.now().isAfter(booking.getStartTime())) {
            throw new BusinessException(3008, "预约已开始，无法取消");
        }

        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(booking);

        log.info("用户 {} 取消预约 {}，操作者: {}", booking.getUserId(), bookingId, userId);
    }

    // ==================== 查询 ====================

    private static final int MAX_PAGE_SIZE = 100;

    public Page<BookingVO> getMyBookings(int pageNum, int pageSize, String status, String date) {
        Long userId = UserContext.getUserId();
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Booking::getStatus, "CHECKED_OUT".equals(status) ? "COMPLETED" : status);
        }
        if (date != null && !date.isEmpty()) {
            LocalDate queryDate = LocalDate.parse(date, DATE_FORMATTER);
            wrapper.ge(Booking::getStartTime, queryDate.atStartOfDay())
                   .lt(Booking::getStartTime, queryDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(Booking::getCreatedAt);

        return convertToVOPage(baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    public BookingVO getBookingById(Long bookingId) {
        Booking booking = baseMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        Seat seat = seatMapper.selectById(booking.getSeatId());
        StudyRoom room = seat != null ? studyRoomMapper.selectById(seat.getRoomId()) : null;
        return buildBookingVO(booking, seat, room);
    }

    public Page<BookingVO> getAllBookings(int pageNum, int pageSize,
                                           Long userId, Long roomId, String status,
                                           String startDate, String endDate) {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(Booking::getUserId, userId);
        if (roomId != null) wrapper.eq(Booking::getRoomId, roomId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Booking::getStatus, "CHECKED_OUT".equals(status) ? "COMPLETED" : status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Booking::getStartTime, LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Booking::getStartTime, LocalDate.parse(endDate, DATE_FORMATTER).plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(Booking::getCreatedAt);

        return convertToVOPage(baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    // ==================== 可用时段 ====================

    public List<AvailableSlotVO> getAvailableSlots(Long seatId, String dateStr) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || seat.getDeleted() == 1) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        if (seat.getStatus() != 1) {
            throw new BusinessException(ResultCode.SEAT_NOT_AVAILABLE);
        }

        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
        LocalDate queryDate = (dateStr != null && !dateStr.isEmpty())
                ? LocalDate.parse(dateStr, DATE_FORMATTER) : LocalDate.now();

        LocalTime openTime = (room != null && room.getOpenTime() != null) ? room.getOpenTime() : LocalTime.of(8, 0);
        LocalTime closeTime = (room != null && room.getCloseTime() != null) ? room.getCloseTime() : LocalTime.of(22, 0);

        LocalDateTime dayOpen = LocalDateTime.of(queryDate, openTime);
        LocalDateTime dayClose = LocalDateTime.of(queryDate, closeTime);

        // 生成1小时步进的时段（非重叠），对齐前端 Mock 预期
        List<LocalDateTime[]> allSlots = new ArrayList<>();
        LocalDateTime slotStart = dayOpen;
        while (slotStart.plusHours(1).isBefore(dayClose) || slotStart.plusHours(1).equals(dayClose)) {
            allSlots.add(new LocalDateTime[]{slotStart, slotStart.plusHours(1)});
            slotStart = slotStart.plusHours(1);
        }

        // 查询当天已有预约
        List<Booking> existingBookings = baseMapper.selectList(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getSeatId, seatId)
                .in(Booking::getStatus, "RESERVED", "CHECKED_IN", "TEMPORARY_LEAVE")
                .lt(Booking::getStartTime, dayClose)
                .gt(Booking::getEndTime, dayOpen));

        // 过滤已被占用和已过期的时段
        LocalDateTime now = LocalDateTime.now();
        List<AvailableSlotVO> result = new ArrayList<>();
        for (LocalDateTime[] slot : allSlots) {
            if (slot[0].isBefore(now.plusMinutes(minAdvanceMinutes))) continue;

            boolean free = existingBookings.stream().noneMatch(
                    b -> slot[0].isBefore(b.getEndTime()) && slot[1].isAfter(b.getStartTime()));
            if (free) {
                result.add(new AvailableSlotVO(slot[0].format(FORMATTER), slot[1].format(FORMATTER)));
            }
        }
        return result;
    }

    // ==================== 辅助方法 ====================

    private LocalDateTime parseDateTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr, FORMATTER);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.RESERVATION_TIME_INVALID);
        }
    }

    /**
     * 构建 BookingVO，含关联的名称信息
     */
    private BookingVO buildBookingVO(Booking booking, Seat seat, StudyRoom room) {
        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setSeatId(booking.getSeatId());
        vo.setSeatCode(seat != null ? seat.getSeatCode() : null);
        vo.setRoomId(booking.getRoomId());
        vo.setRoomName(room != null ? room.getName() : null);

        // 楼栋和校区名称（从 room → floor → building → campus 链查询）
        String buildingName = null;
        String campusName = null;
        if (room != null) {
            Floor floor = floorMapper.selectById(room.getFloorId());
            if (floor != null) {
                Building building = buildingMapper.selectById(floor.getBuildingId());
                if (building != null) {
                    buildingName = building.getName();
                    Campus campus = campusMapper.selectById(building.getCampusId());
                    if (campus != null) campusName = campus.getName();
                }
            }
        }
        vo.setBuildingName(buildingName);
        vo.setCampusName(campusName);

        vo.setStartTime(booking.getStartTime() != null ? booking.getStartTime().format(FORMATTER) : null);
        vo.setEndTime(booking.getEndTime() != null ? booking.getEndTime().format(FORMATTER) : null);

        // 状态映射: COMPLETED → CHECKED_OUT（对齐前端）
        vo.setStatus("COMPLETED".equals(booking.getStatus()) ? "CHECKED_OUT" : booking.getStatus());

        // 签到码: QR + yyyyMMdd + 4位ID补零
        String datePart = booking.getStartTime() != null
                ? booking.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        vo.setCheckinCode("QR" + datePart + String.format("%04d", booking.getId() != null ? booking.getId() : 0));

        vo.setCreatedAt(booking.getCreatedAt() != null ? booking.getCreatedAt().format(FORMATTER) : null);
        return vo;
    }

    /**
     * 批量转换分页结果（优化版：批量查询关联数据，避免 N+1 问题）
     */
    private Page<BookingVO> convertToVOPage(Page<Booking> bookingPage) {
        List<Booking> bookings = bookingPage.getRecords();
        if (bookings.isEmpty()) {
            Page<BookingVO> empty = new Page<>(bookingPage.getCurrent(), bookingPage.getSize(), bookingPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 1. 批量查询所有座位
        Set<Long> seatIds = bookings.stream().map(Booking::getSeatId).collect(Collectors.toSet());
        Map<Long, Seat> seatMap = seatMapper.selectBatchIds(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, Function.identity()));

        // 2. 批量查询所有自习室
        Set<Long> roomIds = seatMap.values().stream().map(Seat::getRoomId).collect(Collectors.toSet());
        Map<Long, StudyRoom> roomMap = studyRoomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(StudyRoom::getId, Function.identity()));

        // 3. 批量查询楼层 → 楼栋 → 校区
        Set<Long> floorIds = roomMap.values().stream().map(StudyRoom::getFloorId).collect(Collectors.toSet());
        Map<Long, Floor> floorMap = floorMapper.selectBatchIds(floorIds).stream()
                .collect(Collectors.toMap(Floor::getId, Function.identity()));

        Set<Long> buildingIds = floorMap.values().stream().map(Floor::getBuildingId).collect(Collectors.toSet());
        Map<Long, Building> buildingMap = buildingMapper.selectBatchIds(buildingIds).stream()
                .collect(Collectors.toMap(Building::getId, Function.identity()));

        Set<Long> campusIds = buildingMap.values().stream().map(Building::getCampusId).collect(Collectors.toSet());
        Map<Long, Campus> campusMap = campusMapper.selectBatchIds(campusIds).stream()
                .collect(Collectors.toMap(Campus::getId, Function.identity()));

        // 4. 在内存中组装 VO（零额外数据库查询）
        List<BookingVO> voList = bookings.stream().map(booking -> {
            Seat seat = seatMap.get(booking.getSeatId());
            StudyRoom room = seat != null ? roomMap.get(seat.getRoomId()) : null;

            BookingVO vo = new BookingVO();
            vo.setId(booking.getId());
            vo.setSeatId(booking.getSeatId());
            vo.setSeatCode(seat != null ? seat.getSeatCode() : null);
            vo.setRoomId(booking.getRoomId());
            vo.setRoomName(room != null ? room.getName() : null);

            if (room != null) {
                Floor floor = floorMap.get(room.getFloorId());
                if (floor != null) {
                    Building building = buildingMap.get(floor.getBuildingId());
                    if (building != null) {
                        vo.setBuildingName(building.getName());
                        Campus campus = campusMap.get(building.getCampusId());
                        if (campus != null) vo.setCampusName(campus.getName());
                    }
                }
            }

            vo.setStartTime(booking.getStartTime() != null ? booking.getStartTime().format(FORMATTER) : null);
            vo.setEndTime(booking.getEndTime() != null ? booking.getEndTime().format(FORMATTER) : null);
            vo.setStatus("COMPLETED".equals(booking.getStatus()) ? "CHECKED_OUT" : booking.getStatus());

            String datePart = booking.getStartTime() != null
                    ? booking.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            vo.setCheckinCode("QR" + datePart + String.format("%04d", booking.getId() != null ? booking.getId() : 0));
            vo.setCreatedAt(booking.getCreatedAt() != null ? booking.getCreatedAt().format(FORMATTER) : null);

            return vo;
        }).collect(Collectors.toList());

        Page<BookingVO> voPage = new Page<>(bookingPage.getCurrent(), bookingPage.getSize(), bookingPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}

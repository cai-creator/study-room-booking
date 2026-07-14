package com.studyroom.booking.modules.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.reservation.entity.Booking;
import com.studyroom.booking.modules.reservation.mapper.BookingMapper;
import com.studyroom.booking.modules.space.entity.StudyRoom;
import com.studyroom.booking.modules.space.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据报表服务（成员C负责部分）
 *
 * @author 郭学威
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingMapper bookingMapper;
    private final StudyRoomMapper studyRoomMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 日均使用率 ====================

    /**
     * 日均使用率统计
     * 统计每天每个自习室的座位使用率 = 已签到/已完成预约数 / 总座位数
     */
    public List<Map<String, Object>> getUsageRate(String startDate, String endDate, Long roomId) {
        LocalDateTime start = LocalDate.parse(startDate, DATE_FMT).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DATE_FMT).plusDays(1).atStartOfDay();

        // 查询时间范围内的有效预约（已签到或已完成）
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .in(Booking::getStatus, "CHECKED_IN", "COMPLETED")
                .ge(Booking::getStartTime, start)
                .lt(Booking::getStartTime, end));

        // 查询所有自习室
        List<StudyRoom> allRooms = studyRoomMapper.selectList(null);

        // 按 roomId 过滤
        if (roomId != null) {
            allRooms = allRooms.stream().filter(r -> r.getId().equals(roomId)).collect(Collectors.toList());
            bookings = bookings.stream().filter(b -> b.getRoomId().equals(roomId)).collect(Collectors.toList());
        }

        // 按日期 + 自习室分组统计
        Map<String, Long> usedByDateRoom = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStartTime().toLocalDate() + "|" + b.getRoomId(),
                        Collectors.counting()));

        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = LocalDate.parse(startDate, DATE_FMT);
        LocalDate endDateParsed = LocalDate.parse(endDate, DATE_FMT);

        while (!current.isAfter(endDateParsed)) {
            String dateStr = current.format(DATE_FMT);
            for (StudyRoom room : allRooms) {
                long used = usedByDateRoom.getOrDefault(dateStr + "|" + room.getId(), 0L);
                int total = room.getTotalSeats() != null && room.getTotalSeats() > 0 ? room.getTotalSeats() : 1;
                double rate = Math.round(used * 10000.0 / total) / 100.0;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("date", dateStr);
                item.put("roomId", room.getId());
                item.put("roomName", room.getName());
                item.put("usedSeats", used);
                item.put("totalSeats", total);
                item.put("usageRate", rate);
                result.add(item);
            }
            current = current.plusDays(1);
        }
        return result;
    }

    // ==================== 时段分布 ====================

    /**
     * 24小时时段分布
     * 统计每个小时段内的预约数量
     */
    public List<Map<String, Object>> getTimeDistribution(String startDate, String endDate, Long roomId) {
        LocalDateTime start = LocalDate.parse(startDate, DATE_FMT).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DATE_FMT).plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .ge(Booking::getStartTime, start)
                .lt(Booking::getStartTime, end);
        if (roomId != null) wrapper.eq(Booking::getRoomId, roomId);

        List<Booking> bookings = bookingMapper.selectList(wrapper);

        // 按小时分组统计
        Map<Integer, Long> hourCount = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStartTime().getHour(),
                        Collectors.counting()));

        // 填充24小时
        List<Map<String, Object>> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", h);
            item.put("label", String.format("%02d:00", h));
            item.put("count", hourCount.getOrDefault(h, 0L));
            result.add(item);
        }
        return result;
    }

    // ==================== 热门时段 ====================

    /**
     * 热门时段 TOP5
     * 按预约开始时间的小时统计，取预约数最多的5个时段
     */
    public List<Map<String, Object>> getHotPeriods(String startDate, String endDate, Long roomId) {
        // 复用时段分布数据，取TOP5
        List<Map<String, Object>> distribution = getTimeDistribution(startDate, endDate, roomId);

        return distribution.stream()
                .filter(d -> (long) d.get("count") > 0)
                .sorted((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")))
                .limit(5)
                .map(d -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("hour", d.get("hour"));
                    item.put("label", d.get("label"));
                    item.put("count", d.get("count"));
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ==================== 导出 Excel ====================

    /**
     * 导出预约数据到 Excel
     */
    public void exportReport(OutputStream outputStream, String startDate, String endDate,
                              Long roomId) throws Exception {
        LocalDateTime start = LocalDate.parse(startDate, DATE_FMT).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DATE_FMT).plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .ge(Booking::getStartTime, start)
                .lt(Booking::getStartTime, end);
        if (roomId != null) wrapper.eq(Booking::getRoomId, roomId);
        wrapper.orderByAsc(Booking::getStartTime);

        List<Booking> bookings = bookingMapper.selectList(wrapper);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预约数据报表");

            // 表头
            Row header = sheet.createRow(0);
            String[] headers = {"预约ID", "用户ID", "座位ID", "自习室ID", "开始时间", "结束时间", "状态", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // 数据行
            int rowNum = 1;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Booking b : bookings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(b.getId() != null ? b.getId() : 0);
                row.createCell(1).setCellValue(b.getUserId() != null ? b.getUserId() : 0);
                row.createCell(2).setCellValue(b.getSeatId() != null ? b.getSeatId() : 0);
                row.createCell(3).setCellValue(b.getRoomId() != null ? b.getRoomId() : 0);
                row.createCell(4).setCellValue(b.getStartTime() != null ? b.getStartTime().format(fmt) : "");
                row.createCell(5).setCellValue(b.getEndTime() != null ? b.getEndTime().format(fmt) : "");
                row.createCell(6).setCellValue(b.getStatus() != null ? b.getStatus() : "");
                row.createCell(7).setCellValue(b.getCreatedAt() != null ? b.getCreatedAt().format(fmt) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            outputStream.flush();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}

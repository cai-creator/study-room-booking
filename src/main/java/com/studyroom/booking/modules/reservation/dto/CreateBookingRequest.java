package com.studyroom.booking.modules.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建预约请求
 * <p>
 * 支持单时段和多时段预约：
 * - 单时段：提供 seatId + startTime + endTime
 * - 多时段：提供 seatId + timeSlots（每个元素包含 startTime 和 endTime）
 *
 * @author 郭学威
 */
@Data
@Schema(description = "创建预约请求")
public class CreateBookingRequest {

    @NotNull(message = "座位ID不能为空")
    @Schema(description = "座位ID", example = "1")
    private Long seatId;

    @Schema(description = "预约开始时间（单时段预约时使用）", example = "2026-07-07 09:00:00")
    private String startTime;

    @Schema(description = "预约结束时间（单时段预约时使用）", example = "2026-07-07 10:00:00")
    private String endTime;

    @Schema(description = "多时段预约列表（多时段预约时使用，每个元素包含startTime和endTime）")
    private List<TimeSlotDTO> timeSlots;

    /**
     * 时段DTO
     */
    @Data
    @Schema(description = "时段信息")
    public static class TimeSlotDTO {
        @Schema(description = "时段开始时间", example = "2026-07-07 09:00:00")
        private String startTime;

        @Schema(description = "时段结束时间", example = "2026-07-07 10:00:00")
        private String endTime;
    }
}

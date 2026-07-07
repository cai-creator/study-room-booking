package com.studyroom.booking.modules.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建预约请求
 *
 * @author 郭学威
 */
@Data
@Schema(description = "创建预约请求")
public class CreateBookingRequest {

    @NotNull(message = "座位ID不能为空")
    @Schema(description = "座位ID", example = "1")
    private Long seatId;

    @NotBlank(message = "开始时间不能为空")
    @Schema(description = "预约开始时间", example = "2026-07-07 09:00:00")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Schema(description = "预约结束时间", example = "2026-07-07 12:00:00")
    private String endTime;
}

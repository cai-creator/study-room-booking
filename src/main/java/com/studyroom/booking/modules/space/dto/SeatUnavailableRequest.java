package com.studyroom.booking.modules.space.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "座位定时不可用请求")
public class SeatUnavailableRequest {

    @Schema(description = "重复类型: ONCE-单次, DAILY-每日, WEEKLY-每周, MONTHLY-每月", example = "ONCE")
    private String repeatType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始日期时间", example = "2026-07-11 10:00:00")
    private LocalDateTime startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束日期时间", example = "2026-07-11 12:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "星期几(每周重复时使用, 1-7对应周日到周六)")
    private Integer dayOfWeek;

    @Schema(description = "每月几号(每月重复时使用, 1-31)")
    private Integer dayOfMonth;

    @Schema(description = "不可用原因")
    private String reason;
}
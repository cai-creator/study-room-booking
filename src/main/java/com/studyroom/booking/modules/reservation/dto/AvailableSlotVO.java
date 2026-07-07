package com.studyroom.booking.modules.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 可用时段响应
 *
 * @author 郭学威
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "可用时段")
public class AvailableSlotVO implements Serializable {

    @Schema(description = "时段开始时间", example = "2026-07-07 08:00:00")
    private String startTime;

    @Schema(description = "时段结束时间", example = "2026-07-07 10:00:00")
    private String endTime;
}

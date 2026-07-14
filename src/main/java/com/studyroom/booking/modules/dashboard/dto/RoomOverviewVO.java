package com.studyroom.booking.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 自习室使用概览 VO
 *
 * @author 郭学威
 */
@Data
@Schema(description = "自习室使用概览")
public class RoomOverviewVO implements Serializable {

    @Schema(description = "自习室ID")
    private Long roomId;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "楼层ID")
    private Long floorId;

    @Schema(description = "总座位数")
    private Integer totalSeats;

    @Schema(description = "可用座位数")
    private Integer availableSeats;

    @Schema(description = "已预约座位数")
    private Integer reservedSeats;

    @Schema(description = "已占用座位数")
    private Integer occupiedSeats;

    @Schema(description = "使用率（百分比）", example = "35.0")
    private Double usageRate;
}
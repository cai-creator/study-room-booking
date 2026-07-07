package com.studyroom.booking.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 校区使用概览 VO
 *
 * @author 郭学威
 */
@Data
@Schema(description = "校区使用概览")
public class CampusOverviewVO implements Serializable {

    @Schema(description = "校区ID")
    private Long campusId;

    @Schema(description = "校区名称")
    private String campusName;

    @Schema(description = "自习室总数")
    private Integer totalRooms;

    @Schema(description = "总座位数")
    private Integer totalSeats;

    @Schema(description = "可用座位数")
    private Integer availableSeats;

    @Schema(description = "使用率（百分比）", example = "30.0")
    private Double usageRate;
}

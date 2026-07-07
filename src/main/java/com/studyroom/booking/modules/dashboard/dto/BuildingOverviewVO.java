package com.studyroom.booking.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 楼栋使用概览 VO
 *
 * @author 郭学威
 */
@Data
@Schema(description = "楼栋使用概览")
public class BuildingOverviewVO implements Serializable {

    @Schema(description = "楼栋ID")
    private Long buildingId;

    @Schema(description = "楼栋名称")
    private String buildingName;

    @Schema(description = "所属校区ID")
    private Long campusId;

    @Schema(description = "所属校区名称")
    private String campusName;

    @Schema(description = "自习室总数")
    private Integer totalRooms;

    @Schema(description = "总座位数")
    private Integer totalSeats;

    @Schema(description = "可用座位数")
    private Integer availableSeats;

    @Schema(description = "使用率（百分比）", example = "45.5")
    private Double usageRate;
}

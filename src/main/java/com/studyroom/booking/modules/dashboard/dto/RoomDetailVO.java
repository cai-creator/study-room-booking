package com.studyroom.booking.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 自习室使用详情 VO
 *
 * @author 郭学威
 */
@Data
@Schema(description = "自习室使用详情")
public class RoomDetailVO implements Serializable {

    @Schema(description = "自习室ID")
    private Long roomId;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "自习室类型")
    private String roomType;

    @Schema(description = "楼层名称")
    private String floorName;

    @Schema(description = "楼栋名称")
    private String buildingName;

    @Schema(description = "校区名称")
    private String campusName;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "关闭时间")
    private String closeTime;

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

    @Schema(description = "座位实时状态列表")
    private List<SeatStatusItem> seats;

    /**
     * 单个座位实时状态
     */
    @Data
    @Schema(description = "座位状态项")
    public static class SeatStatusItem implements Serializable {

        @Schema(description = "座位ID")
        private Long seatId;

        @Schema(description = "座位编号", example = "A-01")
        private String seatCode;

        @Schema(description = "行号")
        private Integer rowNumber;

        @Schema(description = "列号")
        private Integer colNumber;

        @Schema(description = "实时状态: AVAILABLE-空闲, RESERVED-已预约, OCCUPIED-已占用, TEMPORARY_LEAVE-暂离, UNAVAILABLE-不可用")
        private String status;

        @Schema(description = "标签列表")
        private List<String> tags;
    }
}

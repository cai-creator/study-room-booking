package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 自习室座位实时状态 VO
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "自习室座位实时状态")
public class RoomSeatStatusVO {

    @Schema(description = "自习室ID")
    private Long roomId;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "总座位数")
    private Integer totalSeats;

    @Schema(description = "可用座位数")
    private Integer availableSeats;

    @Schema(description = "已预约座位数")
    private Integer reservedSeats;

    @Schema(description = "已占用座位数")
    private Integer occupiedSeats;

    @Schema(description = "座位列表")
    private List<SeatStatusItem> seats;

    /**
     * 单个座位状态
     */
    @Data
    @Schema(description = "座位状态项")
    public static class SeatStatusItem {

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

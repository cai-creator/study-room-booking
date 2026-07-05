package com.studyroom.booking.modules.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 签到/签退/暂离/返回 响应
 *
 * @author 邓祺然
 */
@Data
@Schema(description = "签到操作响应")
public class CheckinVO {

    @Schema(description = "预约ID")
    private Long reservationId;

    @Schema(description = "座位编号")
    private String seatCode;

    @Schema(description = "自习室ID")
    private Long roomId;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "签到时间")
    private String checkinTime;

    @Schema(description = "签退时间")
    private String checkoutTime;

    @Schema(description = "暂离开始时间")
    private String temporaryLeaveTime;

    @Schema(description = "预约开始时间")
    private String startTime;

    @Schema(description = "预约结束时间")
    private String endTime;

    @Schema(description = "预约状态: CHECKED_IN-已签到, TEMPORARY_LEAVE-暂离, COMPLETED-已完成")
    private String status;
}

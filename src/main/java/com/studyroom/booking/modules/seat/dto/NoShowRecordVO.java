package com.studyroom.booking.modules.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 爽约记录响应
 *
 * @author 邓祺然
 */
@Data
@Schema(description = "爽约记录响应")
public class NoShowRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名/学号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "预约ID")
    private Long reservationId;

    @Schema(description = "座位编号")
    private String seatCode;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "预约开始时间")
    private String startTime;

    @Schema(description = "预约结束时间")
    private String endTime;

    @Schema(description = "爽约原因: NO_CHECKIN-未签到, TEMPORARY_LEAVE_TIMEOUT-暂离超时")
    private String reason;

    @Schema(description = "记录日期")
    private String recordDate;

    @Schema(description = "创建时间")
    private String createdAt;
}

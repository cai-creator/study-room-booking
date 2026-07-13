package com.studyroom.booking.modules.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 预约响应VO
 * <p>
 * 字段设计对齐前端 Mock 数据结构（frontend/public/mock/reservation.js），
 * 确保前后端对接无需调整。
 *
 * @author 郭学威
 */
@Data
@Schema(description = "预约信息")
public class BookingVO implements Serializable {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "座位ID")
    private Long seatId;

    @Schema(description = "座位编号")
    private String seatCode;

    @Schema(description = "自习室ID")
    private Long roomId;

    @Schema(description = "自习室名称")
    private String roomName;

    @Schema(description = "楼栋名称")
    private String buildingName;

    @Schema(description = "校区名称")
    private String campusName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "状态: RESERVED/CHECKED_IN/CHECKED_OUT/CANCELLED/NO_SHOW")
    private String status;

    @Schema(description = "签到码")
    private String checkinCode;

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "预约分组ID，同一组多时段预约共享此ID")
    private String groupId;
}

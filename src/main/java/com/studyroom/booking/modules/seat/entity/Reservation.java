package com.studyroom.booking.modules.seat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约记录实体（座位管控模块内部使用）
 * <p>
 * 此实体映射 reservation 表，供座位管控模块（签到/签退/暂离）使用。
 * 预约核心模块（成员C）将有自己的完整实现。
 *
 * @author 邓祺然
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("seat_id")
    private Long seatId;

    @TableField("room_id")
    private Long roomId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 状态: RESERVED-已预约, CHECKED_IN-已签到, TEMPORARY_LEAVE-暂离,
     * COMPLETED-已完成, CANCELLED-已取消, NO_SHOW-爽约
     */
    @TableField("status")
    private String status;

    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    @TableField("checkout_time")
    private LocalDateTime checkoutTime;

    @TableField("temporary_leave_time")
    private LocalDateTime temporaryLeaveTime;

    /** 乐观锁版本号 */
    @TableField("version")
    private Integer version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

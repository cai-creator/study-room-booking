package com.studyroom.booking.modules.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体（预约核心模块）
 * <p>
 * 映射 reservation 表。注意：类名使用 Booking 而非 Reservation，
 * 以避免与座位管控模块（成员D）的 Reservation 实体产生 MyBatis 别名冲突。
 *
 * @author 郭学威
 */
@Data
@TableName("reservation")
public class Booking implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约用户ID */
    @TableField("user_id")
    private Long userId;

    /** 座位ID */
    @TableField("seat_id")
    private Long seatId;

    /** 自习室ID（冗余字段，方便查询） */
    @TableField("room_id")
    private Long roomId;

    /** 预约开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;

    /** 预约结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 状态枚举：
     * RESERVED - 已预约
     * CHECKED_IN - 已签到
     * TEMPORARY_LEAVE - 暂离
     * COMPLETED - 已完成
     * CANCELLED - 已取消
     * NO_SHOW - 爽约
     */
    @TableField("status")
    private String status;

    /** 签到时间 */
    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    /** 签退时间 */
    @TableField("checkout_time")
    private LocalDateTime checkoutTime;

    /** 暂离时间 */
    @TableField("temporary_leave_time")
    private LocalDateTime temporaryLeaveTime;

    /** 预约分组ID，同一组多时段预约共享此ID */
    @TableField("group_id")
    private String groupId;

    /** 乐观锁版本号 */
    @Version
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

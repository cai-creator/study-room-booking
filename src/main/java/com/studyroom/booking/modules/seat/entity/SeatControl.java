package com.studyroom.booking.modules.seat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 座位实体（座位管控模块内部使用）
 * <p>
 * 映射 seat 表，供座位管控模块验证座位信息。
 *
 * @author 邓祺然
 */
@Data
@TableName("seat")
public class SeatControl implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_code")
    private String seatCode;

    @TableField("row_number")
    private Integer rowNumber;

    @TableField("col_number")
    private Integer colNumber;

    @TableField("tags")
    private String tags;

    /** 状态: 0-不可用, 1-可用 */
    @TableField("status")
    private Integer status;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

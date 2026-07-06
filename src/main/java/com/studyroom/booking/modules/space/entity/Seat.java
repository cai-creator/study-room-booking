package com.studyroom.booking.modules.space.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 座位实体
 *
 * @author 陈梦涵
 */
@Data
@TableName("seat")
public class Seat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_code")
    private String seatCode;

    @TableField("`row_number`")
    private Integer rowNumber;

    @TableField("`col_number`")
    private Integer colNumber;

    /** 标签（逗号分隔）: WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍 */
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

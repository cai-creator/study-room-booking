package com.studyroom.booking.modules.seat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 黑名单实体
 *
 * @author 邓祺然
 */
@Data
@TableName("blacklist")
public class Blacklist implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("reason")
    private String reason;

    @TableField("no_show_count")
    private Integer noShowCount;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    /** 状态: 0-已解除, 1-生效中 */
    @TableField("status")
    private Integer status;

    @TableField("operator_id")
    private Long operatorId;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

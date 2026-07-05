package com.studyroom.booking.modules.seat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 爽约记录实体
 *
 * @author 邓祺然
 */
@Data
@TableName("no_show_record")
public class NoShowRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("reservation_id")
    private Long reservationId;

    /** 爽约原因: NO_CHECKIN-未签到, TEMPORARY_LEAVE_TIMEOUT-暂离超时 */
    @TableField("reason")
    private String reason;

    @TableField("record_date")
    private LocalDate recordDate;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

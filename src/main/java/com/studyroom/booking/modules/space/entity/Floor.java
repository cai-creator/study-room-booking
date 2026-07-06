package com.studyroom.booking.modules.space.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 楼层实体
 *
 * @author 陈梦涵
 */
@Data
@TableName("floor")
public class Floor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("building_id")
    private Long buildingId;

    @TableField("floor_number")
    private Integer floorNumber;

    @TableField("name")
    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态: 0-停用, 1-启用 */
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

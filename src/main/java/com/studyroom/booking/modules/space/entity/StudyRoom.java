package com.studyroom.booking.modules.space.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室实体
 *
 * @author 陈梦涵
 */
@Data
@TableName("study_room")
public class StudyRoom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("floor_id")
    private Long floorId;

    @TableField("name")
    private String name;

    /** 类型: LIBRARY-图书馆, TEACHING-教学楼, READING-阅览室 */
    @TableField("room_type")
    private String roomType;

    @TableField("total_seats")
    private Integer totalSeats;

    @TableField("rows_count")
    private Integer rowsCount;

    @TableField("cols_count")
    private Integer colsCount;

    @TableField("open_time")
    private LocalTime openTime;

    @TableField("close_time")
    private LocalTime closeTime;

    /** 状态: 0-关闭, 1-开放, 2-维护中 */
    @TableField("status")
    private Integer status;

    @TableField("description")
    private String description;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

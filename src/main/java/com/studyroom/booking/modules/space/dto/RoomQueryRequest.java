package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自习室分页查询请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "自习室查询参数")
public class RoomQueryRequest {

    @Schema(description = "校区ID")
    private Long campusId;

    @Schema(description = "楼栋ID")
    private Long buildingId;

    @Schema(description = "楼层ID")
    private Long floorId;

    @Schema(description = "房间类型: LIBRARY-图书馆, TEACHING-教学楼, READING-阅览室")
    private String roomType;

    @Schema(description = "状态: 0-关闭, 1-开放, 2-维护中")
    private Integer status;

    @Schema(description = "仅显示有空位的自习室")
    private Boolean hasAvailableSeats;

    @Schema(description = "名称关键词")
    private String keyword;

    @Schema(description = "页码，默认1", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认20，最大100", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方向: asc/desc")
    private String sortOrder;
}

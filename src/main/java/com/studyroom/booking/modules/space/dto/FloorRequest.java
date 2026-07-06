package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 楼层创建/更新请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "楼层请求参数")
public class FloorRequest {

    @NotNull(message = "所属楼栋ID不能为空")
    @Schema(description = "所属楼栋ID", example = "1")
    private Long buildingId;

    @NotNull(message = "楼层号不能为空")
    @Schema(description = "楼层号", example = "3")
    private Integer floorNumber;

    @Schema(description = "楼层名称", example = "三楼")
    private String name;

    @Schema(description = "排序号", example = "3")
    private Integer sortOrder;

    @Schema(description = "状态: 0-停用, 1-启用", example = "1")
    private Integer status;
}

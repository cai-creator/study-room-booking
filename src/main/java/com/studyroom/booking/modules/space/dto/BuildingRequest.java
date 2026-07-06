package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 楼栋创建/更新请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "楼栋请求参数")
public class BuildingRequest {

    @NotNull(message = "所属校区ID不能为空")
    @Schema(description = "所属校区ID", example = "1")
    private Long campusId;

    @NotBlank(message = "楼栋名称不能为空")
    @Schema(description = "楼栋名称", example = "图书馆")
    private String name;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态: 0-停用, 1-启用", example = "1")
    private Integer status;
}

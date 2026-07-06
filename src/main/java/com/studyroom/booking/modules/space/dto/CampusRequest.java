package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 校区创建/更新请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "校区请求参数")
public class CampusRequest {

    @NotBlank(message = "校区名称不能为空")
    @Schema(description = "校区名称", example = "主校区")
    private String name;

    @Schema(description = "地址", example = "XX路1号")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态: 0-停用, 1-启用", example = "1")
    private Integer status;
}

package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 座位更新请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "座位更新请求参数")
public class SeatUpdateRequest {

    @Schema(description = "座位编号", example = "A-01")
    private String seatCode;

    @Min(value = 1, message = "行号至少为1")
    @Max(value = 50, message = "行号不能超过50")
    @Schema(description = "行号")
    private Integer rowNumber;

    @Min(value = 1, message = "列号至少为1")
    @Max(value = 50, message = "列号不能超过50")
    @Schema(description = "列号")
    private Integer colNumber;

    @Schema(description = "标签（逗号分隔）: WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍")
    private String tags;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    @Schema(description = "状态: 0-不可用, 1-可用", example = "1")
    private Integer status;
}

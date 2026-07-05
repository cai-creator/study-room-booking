package com.studyroom.booking.modules.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 签退/暂离/返回座位通用请求
 *
 * @author 邓祺然
 */
@Data
@Schema(description = "座位操作请求（签退/暂离/返回）")
public class SeatActionRequest {

    @NotBlank(message = "座位编号不能为空")
    @Schema(description = "座位编号", example = "A-01")
    private String seatCode;

    @NotNull(message = "自习室ID不能为空")
    @Schema(description = "自习室ID", example = "1")
    private Long roomId;
}

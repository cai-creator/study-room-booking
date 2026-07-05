package com.studyroom.booking.modules.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动加入黑名单请求
 *
 * @author 邓祺然
 */
@Data
@Schema(description = "黑名单操作请求")
public class BlacklistRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "加入原因不能为空")
    @Schema(description = "加入原因", example = "7天内累计爽约3次")
    private String reason;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "黑名单结束时间", example = "2026-07-10 00:00:00")
    private String endTime;
}

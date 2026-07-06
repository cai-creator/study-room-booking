package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新座位标签请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "批量更新座位标签请求")
public class SeatTagsUpdateRequest {

    @NotEmpty(message = "座位ID列表不能为空")
    @Schema(description = "要更新的座位ID列表")
    private List<Long> seatIds;

    @Schema(description = "标签列表: WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍")
    private List<String> tags;
}

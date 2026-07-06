package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量生成座位请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "批量生成座位请求")
public class SeatGenerateRequest {

    @Min(value = 1, message = "行数至少为1")
    @Max(value = 50, message = "行数不能超过50")
    @NotNull(message = "行数不能为空")
    @Schema(description = "座位行数", example = "6")
    private Integer rows;

    @Min(value = 1, message = "列数至少为1")
    @Max(value = 50, message = "列数不能超过50")
    @NotNull(message = "列数不能为空")
    @Schema(description = "座位列数", example = "10")
    private Integer cols;

    @Schema(description = "留空位置列表（不放座位的格子）")
    private List<GridPosition> emptyPositions = new ArrayList<>();

    @Schema(description = "特殊标签位置列表（靠窗/电源/无障碍等）")
    private List<SpecialPosition> specialPositions = new ArrayList<>();

    /**
     * 网格位置
     */
    @Data
    @Schema(description = "网格位置")
    public static class GridPosition {
        @Schema(description = "行号（从1开始）", example = "3")
        private Integer row;
        @Schema(description = "列号（从1开始）", example = "5")
        private Integer col;
    }

    /**
     * 带标签的特殊位置
     */
    @Data
    @Schema(description = "特殊标签位置")
    public static class SpecialPosition {
        @Schema(description = "行号（从1开始）", example = "1")
        private Integer row;
        @Schema(description = "列号（从1开始）", example = "1")
        private Integer col;
        @Schema(description = "标签列表: WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍")
        private List<String> tags = new ArrayList<>();
    }
}

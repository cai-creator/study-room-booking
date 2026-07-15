package com.studyroom.booking.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计信息")
public class UserStatsVO {

    @Schema(description = "累计预约次数")
    private Integer totalBookings;

    @Schema(description = "本月爽约次数")
    private Integer thisMonthNoShow;

    @Schema(description = "信用评分等级: EXCELLENT(优秀), GOOD(良好), FAIR(一般), POOR(较差)")
    private String creditLevel;
}
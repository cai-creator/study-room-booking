package com.studyroom.booking.modules.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 黑名单响应
 *
 * @author 邓祺然
 */
@Data
@Schema(description = "黑名单响应")
public class BlacklistVO {

    @Schema(description = "黑名单ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名/学号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "加入原因")
    private String reason;

    @Schema(description = "累计爽约次数")
    private Integer noShowCount;

    @Schema(description = "黑名单开始时间")
    private String startTime;

    @Schema(description = "黑名单结束时间")
    private String endTime;

    @Schema(description = "状态: 0-已解除, 1-生效中")
    private Integer status;

    @Schema(description = "创建时间")
    private String createdAt;
}

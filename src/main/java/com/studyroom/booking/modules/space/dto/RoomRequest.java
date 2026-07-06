package com.studyroom.booking.modules.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

/**
 * 自习室创建/更新请求
 *
 * @author 陈梦涵
 */
@Data
@Schema(description = "自习室请求参数")
public class RoomRequest {

    @NotNull(message = "所属楼层ID不能为空")
    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @NotBlank(message = "自习室名称不能为空")
    @Schema(description = "自习室名称", example = "图书馆101自习室")
    private String name;

    @Schema(description = "类型: LIBRARY-图书馆, TEACHING-教学楼, READING-阅览室", example = "LIBRARY")
    private String roomType;

    @Schema(description = "开放时间", example = "08:00:00")
    private LocalTime openTime;

    @Schema(description = "关闭时间", example = "22:00:00")
    private LocalTime closeTime;

    @Schema(description = "状态: 0-关闭, 1-开放, 2-维护中", example = "1")
    private Integer status;

    @Schema(description = "描述", example = "安静自习区，禁止讨论")
    private String description;
}

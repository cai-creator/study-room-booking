package com.studyroom.booking.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知偏好设置")
public class NotificationPreferenceVO {

    @Schema(description = "预约提醒")
    private Boolean bookingReminder;

    @Schema(description = "签到提醒")
    private Boolean checkinReminder;

    @Schema(description = "系统通知")
    private Boolean systemNotice;

    @Schema(description = "黑名单预警")
    private Boolean blacklistAlert;

    @Schema(description = "默认校区ID")
    private Long campusId;

    @Schema(description = "默认校区名称")
    private String campusName;

    @Schema(description = "默认自习室类型")
    private String roomType;
}
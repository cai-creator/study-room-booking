package com.studyroom.booking.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求的资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    USER_DISABLED(1004, "账号已被禁用"),

    ROOM_NOT_FOUND(2001, "自习室不存在"),
    ROOM_CLOSED(2002, "自习室已关闭"),
    SEAT_NOT_FOUND(2003, "座位不存在"),
    SEAT_NOT_AVAILABLE(2004, "座位不可用"),

    RESERVATION_NOT_FOUND(3001, "预约记录不存在"),
    RESERVATION_TIME_INVALID(3002, "预约时间无效"),
    RESERVATION_DAILY_LIMIT_EXCEEDED(3003, "今日预约次数已达上限"),
    RESERVATION_DURATION_EXCEEDED(3004, "预约时长超过最大限制"),
    RESERVATION_TOO_EARLY(3005, "预约时间过早"),
    RESERVATION_TOO_LATE(3006, "预约时间过晚"),
    RESERVATION_CONFLICT(3007, "该时段座位已被预约"),
    RESERVATION_CANCEL_NOT_ALLOWED(3008, "当前状态不允许取消预约"),
    RESERVATION_USER_CONFLICT(3009, "该时段您已有其他预约"),

    CHECKIN_NOT_ALLOWED(4001, "无法签到"),
    CHECKIN_ALREADY_DONE(4002, "已签到"),
    CHECKIN_TOO_EARLY(4003, "签到时间过早"),
    CHECKIN_TIMEOUT(4004, "已超过签到时间"),
    CHECKOUT_NOT_ALLOWED(4005, "无法签退"),
    TEMPORARY_LEAVE_NOT_ALLOWED(4006, "当前状态不允许暂离，仅已签到状态可暂离"),
    RETURN_SEAT_NOT_ALLOWED(4007, "当前状态不允许返回，仅暂离状态可返回座位"),
    TEMPORARY_LEAVE_TIMEOUT(4008, "暂离时间已超时，请重新预约"),

    BLACKLISTED(5001, "您已被加入黑名单，禁止预约"),
    BLACKLIST_NOT_FOUND(5002, "黑名单记录不存在"),
    BLACKLIST_ALREADY_EXISTS(5003, "该用户已在黑名单中，请勿重复添加"),

    CAMPUS_NOT_FOUND(6001, "校区不存在"),
    BUILDING_NOT_FOUND(6002, "楼栋不存在"),
    FLOOR_NOT_FOUND(6003, "楼层不存在"),

    CAMPUS_HAS_BUILDINGS(6101, "校区下存在楼栋，无法删除"),
    BUILDING_HAS_FLOORS(6102, "楼栋下存在楼层，无法删除"),
    FLOOR_HAS_ROOMS(6103, "楼层下存在自习室，无法删除"),
    ROOM_HAS_SEATS(6104, "自习室下存在座位，无法删除"),

    FILE_IMPORT_FAILED(7001, "文件导入失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

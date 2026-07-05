package com.studyroom.booking.modules.seat.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.seat.dto.CheckinRequest;
import com.studyroom.booking.modules.seat.dto.CheckinVO;
import com.studyroom.booking.modules.seat.dto.SeatActionRequest;
import com.studyroom.booking.modules.seat.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 签到/签退/暂离/返回 控制器
 *
 * @author 邓祺然
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "座位管控-签到签退", description = "签到、签退、暂离、返回座位操作接口")
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/checkin")
    @RequireRole({"STUDENT"})
    @Operation(summary = "签到", description = "学生在预约时段内到达自习室后签到")
    public Result<CheckinVO> checkin(@Valid @RequestBody CheckinRequest request) {
        CheckinVO result = checkinService.checkin(request.getSeatCode(), request.getRoomId());
        return Result.success("签到成功", result);
    }

    @PostMapping("/checkout")
    @RequireRole({"STUDENT"})
    @Operation(summary = "签退", description = "学生离开自习室时签退")
    public Result<CheckinVO> checkout(@Valid @RequestBody SeatActionRequest request) {
        CheckinVO result = checkinService.checkout(request.getSeatCode(), request.getRoomId());
        return Result.success("签退成功", result);
    }

    @PostMapping("/temporary-leave")
    @RequireRole({"STUDENT"})
    @Operation(summary = "暂离", description = "学生暂时离开座位（如去洗手间），座位将保留一段时间")
    public Result<CheckinVO> temporaryLeave(@Valid @RequestBody SeatActionRequest request) {
        CheckinVO result = checkinService.temporaryLeave(request.getSeatCode(), request.getRoomId());
        return Result.success("暂离成功，请在规定时间内返回", result);
    }

    @PostMapping("/return-seat")
    @RequireRole({"STUDENT"})
    @Operation(summary = "返回座位", description = "学生暂离后返回座位")
    public Result<CheckinVO> returnSeat(@Valid @RequestBody SeatActionRequest request) {
        CheckinVO result = checkinService.returnSeat(request.getSeatCode(), request.getRoomId());
        return Result.success("已返回座位", result);
    }
}

package com.studyroom.booking.modules.seat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.seat.dto.NoShowRecordVO;
import com.studyroom.booking.modules.seat.service.NoShowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 爽约记录管理控制器
 *
 * @author 邓祺然
 */
@RestController
@RequestMapping("/no-show-records")
@RequiredArgsConstructor
@Tag(name = "座位管控-爽约记录", description = "爽约记录的查询接口")
public class NoShowRecordController {

    private final NoShowRecordService noShowRecordService;

    @GetMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "爽约记录列表", description = "分页查询所有爽约记录")
    public Result<Page<NoShowRecordVO>> getNoShowRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {
        return Result.success(noShowRecordService.getNoShowRecordPage(pageNum, pageSize, userId, startDate, endDate));
    }

    @GetMapping("/my")
    @RequireRole({"STUDENT"})
    @Operation(summary = "我的爽约记录", description = "查看当前登录学生的爽约记录")
    public Result<Page<NoShowRecordVO>> getMyNoShowRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(noShowRecordService.getMyNoShowRecords(pageNum, pageSize));
    }
}

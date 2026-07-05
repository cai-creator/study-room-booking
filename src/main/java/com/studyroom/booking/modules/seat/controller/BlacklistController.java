package com.studyroom.booking.modules.seat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.seat.dto.BlacklistRequest;
import com.studyroom.booking.modules.seat.dto.BlacklistVO;
import com.studyroom.booking.modules.seat.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单管理控制器
 *
 * @author 邓祺然
 */
@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
@Tag(name = "座位管控-黑名单管理", description = "黑名单的查询、手动加入/移出操作接口")
public class BlacklistController {

    private final BlacklistService blacklistService;

    @GetMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "黑名单列表", description = "分页查询黑名单列表")
    public Result<Page<BlacklistVO>> getBlacklist(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词（原因搜索）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态: 0-已解除, 1-生效中") @RequestParam(required = false) Integer status) {
        return Result.success(blacklistService.getBlacklistPage(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "黑名单详情", description = "获取黑名单详情")
    public Result<BlacklistVO> getBlacklistById(
            @Parameter(description = "黑名单ID") @PathVariable Long id) {
        return Result.success(blacklistService.getBlacklistById(id));
    }

    @GetMapping("/my")
    @RequireRole({"STUDENT"})
    @Operation(summary = "我的黑名单状态", description = "查看当前登录用户的黑名单状态")
    public Result<BlacklistVO> getMyBlacklist() {
        BlacklistVO result = blacklistService.getMyBlacklistStatus();
        if (result == null) {
            return Result.success("您当前不在黑名单中", null);
        }
        return Result.success(result);
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "加入黑名单", description = "手动将用户加入黑名单")
    public Result<BlacklistVO> addToBlacklist(@Valid @RequestBody BlacklistRequest request) {
        BlacklistVO result = blacklistService.addToBlacklist(
                request.getUserId(), request.getReason(), request.getEndTime());
        return Result.success("已加入黑名单", result);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "移出黑名单", description = "手动将用户移出黑名单（解除黑名单）")
    public Result<Void> removeFromBlacklist(
            @Parameter(description = "黑名单ID") @PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return Result.success("已移出黑名单", null);
    }
}

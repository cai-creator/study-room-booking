package com.studyroom.booking.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import com.studyroom.booking.modules.user.dto.CreateUserRequest;
import com.studyroom.booking.modules.user.dto.UpdateUserRequest;
import com.studyroom.booking.modules.user.dto.ChangePasswordRequest;
import com.studyroom.booking.modules.user.dto.UserVO;
import com.studyroom.booking.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD管理接口")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "用户列表", description = "分页查询用户列表。普通管理员只能看学生和自己；超级管理员可看所有管理员和学生")
    @SecurityRequirement(name = "BearerAuth")
    public Result<Page<UserVO>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        return Result.success(userService.getUserList(pageNum, pageSize, keyword, role, status, currentUserId, currentRole));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情", description = "获取用户详情（管理员或本人可查看）")
    @SecurityRequirement(name = "BearerAuth")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        return Result.success(userService.getUserById(id, currentUserId, currentRole));
    }

    @PostMapping
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "新增用户", description = "管理员可新增学生，超级管理员可新增任意角色用户")
    @SecurityRequirement(name = "BearerAuth")
    public Result<UserVO> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        return Result.success("创建成功", userService.createUser(request, currentRole));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息（管理员或本人可更新）")
    @SecurityRequirement(name = "BearerAuth")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        return Result.success("更新成功", userService.updateUser(id, request, currentUserId, currentRole));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除用户", description = "管理员可删除学生，超级管理员可删除任意用户（逻辑删除）")
    @SecurityRequirement(name = "BearerAuth")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        userService.deleteUser(id, currentUserId, currentRole);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "修改密码", description = "修改用户密码（本人需验证旧密码，管理员可直接重置学生密码，超级管理员可重置任意用户密码）")
    @SecurityRequirement(name = "BearerAuth")
    public Result<Void> changePassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        userService.changePassword(id, request, currentUserId, currentRole);
        return Result.success("密码修改成功", null);
    }

    @PatchMapping("/{id}/status")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "修改状态", description = "管理员可启用/禁用学生，超级管理员可修改任意用户状态")
    @SecurityRequirement(name = "BearerAuth")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(hidden = true) @RequestAttribute("role") String currentRole) {
        userService.updateStatus(id, status, currentUserId, currentRole);
        return Result.success("状态修改成功", null);
    }
}
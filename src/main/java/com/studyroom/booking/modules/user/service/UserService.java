package com.studyroom.booking.modules.user.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.user.dto.CreateUserRequest;
import com.studyroom.booking.modules.user.dto.LoginRequest;
import com.studyroom.booking.modules.user.dto.LoginVO;
import com.studyroom.booking.modules.user.dto.RegisterRequest;
import com.studyroom.booking.modules.user.dto.UpdateUserRequest;
import com.studyroom.booking.modules.user.dto.ChangePasswordRequest;
import com.studyroom.booking.modules.user.dto.UserVO;
import com.studyroom.booking.modules.user.entity.User;
import com.studyroom.booking.modules.user.mapper.UserMapper;
import com.studyroom.booking.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    public LoginVO login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Long expireAt = System.currentTimeMillis() + jwtUtils.getExpireTime();

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpireAt(expireAt);
        loginVO.setUser(convertToVO(user));

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return loginVO;
    }

    /**
     * 用户注册（公开注册，默认STUDENT角色）
     */
    @Transactional
    public UserVO register(RegisterRequest request) {
        User existingUser = userMapper.selectByUsernameIncludeDeleted(request.getUsername());

        if (existingUser != null && existingUser.getDeleted() == 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user;
        if (existingUser != null) {
            userMapper.updateIncludeDeleted(
                    existingUser.getId(),
                    BCrypt.hashpw(request.getPassword()),
                    request.getRealName(),
                    request.getEmail(),
                    request.getPhone(),
                    "STUDENT",
                    1,
                    0,
                    LocalDateTime.now()
            );
            user = existingUser;
            user.setPassword(BCrypt.hashpw(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole("STUDENT");
            user.setStatus(1);
            user.setDeleted(0);
            log.info("已注销用户重新注册: username={}, role=STUDENT", user.getUsername());
        } else {
            user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(BCrypt.hashpw(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole("STUDENT");
            user.setStatus(1);
            user.setDeleted(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册: username={}, role=STUDENT", user.getUsername());
        }

        return convertToVO(user);
    }

    /**
     * 创建用户（超级管理员专用，可指定角色）
     * ADMIN 只能创建 STUDENT 角色用户，SUPER_ADMIN 可创建任意角色
     */
    @Transactional
    public UserVO createUser(CreateUserRequest request, String currentRole) {
        User existingUser = userMapper.selectByUsernameIncludeDeleted(request.getUsername());

        if (existingUser != null && existingUser.getDeleted() == 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        String targetRole = request.getRole() != null ? request.getRole() : "STUDENT";
        if ("ADMIN".equals(currentRole) && !"STUDENT".equals(targetRole)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        User user;
        if (existingUser != null) {
            userMapper.updateIncludeDeleted(
                    existingUser.getId(),
                    BCrypt.hashpw(request.getPassword()),
                    request.getRealName(),
                    request.getEmail(),
                    request.getPhone(),
                    targetRole,
                    1,
                    0,
                    LocalDateTime.now()
            );
            user = existingUser;
            user.setPassword(BCrypt.hashpw(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(targetRole);
            user.setStatus(1);
            user.setDeleted(0);
            log.info("已注销用户重新创建: username={}, role={}", user.getUsername(), targetRole);
        } else {
            user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(BCrypt.hashpw(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(targetRole);
            user.setStatus(1);
            user.setDeleted(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }

        return convertToVO(user);
    }

    /**
     * 获取当前用户信息
     */
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 分页查询用户列表
     * <p>
     * 普通管理员(ADMIN)：只能看所有学生和自己
     * 超级管理员(SUPER_ADMIN)：可看所有普通管理员和学生
     */
    public Page<UserVO> getUserList(Integer pageNum, Integer pageSize, String keyword, String role, Integer status, Long currentUserId, String currentRole) {
        pageSize = Math.min(pageSize, 100);
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        // 根据当前角色过滤可见用户范围
        if ("ADMIN".equals(currentRole)) {
            // 普通管理员：只能看学生 + 自己
            wrapper.and(w -> w.eq(User::getRole, "STUDENT").or().eq(User::getId, currentUserId));
        } else if ("SUPER_ADMIN".equals(currentRole)) {
            // 超级管理员：可看所有普通管理员、学生 + 自己
            wrapper.and(w -> w.in(User::getRole, "ADMIN", "STUDENT").or().eq(User::getId, currentUserId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
            );
        }

        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 获取用户详情
     *
     * 权限规则：
     * - SUPER_ADMIN：可查看所有用户
     * - ADMIN：可查看 STUDENT 和本人，不可查看其他 ADMIN 和 SUPER_ADMIN
     * - STUDENT：仅可查看本人
     */
    public UserVO getUserById(Long id, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 本人可查看自己的详情
        if (currentUserId.equals(id)) {
            return convertToVO(user);
        }

        // SUPER_ADMIN 可查看所有人
        if ("SUPER_ADMIN".equals(currentRole)) {
            return convertToVO(user);
        }

        // ADMIN 不能查看 SUPER_ADMIN
        if ("ADMIN".equals(currentRole) && "SUPER_ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // ADMIN 不能查看其他 ADMIN
        if ("ADMIN".equals(currentRole) && "ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // ADMIN 可查看 STUDENT
        if ("ADMIN".equals(currentRole)) {
            return convertToVO(user);
        }

        // STUDENT 只能查看自己（已在上面通过 currentUserId.equals(id) 处理）
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserVO updateUser(Long id, UpdateUserRequest request, Long operatorId, String operatorRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!"SUPER_ADMIN".equals(operatorRole) && !"ADMIN".equals(operatorRole) && !operatorId.equals(id)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if ("ADMIN".equals(operatorRole) && "ADMIN".equals(user.getRole()) && !operatorId.equals(id)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if ("ADMIN".equals(operatorRole) && "SUPER_ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if ("SUPER_ADMIN".equals(operatorRole)) {
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
        } else if ("ADMIN".equals(operatorRole)) {
            if (request.getStatus() != null && !"ADMIN".equals(user.getRole()) && !"SUPER_ADMIN".equals(user.getRole())) {
                user.setStatus(request.getStatus());
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return convertToVO(user);
    }

    /**
     * 修改密码
     * SUPER_ADMIN 可直接重置任意用户密码
     * ADMIN 可重置 STUDENT 的密码，但不能重置 ADMIN/SUPER_ADMIN 的密码
     * 本人修改自己密码需要验证旧密码
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if ("SUPER_ADMIN".equals(currentRole)) {
            // 超级管理员直接重置任意用户密码
            user.setPassword(BCrypt.hashpw(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        } else if ("ADMIN".equals(currentRole)) {
            // 管理员可重置学生密码，不能重置管理员/超级管理员密码
            if ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            user.setPassword(BCrypt.hashpw(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        } else if (currentUserId.equals(id)) {
            // 本人修改自己密码需要验证旧密码
            if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
            }
            user.setPassword(BCrypt.hashpw(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 删除用户（逻辑删除）
     * ADMIN 只能删除 STUDENT，SUPER_ADMIN 可删除任意用户
     */
    @Transactional
    public void deleteUser(Long id, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 不能删除自己
        if (currentUserId.equals(id)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // ADMIN 只能删除 STUDENT
        if ("ADMIN".equals(currentRole)) {
            if ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        // 使用 MyBatis-Plus deleteById 触发 @TableLogic 逻辑删除
        userMapper.deleteById(id);
        log.info("用户已删除: targetUserId={}, targetUsername={}, operatorId={}, operatorRole={}", id, user.getUsername(), currentUserId, currentRole);
    }

    /**
     * 修改用户状态
     * ADMIN 只能修改 STUDENT 状态，SUPER_ADMIN 可修改任意用户状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // ADMIN 只能修改 STUDENT 的状态
        if ("ADMIN".equals(currentRole)) {
            if ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return vo;
    }
}
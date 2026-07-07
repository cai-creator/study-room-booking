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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        return loginVO;
    }

    /**
     * 用户注册（公开注册，默认STUDENT角色）
     */
    @Transactional
    public UserVO register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (existingUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
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

        return convertToVO(user);
    }

    /**
     * 创建用户（超级管理员专用，可指定角色）
     */
    @Transactional
    public UserVO createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (existingUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "STUDENT");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

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
     */
    public Page<UserVO> getUserList(Integer pageNum, Integer pageSize, String keyword, String role, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

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
     */
    public UserVO getUserById(Long id, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!"SUPER_ADMIN".equals(currentRole) && !"ADMIN".equals(currentRole) && !currentUserId.equals(id)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return convertToVO(user);
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
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, Long currentUserId, String currentRole) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if ("SUPER_ADMIN".equals(currentRole)) {
            user.setPassword(BCrypt.hashpw(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        } else if (currentUserId.equals(id)) {
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
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setDeleted(1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 修改用户状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
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
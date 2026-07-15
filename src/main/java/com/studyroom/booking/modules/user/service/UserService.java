package com.studyroom.booking.modules.user.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.reservation.entity.Booking;
import com.studyroom.booking.modules.reservation.mapper.BookingMapper;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import com.studyroom.booking.modules.user.dto.*;
import com.studyroom.booking.modules.user.entity.RefreshToken;
import com.studyroom.booking.modules.user.entity.User;
import com.studyroom.booking.modules.user.mapper.UserMapper;
import com.studyroom.booking.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final BookingMapper bookingMapper;
    private final NoShowRecordMapper noShowRecordMapper;

    /**
     * 用户登录
     */
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查账户是否被锁定
        if (loginAttemptService.isLocked(user.getId())) {
            long remaining = loginAttemptService.getRemainingLockMinutes(user.getId());
            throw new BusinessException(ResultCode.USER_LOCKED.getCode(),
                    ResultCode.USER_LOCKED.getMessage() + "（剩余" + remaining + "分钟）");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            int failCount = loginAttemptService.recordFailure(user.getId());
            int maxFail = loginAttemptService.getMaxFailCount();
            int remaining = maxFail - failCount;
            if (remaining > 0) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(),
                        "密码错误，还可尝试" + remaining + "次");
            } else {
                throw new BusinessException(ResultCode.USER_LOCKED.getCode(),
                        "连续失败" + maxFail + "次，账户已锁定" + loginAttemptService.getLockMinutes() + "分钟");
            }
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 登录成功，重置失败计数
        loginAttemptService.recordSuccess(user.getId());

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Long expireAt = System.currentTimeMillis() + jwtUtils.getExpireTime();

        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpireAt(expireAt);
        if (refreshToken != null) {
            loginVO.setRefreshToken(refreshToken);
            loginVO.setRefreshExpireAt(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
        }
        loginVO.setUser(convertToVO(user));

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return loginVO;
    }

    /**
     * 刷新令牌
     */
    public LoginVO refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.validateAndConsume(refreshTokenStr);
        if (refreshToken == null) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        User user = userMapper.selectById(refreshToken.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成新的access token
        String newToken = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Long expireAt = System.currentTimeMillis() + jwtUtils.getExpireTime();

        // 生成新的refresh token（轮换机制）
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newToken);
        loginVO.setExpireAt(expireAt);
        if (newRefreshToken != null) {
            loginVO.setRefreshToken(newRefreshToken);
            loginVO.setRefreshExpireAt(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
        }
        loginVO.setUser(convertToVO(user));

        log.info("刷新令牌成功: username={}", user.getUsername());
        return loginVO;
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String authHeader) {
        if (userId != null) {
            // 使所有refresh token失效
            refreshTokenService.invalidateAllTokens(userId);
        }
        // 将access token加入黑名单
        if (authHeader != null) {
            String token = authHeader;
            while (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = jwtUtils.parseToken(token);
            if (claims != null && claims.getExpiration() != null) {
                tokenBlacklistService.blacklist(token, claims.getExpiration().getTime());
            }
        }
        log.info("用户登出: userId={}", userId);
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
     * 初始密码规则：姓名首字母大写 + 账号后4位（账号必须为纯数字）
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

        // 密码：如果管理员手动输入则使用输入的，否则自动生成
        String initialPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : generateInitialPassword(request.getRealName(), request.getUsername());

        User user;
        if (existingUser != null) {
            userMapper.updateIncludeDeleted(
                    existingUser.getId(),
                    BCrypt.hashpw(initialPassword),
                    request.getRealName(),
                    request.getEmail(),
                    request.getPhone(),
                    targetRole,
                    1,
                    0,
                    LocalDateTime.now()
            );
            user = existingUser;
            user.setPassword(BCrypt.hashpw(initialPassword));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(targetRole);
            user.setStatus(1);
            user.setDeleted(0);
            log.info("已注销用户重新创建: username={}, role={}, initialPassword={}", user.getUsername(), targetRole, initialPassword);
        } else {
            user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(BCrypt.hashpw(initialPassword));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRole(targetRole);
            user.setStatus(1);
            user.setDeleted(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("创建用户: username={}, role={}, initialPassword={}", user.getUsername(), targetRole, initialPassword);
        }

        UserVO vo = convertToVO(user);
        vo.setInitialPassword(initialPassword);
        return vo;
    }

    /**
     * 生成初始密码：姓名拼音首字母大写 + 账号后4位
     * 例：蔡佳成 20243155 -> Cjc3155
     * 例：John 20243155 -> J3155
     */
    private String generateInitialPassword(String realName, String username) {
        if (realName == null || realName.isEmpty() || username == null || username.length() < 4) {
            return "Init1234";
        }
        String initials = getNameInitials(realName);
        String last4 = username.substring(username.length() - 4);
        return initials + last4;
    }

    /**
     * 提取姓名的拼音首字母（第一个大写，其余小写）
     * 例：蔡佳成 -> Cjc，John -> J
     */
    private String getNameInitials(String realName) {
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < realName.length(); i++) {
            char c = realName.charAt(i);
            String initial = null;
            if (isChinese(c)) {
                String[] pinyinArray = net.sourceforge.pinyin4j.PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    initial = pinyinArray[0].substring(0, 1);
                }
            } else if (Character.isLetter(c)) {
                initial = String.valueOf(c);
            }
            if (initial != null) {
                if (initials.length() == 0) {
                    initials.append(initial.toUpperCase());
                } else {
                    initials.append(initial.toLowerCase());
                }
            }
        }
        return initials.length() > 0 ? initials.toString() : "U";
    }

    private boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fff';
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
        vo.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return vo;
    }

    /**
     * 获取用户统计信息
     */
    public UserStatsVO getUserStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        long totalBookings = bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getUserId, userId)
                        .eq(Booking::getDeleted, 0)
        );

        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long thisMonthNoShow = noShowRecordMapper.selectCount(
                new LambdaQueryWrapper<NoShowRecord>()
                        .eq(NoShowRecord::getUserId, userId)
                        .ge(NoShowRecord::getRecordDate, monthStart)
        );

        String creditLevel;
        if (thisMonthNoShow == 0) {
            creditLevel = "EXCELLENT";
        } else if (thisMonthNoShow == 1) {
            creditLevel = "GOOD";
        } else if (thisMonthNoShow == 2) {
            creditLevel = "FAIR";
        } else {
            creditLevel = "POOR";
        }

        return UserStatsVO.builder()
                .totalBookings((int) totalBookings)
                .thisMonthNoShow((int) thisMonthNoShow)
                .creditLevel(creditLevel)
                .build();
    }
}
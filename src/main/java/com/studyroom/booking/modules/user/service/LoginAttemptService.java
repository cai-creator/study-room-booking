package com.studyroom.booking.modules.user.service;

import com.studyroom.booking.modules.user.entity.LoginAttempt;
import com.studyroom.booking.modules.user.mapper.LoginAttemptMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 登录失败限制服务
 * <p>连续失败5次后锁定账户30分钟，登录成功后重置计数。
 */
@Slf4j
@Service
public class LoginAttemptService {

    @Resource
    private LoginAttemptMapper loginAttemptMapper;

    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 30;

    /**
     * 检查账户是否被锁定
     * @return true-已锁定，false-未锁定
     */
    public boolean isLocked(Long userId) {
        LoginAttempt attempt = loginAttemptMapper.selectByUserId(userId);
        if (attempt == null || attempt.getLockedUntil() == null) {
            return false;
        }
        // 锁定时间已过，自动解锁
        if (attempt.getLockedUntil().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    /**
     * 获取剩余锁定时间（分钟）
     */
    public long getRemainingLockMinutes(Long userId) {
        LoginAttempt attempt = loginAttemptMapper.selectByUserId(userId);
        if (attempt == null || attempt.getLockedUntil() == null) {
            return 0;
        }
        long minutes = java.time.Duration.between(LocalDateTime.now(), attempt.getLockedUntil()).toMinutes();
        return Math.max(0, minutes);
    }

    /**
     * 记录登录失败
     * @return 当前失败次数
     */
    @Transactional
    public int recordFailure(Long userId) {
        LoginAttempt attempt = loginAttemptMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        if (attempt == null) {
            attempt = new LoginAttempt();
            attempt.setUserId(userId);
            attempt.setFailCount(1);
            attempt.setLastAttemptTime(now);
            attempt.setCreatedAt(now);
            attempt.setUpdatedAt(now);
            loginAttemptMapper.insert(attempt);
        } else {
            attempt.setFailCount(attempt.getFailCount() + 1);
            attempt.setLastAttemptTime(now);

            // 达到最大失败次数，锁定账户
            if (attempt.getFailCount() >= MAX_FAIL_COUNT) {
                attempt.setLockedUntil(now.plusMinutes(LOCK_MINUTES));
                log.warn("用户 {} 连续登录失败 {} 次，账户已锁定 {} 分钟", userId, attempt.getFailCount(), LOCK_MINUTES);
            }

            attempt.setUpdatedAt(now);
            loginAttemptMapper.updateById(attempt);
        }

        return attempt.getFailCount();
    }

    /**
     * 登录成功后重置失败计数
     */
    @Transactional
    public void recordSuccess(Long userId) {
        LoginAttempt attempt = loginAttemptMapper.selectByUserId(userId);
        if (attempt != null && (attempt.getFailCount() > 0 || attempt.getLockedUntil() != null)) {
            loginAttemptMapper.resetFailCount(userId);
            log.info("用户 {} 登录成功，失败计数已重置", userId);
        }
    }

    public int getMaxFailCount() {
        return MAX_FAIL_COUNT;
    }

    public int getLockMinutes() {
        return LOCK_MINUTES;
    }
}

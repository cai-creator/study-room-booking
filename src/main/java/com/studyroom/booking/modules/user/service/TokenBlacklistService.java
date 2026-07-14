package com.studyroom.booking.modules.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token黑名单服务（内存实现）
 * <p>用于登出后即时失效access token，避免被重复使用。
 * <p>定时清理过期条目，防止内存泄漏。
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 将token加入黑名单
     * @param token JWT token
     * @param expireAt token的过期时间戳（毫秒）
     */
    public void blacklist(String token, Long expireAt) {
        if (token != null && !token.isEmpty()) {
            blacklist.put(token, expireAt);
            log.info("Token已加入黑名单，将在 {} 失效", new Date(expireAt));
        }
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long expireAt = blacklist.get(token);
        if (expireAt == null) {
            return false;
        }
        // 如果token已过期，从黑名单中移除
        if (expireAt < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * 定时清理过期的黑名单条目（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        int before = blacklist.size();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
        int removed = before - blacklist.size();
        if (removed > 0) {
            log.debug("清理过期黑名单条目: {} 条", removed);
        }
    }
}

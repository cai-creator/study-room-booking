package com.studyroom.booking.modules.user.service;

import com.studyroom.booking.modules.user.entity.RefreshToken;
import com.studyroom.booking.modules.user.mapper.RefreshTokenMapper;
import com.studyroom.booking.utils.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
public class RefreshTokenService {

    @Resource
    private RefreshTokenMapper refreshTokenMapper;

    @Resource
    private JwtUtils jwtUtils;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateRefreshToken(Long userId) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Transactional
    public String createRefreshToken(Long userId) {
        String token = generateRefreshToken(userId);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setUsed(false);

        try {
            refreshTokenMapper.insert(refreshToken);
            return token;
        } catch (org.springframework.jdbc.BadSqlGrammarException e) {
            log.warn("refresh_token表不存在，跳过刷新令牌存储。请执行数据库迁移脚本创建该表。");
            return null;
        }
    }

    @Transactional
    public void invalidateAllTokens(Long userId) {
        refreshTokenMapper.deleteByUserId(userId);
        log.info("用户 {} 的所有刷新令牌已失效", userId);
    }

    @Transactional
    public RefreshToken validateAndConsume(String token) {
        RefreshToken refreshToken = refreshTokenMapper.selectByToken(token);
        if (refreshToken == null) {
            return null;
        }

        if (Boolean.TRUE.equals(refreshToken.getUsed())) {
            return null;
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        refreshTokenMapper.markTokenUsed(token);
        return refreshToken;
    }

}
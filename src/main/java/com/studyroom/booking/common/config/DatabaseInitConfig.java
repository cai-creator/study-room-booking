package com.studyroom.booking.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitConfig {

    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        createRefreshTokenTableIfNotExists();
        createLoginAttemptsTableIfNotExists();
    }

    private void createRefreshTokenTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS refresh_token (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '令牌ID',
                    user_id BIGINT NOT NULL COMMENT '用户ID',
                    token VARCHAR(64) NOT NULL COMMENT '刷新令牌',
                    expires_at DATETIME NOT NULL COMMENT '过期时间',
                    used TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用: 0-未使用, 1-已使用',
                    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    UNIQUE KEY uk_token (token),
                    KEY idx_user_id (user_id),
                    KEY idx_expires_at (expires_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表'
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("refresh_token表初始化完成");
        } catch (SQLException e) {
            log.warn("创建refresh_token表失败: {}", e.getMessage());
        }
    }

    private void createLoginAttemptsTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS login_attempts (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
                    user_id BIGINT NOT NULL COMMENT '用户ID',
                    fail_count INT NOT NULL DEFAULT 0 COMMENT '连续失败次数',
                    locked_until DATETIME DEFAULT NULL COMMENT '锁定截止时间（NULL表示未锁定）',
                    last_attempt_time DATETIME DEFAULT NULL COMMENT '最后尝试时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    UNIQUE KEY uk_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录失败记录表'
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("login_attempts表初始化完成");
        } catch (SQLException e) {
            log.warn("创建login_attempts表失败: {}", e.getMessage());
        }
    }
}
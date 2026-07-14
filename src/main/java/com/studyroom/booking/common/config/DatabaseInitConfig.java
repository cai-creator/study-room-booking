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
}
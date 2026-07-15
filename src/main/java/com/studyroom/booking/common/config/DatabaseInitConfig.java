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
        createOperationLogTableIfNotExists();
        createNotificationTableIfNotExists();
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

    private void createOperationLogTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS operation_log (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
                    user_id BIGINT DEFAULT NULL COMMENT '操作人ID',
                    username VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
                    role VARCHAR(20) DEFAULT NULL COMMENT '操作人角色',
                    module VARCHAR(50) DEFAULT NULL COMMENT '模块',
                    operation VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
                    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
                    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
                    target_name VARCHAR(100) DEFAULT NULL COMMENT '目标名称',
                    action VARCHAR(50) DEFAULT NULL COMMENT '操作动作',
                    detail TEXT DEFAULT NULL COMMENT '操作详情',
                    result TINYINT DEFAULT NULL COMMENT '结果: 0-失败, 1-成功',
                    error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
                    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
                    user_agent VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
                    request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求URI',
                    request_method VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
                    duration_ms BIGINT DEFAULT NULL COMMENT '耗时（毫秒）',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
                ) COMMENT='操作审计日志表'
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("operation_log表初始化完成");
        } catch (SQLException e) {
            log.warn("创建operation_log表失败: {}", e.getMessage());
        }
    }

    private void createNotificationTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS notification (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
                    user_id BIGINT NOT NULL COMMENT '用户ID',
                    type VARCHAR(50) NOT NULL COMMENT '通知类型',
                    title VARCHAR(100) NOT NULL COMMENT '通知标题',
                    content TEXT NOT NULL COMMENT '通知内容',
                    data TEXT DEFAULT NULL COMMENT '附加数据(JSON)',
                    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '已读标识: 0-未读, 1-已读',
                    sender_id BIGINT DEFAULT NULL COMMENT '发送人ID',
                    expire_at DATETIME DEFAULT NULL COMMENT '过期时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
                ) COMMENT='消息通知表'
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("notification表初始化完成");
        } catch (SQLException e) {
            log.warn("创建notification表失败: {}", e.getMessage());
        }
    }
}
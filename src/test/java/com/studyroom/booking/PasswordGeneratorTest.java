package com.studyroom.booking;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCrypt 密码哈希验证测试
 */
@DisplayName("BCrypt 密码哈希")
class PasswordGeneratorTest {

    private static final String ROOT_PASSWORD = "root123";
    private static final String ADMIN_PASSWORD = "admin123";

    @Test
    @DisplayName("root/root123 BCrypt 哈希一致性")
    void rootPasswordHashConsistent() {
        String hash1 = BCrypt.hashpw(ROOT_PASSWORD);
        String hash2 = BCrypt.hashpw(ROOT_PASSWORD);

        // 每次生成的哈希不同（含随机盐），但验证结果一致
        assertNotEquals(hash1, hash2, "两次生成的哈希应不同（随机盐）");
        assertTrue(BCrypt.checkpw(ROOT_PASSWORD, hash1));
        assertTrue(BCrypt.checkpw(ROOT_PASSWORD, hash2));
        // 用错误密码验证应失败
        assertFalse(BCrypt.checkpw("wrong_password", hash1));
    }

    @Test
    @DisplayName("admin/admin123 BCrypt 哈希一致性")
    void adminPasswordHashConsistent() {
        String hash = BCrypt.hashpw(ADMIN_PASSWORD);

        assertTrue(BCrypt.checkpw(ADMIN_PASSWORD, hash));
        assertFalse(BCrypt.checkpw("wrong_password", hash));
    }

    @Test
    @DisplayName("schema.sql 中的 root 哈希可验证")
    void schemaRootHashValid() {
        // 与 docs/sql/schema.sql 中 root 账号的哈希一致
        String hashFromSchema = "$2a$10$M7aaPNfiOje5WN.ZaLaQReSOl9jocfB83Gmgpw1YaH5fpFTfRaSje";
        assertTrue(BCrypt.checkpw(ROOT_PASSWORD, hashFromSchema),
                "schema.sql 中 root 的 BCrypt 哈希应可验证 root123");
    }

    @Test
    @DisplayName("schema.sql 中的 admin 哈希可验证")
    void schemaAdminHashValid() {
        // 与 docs/sql/schema.sql 中 admin 账号的哈希一致
        String hashFromSchema = "$2a$10$zJ55msMPcCuLSVWk39pLFOGMF96SEr/PD9FGfW/1v9TaC3U3OiTpC";
        assertTrue(BCrypt.checkpw(ADMIN_PASSWORD, hashFromSchema),
                "schema.sql 中 admin 的 BCrypt 哈希应可验证 admin123");
    }
}

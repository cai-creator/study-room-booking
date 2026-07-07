package com.studyroom.booking;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

public class PasswordGeneratorTest {

    @Test
    public void generatePasswordHashes() {
        String rootHash = BCrypt.hashpw("root123");
        String adminHash = BCrypt.hashpw("admin123");

        System.out.println("========================================");
        System.out.println("root/root123 hash: " + rootHash);
        System.out.println("admin/admin123 hash: " + adminHash);
        System.out.println("========================================");

        // 验证
        System.out.println("Verify root123: " + BCrypt.checkpw("root123", rootHash));
        System.out.println("Verify admin123: " + BCrypt.checkpw("admin123", adminHash));
    }
}
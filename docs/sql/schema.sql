-- =====================================================
-- 高校自习室智能预约系统 - 数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 版本: v1.0
-- =====================================================

CREATE DATABASE IF NOT EXISTS study_room_booking
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE study_room_booking;

-- =====================================================
-- 1. 用户与权限模块
-- =====================================================

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名/学号',
    password VARCHAR(255) DEFAULT NULL COMMENT '密码（CAS登录可为空）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, ADMIN-管理员, SUPER_ADMIN-超级管理员',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 空间管理模块
-- =====================================================

DROP TABLE IF EXISTS campus;
CREATE TABLE campus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '校区ID',
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address VARCHAR(255) DEFAULT NULL COMMENT '地址',
    longitude DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    latitude DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校区表';

DROP TABLE IF EXISTS building;
CREATE TABLE building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼栋ID',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    name VARCHAR(100) NOT NULL COMMENT '楼栋名称',
    floor_count INT NOT NULL DEFAULT 0 COMMENT '楼层数',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_campus_id (campus_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼栋表';

DROP TABLE IF EXISTS floor;
CREATE TABLE floor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼层ID',
    building_id BIGINT NOT NULL COMMENT '所属楼栋ID',
    floor_number INT NOT NULL COMMENT '楼层号',
    name VARCHAR(50) DEFAULT NULL COMMENT '楼层名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_building_id (building_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼层表';

DROP TABLE IF EXISTS study_room;
CREATE TABLE study_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自习室ID',
    floor_id BIGINT NOT NULL COMMENT '所属楼层ID',
    name VARCHAR(100) NOT NULL COMMENT '自习室名称',
    room_type VARCHAR(20) NOT NULL DEFAULT 'LIBRARY' COMMENT '类型: LIBRARY-图书馆, TEACHING-教学楼, READING-阅览室',
    total_seats INT NOT NULL DEFAULT 0 COMMENT '总座位数',
    rows_count INT NOT NULL DEFAULT 0 COMMENT '座位行数',
    cols_count INT NOT NULL DEFAULT 0 COMMENT '座位列数',
    open_time TIME NOT NULL DEFAULT '08:00:00' COMMENT '开放时间',
    close_time TIME NOT NULL DEFAULT '22:00:00' COMMENT '关闭时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭, 1-开放, 2-维护中',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_floor_id (floor_id),
    KEY idx_room_type (room_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';

DROP TABLE IF EXISTS seat;
CREATE TABLE seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    room_id BIGINT NOT NULL COMMENT '所属自习室ID',
    seat_code VARCHAR(50) NOT NULL COMMENT '座位编号',
    `row_number` INT NOT NULL COMMENT '行号',
    `col_number` INT NOT NULL COMMENT '列号',
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）: WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-不可用, 1-可用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_seat (room_id, seat_code),
    KEY idx_room_id (room_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- =====================================================
-- 3. 预约核心模块
-- =====================================================

DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    room_id BIGINT NOT NULL COMMENT '自习室ID（冗余，方便查询）',
    start_time DATETIME NOT NULL COMMENT '预约开始时间',
    end_time DATETIME NOT NULL COMMENT '预约结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED' COMMENT '状态: RESERVED-已预约, CHECKED_IN-已签到, TEMPORARY_LEAVE-暂离, COMPLETED-已完成, CANCELLED-已取消, NO_SHOW-爽约',
    checkin_time DATETIME DEFAULT NULL COMMENT '签到时间',
    checkout_time DATETIME DEFAULT NULL COMMENT '签退时间',
    temporary_leave_time DATETIME DEFAULT NULL COMMENT '暂离开始时间',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_seat_id (seat_id),
    KEY idx_room_id (room_id),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_user_date (user_id, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

-- =====================================================
-- 4. 座位管控模块
-- =====================================================

DROP TABLE IF EXISTS blacklist;
CREATE TABLE blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '黑名单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reason VARCHAR(255) NOT NULL COMMENT '加入原因',
    no_show_count INT NOT NULL DEFAULT 0 COMMENT '累计爽约次数',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已解除, 1-生效中',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID（手动操作时）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

DROP TABLE IF EXISTS no_show_record;
CREATE TABLE no_show_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reservation_id BIGINT NOT NULL COMMENT '预约ID',
    reason VARCHAR(50) NOT NULL COMMENT '爽约原因: NO_CHECKIN-未签到, TEMPORARY_LEAVE_TIMEOUT-暂离超时',
    record_date DATE NOT NULL COMMENT '记录日期',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爽约记录表';

-- =====================================================
-- 5. 系统配置与审计
-- =====================================================

DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    config_desc VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '操作人ID',
    username VARCHAR(50) NOT NULL COMMENT '操作人用户名',
    module VARCHAR(50) NOT NULL COMMENT '模块',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
    error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    cost_time BIGINT DEFAULT NULL COMMENT '耗时（毫秒）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 系统配置初始化
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
('booking.max_daily_reservations', '3', '单日预约次数上限'),
('booking.max_duration_hours', '8', '单次最大预约时长（小时）'),
('booking.advance_booking_hours', '24', '提前预约时间窗口（小时）'),
('booking.min_advance_minutes', '15', '最短提前预约时间（分钟）'),
('booking.checkin_grace_minutes', '15', '签到宽限时间（分钟）'),
('booking.temporary_absence_minutes', '30', '暂离保留时间（分钟）'),
('booking.blacklist_threshold', '3', '黑名单爽约阈值（7天内）'),
('booking.blacklist_days', '7', '黑名单持续天数');

-- 超级管理员账号（密码: admin123，实际请修改）
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iX7iAt6Z', '超级管理员', 'SUPER_ADMIN', 1);

-- 示例数据：校区 -> 楼栋 -> 楼层 -> 自习室 -> 座位
INSERT INTO campus (name, address, sort_order, status) VALUES
('主校区', 'XX路1号', 1, 1),
('东校区', 'XX路2号', 2, 1);

INSERT INTO building (campus_id, name, floor_count, sort_order, status) VALUES
(1, '图书馆', 5, 1, 1),
(1, '第一教学楼', 6, 2, 1),
(2, '东区图书馆', 3, 1, 1);

INSERT INTO floor (building_id, floor_number, name, sort_order, status) VALUES
(1, 1, '一楼', 1, 1),
(1, 2, '二楼', 2, 1),
(1, 3, '三楼', 3, 1),
(2, 1, '一楼', 1, 1),
(3, 1, '一楼', 1, 1);

INSERT INTO study_room (floor_id, name, room_type, total_seats, rows_count, cols_count, open_time, close_time, status, description) VALUES
(1, '图书馆101自习室', 'LIBRARY', 60, 6, 10, '08:00:00', '22:00:00', 1, '安静自习区，禁止讨论'),
(1, '图书馆102自习室', 'LIBRARY', 50, 5, 10, '08:00:00', '22:00:00', 1, '讨论区，可小声交流'),
(2, '图书馆201自习室', 'LIBRARY', 80, 8, 10, '08:00:00', '22:30:00', 1, '考研专区'),
(4, '一教101教室', 'TEACHING', 40, 4, 10, '07:00:00', '22:00:00', 1, '教学楼自习室');

-- 生成示例座位数据（以图书馆101自习室为例，6行10列）
DROP PROCEDURE IF EXISTS generate_seats;
DELIMITER //
CREATE PROCEDURE generate_seats(IN p_room_id BIGINT, IN p_rows INT, IN p_cols INT)
BEGIN
    DECLARE r INT DEFAULT 1;
    DECLARE c INT DEFAULT 1;
    DECLARE seat_code VARCHAR(50);
    DECLARE tags VARCHAR(255);

    WHILE r <= p_rows DO
        SET c = 1;
        WHILE c <= p_cols DO
            SET seat_code = CONCAT(CHAR(64 + r), '-', LPAD(c, 2, '0'));
            SET tags = '';
            IF c = 1 OR c = p_cols THEN
                SET tags = 'WINDOW';
            END IF;
            IF r = 3 AND c = 5 THEN
                SET tags = 'ACCESSIBLE';
            END IF;
            IF (r + c) % 4 = 0 THEN
                IF tags != '' THEN
                    SET tags = CONCAT(tags, ',POWER');
                ELSE
                    SET tags = 'POWER';
                END IF;
            END IF;

            INSERT INTO seat (room_id, seat_code, `row_number`, `col_number`, tags, status)
            VALUES (p_room_id, seat_code, r, c, tags, 1);

            SET c = c + 1;
        END WHILE;
        SET r = r + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_seats(1, 6, 10);
CALL generate_seats(2, 5, 10);
CALL generate_seats(3, 8, 10);
CALL generate_seats(4, 4, 10);

DROP PROCEDURE IF EXISTS generate_seats;

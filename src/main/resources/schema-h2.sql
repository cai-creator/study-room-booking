-- H2数据库初始化脚本（兼容MySQL模式）

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used TINYINT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 登录失败记录表
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    fail_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME,
    last_attempt_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 校区表
CREATE TABLE IF NOT EXISTS campus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 楼栋表
CREATE TABLE IF NOT EXISTS building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campus_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    floor_count INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 楼层表
CREATE TABLE IF NOT EXISTS floor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    name VARCHAR(50),
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 自习室表
CREATE TABLE IF NOT EXISTS study_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    room_type VARCHAR(20) NOT NULL DEFAULT 'LIBRARY',
    total_seats INT NOT NULL DEFAULT 0,
    rows_count INT NOT NULL DEFAULT 0,
    cols_count INT NOT NULL DEFAULT 0,
    open_time TIME NOT NULL DEFAULT '08:00:00',
    close_time TIME NOT NULL DEFAULT '22:00:00',
    status TINYINT NOT NULL DEFAULT 1,
    description VARCHAR(500),
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 座位表
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    seat_code VARCHAR(50) NOT NULL,
    row_number INT NOT NULL,
    col_number INT NOT NULL,
    tags VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 座位不可用时间表
CREATE TABLE IF NOT EXISTS seat_unavailable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_id BIGINT NOT NULL,
    repeat_type VARCHAR(20),
    start_date_time DATETIME,
    end_date_time DATETIME,
    day_of_week INT,
    day_of_month INT,
    reason VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 预约记录表
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    checkin_time DATETIME,
    checkout_time DATETIME,
    temporary_leave_time DATETIME,
    version INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 黑名单表
CREATE TABLE IF NOT EXISTS blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    no_show_count INT NOT NULL DEFAULT 0,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    operator_id BIGINT,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 爽约记录表
CREATE TABLE IF NOT EXISTS no_show_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    record_date DATE NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    config_desc VARCHAR(255),
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    role VARCHAR(20),
    module VARCHAR(50),
    operation VARCHAR(100),
    target_type VARCHAR(50),
    target_id BIGINT,
    target_name VARCHAR(100),
    action VARCHAR(50),
    detail TEXT,
    result TINYINT,
    error_message VARCHAR(500),
    ip VARCHAR(50),
    user_agent VARCHAR(500),
    request_uri VARCHAR(255),
    request_method VARCHAR(20),
    duration_ms BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 消息通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    data TEXT,
    read_flag TINYINT NOT NULL DEFAULT 0,
    sender_id BIGINT,
    expire_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 通知偏好设置表
CREATE TABLE IF NOT EXISTS notification_preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    booking_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    checkin_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    system_notice BOOLEAN NOT NULL DEFAULT TRUE,
    blacklist_alert BOOLEAN NOT NULL DEFAULT TRUE,
    campus_id BIGINT,
    room_type VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- 初始化管理员账号（密码: admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('root', '$2a$10$M7aaPNfiOje5WN.ZaLaQReSOl9jocfB83Gmgpw1YaH5fpFTfRaSje', '超级管理员', 'SUPER_ADMIN', 1),
('admin', '$2a$10$zJ55msMPcCuLSVWk39pLFOGMF96SEr/PD9FGfW/1v9TaC3U3OiTpC', '普通管理员', 'ADMIN', 1);

-- 初始化系统配置
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
('booking.max_daily_reservations', '3', '单日预约次数上限'),
('booking.max_duration_hours', '8', '单次最大预约时长（小时）'),
('booking.advance_booking_hours', '24', '提前预约时间窗口（小时）'),
('booking.min_advance_minutes', '15', '最短提前预约时间（分钟）'),
('booking.checkin_grace_minutes', '15', '签到宽限时间（分钟）'),
('booking.temporary_absence_minutes', '30', '暂离保留时间（分钟）'),
('booking.blacklist_threshold', '3', '黑名单爽约阈值（7天内）'),
('booking.blacklist_days', '7', '黑名单持续天数');

-- 初始化示例校区和自习室数据
INSERT INTO campus (name, address, sort_order, status) VALUES
('主校区', 'XX路1号', 1, 1);

INSERT INTO building (campus_id, name, floor_count, sort_order, status) VALUES
(1, '图书馆', 5, 1, 1);

INSERT INTO floor (building_id, floor_number, name, sort_order, status) VALUES
(1, 1, '一楼', 1, 1),
(1, 2, '二楼', 2, 1);

INSERT INTO study_room (floor_id, name, room_type, total_seats, rows_count, cols_count, open_time, close_time, status, description) VALUES
(1, '图书馆101自习室', 'LIBRARY', 60, 6, 10, '08:00:00', '22:00:00', 1, '安静自习区'),
(2, '图书馆201自习室', 'LIBRARY', 80, 8, 10, '08:00:00', '22:30:00', 1, '考研专区');

-- 测试座位数据（自习室1: 6行×10列，这里插入前3行的座位用于测试）
INSERT INTO seat (room_id, seat_code, row_number, col_number, tags, status, deleted, created_at, updated_at) VALUES
(1, 'A-01', 1, 1, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'A-02', 1, 2, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'A-03', 1, 3, null, 1, 0, NOW(), NOW()),
(1, 'A-04', 1, 4, null, 1, 0, NOW(), NOW()),
(1, 'A-05', 1, 5, 'POWER', 1, 0, NOW(), NOW()),
(1, 'A-06', 1, 6, null, 1, 0, NOW(), NOW()),
(1, 'A-07', 1, 7, null, 1, 0, NOW(), NOW()),
(1, 'A-08', 1, 8, null, 1, 0, NOW(), NOW()),
(1, 'A-09', 1, 9, 'POWER', 1, 0, NOW(), NOW()),
(1, 'A-10', 1, 10, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'B-01', 2, 1, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'B-02', 2, 2, null, 1, 0, NOW(), NOW()),
(1, 'B-03', 2, 3, null, 1, 0, NOW(), NOW()),
(1, 'B-04', 2, 4, 'POWER', 1, 0, NOW(), NOW()),
(1, 'B-05', 2, 5, null, 1, 0, NOW(), NOW()),
(1, 'B-06', 2, 6, null, 1, 0, NOW(), NOW()),
(1, 'B-07', 2, 7, 'ACCESSIBLE', 1, 0, NOW(), NOW()),
(1, 'B-08', 2, 8, null, 0, 0, NOW(), NOW()),
(1, 'B-09', 2, 9, null, 1, 0, NOW(), NOW()),
(1, 'B-10', 2, 10, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'C-01', 3, 1, 'WINDOW', 1, 0, NOW(), NOW()),
(1, 'C-02', 3, 2, null, 1, 0, NOW(), NOW()),
(1, 'C-03', 3, 3, null, 1, 0, NOW(), NOW()),
(1, 'C-04', 3, 4, null, 1, 0, NOW(), NOW()),
(1, 'C-05', 3, 5, 'POWER', 1, 0, NOW(), NOW());
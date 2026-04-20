-- 测试环境数据库 Schema（H2 兼容）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    daily_goal INT NOT NULL DEFAULT 20,
    ui_language VARCHAR(10) NOT NULL DEFAULT 'zh-CN',
    theme_mode VARCHAR(10) NOT NULL DEFAULT 'light',
    streak INT NOT NULL DEFAULT 0,
    last_checkin_date DATE
);

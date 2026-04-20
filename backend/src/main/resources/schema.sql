-- 印尼语学习应用数据库 Schema (MySQL 8.0)

CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    email             VARCHAR(255) NOT NULL UNIQUE COMMENT '邮箱地址（小写存储）',
    password_hash     VARCHAR(255) NOT NULL COMMENT 'BCrypt哈希后的密码',
    daily_goal        INT          NOT NULL DEFAULT 20 COMMENT '每日学习目标',
    ui_language       VARCHAR(10)  NOT NULL DEFAULT 'zh-CN' COMMENT '界面语言',
    theme_mode        VARCHAR(10)  NOT NULL DEFAULT 'light' COMMENT '主题模式',
    streak            INT          NOT NULL DEFAULT 0 COMMENT '连续签到天数',
    last_checkin_date DATE COMMENT '最后签到日期',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT ='用户表';

CREATE TABLE IF NOT EXISTS wordbooks
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '词书ID',
    name       VARCHAR(100) NOT NULL COMMENT '词书名称（中文）',
    name_en    VARCHAR(100) COMMENT '词书名称（英文）',
    level      VARCHAR(20)  NOT NULL COMMENT '难度等级',
    word_count INT          NOT NULL DEFAULT 0 COMMENT '单词数量',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT ='词书表';

CREATE TABLE IF NOT EXISTS words
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '单词ID',
    wordbook_id        BIGINT       NOT NULL COMMENT '所属词书ID',
    indonesian         VARCHAR(100) NOT NULL COMMENT '印尼语单词',
    chinese            VARCHAR(100) NOT NULL COMMENT '中文释义',
    english            VARCHAR(100) COMMENT '英文释义',
    example_indonesian VARCHAR(500) COMMENT '印尼语例句',
    example_zh         VARCHAR(500) COMMENT '中文例句翻译',
    example_en         VARCHAR(500) COMMENT '英文例句翻译',
    sort_order         INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wordbook_sort (wordbook_id, sort_order)
) COMMENT ='单词表';

CREATE TABLE IF NOT EXISTS user_wordbooks
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id      BIGINT    NOT NULL COMMENT '用户ID',
    wordbook_id  BIGINT    NOT NULL COMMENT '词书ID',
    activated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '激活时间',
    UNIQUE KEY uk_user_wordbook (user_id, wordbook_id),
    INDEX idx_user (user_id)
) COMMENT ='用户激活的词书';

CREATE TABLE IF NOT EXISTS user_word_progress
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id          BIGINT      NOT NULL COMMENT '用户ID',
    word_id          BIGINT      NOT NULL COMMENT '单词ID',
    status           VARCHAR(20) NOT NULL DEFAULT 'new' COMMENT '状态：new/learning/reviewing/memorized',
    is_favorited     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否收藏',
    is_memorized     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已掌握',
    is_mistake       TINYINT     NOT NULL DEFAULT 0 COMMENT '是否在错题本',
    srs_interval     INT         NOT NULL DEFAULT 1 COMMENT 'SRS复习间隔天数',
    srs_repetitions  INT         NOT NULL DEFAULT 0 COMMENT 'SRS重复次数',
    next_review_date DATE COMMENT '下次复习日期',
    first_learned_at DATE COMMENT '首次学习日期',
    last_reviewed_at DATE COMMENT '最后复习日期',
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_word (user_id, word_id),
    INDEX idx_user_review (user_id, next_review_date, status),
    INDEX idx_user_mistake (user_id, is_mistake)
) COMMENT ='用户单词学习进度';

CREATE TABLE IF NOT EXISTS checkin_records
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id      BIGINT    NOT NULL COMMENT '用户ID',
    checkin_date DATE      NOT NULL COMMENT '签到日期',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    INDEX idx_user (user_id)
) COMMENT ='签到记录';

CREATE TABLE IF NOT EXISTS email_verification_codes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    email      VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    code       VARCHAR(10)  NOT NULL COMMENT '验证码',
    expires_at TIMESTAMP    NOT NULL COMMENT '过期时间',
    used       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已使用',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_email (email),
    INDEX idx_expires (expires_at)
) COMMENT ='邮箱验证码';

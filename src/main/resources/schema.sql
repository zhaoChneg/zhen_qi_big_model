-- =============================================
-- 交投科技政企大模型 - 数据库初始化脚本
-- 数据库：jiaotou_bigmodel
-- =============================================

CREATE DATABASE IF NOT EXISTS jiaotou_bigmodel DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jiaotou_bigmodel;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id           VARCHAR(36)  NOT NULL COMMENT '用户ID',
    username     VARCHAR(50)  NOT NULL COMMENT '用户名',
    password     VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    display_name VARCHAR(50)  COMMENT '显示名称',
    avatar       VARCHAR(200) COMMENT '头像URL',
    department   VARCHAR(100) COMMENT '部门',
    role         VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id            VARCHAR(36)  NOT NULL,
    user_id       VARCHAR(36)  NOT NULL COMMENT '用户ID',
    title         VARCHAR(200) NOT NULL DEFAULT '新对话',
    message_count INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='对话会话表';

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id             VARCHAR(36)  NOT NULL,
    session_id     VARCHAR(36)  NOT NULL COMMENT '会话ID',
    role           VARCHAR(20)  NOT NULL COMMENT 'user/assistant',
    content        MEDIUMTEXT   COMMENT '消息内容',
    attachment_ids VARCHAR(500) COMMENT '附件ID列表（JSON）',
    tokens         INT          DEFAULT 0 COMMENT 'token数量',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session_id (session_id)
) ENGINE=InnoDB COMMENT='对话消息表';

-- 公文文稿表
CREATE TABLE IF NOT EXISTS write_doc (
    id         VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(36)  NOT NULL,
    doc_type   VARCHAR(50)  COMMENT '公文类型',
    title      VARCHAR(200) COMMENT '标题',
    summary    TEXT         COMMENT '摘要',
    content    LONGTEXT     COMMENT '正文（HTML）',
    word_count INT          DEFAULT 0 COMMENT '字数',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='公文文稿表';

-- 问数查询记录表
CREATE TABLE IF NOT EXISTS data_query_record (
    id          VARCHAR(36)  NOT NULL,
    user_id     VARCHAR(36)  NOT NULL,
    source_id   VARCHAR(50)  COMMENT '数据源ID',
    question    VARCHAR(500) COMMENT '用户问题',
    sql_text    TEXT         COMMENT '生成的SQL',
    status      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/running/success/error',
    result_json LONGTEXT     COMMENT '查询结果（JSON）',
    error_msg   VARCHAR(500) COMMENT '错误信息',
    ai_summary  TEXT         COMMENT 'AI数据解读',
    duration    BIGINT       COMMENT '执行耗时(ms)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='问数查询记录表';

-- 文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id             VARCHAR(36)  NOT NULL,
    user_id        VARCHAR(36)  NOT NULL,
    original_name  VARCHAR(200) COMMENT '原始文件名',
    stored_name    VARCHAR(200) COMMENT '存储文件名',
    file_path      VARCHAR(500) COMMENT '文件路径',
    file_size      BIGINT       COMMENT '文件大小(bytes)',
    file_type      VARCHAR(50)  COMMENT '文件类型',
    parsed_content LONGTEXT     COMMENT '解析后的文本内容',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='文件记录表';

-- =============================================
-- 初始数据
-- =============================================

-- 默认管理员账号（密码：Admin@123）
INSERT IGNORE INTO sys_user (id, username, password, display_name, department, role, status)
VALUES (
    'u_admin_001',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '系统管理员',
    '信息技术部',
    'admin',
    1
);

-- 测试用户（密码：User@123）
INSERT IGNORE INTO sys_user (id, username, password, display_name, department, role, status)
VALUES (
    'u_test_001',
    'zhangsan',
    '$2a$10$8K1p/a0dR1xqM2LDHyA.HeWNFnmWkMLGq9X.J9r7f3mJGPJ7t/gOK',
    '张三',
    '业务管理部',
    'user',
    1
);

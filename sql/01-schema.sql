-- ============================================================
-- SpringAli 学习项目 · PostgreSQL 初始化脚本
-- 适用版本：PostgreSQL 10+
-- 执行方式：
--   psql -h localhost -p 5432 -U postgres -d springali -f sql/01-schema.sql
-- ============================================================

-- 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    avatar      VARCHAR(255),
    roles       VARCHAR(255) NOT NULL DEFAULT 'user',
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_user IS '系统用户';
COMMENT ON COLUMN sys_user.roles IS '角色，逗号分隔，如 admin,user';

-- 聊天会话
CREATE TABLE IF NOT EXISTS chat_conversation (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL UNIQUE,
    user_id         BIGINT,
    title           VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_conversation IS 'AI 聊天会话';

-- 聊天消息
CREATE TABLE IF NOT EXISTS chat_message (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    role            VARCHAR(16) NOT NULL,
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_message IS 'AI 聊天消息';
COMMENT ON COLUMN chat_message.role IS '角色：system / user / assistant';

CREATE INDEX IF NOT EXISTS idx_chat_message_conversation_id
    ON chat_message (conversation_id);

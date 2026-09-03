-- V2：会话、消息、标签要求确认表（MySQL 8 兼容）
USE `wait`;

CREATE TABLE IF NOT EXISTS `conversation` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `initiator_id`    BIGINT       NOT NULL COMMENT '发起方 user_id',
  `target_id`       BIGINT       NOT NULL COMMENT '接收方 user_id',
  `greeting_msg`    VARCHAR(256) NOT NULL COMMENT '招呼语',
  `state`           ENUM('PENDING','ACTIVE','ENDED') NOT NULL DEFAULT 'PENDING',
  `ended_by`        BIGINT       DEFAULT NULL COMMENT '结束方 user_id',
  `ended_reason`    ENUM('MANUAL','TIMEOUT_UNREAD','TIMEOUT_UNREPLIED') DEFAULT NULL,
  `initiator_read_at` DATETIME  DEFAULT NULL COMMENT '发起方最后读时间',
  `target_read_at`  DATETIME     DEFAULT NULL COMMENT '接收方最后读时间',
  `last_msg_at`     DATETIME     DEFAULT NULL COMMENT '最后消息时间',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_initiator_state` (`initiator_id`,`state`),
  KEY `idx_target_state` (`target_id`,`state`),
  KEY `idx_state_created` (`state`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE IF NOT EXISTS `message` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT       NOT NULL,
  `sender_id`       BIGINT       NOT NULL,
  `content`         VARCHAR(2048) NOT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conv_created` (`conversation_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE IF NOT EXISTS `tag_requirement_confirmation` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
  `confirmer_id`          BIGINT       NOT NULL COMMENT '确认方 user_id',
  `requirement_owner_id`  BIGINT       NOT NULL COMMENT '要求所有方 user_id',
  `requirement_key`       VARCHAR(64)  NOT NULL COMMENT '要求项 key',
  `requirement_value`     VARCHAR(256) DEFAULT NULL COMMENT '要求项 value',
  `direction`             ENUM('GREETING','REPLY') NOT NULL COMMENT '打招呼阶段/回复阶段',
  `confirmed`             TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已确认满足',
  `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_confirmer_owner_key_dir` (`confirmer_id`,`requirement_owner_id`,`requirement_key`,`direction`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签要求确认';

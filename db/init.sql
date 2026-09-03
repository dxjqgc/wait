-- wait 数据库初始化脚本
-- 用法： mysql -uroot -p < db/init.sql

CREATE DATABASE IF NOT EXISTS `wait`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `wait`;

-- 用户表（Casdoor 的本地影子记录）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `casdoor_id` VARCHAR(128) NOT NULL COMMENT 'Casdoor 用户唯一标识',
  `username`   VARCHAR(64)  NOT NULL,
  `nickname`   VARCHAR(128) DEFAULT NULL,
  `avatar`     VARCHAR(512) DEFAULT NULL,
  `email`      VARCHAR(128) DEFAULT NULL,
  `phone`      VARCHAR(32)  DEFAULT NULL,
  `gender`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0-未知 1-男 2-女',
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0-未激活 1-正常 2-封禁',
  `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_casdoor_id` (`casdoor_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户资料表（交友相关扩展信息，后续迭代添加字段）
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `bio`         VARCHAR(512) DEFAULT NULL COMMENT '个人简介',
  `birthday`    DATE         DEFAULT NULL,
  `location`    VARCHAR(128) DEFAULT NULL,
  `tags`        JSON         DEFAULT NULL COMMENT '兴趣标签',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

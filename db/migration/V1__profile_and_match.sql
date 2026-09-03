-- V1：资料与匹配相关字段（MySQL 8 兼容）
-- 可重复执行：自动跳过已存在的列
USE `wait`;

-- user 表扩展
DROP PROCEDURE IF EXISTS `p_alter_user`;
DELIMITER $$
CREATE PROCEDURE `p_alter_user`()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'engagement_state') THEN
        ALTER TABLE `user` ADD COLUMN `engagement_state` ENUM('FREE','ENGAGED') NOT NULL DEFAULT 'FREE'
            COMMENT '会话占用状态：FREE-自由 ENGAGED-占用中';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'last_active_at') THEN
        ALTER TABLE `user` ADD COLUMN `last_active_at` DATETIME DEFAULT NULL COMMENT '最后活跃时间';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'last_lng') THEN
        ALTER TABLE `user` ADD COLUMN `last_lng` DECIMAL(10,7) DEFAULT NULL COMMENT '最近定位经度';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'last_lat') THEN
        ALTER TABLE `user` ADD COLUMN `last_lat` DECIMAL(10,7) DEFAULT NULL COMMENT '最近定位纬度';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'located_at') THEN
        ALTER TABLE `user` ADD COLUMN `located_at` DATETIME DEFAULT NULL COMMENT '最近定位时间';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
                     AND COLUMN_NAME = 'match_visibility') THEN
        ALTER TABLE `user` ADD COLUMN `match_visibility` TINYINT NOT NULL DEFAULT 1 COMMENT '0-不可被匹配 1-可被匹配';
    END IF;
END$$
DELIMITER ;
CALL `p_alter_user`();
DROP PROCEDURE `p_alter_user`;

-- user_profile 表扩展
DROP PROCEDURE IF EXISTS `p_alter_profile`;
DELIMITER $$
CREATE PROCEDURE `p_alter_profile`()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'real_name') THEN
        ALTER TABLE `user_profile` ADD COLUMN `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'age') THEN
        ALTER TABLE `user_profile` ADD COLUMN `age` INT DEFAULT NULL COMMENT '年龄';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'city') THEN
        ALTER TABLE `user_profile` ADD COLUMN `city` VARCHAR(64) DEFAULT NULL COMMENT '常住地-城市';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'district') THEN
        ALTER TABLE `user_profile` ADD COLUMN `district` VARCHAR(64) DEFAULT NULL COMMENT '常住地-区县';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'profession') THEN
        ALTER TABLE `user_profile` ADD COLUMN `profession` VARCHAR(64) DEFAULT NULL COMMENT '职业';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'height') THEN
        ALTER TABLE `user_profile` ADD COLUMN `height` INT DEFAULT NULL COMMENT '身高cm';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'education') THEN
        ALTER TABLE `user_profile` ADD COLUMN `education` VARCHAR(32) DEFAULT NULL COMMENT '学历';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'personality') THEN
        ALTER TABLE `user_profile` ADD COLUMN `personality` JSON DEFAULT NULL COMMENT '性格特点，数组';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'appearances') THEN
        ALTER TABLE `user_profile` ADD COLUMN `appearances` JSON DEFAULT NULL COMMENT '外形描述，数组';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'hobbies') THEN
        ALTER TABLE `user_profile` ADD COLUMN `hobbies` JSON DEFAULT NULL COMMENT '兴趣爱好，数组';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'tags') THEN
        ALTER TABLE `user_profile` ADD COLUMN `tags` JSON DEFAULT NULL COMMENT '个人标签，数组';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'requirement_preset') THEN
        ALTER TABLE `user_profile` ADD COLUMN `requirement_preset` JSON DEFAULT NULL COMMENT '对方预设要求';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_profile'
                     AND COLUMN_NAME = 'requirement_custom') THEN
        ALTER TABLE `user_profile` ADD COLUMN `requirement_custom` JSON DEFAULT NULL COMMENT '对方自定义要求';
    END IF;
END$$
DELIMITER ;
CALL `p_alter_profile`();
DROP PROCEDURE `p_alter_profile`;

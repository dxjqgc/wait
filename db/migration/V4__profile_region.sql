-- V4: user_profile 表加 province_code/city_code/district_code（行政区划代码）
-- 注意：MySQL 8 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS（那是 MariaDB 语法）
-- 首次执行即可；如果重复执行会报 "Duplicate column name"，忽略即可
USE `wait`;

ALTER TABLE `user_profile` ADD COLUMN `province_code` VARCHAR(6) DEFAULT NULL COMMENT '省级行政区划代码';
ALTER TABLE `user_profile` ADD COLUMN `city_code` VARCHAR(6) DEFAULT NULL COMMENT '市级行政区划代码';
ALTER TABLE `user_profile` ADD COLUMN `district_code` VARCHAR(6) DEFAULT NULL COMMENT '区县行政区划代码';

-- 迁移旧数据：把原 city/district 字段的中文名转成 code（如果能匹配）
-- 找不到匹配的置 NULL（用户需要重新选择）
UPDATE `user_profile` p
  LEFT JOIN `region` r1 ON r1.name = p.city AND r1.level = 2
  SET p.province_code = (
    SELECT r2.parent_code FROM `region` r2 WHERE r2.code = r1.parent_code AND r2.level = 1 LIMIT 1
  ),
  p.city_code = r1.code
  WHERE p.city IS NOT NULL AND p.city != '';

UPDATE `user_profile` p
  LEFT JOIN `region` r3 ON r3.name = p.district AND r3.level = 3
  SET p.district_code = r3.code
  WHERE p.district IS NOT NULL AND p.district != '';

-- 注意：原 city/district 列保留，不删除，避免数据丢失

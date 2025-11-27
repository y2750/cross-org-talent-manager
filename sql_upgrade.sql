-- 数据库升级脚本
-- 用于将现有的company表升级到支持行业大类和子类的新结构

-- 1. 添加行业大类字段
ALTER TABLE `company` ADD COLUMN `industry_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行业大类' AFTER `email`;

-- 2. 修改industry字段长度，以支持存储JSON数组
ALTER TABLE `company` MODIFY COLUMN `industry` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行业子类（JSON数组格式）';

-- 3. 数据迁移示例（可选）
-- 如果现有数据中industry字段存在值，可以执行以下SQL将其转换为JSON格式
-- 注意：需要根据实际情况手动调整行业大类的分配

-- 示例：将单个行业值转换为JSON数组格式
-- UPDATE `company` SET `industry` = CONCAT('["', `industry`, '"]') WHERE `industry` IS NOT NULL AND `industry` != '';

-- 注意事项：
-- 1. 执行前请备份数据库
-- 2. 如果有现有数据，需要根据业务逻辑手动设置 industry_category 字段
-- 3. 需要将旧的 industry 字段值转换为 JSON 数组格式


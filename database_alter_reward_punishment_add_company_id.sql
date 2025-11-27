-- 为 reward_punishment 表添加 company_id 字段
ALTER TABLE `reward_punishment` 
ADD COLUMN `company_id` bigint NULL COMMENT '公司ID' AFTER `employee_id`;

-- 更新现有数据的 company_id
UPDATE `reward_punishment` 
SET `company_id` = 344582708126408704 
WHERE `company_id` IS NULL;

-- 将 company_id 设置为 NOT NULL（可选，如果需要强制要求）
-- ALTER TABLE `reward_punishment` 
-- MODIFY COLUMN `company_id` bigint NOT NULL COMMENT '公司ID';

-- 添加外键约束（可选，如果需要）
-- ALTER TABLE `reward_punishment` 
-- ADD CONSTRAINT `fk_reward_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- 添加索引以提高查询性能（可选）
-- CREATE INDEX `idx_company_id` ON `reward_punishment` (`company_id`);


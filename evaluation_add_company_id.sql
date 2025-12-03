-- 为evaluation表添加company_id字段
ALTER TABLE `evaluation` 
ADD COLUMN `company_id` bigint NULL COMMENT '评价时员工所属公司ID' AFTER `employee_id`;

-- 添加索引
CREATE INDEX `idx_evaluation_company` ON `evaluation` (`company_id`);

-- 更新已有数据：从employee表获取company_id
UPDATE `evaluation` e
INNER JOIN `employee` emp ON e.employee_id = emp.id
SET e.company_id = emp.company_id
WHERE e.company_id IS NULL;


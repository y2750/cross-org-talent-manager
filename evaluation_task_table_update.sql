-- 为evaluation_task表添加部门信息字段
-- 因为员工离职后departmentId会被清空，需要在创建任务时保存部门信息

ALTER TABLE `evaluation_task` 
ADD COLUMN `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID（创建任务时保存，避免员工离职后无法查询）' AFTER `employee_id`,
ADD COLUMN `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称（创建任务时保存）' AFTER `department_id`,
ADD INDEX `idx_task_department`(`department_id` ASC) USING BTREE;



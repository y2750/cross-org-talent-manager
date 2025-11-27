-- ============================================
-- 跨组织人才管理系统 - 数据库改进脚本
-- ============================================
-- 说明：此脚本包含所有数据库结构改进和新增表
-- 执行前请备份数据库！
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 修复现有表结构问题
-- ============================================

-- 1.1 修复evaluation表：添加employee_id字段、评价类型和评价周期
ALTER TABLE `evaluation` 
ADD COLUMN `employee_id` bigint NOT NULL COMMENT '被评价员工ID' AFTER `id`,
ADD COLUMN `employee_profile_id` bigint NULL COMMENT '关联的员工档案ID' AFTER `employee_id`,
ADD COLUMN `evaluation_type` tinyint NOT NULL DEFAULT 1 COMMENT '评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）' AFTER `is_leader_evaluation`,
ADD COLUMN `evaluation_period` tinyint NULL COMMENT '评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）' AFTER `evaluation_type`,
ADD COLUMN `period_year` int NULL COMMENT '评价年份（如2024）' AFTER `evaluation_period`,
ADD COLUMN `period_quarter` tinyint NULL COMMENT '评价季度（1-4，仅季度评价时有效）' AFTER `period_year`,
ADD INDEX `idx_evaluation_employee`(`employee_id`),
ADD INDEX `idx_evaluation_profile`(`employee_profile_id`),
ADD INDEX `idx_evaluation_type`(`evaluation_type`),
ADD INDEX `idx_evaluation_period`(`evaluation_period`, `period_year`, `period_quarter`),
ADD INDEX `idx_evaluator_employee`(`evaluator_id`, `employee_id`);

-- 1.2 修复department表：leader_id改为可空
ALTER TABLE `department` 
MODIFY COLUMN `leader_id` bigint NULL DEFAULT NULL COMMENT '部门领导';

-- 1.3 在employee_profile表中添加公开范围字段
ALTER TABLE `employee_profile` 
ADD COLUMN `visibility` tinyint DEFAULT 0 COMMENT '公开范围（0=完全保密，1=对认证企业可见，2=公开）' AFTER `operator_id`;

-- 1.4 在company表中添加总积分字段（可选，也可以通过视图计算）
ALTER TABLE `company` 
ADD COLUMN `total_points` decimal(12, 2) DEFAULT 0 COMMENT '当前总积分' AFTER `industry`;

-- ============================================
-- 2. 新增表：评价标签系统
-- ============================================

-- 2.1 评价标签表
DROP TABLE IF EXISTS `evaluation_tag`;
CREATE TABLE `evaluation_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `type` tinyint NOT NULL COMMENT '标签类型（1=正面，2=中性）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_type`(`type`),
  INDEX `idx_active`(`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价标签库';

-- 2.2 评价与标签关联表（不使用外键，仅使用索引）
DROP TABLE IF EXISTS `evaluation_tag_relation`;
CREATE TABLE `evaluation_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_evaluation_tag`(`evaluation_id`, `tag_id`),
  INDEX `idx_tag_relation_evaluation`(`evaluation_id`),
  INDEX `idx_tag_relation_tag`(`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价标签关联表';

-- ============================================
-- 3. 新增表：维度评分系统
-- ============================================

-- 3.1 评价维度表
DROP TABLE IF EXISTS `evaluation_dimension`;
CREATE TABLE `evaluation_dimension` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维度名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维度描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_active`(`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价维度配置表';

-- 3.2 维度评分记录表（不使用外键，仅使用索引）
DROP TABLE IF EXISTS `evaluation_dimension_score`;
CREATE TABLE `evaluation_dimension_score` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `dimension_id` bigint NOT NULL COMMENT '维度ID',
  `score` tinyint NOT NULL COMMENT '评分（1-5分）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_evaluation_dimension`(`evaluation_id`, `dimension_id`),
  INDEX `idx_dimension_score_evaluation`(`evaluation_id`),
  INDEX `idx_dimension_score_dimension`(`dimension_id`),
  CONSTRAINT `chk_score_range` CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维度评分记录表';

-- ============================================
-- 4. 新增表：档案查阅请求系统
-- ============================================

-- 4.1 档案查阅请求表
DROP TABLE IF EXISTS `profile_access_request`;
CREATE TABLE `profile_access_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_company_id` bigint NOT NULL COMMENT '请求企业ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_profile_id` bigint NULL DEFAULT NULL COMMENT '请求查阅的档案ID（可选，为空表示查阅所有档案）',
  `request_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0=待处理，1=已授权，2=已拒绝，3=已过期）',
  `request_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '授权过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_request_company`(`request_company_id`),
  INDEX `idx_request_employee`(`employee_id`),
  INDEX `idx_request_profile`(`employee_profile_id`),
  INDEX `idx_status`(`status`),
  INDEX `idx_request_time`(`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='档案查阅请求表';

-- 4.2 档案查阅记录表（审计用）
DROP TABLE IF EXISTS `profile_access_log`;
CREATE TABLE `profile_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` bigint NOT NULL COMMENT '请求ID',
  `access_company_id` bigint NOT NULL COMMENT '查阅企业ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_profile_id` bigint NOT NULL COMMENT '被查阅的档案ID',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '查阅时间',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  PRIMARY KEY (`id`),
  INDEX `idx_log_request`(`request_id`),
  INDEX `idx_log_company`(`access_company_id`),
  INDEX `idx_log_employee`(`employee_id`),
  INDEX `idx_log_profile`(`employee_profile_id`),
  INDEX `idx_access_time`(`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='档案查阅记录表';

-- ============================================
-- 5. 新增表：通知系统
-- ============================================

-- 5.1 通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` tinyint NOT NULL COMMENT '通知类型（1=评价任务，2=查阅请求，3=系统通知，4=投诉处理）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（如评价任务ID、请求ID等）',
  `status` tinyint DEFAULT 0 COMMENT '状态（0=未读，1=已读，2=已处理）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_notification_user`(`user_id`),
  INDEX `idx_status`(`status`),
  INDEX `idx_type`(`type`),
  INDEX `idx_create_time`(`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

-- ============================================
-- 6. 新增表：投诉系统
-- ============================================

-- 6.1 投诉表
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complainant_id` bigint NOT NULL COMMENT '投诉人ID（员工）',
  `evaluation_id` bigint NULL DEFAULT NULL COMMENT '被投诉的评价ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '被投诉的企业ID',
  `type` tinyint NOT NULL COMMENT '投诉类型（1=恶意评价，2=虚假信息，3=其他）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉内容',
  `evidence` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证据（JSON格式，可包含图片、链接等）',
  `status` tinyint DEFAULT 0 COMMENT '状态（0=待处理，1=处理中，2=已处理，3=已驳回）',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID（系统管理员）',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_complaint_complainant`(`complainant_id`),
  INDEX `idx_complaint_evaluation`(`evaluation_id`),
  INDEX `idx_complaint_company`(`company_id`),
  INDEX `idx_complaint_handler`(`handler_id`),
  INDEX `idx_status`(`status`),
  INDEX `idx_type`(`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投诉表';

-- ============================================
-- 7. 新增表：评价模板系统（可选）
-- ============================================

-- 7.1 评价模板表
DROP TABLE IF EXISTS `evaluation_template`;
CREATE TABLE `evaluation_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板描述',
  `company_id` bigint NULL DEFAULT NULL COMMENT '所属企业ID（NULL表示系统模板）',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容（JSON格式）',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统模板',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_template_company`(`company_id`),
  INDEX `idx_system`(`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价模板表';

-- ============================================
-- 8. 索引优化
-- ============================================

-- 8.1 employee表索引优化
ALTER TABLE `employee` 
ADD INDEX `idx_company_status`(`company_id`, `status`),
ADD INDEX `idx_department`(`department_id`);

-- 8.2 employee_profile表索引优化
ALTER TABLE `employee_profile` 
ADD INDEX `idx_employee_company`(`employee_id`, `company_id`),
ADD INDEX `idx_start_date`(`start_date`);

-- 8.3 evaluation表索引优化（添加employee_id后）
ALTER TABLE `evaluation` 
ADD INDEX `idx_employee_date`(`employee_id`, `evaluation_date`),
ADD INDEX `idx_evaluator`(`evaluator_id`);

-- 8.4 company_points表索引优化
ALTER TABLE `company_points` 
ADD INDEX `idx_company_date`(`company_id`, `change_date`);

-- ============================================
-- 9. 数据完整性约束
-- ============================================

-- 9.1 employee_profile日期范围检查
ALTER TABLE `employee_profile` 
ADD CONSTRAINT `chk_date_range` CHECK (`end_date` IS NULL OR `start_date` <= `end_date`);

-- 9.2 profile_access_request过期时间检查
ALTER TABLE `profile_access_request` 
ADD CONSTRAINT `chk_expire_time` CHECK (`expire_time` IS NULL OR `expire_time` > `request_time`);

-- ============================================
-- 10. 初始化数据
-- ============================================

-- 10.1 初始化评价维度数据
INSERT INTO `evaluation_dimension` (`name`, `description`, `sort_order`) VALUES
('工作业绩', '员工工作成果和业绩表现', 1),
('责任心', '员工工作责任心和主动性', 2),
('专业技能', '员工专业能力和技术水平', 3),
('团队协作', '员工团队合作和沟通能力', 4),
('创新能力', '员工创新思维和解决问题能力', 5);

-- 10.2 初始化评价标签数据（正面标签）
INSERT INTO `evaluation_tag` (`name`, `type`, `description`, `sort_order`) VALUES
('团队协作', 1, '善于与团队成员合作', 1),
('创新能力', 1, '具有创新思维和解决问题的能力', 2),
('责任心强', 1, '工作认真负责，积极主动', 3),
('专业技能突出', 1, '专业能力优秀', 4),
('沟通能力强', 1, '善于沟通表达', 5),
('学习能力强', 1, '快速学习新知识', 6),
('执行力强', 1, '高效执行任务', 7),
('抗压能力强', 1, '能够承受工作压力', 8);

-- 10.3 初始化评价标签数据（中性标签）
INSERT INTO `evaluation_tag` (`name`, `type`, `description`, `sort_order`) VALUES
('需提升沟通技巧', 2, '沟通能力有待提升', 1),
('需加强团队协作', 2, '团队协作能力需要加强', 2),
('专业技能需提升', 2, '专业技能需要进一步提升', 3),
('工作主动性待加强', 2, '工作主动性需要提升', 4),
('时间管理需改进', 2, '时间管理能力需要改进', 5);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 脚本执行完成
-- ============================================
-- 注意：
-- 1. 如果evaluation表已有数据，需要先处理数据再添加外键约束
-- 2. 执行前请备份数据库
-- 3. 建议在测试环境先执行验证
-- ============================================


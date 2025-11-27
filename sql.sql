/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : cross_org_talent_manager

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 25/11/2025 13:56:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for company
-- ----------------------------
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
  `contact_person_id` bigint NOT NULL COMMENT '联系人id',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业邮箱',
  `industry_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行业大类',
  `industry` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行业子类（JSON数组格式）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 344592263740768257 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业信息管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for company_points
-- ----------------------------
DROP TABLE IF EXISTS `company_points`;
CREATE TABLE `company_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '所属企业',
  `points` decimal(12, 2) NOT NULL COMMENT '积分变动（正/负）',
  `change_reason` tinyint(1) NOT NULL COMMENT '变动原因（,1=建立档案，2=员工评价）',
  `with_employee_id` bigint NULL DEFAULT NULL COMMENT '关联员工id',
  `score` tinyint NULL DEFAULT NULL COMMENT '评分（0-100，变动原因=0时有效）',
  `change_date` date NOT NULL COMMENT '变动日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_points_company`(`company_id` ASC) USING BTREE,
  INDEX `fk_points_evaluation`(`with_employee_id` ASC) USING BTREE,
  CONSTRAINT `fk_points_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业积分变动记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `company_id` bigint NOT NULL COMMENT '所属企业',
  `leader_id` bigint NOT NULL COMMENT '部门领导',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_department_company`(`company_id` ASC) USING BTREE,
  INDEX `fk_department_leader`(`leader_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 349689702444036097 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录公司部门及部门领导' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `id_card_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `department_id` bigint NULL DEFAULT NULL COMMENT '所属部门',
  `status` tinyint(1) NOT NULL COMMENT '在职状态',
  `photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '员工照片',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `company_id` bigint NOT NULL COMMENT '所属公司id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_card_number`(`id_card_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 349776742455980033 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工基本信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_profile
-- ----------------------------
DROP TABLE IF EXISTS `employee_profile`;
CREATE TABLE `employee_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `company_id` bigint NOT NULL COMMENT '所属企业',
  `start_date` date NOT NULL COMMENT '入职日期',
  `end_date` date NULL DEFAULT NULL COMMENT '离职日期',
  `performance_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '绩效摘要',
  `attendance_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '出勤率',
  `has_major_incident` tinyint(1) NOT NULL COMMENT '重大违纪',
  `reason_for_leaving` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离职原因',
  `occupation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职业',
  `annual_salary` decimal(12, 2) NULL DEFAULT NULL COMMENT '年薪',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  `operator_id` bigint NOT NULL COMMENT '操作人id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_profile_employee`(`employee_id` ASC) USING BTREE,
  INDEX `fk_profile_company`(`company_id` ASC) USING BTREE,
  CONSTRAINT `fk_profile_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 349777773592064001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工档案信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluator_id` bigint NOT NULL COMMENT '评价人',
  `score` tinyint NOT NULL COMMENT '评分（0-100分）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `evaluation_date` date NOT NULL COMMENT '评价日期',
  `is_leader_evaluation` tinyint(1) NOT NULL COMMENT '是否为领导评价',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_evaluation_evaluator`(`evaluator_id` ASC) USING BTREE,
  CONSTRAINT `fk_evaluation_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `evaluation_chk_1` CHECK (`score` between 0 and 100)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工评价记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_punishment
-- ----------------------------
DROP TABLE IF EXISTS `reward_punishment`;
CREATE TABLE `reward_punishment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `type` tinyint NOT NULL COMMENT '类型（1为奖励，2为惩罚）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细描述',
  `amount` decimal(12, 2) NOT NULL COMMENT '金额',
  `date` date NOT NULL COMMENT '发生日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  `operator_id` bigint NOT NULL COMMENT '操作人id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_reward_employee`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `fk_reward_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 350451214160039937 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '奖惩记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户角色',
  `company_id` bigint NULL DEFAULT NULL COMMENT '所属企业',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名字',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `fk_user_company`(`company_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 350550633466048513 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户管理' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

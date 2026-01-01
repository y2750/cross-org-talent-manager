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

 Date: 01/01/2026 19:49:35
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
  `total_points` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '当前总积分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 359619316558876673 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业信息管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company
-- ----------------------------
INSERT INTO `company` VALUES (344582708126408704, '公司1', 344581859291447296, '13712341234', '123456@qq.com', '信息技术/互联网', '[\"移动应用开发\",\"软件开发\",\"互联网平台\"]', 45.00, '2025-11-08 09:49:39', '2025-12-04 16:59:35', 0);
INSERT INTO `company` VALUES (344592263740768256, '公司2', 344592157545185280, '13712341211', '12345611@qq.com', '专业服务', '[\"人力资源\",\"咨询/管理\"]', 4.30, '2025-11-08 10:27:37', '2025-11-25 14:11:37', 0);
INSERT INTO `company` VALUES (359619316558876672, '腾讯公司', 359619316391104512, '16738502811', '2774680379@qq.com', '信息技术/互联网', '[\"软件开发\",\"游戏/娱乐\",\"移动应用开发\",\"大数据/云计算\",\"人工智能\",\"互联网平台\"]', 0.00, '2025-12-19 21:39:46', '2025-12-19 21:39:46', 0);

-- ----------------------------
-- Table structure for company_points
-- ----------------------------
DROP TABLE IF EXISTS `company_points`;
CREATE TABLE `company_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '所属企业',
  `points` decimal(12, 2) NOT NULL COMMENT '积分变动（正/负）',
  `change_reason` tinyint(1) NOT NULL COMMENT '变动原因（1=建立档案，2=员工评价，3=权益消耗，4=评价申诉）',
  `with_employee_id` bigint NULL DEFAULT NULL COMMENT '关联员工id',
  `change_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变动说明（后端自动生成）',
  `change_date` date NOT NULL COMMENT '变动日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_points_evaluation`(`with_employee_id` ASC) USING BTREE,
  INDEX `idx_company_date`(`company_id` ASC, `change_date` ASC) USING BTREE,
  CONSTRAINT `fk_points_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 354113066851893249 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业积分变动记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company_points
-- ----------------------------
INSERT INTO `company_points` VALUES (352592667462967296, 344582708126408704, 10.00, 1, 349446284006400000, '为离职员工赵武建立完整档案', '2025-11-30', '2025-11-30 12:18:22', '2025-11-30 12:18:22', 0);
INSERT INTO `company_points` VALUES (352607790835478528, 344582708126408704, 5.00, 2, 349446284006400000, '对员工赵武进行了客观评价', '2025-11-30', '2025-11-30 13:18:28', '2025-11-30 13:18:28', 0);
INSERT INTO `company_points` VALUES (352608223687651328, 344582708126408704, 5.00, 2, 349446284006400000, '对员工赵武进行了客观评价', '2025-11-30', '2025-11-30 13:20:11', '2025-11-30 13:20:11', 0);
INSERT INTO `company_points` VALUES (353025842693545984, 344592263740768256, 10.00, 1, 349446284006400000, '为离职员工赵武建立完整档案', '2025-12-01', '2025-12-01 16:59:39', '2025-12-01 16:59:39', 0);
INSERT INTO `company_points` VALUES (353415188483530752, 344582708126408704, -50.00, 4, 349446284006400000, '因投诉通过，扣除积分50分', '2025-12-02', '2025-12-02 18:46:47', '2025-12-02 18:46:47', 0);
INSERT INTO `company_points` VALUES (353711605211058176, 344582708126408704, 5.00, 2, 350947173493932032, '对员工杨过进行了客观评价', '2025-12-03', '2025-12-03 14:24:38', '2025-12-03 14:24:38', 0);
INSERT INTO `company_points` VALUES (353721814390263808, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:05:12', '2025-12-03 15:05:12', 0);
INSERT INTO `company_points` VALUES (353721856060674048, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:05:22', '2025-12-03 15:05:22', 0);
INSERT INTO `company_points` VALUES (353722244532916224, 344592263740768256, -0.20, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:06:54', '2025-12-03 15:06:54', 0);
INSERT INTO `company_points` VALUES (353723803786399744, 344592263740768256, -0.20, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:13:06', '2025-12-03 15:13:06', 0);
INSERT INTO `company_points` VALUES (353723822228754432, 344592263740768256, -0.20, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:13:11', '2025-12-03 15:13:11', 0);
INSERT INTO `company_points` VALUES (353723843628093440, 344592263740768256, -0.20, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:13:16', '2025-12-03 15:13:16', 0);
INSERT INTO `company_points` VALUES (353723889652191232, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:13:27', '2025-12-03 15:13:27', 0);
INSERT INTO `company_points` VALUES (353723904407752704, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:13:30', '2025-12-03 15:13:30', 0);
INSERT INTO `company_points` VALUES (353724029226045440, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:14:00', '2025-12-03 15:14:00', 0);
INSERT INTO `company_points` VALUES (353725799058755584, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:21:02', '2025-12-03 15:21:02', 0);
INSERT INTO `company_points` VALUES (353725801671806976, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:21:03', '2025-12-03 15:21:03', 0);
INSERT INTO `company_points` VALUES (353725808512716800, 344592263740768256, -0.50, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:21:04', '2025-12-03 15:21:04', 0);
INSERT INTO `company_points` VALUES (353728732374614016, 344592263740768256, -0.20, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:32:41', '2025-12-03 15:32:41', 0);
INSERT INTO `company_points` VALUES (353728933856395264, 344592263740768256, -0.70, 3, NULL, '高级搜索消耗积分', '2025-12-03', '2025-12-03 15:33:29', '2025-12-03 15:33:29', 0);
INSERT INTO `company_points` VALUES (353728933856395265, 344582708126408704, 100.00, 1, NULL, '系统赠送', '2025-12-04', '2025-12-04 16:59:12', '2025-12-04 16:59:12', 0);
INSERT INTO `company_points` VALUES (354113066851893248, 344582708126408704, -30.00, 3, NULL, '深度智能分析（3人对比）', '2025-12-04', '2025-12-04 16:59:54', '2025-12-04 16:59:54', 0);

-- ----------------------------
-- Table structure for company_preference
-- ----------------------------
DROP TABLE IF EXISTS `company_preference`;
CREATE TABLE `company_preference`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `preferred_occupations` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '偏好职位（JSON数组）',
  `preferred_tag_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '偏好标签ID（JSON数组）',
  `excluded_tag_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '排除标签ID（JSON数组）',
  `min_score` decimal(3, 2) NULL DEFAULT NULL COMMENT '最低评分要求',
  `exclude_major_incident` tinyint(1) NULL DEFAULT 1 COMMENT '是否排除有重大违纪记录',
  `min_attendance_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低出勤率要求',
  `requirement_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '具体要求描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company`(`company_id` ASC) USING BTREE,
  CONSTRAINT `fk_preference_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 353675671539146753 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业招聘偏好表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of company_preference
-- ----------------------------
INSERT INTO `company_preference` VALUES (353675671539146752, 344582708126408704, '[\"java\",\"前端\"]', '[4,7]', '[]', NULL, 0, NULL, '想要更稳定的员工', NULL, '2025-12-03 12:02:05', 0);

-- ----------------------------
-- Table structure for company_registration_request
-- ----------------------------
DROP TABLE IF EXISTS `company_registration_request`;
CREATE TABLE `company_registration_request`  (
  `id` bigint NOT NULL,
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业名称',
  `address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业地址',
  `company_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业邮箱',
  `admin_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理人姓名',
  `admin_phone` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理人电话',
  `admin_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理人邮箱',
  `admin_id_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理人身份证号',
  `industry_category` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行业大类',
  `industries` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '行业子类（JSON 数组）',
  `proof_materials` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证明材料图片 URL 列表（JSON 数组）',
  `status` int NOT NULL DEFAULT 0 COMMENT '申请状态（0=待处理，1=已通过，2=已拒绝）',
  `reject_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记',
  `admin_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理人账号名（用于登录）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业注册申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company_registration_request
-- ----------------------------
INSERT INTO `company_registration_request` VALUES (359613135043977216, '腾讯公司', '广东省深圳市南山区深南街道10000号', '2774680379@qq.com', '马化腾', '16738502811', '2774680379@qq.com', '36220219930601230', '信息技术/互联网', '[\"软件开发\",\"游戏/娱乐\",\"移动应用开发\",\"大数据/云计算\",\"人工智能\",\"互联网平台\"]', '[\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/company/registration/proof/1766150104512_v2-f1d244083449a21ab70d4e949688f55c_r.jpg\"]', 1, NULL, '2025-12-19 21:15:12', '2025-12-19 21:15:12', 0, '');
INSERT INTO `company_registration_request` VALUES (359622619233583104, '组啊企业', '广东省深圳市南山区深南街道10000号', '2774680379@qq.com', '马化腾', '16738502811', '2774680379@qq.com', '36220219930601999', '专业服务', '[\"咨询/管理\",\"法律服务\"]', '[\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/company/registration/proof/1766152372993_b6ffabe9deb8170892aed92ffe8628d.jpg\"]', 2, '材料有误', '2025-12-19 21:52:53', '2025-12-19 21:52:53', 0, 'admin1');

-- ----------------------------
-- Table structure for complaint
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complainant_id` bigint NOT NULL COMMENT '投诉人ID（员工）',
  `evaluation_id` bigint NULL DEFAULT NULL COMMENT '被投诉的评价ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '被投诉的企业ID',
  `type` tinyint NOT NULL COMMENT '投诉类型（1=恶意评价，2=虚假信息，3=其他）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉内容',
  `evidence` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证据（JSON格式，可包含图片、链接等）',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态（0=待处理，1=处理中，2=已处理，3=已驳回）',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID（系统管理员）',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_complaint_complainant`(`complainant_id` ASC) USING BTREE,
  INDEX `idx_complaint_evaluation`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_complaint_company`(`company_id` ASC) USING BTREE,
  INDEX `idx_complaint_handler`(`handler_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 353413312966942721 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投诉表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of complaint
-- ----------------------------
INSERT INTO `complaint` VALUES (353405052624830464, 349446283922513920, 352607790537682944, 344582708126408704, 1, '测试内容', '测试内容111', '[\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/complaint/evidence/349446283922513920/1764669989172_微信图片_20241021214959.jpg\",\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/complaint/evidence/349446283922513920/1764669989919_微信图片_20231124092706.jpg\"]', 3, 341422268628860928, NULL, '2025-12-02 18:38:19', '2025-12-02 18:06:30', '2025-12-02 18:06:30', 0);
INSERT INTO `complaint` VALUES (353413312966942720, 349446283922513920, 352607790537682944, 344582708126408704, 2, '测试888', '测试888111', NULL, 2, 341422268628860928, '通过', '2025-12-02 18:46:47', '2025-12-02 18:39:19', '2025-12-02 18:39:19', 0);

-- ----------------------------
-- Table structure for contact_access_request
-- ----------------------------
DROP TABLE IF EXISTS `contact_access_request`;
CREATE TABLE `contact_access_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_company_id` bigint NOT NULL COMMENT '请求企业ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `request_type` tinyint NOT NULL COMMENT '请求类型（1=查看电话，2=查看邮箱，3=查看身份证号，4=查看所有联系方式，5=查看电话和邮箱）',
  `request_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0=待处理，1=已授权，2=已拒绝，3=已过期）',
  `request_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '授权过期时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_company`(`request_company_id` ASC) USING BTREE,
  INDEX `idx_request_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  INDEX `idx_company_employee_status`(`request_company_id` ASC, `employee_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_contact_request_company` FOREIGN KEY (`request_company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_contact_request_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_contact_expire_time` CHECK ((`expire_time` is null) or (`expire_time` > `request_time`))
) ENGINE = InnoDB AUTO_INCREMENT = 353729101611778049 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '联系方式查看请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contact_access_request
-- ----------------------------
INSERT INTO `contact_access_request` VALUES (353714696807370752, 344582708126408704, 350947173493932032, 5, '444', 1, '2025-12-03 14:36:55', '2025-12-03 14:44:00', '2025-12-10 14:36:55', '2025-12-03 14:36:55', '2025-12-03 14:36:55', 0);
INSERT INTO `contact_access_request` VALUES (353729101611778048, 344592263740768256, 350947173493932032, 5, '999', 0, '2025-12-03 15:34:10', NULL, '2025-12-10 15:34:10', '2025-12-03 15:34:09', '2025-12-03 15:34:09', 0);

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `company_id` bigint NOT NULL COMMENT '所属企业',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '部门领导',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_department_company`(`company_id` ASC) USING BTREE,
  INDEX `fk_department_leader`(`leader_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 350953646399717377 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录公司部门及部门领导' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (349415061020217344, '后勤部（测试）', 344582708126408704, 349419495393714176, '2025-11-21 17:51:42', '2025-11-21 17:51:42', 0);
INSERT INTO `department` VALUES (349689702444036096, '技术部（测试）', 344592263740768256, NULL, '2025-11-22 12:03:01', '2025-11-27 11:47:46', 0);
INSERT INTO `department` VALUES (350953646399717376, '技术部', 344582708126408704, NULL, '2025-11-25 23:45:29', '2025-11-27 13:41:54', 0);

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
  `company_id` bigint NULL DEFAULT NULL COMMENT '所属公司id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_card_number`(`id_card_number` ASC) USING BTREE,
  INDEX `idx_company_status`(`company_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_department`(`department_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 354408883668250625 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工基本信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES (349419495393714176, '张三', '男', '13738501250', '27504174@163.com', '362121199912127548', 349415061020217344, 1, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349419495393714176/1764069660276_OIP (3).webp', '2025-11-21 18:09:19', '2025-11-21 18:09:19', 0, 349419495334993920, 344582708126408704);
INSERT INTO `employee` VALUES (349446284006400000, '赵武', '女', '13738506699', '27504555@qq.com', '362121199912127348', 349689702444036096, 1, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp', '2025-11-21 19:55:46', '2025-11-30 12:18:25', 0, 349446283922513920, 344592263740768256);
INSERT INTO `employee` VALUES (349694061756129280, '李斯', '男', '13738506699', '27504555@qq.com', '362121199910043478', 349689702444036096, 1, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349694061756129280/1764069674700_OIP.jpg', '2025-11-22 12:20:21', '2025-11-22 12:22:19', 0, 349694061701603328, 344592263740768256);
INSERT INTO `employee` VALUES (349776742455980032, '王柳', '女', '13731236691', '27504555@qq.com', '362121199910046666', 350953646399717376, 1, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg', '2025-11-22 17:48:53', '2025-11-30 12:15:39', 0, 349776742242070528, 344582708126408704);
INSERT INTO `employee` VALUES (350929096802848768, '李云', '女', '16738502822', '666668@163.com', '362202199602104427', NULL, 0, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350929096802848768/1764080742312_头像.webp', '2025-11-25 22:07:56', '2025-11-25 22:07:56', 0, 350929096651853824, NULL);
INSERT INTO `employee` VALUES (350947173493932032, '杨过', '男', '16738987777', '27765550379@qq.com', '361102198802104477', NULL, 0, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg', '2025-11-25 23:19:46', '2025-12-02 13:33:42', 0, 350947173338742784, NULL);
INSERT INTO `employee` VALUES (351129930824167424, '李云龙', '男', '13323236561', '16999965@hotmail.com', '654202198808177516', 350953646399717376, 1, 'https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/351129930673172480/1764162439685_OIP (4).png', '2025-11-26 11:25:59', '2025-11-26 18:40:18', 0, 351129930673172480, 344582708126408704);
INSERT INTO `employee` VALUES (351238108501110784, '马保国', '女', '13323236333', '16992263@hotmail.com', '654202198808161165', 349689702444036096, 1, NULL, '2025-11-26 18:35:50', '2025-11-27 13:41:54', 0, 351238108329144320, 344592263740768256);
INSERT INTO `employee` VALUES (354398132471631872, '李雪', '女', '13317036325', '3665495@qq.com', '362202199912307564', 349415061020217344, 1, NULL, '2025-12-05 11:52:39', '2025-12-05 11:52:39', 0, 354398132337414144, 344582708126408704);
INSERT INTO `employee` VALUES (354398132538740736, '张无忌', '男', '13656474124', '111999@163.com', '421502199104059615', NULL, 1, NULL, '2025-12-05 11:52:39', '2025-12-05 11:52:39', 0, 354398132526157824, 344582708126408704);
INSERT INTO `employee` VALUES (354408883668250624, '赵东', '男', '13317036655', '9092882@htomail.com', '362202199901235612', 349415061020217344, 1, NULL, '2025-12-05 12:35:22', '2025-12-05 12:35:22', 0, 354408883601141760, 344582708126408704);

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
  `visibility` tinyint NULL DEFAULT 2 COMMENT '公开范围（0=完全保密，1=对认证企业可见，2=公开）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_profile_company`(`company_id` ASC) USING BTREE,
  INDEX `idx_employee_company`(`employee_id` ASC, `company_id` ASC) USING BTREE,
  INDEX `idx_start_date`(`start_date` ASC) USING BTREE,
  CONSTRAINT `fk_profile_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_date_range` CHECK ((`end_date` is null) or (`start_date` <= `end_date`))
) ENGINE = InnoDB AUTO_INCREMENT = 353025842320252930 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工档案信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee_profile
-- ----------------------------
INSERT INTO `employee_profile` VALUES (349694748074287104, 349694061756129280, 344592263740768256, '2024-09-01', NULL, NULL, NULL, 0, NULL, 'java后端', 18.00, '2025-11-22 12:23:04', '2025-11-22 12:23:04', 0, 344592157545185280, 2);
INSERT INTO `employee_profile` VALUES (349723123581018112, 349419495393714176, 344582708126408704, '2024-09-01', '2025-11-19', '该员工表现良好，无旷工，迟到早退', 98.00, 0, '正常调岗', '采购', 10.00, '2025-11-22 14:15:50', '2025-12-01 15:57:47', 0, 341422268628860928, 1);
INSERT INTO `employee_profile` VALUES (349765193762877440, 349419495393714176, 344582708126408704, '2024-09-01', NULL, NULL, NULL, 0, NULL, '采购', 8.00, '2025-11-22 17:03:00', '2025-11-25 00:21:30', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (349777773592064000, 349776742455980032, 344582708126408704, '2024-09-21', '2025-11-30', NULL, 98.00, 0, '自主离职', '财务', 9.20, '2025-11-22 17:52:59', '2025-11-30 12:15:36', 0, 350528138855968768, 2);
INSERT INTO `employee_profile` VALUES (350884381214670848, 349419495393714176, 344582708126408704, '2025-11-19', NULL, NULL, NULL, 0, NULL, '采购组长', 12.00, '2025-11-25 19:10:15', '2025-12-01 15:57:50', 0, 341422268628860928, 2);
INSERT INTO `employee_profile` VALUES (350948236850659328, 350947173493932032, 344582708126408704, '2025-05-25', '2025-11-25', '出勤率一般，工作尚可', 94.00, 0, '公司缩编', '出纳', 8.90, '2025-11-25 23:23:59', '2025-11-26 10:54:01', 0, 341422268628860928, 2);
INSERT INTO `employee_profile` VALUES (351138204785160192, 351129930824167424, 344582708126408704, '2023-06-21', '2025-01-09', '良好', 100.00, 0, '升职', '后端开发工程师', 18.00, '2025-11-26 11:58:51', '2025-11-26 12:19:28', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351143577780871168, 351129930824167424, 344582708126408704, '2025-01-31', '2025-11-19', '存在泄密行为', 98.00, 1, '存在泄密行为', '产品经理', 26.00, '2025-11-26 12:20:12', '2025-11-26 13:14:02', 0, 341422268628860928, 2);
INSERT INTO `employee_profile` VALUES (351157758940798976, 351129930824167424, 344582708126408704, '2025-11-07', NULL, NULL, NULL, 0, NULL, '财务', NULL, '2025-11-26 13:16:33', '2025-11-26 13:16:33', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351158098259992576, 350947173493932032, 344582708126408704, '2025-10-31', '2025-11-27', NULL, 100.00, 0, '主动辞职', '产品经理', 14.00, '2025-11-26 13:17:54', '2025-11-27 21:45:39', 0, 350528138855968768, 2);
INSERT INTO `employee_profile` VALUES (351158538640846848, 351129930824167424, 344582708126408704, '2025-10-30', NULL, NULL, NULL, 0, NULL, '采购', NULL, '2025-11-26 13:19:39', '2025-11-26 13:34:07', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351162224888803328, 351129930824167424, 344582708126408704, '2025-10-30', NULL, NULL, NULL, 0, NULL, '采购', NULL, '2025-11-26 13:34:18', '2025-11-26 13:34:18', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351162370921885696, 351129930824167424, 344582708126408704, '2023-06-21', '2025-01-09', '良好', 100.00, 0, '升职', '后端开发工程师', 18.00, '2025-11-26 13:34:53', '2025-11-26 13:34:53', 0, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351162605790326784, 351129930824167424, 344582708126408704, '2025-10-30', NULL, NULL, NULL, 0, NULL, '采购', NULL, '2025-11-26 13:35:49', '2025-11-26 13:35:49', 1, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351526518780342272, 351238108501110784, 344582708126408704, '2025-03-27', '2025-11-27', '尚可', 98.00, 0, '自主离职', '前端开发工程师', 9.00, '2025-11-27 13:41:53', '2025-11-27 13:41:53', 0, 344581859291447296, 2);
INSERT INTO `employee_profile` VALUES (351527072151646208, 351238108501110784, 344592263740768256, '2025-11-27', NULL, NULL, NULL, 0, NULL, '前端开发工程师', 9.50, '2025-11-27 13:44:05', '2025-11-27 13:44:05', 0, 350550633466048512, 2);
INSERT INTO `employee_profile` VALUES (352592667408441344, 349446284006400000, 344582708126408704, '2025-02-28', '2025-11-30', '优秀', 99.00, 0, '部门优化', '出纳', 8.00, '2025-11-30 12:18:22', '2025-12-01 16:40:46', 0, 350528138855968768, 0);
INSERT INTO `employee_profile` VALUES (353025842320252928, 349446284006400000, 344592263740768256, '2025-09-01', '2025-12-27', NULL, 99.00, 0, NULL, '运维', 8.00, '2025-12-01 16:59:39', '2025-12-01 16:59:39', 0, 350550633466048512, 2);
INSERT INTO `employee_profile` VALUES (353025842320252929, 349446284006400000, 344582708126408704, '2024-02-28', '2025-02-27', NULL, NULL, 0, '职位调动', '财务', 8.00, '2025-12-01 17:02:03', '2025-12-01 17:02:03', 0, 350528138855968768, 2);

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '被评价员工ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '评价时员工所属公司ID',
  `evaluator_id` bigint NOT NULL COMMENT '评价人',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `evaluation_date` date NOT NULL COMMENT '评价日期',
  `evaluation_type` tinyint NOT NULL DEFAULT 1 COMMENT '评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）',
  `evaluation_period` tinyint NULL DEFAULT NULL COMMENT '评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）',
  `period_year` int NULL DEFAULT NULL COMMENT '评价年份（如2024）',
  `period_quarter` tinyint NULL DEFAULT NULL COMMENT '评价季度（1-4，仅季度评价时有效）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_evaluation_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_evaluation_type`(`evaluation_type` ASC) USING BTREE,
  INDEX `idx_evaluation_period`(`evaluation_period` ASC, `period_year` ASC, `period_quarter` ASC) USING BTREE,
  INDEX `idx_evaluator_employee`(`evaluator_id` ASC, `employee_id` ASC) USING BTREE,
  INDEX `idx_employee_date`(`employee_id` ASC, `evaluation_date` ASC) USING BTREE,
  INDEX `idx_evaluator`(`evaluator_id` ASC) USING BTREE,
  INDEX `idx_evaluation_company`(`company_id` ASC) USING BTREE,
  CONSTRAINT `fk_evaluation_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 353711604833570817 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工评价记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation
-- ----------------------------
INSERT INTO `evaluation` VALUES (351682063856418816, 350947173493932032, 344582708126408704, 349419495334993920, '挺不错的', '2025-11-27', 1, 3, 2025, NULL, '2025-11-28 14:09:09', '2025-11-28 18:57:06', 0);
INSERT INTO `evaluation` VALUES (351958880668368896, 350947173493932032, 344582708126408704, 350528138855968768, '还不错', '2025-11-28', 3, 3, 2025, NULL, NULL, '2025-11-28 18:57:06', 0);
INSERT INTO `evaluation` VALUES (352342988980563968, 350947173493932032, 344582708126408704, 349446283922513920, '对待同事间很友善', '2025-11-29', 2, 3, 2025, NULL, NULL, '2025-11-30 17:54:28', 1);
INSERT INTO `evaluation` VALUES (352605283602243584, 349776742455980032, 344582708126408704, 350528138855968768, '善于与他人合作，创新能力强，责任心强，抗压能力需提升\n', '2025-11-30', 3, 3, 2025, NULL, NULL, '2025-11-30 13:17:45', 0);
INSERT INTO `evaluation` VALUES (352607790537682944, 349446284006400000, 344582708126408704, 350528138855968768, '表现一般', '2025-11-30', 3, 3, 2025, NULL, NULL, '2025-12-02 18:46:47', 1);
INSERT INTO `evaluation` VALUES (352608223574405120, 349446284006400000, 344582708126408704, 349419495334993920, '需要提升一下专业技能', '2025-11-30', 1, 3, 2025, NULL, NULL, NULL, 0);
INSERT INTO `evaluation` VALUES (353711604833570816, 350947173493932032, 344582708126408704, 350528138855968768, '人还不错，但是专业技能可以再提升', '2025-12-03', 3, 3, 2025, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for evaluation_dimension
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_dimension`;
CREATE TABLE `evaluation_dimension`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维度名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维度描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价维度配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_dimension
-- ----------------------------
INSERT INTO `evaluation_dimension` VALUES (1, '工作业绩', '员工工作成果和业绩表现', 1, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_dimension` VALUES (2, '责任心', '员工工作责任心和主动性', 2, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_dimension` VALUES (3, '专业技能', '员工专业能力和技术水平', 3, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_dimension` VALUES (4, '团队协作', '员工团队合作和沟通能力', 4, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_dimension` VALUES (5, '创新能力', '员工创新思维和解决问题能力', 5, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);

-- ----------------------------
-- Table structure for evaluation_dimension_score
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_dimension_score`;
CREATE TABLE `evaluation_dimension_score`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `dimension_id` bigint NOT NULL COMMENT '维度ID',
  `score` tinyint NOT NULL COMMENT '评分（1-5分）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_evaluation_dimension`(`evaluation_id` ASC, `dimension_id` ASC) USING BTREE,
  INDEX `idx_dimension_score_evaluation`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_dimension_score_dimension`(`dimension_id` ASC) USING BTREE,
  CONSTRAINT `chk_score_range` CHECK (`score` between 1 and 5)
) ENGINE = InnoDB AUTO_INCREMENT = 353711605122977793 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维度评分记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_dimension_score
-- ----------------------------
INSERT INTO `evaluation_dimension_score` VALUES (351682064162603008, 351682063856418816, 1, 4, NULL, '2025-11-28 00:12:14');
INSERT INTO `evaluation_dimension_score` VALUES (351682064208740352, 351682063856418816, 2, 4, NULL, '2025-11-28 00:12:14');
INSERT INTO `evaluation_dimension_score` VALUES (351682064225517568, 351682063856418816, 3, 4, NULL, '2025-11-28 00:12:14');
INSERT INTO `evaluation_dimension_score` VALUES (351682064238100480, 351682063856418816, 4, 3, NULL, '2025-11-28 00:12:14');
INSERT INTO `evaluation_dimension_score` VALUES (351682064250683392, 351682063856418816, 5, 3, NULL, '2025-11-28 00:12:14');
INSERT INTO `evaluation_dimension_score` VALUES (351958880987136000, 351958880668368896, 1, 4, NULL, '2025-11-28 18:19:56');
INSERT INTO `evaluation_dimension_score` VALUES (351958881012301824, 351958880668368896, 2, 3, NULL, '2025-11-28 18:19:56');
INSERT INTO `evaluation_dimension_score` VALUES (351958881016496128, 351958880668368896, 3, 4, NULL, '2025-11-28 18:19:56');
INSERT INTO `evaluation_dimension_score` VALUES (351958881041661952, 351958880668368896, 4, 4, NULL, '2025-11-28 18:19:56');
INSERT INTO `evaluation_dimension_score` VALUES (351958881050050560, 351958880668368896, 5, 4, NULL, '2025-11-28 18:19:56');
INSERT INTO `evaluation_dimension_score` VALUES (352342989282553856, 352342988980563968, 1, 3, NULL, '2025-11-29 19:46:14');
INSERT INTO `evaluation_dimension_score` VALUES (352342989299331072, 352342988980563968, 2, 4, NULL, '2025-11-29 19:46:14');
INSERT INTO `evaluation_dimension_score` VALUES (352342989311913984, 352342988980563968, 3, 4, NULL, '2025-11-29 19:46:14');
INSERT INTO `evaluation_dimension_score` VALUES (352342989324496896, 352342988980563968, 4, 4, NULL, '2025-11-29 19:46:14');
INSERT INTO `evaluation_dimension_score` VALUES (352342989332885504, 352342988980563968, 5, 3, NULL, '2025-11-29 19:46:14');
INSERT INTO `evaluation_dimension_score` VALUES (352605283774210048, 352605283602243584, 1, 4, NULL, '2025-11-30 13:08:30');
INSERT INTO `evaluation_dimension_score` VALUES (352605283786792960, 352605283602243584, 2, 4, NULL, '2025-11-30 13:08:30');
INSERT INTO `evaluation_dimension_score` VALUES (352605283795181568, 352605283602243584, 3, 3, NULL, '2025-11-30 13:08:30');
INSERT INTO `evaluation_dimension_score` VALUES (352605283807764480, 352605283602243584, 4, 4, NULL, '2025-11-30 13:08:30');
INSERT INTO `evaluation_dimension_score` VALUES (352605283811958784, 352605283602243584, 5, 4, NULL, '2025-11-30 13:08:30');
INSERT INTO `evaluation_dimension_score` VALUES (352607790705455104, 352607790537682944, 1, 3, NULL, '2025-11-30 13:18:28');
INSERT INTO `evaluation_dimension_score` VALUES (352607790722232320, 352607790537682944, 2, 3, NULL, '2025-11-30 13:18:28');
INSERT INTO `evaluation_dimension_score` VALUES (352607790730620928, 352607790537682944, 3, 3, NULL, '2025-11-30 13:18:28');
INSERT INTO `evaluation_dimension_score` VALUES (352607790739009536, 352607790537682944, 4, 3, NULL, '2025-11-30 13:18:28');
INSERT INTO `evaluation_dimension_score` VALUES (352607790747398144, 352607790537682944, 5, 3, NULL, '2025-11-30 13:18:28');
INSERT INTO `evaluation_dimension_score` VALUES (352608223616348160, 352608223574405120, 1, 3, NULL, '2025-11-30 13:20:11');
INSERT INTO `evaluation_dimension_score` VALUES (352608223620542464, 352608223574405120, 2, 3, NULL, '2025-11-30 13:20:11');
INSERT INTO `evaluation_dimension_score` VALUES (352608223628931072, 352608223574405120, 3, 2, NULL, '2025-11-30 13:20:11');
INSERT INTO `evaluation_dimension_score` VALUES (352608223633125376, 352608223574405120, 4, 4, NULL, '2025-11-30 13:20:11');
INSERT INTO `evaluation_dimension_score` VALUES (352608223637319680, 352608223574405120, 5, 3, NULL, '2025-11-30 13:20:11');
INSERT INTO `evaluation_dimension_score` VALUES (353711605068451840, 353711604833570816, 1, 4, NULL, '2025-12-03 14:24:38');
INSERT INTO `evaluation_dimension_score` VALUES (353711605093617664, 353711604833570816, 2, 4, NULL, '2025-12-03 14:24:38');
INSERT INTO `evaluation_dimension_score` VALUES (353711605102006272, 353711604833570816, 3, 3, NULL, '2025-12-03 14:24:38');
INSERT INTO `evaluation_dimension_score` VALUES (353711605110394880, 353711604833570816, 4, 4, NULL, '2025-12-03 14:24:38');
INSERT INTO `evaluation_dimension_score` VALUES (353711605122977792, 353711604833570816, 5, 4, NULL, '2025-12-03 14:24:38');

-- ----------------------------
-- Table structure for evaluation_tag
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_tag`;
CREATE TABLE `evaluation_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `type` tinyint NOT NULL COMMENT '标签类型（1=正面，2=中性）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价标签库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_tag
-- ----------------------------
INSERT INTO `evaluation_tag` VALUES (1, '团队协作', 1, '善于与团队成员合作', 1, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (2, '创新能力', 1, '具有创新思维和解决问题的能力', 2, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (3, '责任心强', 1, '工作认真负责，积极主动', 3, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (4, '专业技能突出', 1, '专业能力优秀', 4, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (5, '沟通能力强', 1, '善于沟通表达', 5, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (6, '学习能力强', 1, '快速学习新知识', 6, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (7, '执行力强', 1, '高效执行任务', 7, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (8, '抗压能力强', 1, '能够承受工作压力', 8, 1, '2025-11-27 00:24:53', '2025-11-27 00:24:53', 0);
INSERT INTO `evaluation_tag` VALUES (9, '需提升沟通技巧', 2, '沟通能力有待提升', 5, 1, '2025-11-27 00:24:53', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (10, '需加强团队协作', 2, '团队协作能力需要加强', 1, 1, '2025-11-27 00:24:53', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (11, '专业技能需提升', 2, '专业技能需要进一步提升', 4, 1, '2025-11-27 00:24:53', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (14, '创新能力需提升', 2, '创新能力需要进一步提升', 2, 1, '2025-11-28 23:07:21', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (15, '责任心待加强', 2, '责任心需要加强', 3, 1, '2025-11-28 23:07:21', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (16, '学习能力需提升', 2, '学习能力需要进一步提升', 6, 1, '2025-11-28 23:07:21', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (17, '执行力需提升', 2, '执行力需要进一步提升', 7, 1, '2025-11-28 23:07:21', '2025-11-28 23:07:21', 0);
INSERT INTO `evaluation_tag` VALUES (18, '抗压能力需提升', 2, '抗压能力需要进一步提升', 8, 1, '2025-11-28 23:07:21', '2025-11-28 23:07:21', 0);

-- ----------------------------
-- Table structure for evaluation_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_tag_relation`;
CREATE TABLE `evaluation_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_id` bigint NOT NULL COMMENT '评价ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0=未删除，1=已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_evaluation_tag`(`evaluation_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_relation_evaluation`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_tag_relation_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 353711605164920833 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_tag_relation
-- ----------------------------
INSERT INTO `evaluation_tag_relation` VALUES (351682064263266304, 351682063856418816, 3, NULL, '2025-11-28 00:12:14', 0);
INSERT INTO `evaluation_tag_relation` VALUES (351682064296820736, 351682063856418816, 10, NULL, '2025-11-28 00:12:14', 0);
INSERT INTO `evaluation_tag_relation` VALUES (351682064309403648, 351682063856418816, 5, NULL, '2025-11-28 00:12:14', 0);
INSERT INTO `evaluation_tag_relation` VALUES (351958881058439168, 351958880668368896, 1, NULL, '2025-11-28 18:19:56', 0);
INSERT INTO `evaluation_tag_relation` VALUES (351958881071022080, 351958880668368896, 3, NULL, '2025-11-28 18:19:56', 0);
INSERT INTO `evaluation_tag_relation` VALUES (351958881083604992, 351958880668368896, 5, NULL, '2025-11-28 18:19:56', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352342989341274112, 352342988980563968, 14, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352342989349662720, 352342988980563968, 3, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352342989358051328, 352342988980563968, 1, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352342989362245632, 352342988980563968, 9, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352342989370634240, 352342988980563968, 4, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352342989374828544, 352342988980563968, 16, NULL, '2025-11-29 19:46:14', 1);
INSERT INTO `evaluation_tag_relation` VALUES (352605283824541696, 352605283602243584, 2, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283837124608, 352605283602243584, 1, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283845513216, 352605283602243584, 11, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283849707520, 352605283602243584, 18, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283858096128, 352605283602243584, 6, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283862290432, 352605283602243584, 5, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352605283874873344, 352605283602243584, 7, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352607790764175360, 352607790537682944, 9, NULL, '2025-11-30 13:18:28', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352607790776758272, 352607790537682944, 16, NULL, '2025-11-30 13:18:28', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352607790785146880, 352607790537682944, 11, NULL, '2025-11-30 13:18:28', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352608223649902592, 352608223574405120, 11, NULL, '2025-11-30 13:20:11', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352608223658291200, 352608223574405120, 1, NULL, '2025-11-30 13:20:11', 0);
INSERT INTO `evaluation_tag_relation` VALUES (352608223666679808, 352608223574405120, 16, NULL, '2025-11-30 13:20:11', 0);
INSERT INTO `evaluation_tag_relation` VALUES (353711605131366400, 353711604833570816, 1, NULL, NULL, 0);
INSERT INTO `evaluation_tag_relation` VALUES (353711605143949312, 353711604833570816, 5, NULL, NULL, 0);
INSERT INTO `evaluation_tag_relation` VALUES (353711605160726528, 353711604833570816, 11, NULL, NULL, 0);
INSERT INTO `evaluation_tag_relation` VALUES (353711605164920832, 353711604833570816, 3, NULL, NULL, 0);

-- ----------------------------
-- Table structure for evaluation_task
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_task`;
CREATE TABLE `evaluation_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '被评价员工ID',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID（创建任务时保存，避免员工离职后无法查询）',
  `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称（创建任务时保存）',
  `evaluator_id` bigint NOT NULL COMMENT '评价人ID（HR或同事的userId）',
  `evaluation_type` tinyint NOT NULL COMMENT '评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）',
  `evaluation_period` tinyint NOT NULL COMMENT '评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）',
  `period_year` int NULL DEFAULT NULL COMMENT '评价年份（如2024）',
  `period_quarter` tinyint NULL DEFAULT NULL COMMENT '评价季度（1-4，仅季度评价时有效）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态（0=待评价，1=已完成，2=已过期）',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `evaluation_id` bigint NULL DEFAULT NULL COMMENT '关联的评价ID（完成后关联）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_task_evaluator`(`evaluator_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`status` ASC) USING BTREE,
  INDEX `idx_task_period`(`evaluation_period` ASC, `period_year` ASC, `period_quarter` ASC) USING BTREE,
  INDEX `idx_task_evaluation`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_task_deadline`(`deadline` ASC) USING BTREE,
  INDEX `idx_task_evaluator_status`(`evaluator_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_task_department`(`department_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_evaluation` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 353336401163030529 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of evaluation_task
-- ----------------------------
INSERT INTO `evaluation_task` VALUES (351648535651340288, 350947173493932032, 349415061020217344, '后勤部（测试）', 350528138855968768, 3, 3, 2025, NULL, 1, '2025-12-04 21:46:44', 351958880668368896, NULL, '2025-11-27 23:46:14', 0);
INSERT INTO `evaluation_task` VALUES (351648535944941568, 350947173493932032, 349415061020217344, '后勤部（测试）', 349419495334993920, 1, 3, 2025, NULL, 1, '2025-12-04 21:46:44', 351682063856418816, NULL, '2025-11-27 23:46:14', 0);
INSERT INTO `evaluation_task` VALUES (351648536100130816, 350947173493932032, 349415061020217344, '后勤部（测试）', 349446283922513920, 2, 3, 2025, NULL, 1, '2025-12-04 21:46:44', 352342988980563968, NULL, '2025-11-27 23:46:14', 0);
INSERT INTO `evaluation_task` VALUES (351648536158851072, 350947173493932032, 349415061020217344, '后勤部（测试）', 349776742242070528, 2, 3, 2025, NULL, 0, '2025-12-04 21:46:44', NULL, NULL, '2025-11-27 23:46:14', 0);
INSERT INTO `evaluation_task` VALUES (352591980859596800, 349776742455980032, 349415061020217344, '后勤部（测试）', 350528138855968768, 3, 3, 2025, NULL, 1, '2025-12-07 12:15:39', 352605283602243584, NULL, '2025-11-30 13:08:30', 0);
INSERT INTO `evaluation_task` VALUES (352591980989620224, 349776742455980032, 349415061020217344, '后勤部（测试）', 349419495334993920, 1, 3, 2025, NULL, 0, '2025-12-07 12:15:39', NULL, NULL, NULL, 0);
INSERT INTO `evaluation_task` VALUES (352591981044146176, 349776742455980032, 349415061020217344, '后勤部（测试）', 349446283922513920, 2, 3, 2025, NULL, 0, '2025-12-07 12:15:39', NULL, NULL, NULL, 0);
INSERT INTO `evaluation_task` VALUES (352592678888251392, 349446284006400000, 349415061020217344, '后勤部（测试）', 350528138855968768, 3, 3, 2025, NULL, 1, '2025-12-07 12:18:25', 352607790537682944, NULL, '2025-11-30 13:18:28', 0);
INSERT INTO `evaluation_task` VALUES (352592678921805824, 349446284006400000, 349415061020217344, '后勤部（测试）', 349419495334993920, 1, 3, 2025, NULL, 1, '2025-12-07 12:18:26', 352608223574405120, NULL, '2025-11-30 13:20:11', 0);
INSERT INTO `evaluation_task` VALUES (353336401053978624, 350947173493932032, 350953646399717376, '技术部', 350528138855968768, 3, 3, 2025, NULL, 1, '2025-12-09 13:33:43', 353711604833570816, NULL, '2025-12-03 14:24:38', 0);
INSERT INTO `evaluation_task` VALUES (353336401163030528, 350947173493932032, 350953646399717376, '技术部', 351129930673172480, 2, 3, 2025, NULL, 0, '2025-12-09 13:33:43', NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for evaluation_template
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_template`;
CREATE TABLE `evaluation_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板描述',
  `company_id` bigint NULL DEFAULT NULL COMMENT '所属企业ID（NULL表示系统模板）',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容（JSON格式）',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统模板',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_company`(`company_id` ASC) USING BTREE,
  INDEX `idx_system`(`is_system` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_template
-- ----------------------------

-- ----------------------------
-- Table structure for evaluation_unlock
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_unlock`;
CREATE TABLE `evaluation_unlock`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '解锁企业ID',
  `employee_id` bigint NOT NULL COMMENT '被查看的员工ID',
  `evaluation_id` bigint NOT NULL COMMENT '被解锁的评价ID',
  `evaluation_type` tinyint NOT NULL COMMENT '评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）',
  `points_cost` decimal(12, 2) NOT NULL COMMENT '消耗的积分',
  `unlock_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_evaluation`(`company_id` ASC, `evaluation_id` ASC) USING BTREE COMMENT '同一企业对同一评价只能解锁一次',
  INDEX `idx_company_employee`(`company_id` ASC, `employee_id` ASC) USING BTREE,
  INDEX `idx_evaluation`(`evaluation_id` ASC) USING BTREE,
  INDEX `idx_unlock_time`(`unlock_time` ASC) USING BTREE,
  INDEX `fk_unlock_employee`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `fk_unlock_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_unlock_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_unlock_evaluation` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价解锁记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of evaluation_unlock
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` tinyint NOT NULL COMMENT '通知类型（1=评价任务，2=查阅请求，3=系统通知，4=投诉处理）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（如评价任务ID、请求ID等）',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态（0=未读，1=已读，2=已处理）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 359622619351023617 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (353097497038147584, 350528138855968768, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-08', 353097496799072256, 1, '2025-12-01 21:44:23', '2025-12-01 22:02:21', 1);
INSERT INTO `notification` VALUES (353307569378836480, 351129930673172480, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353307569248813056, 1, '2025-12-02 11:39:08', '2025-12-02 11:39:08', 0);
INSERT INTO `notification` VALUES (353309050039451648, 350528138855968768, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353309050026868736, 1, '2025-12-02 11:45:01', '2025-12-02 11:48:54', 1);
INSERT INTO `notification` VALUES (353309050098171904, 351129930673172480, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353309050089783296, 1, '2025-12-02 11:45:01', '2025-12-02 11:45:01', 0);
INSERT INTO `notification` VALUES (353311098139066368, 350528138855968768, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353311098109706240, 1, '2025-12-02 11:53:10', '2025-12-02 11:53:10', 0);
INSERT INTO `notification` VALUES (353311098189398016, 351129930673172480, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353311098181009408, 0, '2025-12-02 11:53:10', '2025-12-02 11:53:10', 0);
INSERT INTO `notification` VALUES (353333442303856640, 350528138855968768, 3, '测试', '测试通知', NULL, 1, '2025-12-02 13:21:57', '2025-12-02 13:21:57', 0);
INSERT INTO `notification` VALUES (353336401116893184, 350528138855968768, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353336401053978624, 1, '2025-12-02 13:33:42', '2025-12-02 13:33:42', 0);
INSERT INTO `notification` VALUES (353336401200779264, 351129930673172480, 1, '评价任务提醒', '你有新的【离职评价】评价任务，需要对员工【杨过】进行评价，截止日期：2025-12-09', 353336401163030528, 0, '2025-12-02 13:33:42', '2025-12-02 13:33:42', 0);
INSERT INTO `notification` VALUES (353401982046236672, 341422268628860928, 4, '新的投诉待处理', '员工对评价ID为352607790537682944的评价进行了投诉，请及时处理', 353401980586618880, 1, '2025-12-02 17:54:18', '2025-12-02 17:54:18', 0);
INSERT INTO `notification` VALUES (353405052771631104, 341422268628860928, 4, '新的投诉待处理', '员工对评价ID为352607790537682944的评价进行了投诉，请及时处理', 353405052624830464, 1, '2025-12-02 18:06:30', '2025-12-02 18:06:30', 0);
INSERT INTO `notification` VALUES (353413056023879680, 349446283922513920, 4, '投诉处理结果', '您对公司1：赵武的投诉已被驳回。驳回原因：无', NULL, 1, '2025-12-02 18:38:18', '2025-12-02 18:38:18', 0);
INSERT INTO `notification` VALUES (353413312996302848, 341422268628860928, 4, '新的投诉待处理', '投诉人：赵武，被投诉公司：公司1，被投诉人：赵武', 353413312966942720, 1, '2025-12-02 18:39:19', '2025-12-02 18:39:19', 0);
INSERT INTO `notification` VALUES (353415188617748480, 349446283922513920, 4, '投诉处理结果', '您对公司1：赵武的投诉已通过处理，该评价已被删除', NULL, 1, '2025-12-02 18:46:47', '2025-12-02 18:46:47', 0);
INSERT INTO `notification` VALUES (353415188638720000, 350528138855968768, 4, '评价投诉处理通知', '您对赵武的评价因投诉已被删除，投诉人：赵武', NULL, 1, '2025-12-02 18:46:47', '2025-12-02 18:46:47', 0);
INSERT INTO `notification` VALUES (353415188655497216, 344581859291447296, 4, '评价投诉处理通知', '您公司的员工评价因投诉已被删除，被投诉人：赵武，投诉人：赵武', NULL, 1, '2025-12-02 18:46:47', '2025-12-02 18:46:47', 0);
INSERT INTO `notification` VALUES (353697749180882944, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话】，原因：是由有兴趣详聊', 353697749000527872, 1, '2025-12-03 13:29:34', '2025-12-03 14:02:27', 1);
INSERT INTO `notification` VALUES (353697749180882945, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看邮箱】，原因：是由有兴趣详聊', 353697749000527873, 1, '2025-12-03 13:29:34', '2025-12-03 14:02:27', 1);
INSERT INTO `notification` VALUES (353697993461342208, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看邮箱】，原因：111', 353697993436176385, 1, '2025-12-03 13:30:33', '2025-12-03 14:02:27', 1);
INSERT INTO `notification` VALUES (353697993461342209, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话】，原因：111', 353697993436176384, 1, '2025-12-03 13:30:33', '2025-12-03 14:02:25', 1);
INSERT INTO `notification` VALUES (353706078410153984, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话和邮箱】，原因：111', 353706078292713472, 1, '2025-12-03 14:02:40', '2025-12-03 14:19:40', 1);
INSERT INTO `notification` VALUES (353710505548357632, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话和邮箱】，原因：222', 353710505485443072, 1, '2025-12-03 14:20:16', '2025-12-03 14:20:16', 0);
INSERT INTO `notification` VALUES (353711766788231168, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话和邮箱】，原因：333', 353711766733705216, 1, '2025-12-03 14:25:16', '2025-12-03 14:25:16', 0);
INSERT INTO `notification` VALUES (353714696920616960, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司1】申请查看你的【查看电话和邮箱】，原因：444', 353714696807370752, 1, '2025-12-03 14:36:55', '2025-12-03 14:36:55', 0);
INSERT INTO `notification` VALUES (353716478505766912, 344581859291447296, 2, '联系方式查看请求结果通知', '你发起的查看员工【杨过】【查看电话和邮箱】的请求已同意。\n员工ID：350947173493932032', 350947173493932032, 1, '2025-12-03 14:44:00', '2025-12-03 14:44:00', 0);
INSERT INTO `notification` VALUES (353716478556098560, 350528138855968768, 2, '联系方式查看请求结果通知', '你发起的查看员工【杨过】【查看电话和邮箱】的请求已同意。\n员工ID：350947173493932032', 350947173493932032, 1, '2025-12-03 14:44:00', '2025-12-03 14:44:00', 0);
INSERT INTO `notification` VALUES (353729101683081216, 350947173338742784, 2, '联系方式查看请求通知', '企业【公司2】申请查看你的【查看电话和邮箱】，原因：999，授权天数：6天', 353729101611778048, 0, '2025-12-03 15:34:09', '2025-12-03 15:34:09', 0);
INSERT INTO `notification` VALUES (359613135266275328, 341422268628860928, 3, '新的企业注册申请待处理', '企业名称：腾讯公司，联系人：马化腾（电话：16738502811，邮箱：2774680379@qq.com）', NULL, 1, '2025-12-19 21:15:12', '2025-12-19 21:15:12', 0);
INSERT INTO `notification` VALUES (359622619351023616, 341422268628860928, 5, '新的企业注册申请待处理', '企业名称：组啊企业，联系人：马化腾（电话：16738502811，邮箱：2774680379@qq.com）', 359622619233583104, 1, '2025-12-19 21:52:53', '2025-12-19 21:52:53', 0);

-- ----------------------------
-- Table structure for profile_access_log
-- ----------------------------
DROP TABLE IF EXISTS `profile_access_log`;
CREATE TABLE `profile_access_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` bigint NOT NULL COMMENT '请求ID',
  `access_company_id` bigint NOT NULL COMMENT '查阅企业ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_profile_id` bigint NOT NULL COMMENT '被查阅的档案ID',
  `access_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查阅时间',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_log_request`(`request_id` ASC) USING BTREE,
  INDEX `idx_log_company`(`access_company_id` ASC) USING BTREE,
  INDEX `idx_log_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_log_profile`(`employee_profile_id` ASC) USING BTREE,
  INDEX `idx_access_time`(`access_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '档案查阅记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of profile_access_log
-- ----------------------------

-- ----------------------------
-- Table structure for profile_access_request
-- ----------------------------
DROP TABLE IF EXISTS `profile_access_request`;
CREATE TABLE `profile_access_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_company_id` bigint NOT NULL COMMENT '请求企业ID',
  `request_user_id` bigint NULL DEFAULT NULL COMMENT '请求用户ID（创建请求的用户）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_profile_id` bigint NULL DEFAULT NULL COMMENT '请求查阅的档案ID（可选，为空表示查阅所有档案）',
  `request_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0=待处理，1=已授权，2=已拒绝，3=已过期）',
  `request_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '授权过期时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_company`(`request_company_id` ASC) USING BTREE,
  INDEX `idx_request_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_request_profile`(`employee_profile_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  CONSTRAINT `chk_expire_time` CHECK ((`expire_time` is null) or (`expire_time` > `request_time`))
) ENGINE = InnoDB AUTO_INCREMENT = 353012772575379457 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '档案查阅请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of profile_access_request
-- ----------------------------
INSERT INTO `profile_access_request` VALUES (353012772575379456, 344592263740768256, NULL, 349419495393714176, 349723123581018112, '11', 0, '2025-12-01 20:19:26', NULL, '2025-12-03 15:57:47', '2025-12-01 16:07:43', '2025-12-01 20:19:47', 0);

-- ----------------------------
-- Table structure for reward_punishment
-- ----------------------------
DROP TABLE IF EXISTS `reward_punishment`;
CREATE TABLE `reward_punishment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 351534131412729857 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '奖惩记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reward_punishment
-- ----------------------------
INSERT INTO `reward_punishment` VALUES (350451214160039936, 349419495393714176, 344582708126408704, 1, '业绩优秀，奖励600', 600.00, '2025-11-24', '2025-11-24 14:29:00', '2025-11-27 01:01:17', 0, 344581859291447296);
INSERT INTO `reward_punishment` VALUES (351509633913450496, 351238108501110784, 344582708126408704, 2, '迟到扣款', 50.00, '2025-11-27', '2025-11-27 12:34:47', '2025-11-27 12:42:13', 1, 344581859291447296);
INSERT INTO `reward_punishment` VALUES (351509841334366208, 349419495393714176, 344582708126408704, 2, '迟到扣款', 50.00, '2025-11-26', '2025-11-27 12:35:36', '2025-11-27 12:35:36', 0, 344581859291447296);
INSERT INTO `reward_punishment` VALUES (351526079284391936, 351238108501110784, 344582708126408704, 2, '迟到罚款50', 50.00, '2025-11-27', '2025-11-27 13:40:08', '2025-11-27 14:14:13', 0, 344581859291447296);
INSERT INTO `reward_punishment` VALUES (351534131412729856, 351238108501110784, 344592263740768256, 1, '完成任务绩效奖励', 300.00, '2025-11-27', '2025-11-27 14:12:08', '2025-11-27 14:12:08', 0, 350550633466048512);

-- ----------------------------
-- Table structure for talent_bookmark
-- ----------------------------
DROP TABLE IF EXISTS `talent_bookmark`;
CREATE TABLE `talent_bookmark`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '收藏企业ID',
  `employee_id` bigint NOT NULL COMMENT '被收藏的员工ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_employee`(`company_id` ASC, `employee_id` ASC) USING BTREE COMMENT '同一企业对同一员工只能收藏一次',
  INDEX `idx_company`(`company_id` ASC) USING BTREE,
  INDEX `idx_employee`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `fk_bookmark_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_bookmark_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 354526493428711425 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人才收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of talent_bookmark
-- ----------------------------
INSERT INTO `talent_bookmark` VALUES (354526493428711424, 344582708126408704, 349776742455980032, NULL, NULL, '2025-12-05 20:27:39', 0);

-- ----------------------------
-- Table structure for talent_compare_record
-- ----------------------------
DROP TABLE IF EXISTS `talent_compare_record`;
CREATE TABLE `talent_compare_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '对比企业ID',
  `employee_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对比员工ID列表（JSON数组格式）',
  `compare_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '对比结果（JSON格式，包含完整的对比数据）',
  `ai_analysis_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI分析结果（JSON格式字符串）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_compare_record_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 354532830879449089 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人才对比记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of talent_compare_record
-- ----------------------------
INSERT INTO `talent_compare_record` VALUES (353849883763503104, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764827271053\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和杨过。两人均处于离职状态，但工作年限、经历和评价信息存在差异。王柳有1年财务工作经验，综合评分3.80分，评价信息较少但维度评分较高，尤其在团队协作和创新能力方面表现突出，但专业技能和抗压能力被指出需提升。杨过工作年限为0年，但有两段短期工作经历，综合评分3.73分，评价信息更丰富，显示其在责任心、沟通能力方面获得认可，但团队协作和专业技能的稳定性有待观察，且其中一段离职原因为“公司缩编”。从公司招聘偏好（Java、前端）来看，两人均无相关职位经验，匹配度较低。两人均无重大违纪记录，符合公司排除要求。整体而言，王柳的工作表现更稳定、评价更聚焦，而杨过的工作经历略显复杂且稳定性存疑。\",\n        \"strengths\": {\n            \"王柳\": [\"工作表现稳定，出勤率达98%\", \"团队协作能力强，评价中明确提及\", \"创新能力获得认可，维度评分4分\", \"责任心强，维度评分4分\", \"学习能力和执行力获得正面标签\"],\n            \"杨过\": [\"沟通能力强，多次获得正面评价\", \"责任心获得多次认可\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好，平均97%\", \"评价信息相对更丰富，来自HR和领导\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、前端职位完全不匹配\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、前端职位完全不匹配\", \"一段离职原因为“公司缩编”，可能涉及被动离职\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"工作稳定性与经历\",\n                \"comparison\": \"王柳有1年连续的工作经历，岗位为财务，离职原因为自主离职，显示出相对稳定的职业选择。杨过则有两段短期工作经历（产品经理和出纳），其中一段离职原因为“公司缩编”，另一段为主动辞职，工作稳定性明显低于王柳，且职业方向（产品、出纳）与之前经历关联度不高，可能缺乏清晰的职业规划。\"\n            },\n            {\n                \"aspect\": \"能力评价与标签\",\n                \"comparison\": \"王柳的评价虽然只有一条，但维度评分较高且一致，优势集中在软技能（团队协作、创新、责任心），弱项明确（专业技能、抗压能力）。杨过的评价更多元（3条），但维度评分存在波动（如团队协作评分在3-4分间），标签也显示出矛盾（既有“团队协作”正面标签，也有“需加强团队协作”中性标签），说明其团队协作表现可能不稳定，专业技能的提升需求也被多次提及。\"\n            },\n            {\n                \"aspect\": \"与招聘岗位的匹配度\",\n                \"comparison\": \"两人均与公司偏好的Java、前端开发职位严重不匹配。王柳的背景是财务，杨过的背景是产品经理和出纳，均无技术开发相关经验或技能展示。从职位匹配角度看，两人都不符合核心招聘需求。如果公司考虑非技术岗位，则需重新评估岗位需求。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 65,\n                \"reason\": \"王柳在工作态度、团队协作和责任心方面表现突出，出勤率高，离职原因明确为自主离职，职业记录清晰。其学习能力和执行力标签表明有成长潜力。然而，最大的短板是与目标职位（Java/前端）零匹配，且专业技能被明确标记为需提升。如果没有相关技术背景或强烈的转行意愿与学习计划，将其招聘至技术岗位风险极高。评分主要基于其通用职业素质，而非岗位适配性。\",\n                \"suitability\": \"不适合Java或前端开发岗位。如果公司有财务、行政或需要强沟通协作、创新能力的非技术类岗位，可予以考虑。\",\n                \"riskPoints\": [\"专业技能与目标岗位完全不匹配，培训成本极高\", \"抗压能力被指出需提升，可能不适应高强度技术开发环境\", \"缺乏技术岗位所需的基础知识和经验\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 60,\n                \"reason\": \"杨过在沟通能力和责任心方面获得较多正面评价，且评价来源更丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/前端开发职位毫无关联，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性不如王柳。\",\n                \"suitability\": \"不适合Java或前端开发岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司当前的招聘偏好（Java、前端），候选人王柳和杨过均不具备相关的专业背景、工作经历或技能证明，因此都不推荐录用至目标技术岗位。强行录用将带来极高的培训成本、用人风险和岗位不匹配风险。建议：1. 重新审视招聘需求，如果确需Java/前端人才，应继续寻找具有相关技术背景和经验的候选人。2. 如果公司同时有其他非技术类岗位空缺（如财务、产品助理、行政等），可以分别考虑王柳和杨过。王柳更适合对稳定性、团队协作和责任心要求高的岗位；杨过则可考虑对沟通能力要求高、但能接受一定经验波动的初级岗位，但需在面试中重点考察其离职原因和职业规划。在当前设定下，不建议为技术岗位录用其中任何一人。\"\n    }\n}\n```', '2025-12-03 23:34:06', '2025-12-03 23:34:06', 0);
INSERT INTO `talent_compare_record` VALUES (353857578046324736, 344582708126408704, '[349446284006400000,349776742455980032]', '{\"items\":[{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]},{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]}],\"dimensionRadarData\":{\"工作业绩\":[3,4],\"责任心\":[3,4],\"专业技能\":[2,3],\"团队协作\":[4,4],\"创新能力\":[3,4]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1391756095_1764827599828\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：赵武和王柳。两人均为女性，工作年限均为1年，但当前状态、工作经历和评价表现存在明显差异。赵武目前在职，有三段工作经历，涉及财务和运维岗位，但综合评分较低（3.00分），且领导评价明确指出其专业技能需提升。王柳目前离职，只有一段财务工作经历，综合评分较高（3.80分），HR评价在团队协作、创新能力、责任心等方面给予积极肯定，但同样指出专业技能和抗压能力需提升。从公司招聘偏好（Java、前端）来看，两人均无相关职位经验，匹配度极低。两人均无重大违纪记录，符合公司排除要求。整体而言，王柳在软技能和工作表现上更受认可，而赵武的工作经历更复杂但评价反馈一般，且专业技能短板明显。\",\n        \"strengths\": {\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"],\n            \"王柳\": [\"综合评分较高（3.80分），整体评价更优\", \"工作业绩、责任心、创新能力维度评分均为4分，表现突出\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"离职原因明确为自主离职，记录清晰\"]\n        },\n        \"weaknesses\": {\n            \"赵武\": [\"综合评分低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面\", \"与公司偏好的Java、前端职位完全不匹配\"],\n            \"王柳\": [\"专业技能被指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"目前处于离职状态\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、前端职位完全不匹配\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"工作表现与评价反馈\",\n                \"comparison\": \"王柳在多个核心维度（工作业绩、责任心、创新能力）上获得4分的高评价，并收获了丰富的正面标签，表明其工作态度、协作精神和潜力得到了HR的充分肯定。赵武则在工作业绩、责任心、专业技能等维度评分均为3分或以下，且评价内容直接指出“需要提升一下专业技能”，反馈明显更为负面。王柳的整体职业形象更为积极。\"\n            },\n            {\n                \"aspect\": \"工作经历与稳定性\",\n                \"comparison\": \"赵武在1年工作年限内拥有三段工作经历，虽然其中一段信息不可见，另一段为内部职位调动，但频繁的岗位变动可能暗示其职业定位尚不清晰或适应过程存在挑战。王柳拥有连续1年的单一财务岗位经历，离职原因为自主离职，职业记录相对简洁清晰，稳定性看似更优。\"\n            },\n            {\n                \"aspect\": \"能力短板与发展潜力\",\n                \"comparison\": \"两人都被指出“专业技能需提升”。赵武的短板更为严重（评分2分），且未显示明确的学习能力优势标签。王柳虽专业技能评分（3分）也需提升，但拥有“学习能力强”的正面标签，暗示其具备更好的学习意愿和成长潜力，能够更快地弥补技能差距。此外，王柳的“抗压能力需提升”是其另一个需要注意的方面。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 68,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出良好的职业素养和发展潜力。其离职原因明确，记录清晰。尽管目前处于离职状态，但综合表现优于赵武。然而，其最大的劣势与赵武相同，即完全不具备Java或前端开发所需的专业技能和经验，招聘至技术岗位风险极高。此评分主要基于其通用职业素质的突出表现。\",\n                \"suitability\": \"不适合Java或前端开发岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术开发环境\", \"缺乏技术背景，转型难度和成本大\"]\n            },\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 58,\n                \"reason\": \"赵武的优势在于目前在职且出勤率极高，显示出良好的纪律性。但其综合评分低，领导评价直接点明专业技能不足，这是硬伤。工作经历在短期内涉及不同岗位，可能反映其职业规划不够清晰或适应能力有待考验。与王柳相比，其在各项能力维度的评价均不占优，且缺乏显示其学习潜力的正面标签。因此，即使对于非技术岗位，其竞争力也相对较弱。\",\n                \"suitability\": \"不适合Java或前端开发岗位。可考虑对专业技能要求不高的初级运维支持或行政辅助类岗位，但需在面试中重点考察其专业技能水平和职业稳定性。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"与目标技术岗位完全不匹配\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、前端），候选人赵武和王柳均无任何相关技术背景、工作经验或技能证明，因此绝对不推荐录用至Java或前端开发岗位。若强行录用，将面临极高的技能不匹配风险、巨大的培训成本和极可能的人员流失。建议：1. 坚持技术岗位招聘方向，继续寻找具有Java或前端专业背景的候选人。2. 如果公司同时存在非技术类岗位（如财务、行政、运维支持等）的招聘需求，可以分别考虑两人。在此情况下，王柳因其更优秀的软技能评价、学习潜力和清晰的职业记录，是比赵武更优先的选择，尤其适合需要团队协作和创新能力的岗位。赵武则需谨慎评估，重点考察其专业技能的真实水平和频繁更换岗位的具体原因。在当前技术岗位的招聘目标下，不建议录用其中任何一人。\"\n    }\n}\n```', '2025-12-04 00:04:40', '2025-12-04 00:04:40', 0);
INSERT INTO `talent_compare_record` VALUES (354069931488751616, 344582708126408704, '[349446284006400000,349776742455980032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]},{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,3],\"责任心\":[4,3],\"专业技能\":[3,2],\"团队协作\":[4,4],\"创新能力\":[4,3]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1391756095_1764828509965\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和赵武。两人均为女性，工作年限均为1年，但当前状态、工作经历和评价表现存在差异。王柳目前离职，有一段财务工作经历，综合评分较高（3.80分），HR评价在团队协作、创新能力、责任心等方面给予积极肯定，但指出专业技能和抗压能力需提升。赵武目前在职，有三段工作经历，涉及财务和运维岗位，综合评分较低（3.00分），领导评价明确指出其专业技能需提升。从公司招聘偏好（Java、运维）来看，赵武有运维岗位经验，与偏好部分匹配，但其专业技能评价很低；王柳则无相关经验，完全不匹配。两人均无重大违纪记录，符合公司排除要求。整体而言，赵武在职位经验上略有优势但能力评价不佳，王柳在软技能和工作表现上更受认可但岗位不匹配。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分较高（3.80分），整体评价更优\", \"工作业绩、责任心、创新能力、团队协作维度评分均为4分，表现突出\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰\"],\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"有运维岗位的工作经历，与公司招聘偏好部分匹配\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"目前处于离职状态\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配\"],\n            \"赵武\": [\"综合评分低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分，短板严重\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面\", \"虽有运维经验，但专业技能评价极低，与“专业技能突出”的偏好标签不符\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"赵武拥有运维岗位的实际工作经验，与公司招聘偏好中的“运维”职位直接相关，这是其关键优势。然而，其专业技能维度评分仅为2分，并被评价明确指出“需要提升一下专业技能”，这与公司“专业技能突出”的偏好标签严重不符。王柳则无任何Java或运维相关经验，完全不匹配，但其专业技能评分（3分）虽需提升，却高于赵武，且拥有“学习能力强”的标签。\"\n            },\n            {\n                \"aspect\": \"工作表现与综合评价\",\n                \"comparison\": \"王柳在多个核心软技能维度（工作业绩、责任心、团队协作、创新能力）上均获得4分的高评价，并收获了丰富的正面标签，表明其工作态度、协作精神和潜力得到了充分肯定。赵武则在工作业绩、责任心、专业技能等维度评分均为3分或以下，评价内容直接点明短板，反馈明显更为负面。王柳的整体职业形象和表现记录明显优于赵武。\"\n            },\n            {\n                \"aspect\": \"工作经历与稳定性\",\n                \"comparison\": \"王柳拥有连续1年的单一财务岗位经历，离职原因为自主离职，职业记录相对简洁清晰。赵武在1年工作年限内拥有三段工作经历，虽然其中一段为内部职位调动，另一段信息不可见，但频繁的岗位变动可能暗示其职业定位尚不清晰、适应过程存在挑战或稳定性较差，这是需要关注的风险点。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 62,\n                \"reason\": \"赵武是目前唯一与公司招聘偏好（运维）有直接关联的候选人，其运维岗位经验是显著优势。同时，其目前在职且出勤率极高，显示出良好的纪律性。然而，其最大的硬伤是专业技能评价极低（2分），这与公司“专业技能突出”的核心偏好严重冲突。领导评价也直接证实了这一点。此外，其工作经历在短期内涉及不同岗位，稳定性存疑。因此，尽管有岗位经验，但能否胜任并达到“专业技能突出”的要求存在巨大疑问。\",\n                \"suitability\": \"可考虑初级运维或运维支持类岗位，但必须对其专业技能进行严格、深入的面试和技术考核，确认其真实水平是否满足岗位基本要求。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项，与偏好严重不符\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"虽有运维经验，但能力评价不高，可能无法独立胜任核心运维工作\"]\n            },\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 58,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出优秀的职业素养和良好的发展潜力。其离职原因明确，记录清晰。然而，其最大的劣势是完全不具备Java或运维所需的专业技能和相关工作经验，与招聘偏好完全不匹配。将其招聘至技术岗位，需要从零开始培养，转型难度和成本极高。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术运维环境\", \"缺乏技术背景，转型难度和成本巨大\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、运维，且偏好“专业技能突出”），两位候选人均未达到理想匹配。赵武虽有运维经验，但专业技能评价极低，与“突出”要求相去甚远，录用风险很高，除非后续技术面试能证明其实际能力远超现有评价。王柳软技能优秀但完全无技术背景，不适合技术岗位。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关经验”和“专业技能突出”的候选人。2. 如果对运维岗位的技能要求可以放宽到“具备基础、有学习潜力”，可给予赵武面试机会，但必须进行严格的技术评估。3. 如果公司同时存在非技术类岗位（如财务、行政等）的招聘需求，王柳是更优先的选择。在当前以技术岗位为核心的招聘目标下，不推荐直接录用赵武，更不推荐录用王柳至技术岗。建议优先寻找更匹配的候选人。\"\n    }\n}\n```', '2025-12-04 14:08:29', '2025-12-04 14:08:29', 0);
INSERT INTO `talent_compare_record` VALUES (354113068181487616, 344582708126408704, '[349446284006400000,349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[\"团队协作有待提升\"]},{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"团队协作表现突出\",\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4,3],\"责任心\":[4,3.67,3],\"专业技能\":[3,3.67,2],\"团队协作\":[4,3.67,4],\"创新能力\":[4,3.67,3]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1943142277_1764838794523\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了三位候选人：王柳、杨过和赵武。三人均无重大违纪记录，符合公司排除要求。从公司招聘偏好（Java、运维，偏好“专业技能突出”）来看，匹配度普遍较低。赵武是唯一拥有运维岗位实际经验的候选人，但其专业技能评价极低（2分），与“突出”要求严重不符。王柳和杨过均无相关技术背景，王柳有财务经验，杨过有产品经理和出纳经验。在工作表现上，王柳的综合评分最高（3.80分），软技能（团队协作、创新能力、责任心）评价突出，但专业技能和抗压能力被指出需提升。杨过评价信息更丰富，沟通能力和责任心获得认可，但工作稳定性存疑（两段短期经历），专业技能也被指出需提升。赵武虽在职且有运维经验，但综合评分最低（3.00分），专业技能短板明显。整体而言，三人在专业技能方面均未达到公司偏好，赵武有经验但能力不足，王柳和杨过则完全缺乏相关经验。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分最高（3.80分），整体评价最优\", \"软技能突出：团队协作、创新能力、责任心维度评分均为4分\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰\"],\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"],\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"拥有运维岗位的实际工作经历，与公司招聘偏好部分匹配\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理1个月，出纳6个月）\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\"],\n            \"赵武\": [\"综合评分最低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分，短板极其严重\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面，明确指出需提升专业技能\", \"虽有运维经验，但专业技能评价极低，与“专业技能突出”的偏好标签严重不符\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"赵武是唯一拥有运维岗位经验的候选人，与招聘偏好直接相关，这是其核心优势。然而，其专业技能评分仅为2分，并被评价明确指出“需要提升一下专业技能”，这与“专业技能突出”的要求背道而驰。王柳和杨过均无任何Java或运维相关经验，完全不匹配。王柳的专业技能评分（3分）虽需提升，但高于赵武，且拥有“学习能力强”的标签。杨过的专业技能评分（平均3.67分）在三者中最高，但也多次被指出需提升，且无相关经验。\"\n            },\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"王柳拥有连续1年的单一岗位经历，记录简洁清晰。杨过在短时间内有两段不同岗位的短期经历，稳定性最差，职业规划可能不够清晰。赵武在1年内有三段经历（包括一次内部调动），稳定性也存疑。从稳定性角度看，王柳最优，赵武次之（部分为内部调动），杨过最弱。\"\n            },\n            {\n                \"aspect\": \"软技能与综合评价\",\n                \"comparison\": \"王柳在软技能方面表现最为突出，团队协作、创新能力、责任心均获高分，并收获大量正面标签，职业形象积极。杨过在沟通能力和责任心方面获得较多认可，但团队协作表现存在不一致的评价。赵武的团队协作获得认可，但其他维度评分普遍较低，评价内容偏负面。整体而言，王柳的软技能评价最优，杨过次之，赵武最弱。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 65,\n                \"reason\": \"赵武是目前唯一与公司招聘偏好（运维）有直接关联的候选人，其运维岗位经验是显著优势，且目前在职、出勤率高。然而，其最大的硬伤是专业技能评价极低（2分），这与公司“专业技能突出”的核心偏好严重冲突。领导评价也直接证实了这一点。此外，其工作经历在短期内涉及不同岗位，稳定性存疑。因此，尽管有岗位经验，但能否胜任并达到“专业技能突出”的要求存在巨大疑问。推荐分数基于其岗位匹配度，但能力风险极高。\",\n                \"suitability\": \"可考虑初级运维或运维支持类岗位，但必须对其专业技能进行严格、深入的面试和技术考核，确认其真实水平是否满足岗位基本要求。不适合对专业技能要求高的核心运维岗位。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项，与“突出”偏好严重不符\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"虽有运维经验，但能力评价不高，可能无法独立胜任工作\"]\n            },\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 60,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出优秀的职业素养和良好的发展潜力。其离职原因明确，记录清晰，稳定性相对较好。然而，其最大的劣势是完全不具备Java或运维所需的专业技能和相关工作经验，与招聘偏好完全不匹配。将其招聘至技术岗位，需要从零开始培养，转型难度和成本极高。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术运维环境\", \"缺乏技术背景，转型难度和成本巨大\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 55,\n                \"reason\": \"杨过在沟通能力和责任心方面获得认可，评价信息相对丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/运维开发职位毫无关联，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性是三人中最差的。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、运维，且偏好“专业技能突出”），三位候选人均未达到理想匹配。赵武虽有运维经验，但专业技能评价极低，与“突出”要求相去甚远，录用风险很高，除非后续技术面试能证明其实际能力远超现有评价。王柳和杨过完全无技术背景，不适合技术岗位。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关经验”和“专业技能突出”的候选人。2. 如果对运维岗位的技能要求可以放宽到“具备基础、有学习潜力”，可给予赵武面试机会，但必须进行严格的技术评估，重点考察其专业技能的真实水平。3. 如果公司同时存在非技术类岗位（如财务、行政、产品助理等）的招聘需求，可以分别考虑王柳和杨过。在此情况下，王柳因其更优秀的软技能评价、学习潜力和相对清晰的职业记录，是比杨过更优先的选择。赵武则不适合非技术岗位。在当前以技术岗位为核心的招聘目标下，不推荐直接录用任何一人，建议优先寻找更匹配的候选人。\"\n    }\n}\n```', '2025-12-04 16:59:54', '2025-12-04 16:59:54', 0);
INSERT INTO `talent_compare_record` VALUES (354117928461352960, 344582708126408704, '[349446284006400000,349776742455980032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]},{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,3],\"责任心\":[4,3],\"专业技能\":[3,2],\"团队协作\":[4,4],\"创新能力\":[4,3]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1391756095_1764839953304\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和赵武。两人均为女性，工作年限均为1年，但当前状态、工作经历和评价表现存在差异。王柳目前离职，有一段财务工作经历，综合评分较高（3.80分），HR评价在团队协作、创新能力、责任心等方面给予积极肯定，但指出专业技能和抗压能力需提升。赵武目前在职，有三段工作经历，涉及财务和运维岗位，综合评分较低（3.00分），领导评价明确指出其专业技能需提升。从公司招聘偏好（Java、运维，偏好“专业技能突出”）来看，赵武有运维岗位经验，与偏好部分匹配，但其专业技能评价很低（2分）；王柳则无相关经验，完全不匹配。两人均无重大违纪记录，符合公司排除要求。整体而言，赵武在职位经验上略有优势但能力评价不佳，王柳在软技能和工作表现上更受认可但岗位不匹配。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分较高（3.80分），整体评价更优\", \"工作业绩、责任心、创新能力、团队协作维度评分均为4分，表现突出\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰\"],\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"有运维岗位的工作经历，与公司招聘偏好部分匹配\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"目前处于离职状态\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配\"],\n            \"赵武\": [\"综合评分低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分，短板严重\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面\", \"虽有运维经验，但专业技能评价极低，与“专业技能突出”的偏好标签严重不符\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"赵武拥有运维岗位的实际工作经验，与公司招聘偏好中的“运维”职位直接相关，这是其关键优势。然而，其专业技能维度评分仅为2分，并被评价明确指出“需要提升一下专业技能”，这与公司“专业技能突出”的偏好标签严重不符。王柳则无任何Java或运维相关经验，完全不匹配，但其专业技能评分（3分）虽需提升，却高于赵武，且拥有“学习能力强”的标签。\"\n            },\n            {\n                \"aspect\": \"工作表现与综合评价\",\n                \"comparison\": \"王柳在多个核心软技能维度（工作业绩、责任心、团队协作、创新能力）上均获得4分的高评价，并收获了丰富的正面标签，表明其工作态度、协作精神和潜力得到了充分肯定。赵武则在工作业绩、责任心、专业技能等维度评分均为3分或以下，评价内容直接点明短板，反馈明显更为负面。王柳的整体职业形象和表现记录明显优于赵武。\"\n            },\n            {\n                \"aspect\": \"工作经历与稳定性\",\n                \"comparison\": \"王柳拥有连续1年的单一财务岗位经历，离职原因为自主离职，职业记录相对简洁清晰。赵武在1年工作年限内拥有三段工作经历，虽然其中一段为内部职位调动，另一段信息不可见，但频繁的岗位变动可能暗示其职业定位尚不清晰、适应过程存在挑战或稳定性较差，这是需要关注的风险点。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 62,\n                \"reason\": \"赵武是目前唯一与公司招聘偏好（运维）有直接关联的候选人，其运维岗位经验是显著优势。同时，其目前在职且出勤率极高，显示出良好的纪律性。然而，其最大的硬伤是专业技能评价极低（2分），这与公司“专业技能突出”的核心偏好严重冲突。领导评价也直接证实了这一点。此外，其工作经历在短期内涉及不同岗位，稳定性存疑。因此，尽管有岗位经验，但能否胜任并达到“专业技能突出”的要求存在巨大疑问。\",\n                \"suitability\": \"可考虑初级运维或运维支持类岗位，但必须对其专业技能进行严格、深入的面试和技术考核，确认其真实水平是否满足岗位基本要求。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项，与偏好严重不符\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"虽有运维经验，但能力评价不高，可能无法独立胜任核心运维工作\"]\n            },\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 58,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出优秀的职业素养和良好的发展潜力。其离职原因明确，记录清晰。然而，其最大的劣势是完全不具备Java或运维所需的专业技能和相关工作经验，与招聘偏好完全不匹配。将其招聘至技术岗位，需要从零开始培养，转型难度和成本极高。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术运维环境\", \"缺乏技术背景，转型难度和成本巨大\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、运维，且偏好“专业技能突出”），两位候选人均未达到理想匹配。赵武虽有运维经验，但专业技能评价极低，与“突出”要求相去甚远，录用风险很高，除非后续技术面试能证明其实际能力远超现有评价。王柳软技能优秀但完全无技术背景，不适合技术岗位。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关经验”和“专业技能突出”的候选人。2. 如果对运维岗位的技能要求可以放宽到“具备基础、有学习潜力”，可给予赵武面试机会，但必须进行严格的技术评估。3. 如果公司同时存在非技术类岗位（如财务、行政等）的招聘需求，王柳是更优先的选择。在当前以技术岗位为核心的招聘目标下，不推荐直接录用赵武，更不推荐录用王柳至技术岗。建议优先寻找更匹配的候选人。\"\n    }\n}\n```', '2025-12-04 17:19:13', '2025-12-04 17:19:13', 0);
INSERT INTO `talent_compare_record` VALUES (354117942902341632, 344592263740768256, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_111967763_1764839956775\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和杨过。两人均处于离职状态，但工作背景和稳定性差异显著。王柳有1年财务工作经验，仅有一次工作经历，出勤率高，评价维度得分均衡，优势在于团队协作、创新能力和责任心，但专业技能和抗压能力被指出需提升。杨过工作年限为0年，但在短期内有两段工作经历（出纳和产品经理），离职原因包括“公司缩编”和“主动辞职”，稳定性存疑。其评价数量较多，但责任心、专业技能等维度评分存在波动，且被多次提及需加强团队协作和专业技能。整体而言，王柳展现出更稳定的职业轨迹和明确的优势标签，而杨过的短期多段经历和评价波动是需要关注的风险点。\",\n        \"strengths\": {\n            \"王柳\": [\"工作经历稳定，仅有一段财务工作经历\", \"出勤率高（98%）\", \"团队协作、创新能力、责任心维度评分均为4分，表现突出\", \"获得多项正面标签，如沟通能力强、学习能力强、执行力强\"],\n            \"杨过\": [\"出勤率表现良好（平均97%）\", \"获得责任心强、沟通能力强等正面标签\", \"评价数量较多（3条），提供了更丰富的参考信息\", \"工作业绩维度评分稳定在4分\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"工作年限较短（仅1年）\", \"专业技能维度评分较低（3分），且被标签明确提示需提升\", \"抗压能力被评价为需提升\", \"评价信息较少（仅1条），信息维度可能不全面\"],\n            \"杨过\": [\"工作稳定性差，0年工作经验内有两段短期工作经历，且离职原因不一\", \"责任心、专业技能、团队协作、创新能力等维度评分存在内部波动（如责任心评分在3-4分间）\", \"被多次提及需加强团队协作和提升专业技能\", \"职位历史跨度大（从出纳到产品经理），可能缺乏深度积累\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"工作稳定性与职业轨迹\",\n                \"comparison\": \"王柳的职业轨迹清晰稳定，仅有一段为期1年多的财务工作经历，离职原因为自主离职，显示出一定的计划性。杨过的职业轨迹则较为跳跃，在极短的时间内（约半年）先后担任出纳和产品经理两个差异较大的职位，且离职原因包括被动（公司缩编）和主动（辞职），其职业稳定性和连续性明显弱于王柳，存在较高的适应性和职业规划方面的疑问。\"\n            },\n            {\n                \"aspect\": \"能力评价与潜在风险\",\n                \"comparison\": \"王柳的能力评价较为一致，优势（协作、创新、责任心）和待改进点（专业技能、抗压能力）明确。杨过的评价则显示出更多的不一致性：责任心评分在3-4分间波动，团队协作和专业技能被不同评价者反复提及需要加强。这种评价的波动性可能反映了其表现的不稳定，或是不同岗位适应性的差异，是比单一明确短板更值得关注的综合风险信号。\"\n            },\n            {\n                \"aspect\": \"信息充分性与评估基础\",\n                \"comparison\": \"杨过拥有3条来自领导和HR的评价，信息量更丰富，有助于多角度了解。王柳仅有1条HR评价，信息相对单薄，对其全面能力的判断可能存在局限。然而，杨过多条评价中暴露出的矛盾点和反复提及的改进项，反而增加了评估的复杂性。王柳的单一评价虽信息量少，但结论清晰一致。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 78,\n                \"reason\": \"推荐王柳的主要理由是其清晰的职业路径、稳定的工作表现以及明确的优势能力。她在团队协作、创新能力和责任心方面获得了高分和正面标签，出勤率优秀，这些是团队贡献的积极信号。虽然工作年限短、评价信息少是短板，但其唯一的工作经历完整，离职原因正常，显示出较好的基础职业素养。专业技能和抗压能力被标识为需提升，这在初级员工中较为常见，可通过培训和指导进行改善。相较于杨过，她的风险更可控，潜力更易判断。\",\n                \"suitability\": \"适合需要较强团队协作、沟通执行力和责任心的初级财务或相关行政支持岗位。在结构清晰、培训体系完善的团队中能较快成长。\",\n                \"riskPoints\": [\"工作经验尚浅，处理复杂专业任务的能力有待验证\", \"抗压能力被明确指出需提升，可能不适应高强度或高压工作环境\", \"仅有一次工作经历和一条评价，对其长期表现和适应性的预测存在不确定性\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 65,\n                \"reason\": \"杨过拥有更多的评价记录，且在沟通能力和工作业绩上有不错反馈。然而，不推荐的主要原因是其极差的职业稳定性和评价中暴露的潜在问题。在近乎零工作经验的周期内更换两个不同性质的岗位且均短期离职，强烈暗示其职业规划不清、适应能力可能存在问题或存在未明言的离职原因。评价中团队协作和专业技能被多次要求加强，且核心维度评分存在波动，进一步印证了其表现可能不稳定。这些综合风险较高，在缺乏足够令人信服的正面业绩证明的情况下，招聘风险大于收益。\",\n                \"suitability\": \"目前阶段，不建议直接聘用。如果考虑，需经过极其严格的背景调查和多轮深度面试，重点考察其短期频繁离职的真实原因、职业规划以及对团队协作和专业技能的深刻认识。仅适合对稳定性要求极低、且能提供密集指导与监督的特定临时性或项目辅助岗位。\",\n                \"riskPoints\": [\"职业稳定性极差，短期频繁离职，离职原因混杂，存在较高的人才流失风险\", \"评价显示团队协作和专业技能存在短板，且不同评价间有波动，表现可能不稳定\", \"从出纳到产品经理的职位跨度缺乏合理的经验积累解释，专业深度存疑\"]\n            }\n        ],\n        \"finalSuggestion\": \"综合对比分析，明确推荐候选人王柳。尽管她工作经验仅1年且存在专业技能需提升的明确短板，但其优势（协作、创新、责任心）突出、职业轨迹稳定、出勤率高，这些特质构成了可培养的坚实基础。其劣势（专业技能、抗压能力）在初级员工中常见，且已被识别，便于针对性管理和培养。相比之下，候选人杨过的短期多段工作经历是重大的职业稳定性红灯，结合评价中反复出现的改进要求，其聘用风险显著高于王柳。在无公司特定招聘偏好的情况下，遵循通用标准，应优先选择基础素质扎实、发展轨迹清晰、风险可控的候选人。因此，建议向王柳发出面试邀请，并在面试中重点考察其专业技能的具体水平、抗压能力的实际表现以及职业发展规划；对于杨过，建议暂不推进，或仅在其能对过往经历提供极其合理且可信的解释后再做考虑。\"\n    }\n}\n```', '2025-12-04 17:19:16', '2025-12-04 17:19:16', 0);
INSERT INTO `talent_compare_record` VALUES (354144190860886016, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764846214746\"}', NULL, '2025-12-04 19:03:34', '2025-12-04 19:03:34', 0);
INSERT INTO `talent_compare_record` VALUES (354148450340257792, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764847230283\"}', NULL, '2025-12-04 19:20:30', '2025-12-04 19:20:30', 0);
INSERT INTO `talent_compare_record` VALUES (354151226621329408, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764847892200\"}', NULL, '2025-12-04 19:31:32', '2025-12-04 19:31:32', 0);
INSERT INTO `talent_compare_record` VALUES (354153176905666560, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764848357187\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和杨过。两人均处于离职状态，无重大违纪记录，符合公司排除要求。从公司招聘偏好（Java、运维，偏好“专业技能突出”）来看，两人均无相关职位经验，匹配度极低。王柳有1年财务工作经验，综合评分3.80分，评价信息较少但维度评分较高，尤其在团队协作、创新能力、责任心等软技能方面表现突出，但专业技能和抗压能力被指出需提升。杨过工作年限为0年，但有两段短期工作经历（产品经理和出纳），综合评分3.73分，评价信息更丰富，显示其在沟通能力、责任心方面获得认可，但团队协作和专业技能的稳定性有待观察，且其中一段离职原因为“公司缩编”。整体而言，王柳的工作表现更稳定、软技能评价更聚焦，而杨过的工作经历略显复杂且稳定性存疑。两人均不适合目标技术岗位。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分较高（3.80分），整体评价更优\", \"软技能突出：团队协作、创新能力、责任心维度评分均为4分\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰\"],\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理约1个月，出纳约6个月）\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"王柳拥有连续1年的单一财务岗位经历，离职原因为自主离职，职业记录相对简洁清晰，稳定性较好。杨过则在短时间内有两段不同岗位的短期经历，其中一段离职原因为“公司缩编”，另一段为主动辞职，工作稳定性明显低于王柳，且职业方向（产品、出纳）与之前经历关联度不高，可能缺乏清晰的职业规划。\"\n            },\n            {\n                \"aspect\": \"能力评价与标签一致性\",\n                \"comparison\": \"王柳的评价虽然只有一条，但维度评分较高且一致，优势集中在软技能（团队协作、创新、责任心），弱项明确（专业技能、抗压能力）。杨过的评价更多元，但维度评分存在波动（如团队协作评分在3-4分间），标签也显示出矛盾（既有“团队协作”正面标签，也有“需加强团队协作”中性标签），说明其团队协作表现可能不稳定，专业技能的提升需求也被多次提及。王柳的“学习能力强”标签暗示了更好的发展潜力。\"\n            },\n            {\n                \"aspect\": \"与招聘岗位的匹配度\",\n                \"comparison\": \"两人均与公司偏好的Java、运维开发职位严重不匹配。王柳的背景是财务，杨过的背景是产品经理和出纳，均无技术开发或运维相关经验或技能展示。从职位匹配角度看，两人都不符合核心招聘需求。如果公司考虑非技术岗位，则需重新评估岗位需求。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 65,\n                \"reason\": \"王柳在工作态度、团队协作和责任心方面表现突出，出勤率高，离职原因明确为自主离职，职业记录清晰。其学习能力和执行力标签表明有成长潜力。然而，最大的短板是与目标职位（Java/运维）零匹配，且专业技能被明确标记为需提升。如果没有相关技术背景或强烈的转行意愿与学习计划，将其招聘至技术岗位风险极高。评分主要基于其通用职业素质，而非岗位适配性。参考历史记录，其评分与之前对比（65分、60分、58分）基本保持稳定，反映了其软技能优势与岗位不匹配的矛盾。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通协作、创新能力的非技术类岗位，可予以考虑。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，培训成本极高\", \"抗压能力被指出需提升，可能不适应高强度技术运维环境\", \"缺乏技术岗位所需的基础知识和经验\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 60,\n                \"reason\": \"杨过在沟通能力和责任心方面获得较多正面评价，且评价来源更丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/运维开发职位毫无关联，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性不如王柳。参考历史记录，其评分（55分、60分）保持稳定，反映了其沟通能力优势与稳定性差、岗位不匹配的综合评估。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司当前的招聘偏好（Java、运维，且偏好“专业技能突出”），候选人王柳和杨过均不具备相关的专业背景、工作经历或技能证明，因此都不推荐录用至目标技术岗位。强行录用将带来极高的培训成本、用人风险和岗位不匹配风险。建议：1. 重新审视招聘需求，如果确需Java/运维人才，应继续寻找具有相关技术背景和经验的候选人。2. 如果公司同时有其他非技术类岗位空缺（如财务、产品助理、行政等），可以分别考虑王柳和杨过。王柳更适合对稳定性、团队协作和责任心要求高的岗位；杨过则可考虑对沟通能力要求高、但能接受一定经验波动的初级岗位，但需在面试中重点考察其离职原因和职业规划。在当前以技术岗位为核心的招聘目标下，不建议为技术岗位录用其中任何一人。综合历史对比记录，此结论具有一致性。\"\n    }\n}\n```', '2025-12-04 19:39:17', '2025-12-04 19:39:17', 0);
INSERT INTO `talent_compare_record` VALUES (354155833472856064, 344582708126408704, '[349446284006400000,349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"团队协作表现突出\",\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[\"团队协作有待提升\"]},{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]}],\"dimensionRadarData\":{\"工作业绩\":[3,4,4],\"责任心\":[3,3.67,4],\"专业技能\":[2,3.67,3],\"团队协作\":[4,3.67,4],\"创新能力\":[3,3.67,4]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1943142277_1764848990568\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了三位候选人：赵武、杨过和王柳。三人均无重大违纪记录，符合公司排除要求。从公司招聘偏好（Java、运维，偏好“专业技能突出”）来看，匹配度普遍较低。赵武是唯一拥有运维岗位实际经验的候选人，但其专业技能评价极低（2分），与“突出”要求严重不符。杨过和王柳均无相关技术背景，杨过有产品经理和出纳经验，王柳有财务经验。在工作表现上，王柳的综合评分最高（3.80分），软技能（团队协作、创新能力、责任心）评价突出，但专业技能和抗压能力被指出需提升。杨过评价信息更丰富，沟通能力和责任心获得认可，但工作稳定性存疑（两段短期经历），专业技能也被指出需提升。赵武虽在职且有运维经验，但综合评分最低（3.00分），专业技能短板明显。整体而言，三人在专业技能方面均未达到公司偏好，赵武有经验但能力不足，王柳和杨过则完全缺乏相关经验。\",\n        \"strengths\": {\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"拥有运维岗位的实际工作经历，与公司招聘偏好部分匹配\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"],\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"],\n            \"王柳\": [\"综合评分最高（3.80分），整体评价最优\", \"软技能突出：团队协作、创新能力、责任心维度评分均为4分\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰\"]\n        },\n        \"weaknesses\": {\n            \"赵武\": [\"综合评分最低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分，短板极其严重\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面，明确指出需提升专业技能\", \"虽有运维经验，但专业技能评价极低，与“专业技能突出”的偏好标签严重不符\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理1个月，出纳6个月）\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\"],\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"赵武是唯一拥有运维岗位经验的候选人，与招聘偏好直接相关，这是其核心优势。然而，其专业技能评分仅为2分，并被评价明确指出“需要提升一下专业技能”，这与“专业技能突出”的要求背道而驰。王柳和杨过均无任何Java或运维相关经验，完全不匹配。王柳的专业技能评分（3分）虽需提升，但高于赵武，且拥有“学习能力强”的标签。杨过的专业技能评分（平均3.67分）在三者中最高，但也多次被指出需提升，且无相关经验。\"\n            },\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"王柳拥有连续1年的单一岗位经历，记录简洁清晰。杨过在短时间内有两段不同岗位的短期经历，稳定性最差，职业规划可能不够清晰。赵武在1年内有三段经历（包括一次内部调动），稳定性也存疑。从稳定性角度看，王柳最优，赵武次之（部分为内部调动），杨过最弱。\"\n            },\n            {\n                \"aspect\": \"软技能与综合评价\",\n                \"comparison\": \"王柳在软技能方面表现最为突出，团队协作、创新能力、责任心均获高分，并收获大量正面标签，职业形象积极。杨过在沟通能力和责任心方面获得较多认可，但团队协作表现存在不一致的评价。赵武的团队协作获得认可，但其他维度评分普遍较低，评价内容偏负面。整体而言，王柳的软技能评价最优，杨过次之，赵武最弱。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 65,\n                \"reason\": \"赵武是目前唯一与公司招聘偏好（运维）有直接关联的候选人，其运维岗位经验是显著优势，且目前在职、出勤率高。然而，其最大的硬伤是专业技能评价极低（2分），这与公司“专业技能突出”的核心偏好严重冲突。领导评价也直接证实了这一点。此外，其工作经历在短期内涉及不同岗位，稳定性存疑。因此，尽管有岗位经验，但能否胜任并达到“专业技能突出”的要求存在巨大疑问。推荐分数基于其岗位匹配度，但能力风险极高。参考历史记录，其评分（65分、62分、58分）保持稳定，反映了其经验优势与能力短板的矛盾。\",\n                \"suitability\": \"可考虑初级运维或运维支持类岗位，但必须对其专业技能进行严格、深入的面试和技术考核，确认其真实水平是否满足岗位基本要求。不适合对专业技能要求高的核心运维岗位。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项，与“突出”偏好严重不符\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"虽有运维经验，但能力评价不高，可能无法独立胜任工作\"]\n            },\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 60,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出优秀的职业素养和良好的发展潜力。其离职原因明确，记录清晰，稳定性相对较好。然而，其最大的劣势是完全不具备Java或运维所需的专业技能和相关工作经验，与招聘偏好完全不匹配。将其招聘至技术岗位，需要从零开始培养，转型难度和成本极高。参考历史记录，其评分（60分、65分、58分、68分）基本保持稳定，反映了其软技能优势与岗位不匹配的矛盾。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术运维环境\", \"缺乏技术背景，转型难度和成本巨大\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 55,\n                \"reason\": \"杨过在沟通能力和责任心方面获得认可，评价信息相对丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/运维开发职位毫无关联，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性是三人中最差的。参考历史记录，其评分（55分、60分）保持稳定，反映了其沟通能力优势与稳定性差、岗位不匹配的综合评估。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、运维，且偏好“专业技能突出”），三位候选人均未达到理想匹配。赵武虽有运维经验，但专业技能评价极低，与“突出”要求相去甚远，录用风险很高，除非后续技术面试能证明其实际能力远超现有评价。王柳和杨过完全无技术背景，不适合技术岗位。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关经验”和“专业技能突出”的候选人。2. 如果对运维岗位的技能要求可以放宽到“具备基础、有学习潜力”，可给予赵武面试机会，但必须进行严格的技术评估，重点考察其专业技能的真实水平。3. 如果公司同时存在非技术类岗位（如财务、行政、产品助理等）的招聘需求，可以分别考虑王柳和杨过。在此情况下，王柳因其更优秀的软技能评价、学习潜力和相对清晰的职业记录，是比杨过更优先的选择。赵武则不适合非技术岗位。在当前以技术岗位为核心的招聘目标下，不推荐直接录用任何一人，建议优先寻找更匹配的候选人。\"\n    }\n}\n```', '2025-12-04 19:49:50', '2025-12-04 19:49:50', 0);
INSERT INTO `talent_compare_record` VALUES (354156947995897856, 344582708126408704, '[349446284006400000,349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"工作业绩表现突出\",\"责任心表现突出\",\"专业技能表现突出\"],\"disadvantages\":[\"团队协作有待提升\"]},{\"employeeId\":349446284006400000,\"name\":\"赵武\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349446284006400000/1763726327311_OIP.webp\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":1,\"profileCount\":3,\"latestOccupation\":\"运维\",\"occupationHistory\":[\"运维\",\"**\",\"财务\"],\"averageScore\":3,\"dimensionScores\":{\"工作业绩\":3,\"责任心\":3,\"专业技能\":2,\"团队协作\":4,\"创新能力\":3},\"evaluationCount\":1,\"avgAttendanceRate\":99,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":16,\"tagName\":\"学习能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"团队协作表现突出\",\"工作经验丰富\"],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4,3],\"责任心\":[4,3.67,3],\"专业技能\":[3,3.67,2],\"团队协作\":[4,3.67,4],\"创新能力\":[4,3.67,3]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_1943142277_1764849256314\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了三位候选人：王柳、杨过和赵武。三人均无重大违纪记录。从公司招聘偏好（Java、运维，偏好“专业技能突出”，且“想要更稳定的员工”）来看，匹配度普遍较低。赵武是唯一拥有运维岗位实际经验的候选人，但其专业技能评价极低（2分），与“突出”要求严重不符。王柳和杨过均无相关技术背景，王柳有财务经验，杨过有产品经理和出纳经验。在工作表现上，王柳的综合评分最高（3.80分），软技能（团队协作、创新能力、责任心）评价突出，但专业技能和抗压能力被指出需提升。杨过评价信息更丰富，沟通能力和责任心获得认可，但工作稳定性存疑（两段短期经历），专业技能也被指出需提升。赵武虽在职且有运维经验，但综合评分最低（3.00分），专业技能短板明显，且工作经历在短期内变动较多。整体而言，三人在专业技能方面均未达到公司偏好，赵武有经验但能力不足，王柳和杨过则完全缺乏相关经验。在稳定性方面，王柳的职业记录相对最清晰，杨过最差。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分最高（3.80分），整体评价最优\", \"软技能突出：团队协作、创新能力、责任心维度评分均为4分\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强、执行力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰，职业轨迹相对稳定\"],\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"],\n            \"赵武\": [\"目前在职，状态稳定\", \"出勤率高达99%，工作态度认真\", \"团队协作能力获得认可，维度评分4分\", \"拥有运维岗位的实际工作经历，与公司招聘偏好部分匹配\", \"有跨岗位（财务、运维）的工作经历，适应性可能较强\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分）\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理约1个月，出纳约6个月），与公司“想要更稳定的员工”偏好冲突\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、运维职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\"],\n            \"赵武\": [\"综合评分最低（3.00分），整体表现评价不高\", \"专业技能被明确批评需提升，维度评分仅2分，短板极其严重，与“专业技能突出”偏好严重不符\", \"工作经历相对复杂，1年内有三段经历，稳定性存疑（其中一段信息不可见）\", \"评价信息仅1条，且内容偏负面，明确指出需提升专业技能\", \"虽有运维经验，但能力评价不高\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"赵武是唯一拥有运维岗位经验的候选人，与招聘偏好直接相关，这是其核心优势。然而，其专业技能评分仅为2分，并被评价明确指出“需要提升一下专业技能”，这与“专业技能突出”的要求背道而驰。王柳和杨过均无任何Java或运维相关经验，完全不匹配。王柳的专业技能评分（3分）虽需提升，但高于赵武，且拥有“学习能力强”的标签。杨过的专业技能评分（平均3.67分）在三者中最高，但也多次被指出需提升，且无相关经验。\"\n            },\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"结合公司“想要更稳定的员工”的偏好，王柳拥有连续1年的单一岗位经历，离职原因明确，记录简洁清晰，稳定性相对最好。杨过在短时间内有两段不同岗位的短期经历，稳定性最差，职业规划可能不够清晰，风险最高。赵武在1年内有三段经历（包括一次内部调动），稳定性也存疑。从稳定性角度看，王柳最优，赵武次之（部分为内部调动），杨过最弱。\"\n            },\n            {\n                \"aspect\": \"软技能与综合评价\",\n                \"comparison\": \"王柳在软技能方面表现最为突出，团队协作、创新能力、责任心均获高分，并收获大量正面标签，职业形象积极。杨过在沟通能力和责任心方面获得较多认可，但团队协作表现存在不一致的评价。赵武的团队协作获得认可，但其他维度评分普遍较低，评价内容偏负面。整体而言，王柳的软技能评价最优，杨过次之，赵武最弱。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"赵武\",\n                \"score\": 65,\n                \"reason\": \"赵武是目前唯一与公司招聘偏好（运维）有直接关联的候选人，其运维岗位经验是显著优势，且目前在职、出勤率高。然而，其最大的硬伤是专业技能评价极低（2分），这与公司“专业技能突出”的核心偏好严重冲突。领导评价也直接证实了这一点。此外，其工作经历在短期内涉及不同岗位，稳定性存疑，与公司“想要更稳定的员工”的偏好不完全匹配。因此，尽管有岗位经验，但能否胜任并达到“专业技能突出”的要求存在巨大疑问。推荐分数基于其岗位匹配度，但能力风险极高。参考历史记录，其评分（65分、62分、58分）保持稳定，反映了其经验优势与能力短板的矛盾。\",\n                \"suitability\": \"可考虑初级运维或运维支持类岗位，但必须对其专业技能进行严格、深入的面试和技术考核，确认其真实水平是否满足岗位基本要求。不适合对专业技能要求高的核心运维岗位。\",\n                \"riskPoints\": [\"专业技能薄弱，被明确列为需提升项，与“突出”偏好严重不符\", \"工作经历较短且变动较多，稳定性存疑\", \"评价信息较少且偏负面，需谨慎评估\", \"虽有运维经验，但能力评价不高，可能无法独立胜任工作\"]\n            },\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 60,\n                \"reason\": \"王柳在软技能方面表现卓越，责任心、团队协作和创新能力均获得高分评价，且学习能力和执行力强，显示出优秀的职业素养和良好的发展潜力。其离职原因明确，记录清晰，拥有连续一年的稳定工作经历，在三人中稳定性相对最好，与公司“想要更稳定的员工”的偏好有一定契合度。然而，其最大的劣势是完全不具备Java或运维所需的专业技能和相关工作经验，与招聘偏好完全不匹配。将其招聘至技术岗位，需要从零开始培养，转型难度和成本极高。参考历史记录，其评分（60分、65分、58分、68分）基本保持稳定，反映了其软技能优势与岗位不匹配的矛盾。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有财务、行政、助理或任何需要强沟通、强协作、高创新意识的非技术类岗位，王柳是更合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术运维环境\", \"缺乏技术背景，转型难度和成本巨大\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 55,\n                \"reason\": \"杨过在沟通能力和责任心方面获得认可，评价信息相对丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解，这与公司“想要更稳定的员工”的偏好直接冲突。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/运维开发职位毫无关联，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性是三人中最差的。参考历史记录，其评分（55分、60分）保持稳定，反映了其沟通能力优势与稳定性差、岗位不匹配的综合评估。\",\n                \"suitability\": \"不适合Java或运维技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险，与公司偏好严重不符\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、运维，偏好“专业技能突出”，且“想要更稳定的员工”），三位候选人均未达到理想匹配。赵武虽有运维经验，但专业技能评价极低，与“突出”要求相去甚远，且工作经历变动较多，稳定性并非最佳，录用风险很高。王柳和杨过完全无技术背景，不适合技术岗位。在稳定性方面，王柳优于杨过。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关经验”、“专业技能突出”且“职业记录稳定”的候选人。2. 如果对运维岗位的技能要求可以放宽到“具备基础、有学习潜力”，可给予赵武面试机会，但必须进行严格的技术评估，重点考察其专业技能的真实水平，并深入了解其工作变动原因。3. 如果公司同时存在非技术类岗位（如财务、行政、产品助理等）的招聘需求，可以分别考虑王柳和杨过。在此情况下，王柳因其更优秀的软技能评价、学习潜力、相对清晰的职业记录和更好的稳定性，是比杨过更优先的选择。赵武则不适合非技术岗位。在当前以技术岗位为核心的招聘目标下，不推荐直接录用任何一人，建议优先寻找更匹配的候选人。\"\n    }\n}\n```', '2025-12-04 19:54:16', '2025-12-04 19:54:16', 0);
INSERT INTO `talent_compare_record` VALUES (354171404511883264, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764852702968\"}', NULL, '2025-12-04 20:51:43', '2025-12-04 20:51:43', 0);
INSERT INTO `talent_compare_record` VALUES (354171420378935296, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":false,\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764852706800\"}', NULL, '2025-12-04 20:51:46', '2025-12-04 20:51:46', 0);
INSERT INTO `talent_compare_record` VALUES (354388712089989120, 344582708126408704, '[350947173493932032,351238108501110784]', '{\"items\":[{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"综合评分高于平均\"],\"disadvantages\":[]},{\"employeeId\":351238108501110784,\"name\":\"马保国\",\"gender\":\"女\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"前端开发工程师\",\"occupationHistory\":[\"前端开发工程师\"],\"averageScore\":0,\"dimensionScores\":{\"工作业绩\":0,\"责任心\":0,\"专业技能\":0,\"团队协作\":0,\"创新能力\":0},\"evaluationCount\":0,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[],\"topNeutralTags\":[],\"advantages\":[],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,0],\"责任心\":[3.67,0],\"专业技能\":[3.67,0],\"团队协作\":[3.67,0],\"创新能力\":[3.67,0]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_561669106_1764904513179\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：杨过和马保国。两人均无重大违纪记录。从公司招聘偏好（Java、前端，偏好“专业技能突出”，且“想要更稳定的员工”）来看，匹配度存在显著差异。马保国是唯一拥有前端开发工程师岗位实际经验的候选人，与招聘偏好直接相关，且目前在职，稳定性较好。杨过则无任何技术背景，其工作经历为产品经理和出纳，与目标岗位完全不匹配。在工作表现上，杨过的综合评分（3.73分）和评价信息（3条）更为丰富，显示其在沟通能力、责任心方面获得认可，但专业技能被指出需提升，且工作稳定性是重大风险（两段短期经历）。马保国则缺乏任何评价信息，综合评分为0分，无法从现有数据评估其工作能力。整体而言，马保国在岗位匹配度和当前稳定性上占优，但能力未知；杨过在软技能上有一定表现，但岗位不匹配且稳定性差。\",\n        \"strengths\": {\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"],\n            \"马保国\": [\"拥有前端开发工程师的实际工作经历，与公司招聘偏好（前端）直接匹配\", \"目前在职，状态稳定，符合公司“想要更稳定的员工”偏好\", \"出勤率表现良好（平均98%）\", \"无重大违纪记录\"]\n        },\n        \"weaknesses\": {\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理约1个月，出纳约6个月），与公司“想要更稳定的员工”偏好严重冲突\", \"专业技能被明确指出需提升，且无任何技术背景\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"与公司偏好的Java、前端职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\", \"工作年限为0年，经验尚浅\"],\n            \"马保国\": [\"缺乏任何评价信息，综合评分为0分，无法评估其工作能力、责任心、团队协作等关键维度\", \"专业技能水平未知，无法判断是否符合“专业技能突出”的偏好\", \"工作年限为0年，经验尚浅\", \"评价数量为0条，信息严重不足，评估风险高\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"专业技能与岗位匹配度\",\n                \"comparison\": \"马保国是唯一与公司招聘偏好（前端）直接匹配的候选人，其前端开发工程师的工作经历是核心优势。然而，其专业技能水平完全未知，无法判断是否达到“突出”的要求。杨过则完全不匹配，其背景（产品、出纳）与技术开发无关，且专业技能被多次指出需提升。从岗位适配性看，马保国具有潜在可能性，但需验证；杨过则完全不合适。\"\n            },\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"结合公司“想要更稳定的员工”的偏好，马保国目前在职，且在两段工作经历中，前一段（公司1）持续了8个月，后一段（公司2）刚入职，职业记录相对连续，稳定性较好。杨过则有两段短期且不连续的任职经历，稳定性是显著短板，风险极高。从稳定性角度看，马保国明显优于杨过。\"\n            },\n            {\n                \"aspect\": \"能力评估与信息透明度\",\n                \"comparison\": \"杨过的能力评估信息相对丰富，软技能（沟通、责任心）获得认可，但硬技能（专业技能）存在短板。马保国则是一片空白，没有任何评价或评分数据，其工作能力、态度、团队协作等均为未知数，招聘决策存在巨大的信息不对称风险。杨过的风险在于已知的稳定性差和技能不匹配；马保国的风险在于完全未知的能力表现。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"马保国\",\n                \"score\": 70,\n                \"reason\": \"马保国是唯一与公司招聘岗位（前端）直接相关的候选人，其前端开发工程师的工作经历是核心优势，且目前在职，职业记录相对连续，符合公司“想要更稳定的员工”的偏好。然而，其最大的风险是缺乏任何评价信息，专业技能、工作态度、团队协作等关键能力维度完全未知，无法判断是否符合“专业技能突出”的要求。推荐分数主要基于其岗位匹配度和当前稳定性，但强烈建议通过面试和技术考核进行深入评估。参考历史记录中对赵武（有经验但能力评价低）的评分逻辑（65分），马保国因信息未知且稳定性更好，评分略高。\",\n                \"suitability\": \"前端开发工程师岗位。但必须安排严格的技术面试和综合能力评估，以弥补信息空白，确认其真实技能水平和职业素养。\",\n                \"riskPoints\": [\"缺乏任何评价信息，能力表现完全未知，招聘风险高\", \"专业技能是否“突出”无法验证\", \"工作年限为0年，可能为初级工程师，经验尚浅\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 55,\n                \"reason\": \"杨过在沟通能力和责任心方面获得认可，评价信息相对丰富。但其工作稳定性是致命弱点，两段短期经历和“公司缩编”的离职原因与公司“想要更稳定的员工”的偏好直接冲突。此外，其背景（产品、出纳）与Java/前端开发职位毫无关联，专业技能也被指出需提升，匹配度为零。综合来看，其职业轨迹的清晰度和稳定性是重大风险。参考历史记录，其评分（55分、60分）保持稳定，反映了其沟通能力优势与稳定性差、岗位不匹配的综合评估。\",\n                \"suitability\": \"不适合Java或前端技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历和高稳定性风险，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性极差，存在频繁离职风险，与公司偏好严重不符\", \"与目标技术岗位完全不匹配，无相关技能\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、前端，且“想要更稳定的员工”），两位候选人均未达到理想匹配，但情况不同。马保国在岗位匹配度和当前稳定性上具有明显优势，是唯一有相关技术经验的候选人，但其能力完全未知，风险在于信息空白。杨过则完全不匹配且稳定性差，风险明确且较大。建议：1. 优先给予马保国面试机会。必须安排深入的技术面试和综合能力评估，重点考察其前端专业技能的真实水平、项目经验、工作态度和团队协作能力，以弥补评价信息的缺失。只有在面试确认其能力符合要求（至少达到岗位基本标准）且职业动机稳定后，方可考虑录用。2. 杨过不适合当前技术岗位招聘。除非公司有完全对口的非技术岗位（如产品助理），且能接受其稳定性风险，否则不建议考虑。在当前以技术岗位为核心的招聘目标下，马保国是唯一值得进一步考察的候选人，但需以严格的面试作为前提。\"\n    }\n}\n```', '2025-12-05 11:15:13', '2025-12-05 11:15:13', 0);
INSERT INTO `talent_compare_record` VALUES (354389821391122432, 344582708126408704, '[350947173493932032,351238108501110784]', '{\"items\":[{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"综合评分高于平均\"],\"disadvantages\":[]},{\"employeeId\":351238108501110784,\"name\":\"马保国\",\"gender\":\"女\",\"status\":true,\"currentCompanyName\":\"公司2\",\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"前端开发工程师\",\"occupationHistory\":[\"前端开发工程师\"],\"averageScore\":0,\"dimensionScores\":{\"工作业绩\":0,\"责任心\":0,\"专业技能\":0,\"团队协作\":0,\"创新能力\":0},\"evaluationCount\":0,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[],\"topNeutralTags\":[],\"advantages\":[],\"disadvantages\":[\"工作业绩有待提升\",\"责任心有待提升\",\"专业技能有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,0],\"责任心\":[3.67,0],\"专业技能\":[3.67,0],\"团队协作\":[3.67,0],\"创新能力\":[3.67,0]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_561669106_1764904777660\"}', NULL, '2025-12-05 11:19:37', '2025-12-05 11:19:37', 0);
INSERT INTO `talent_compare_record` VALUES (354532830879449088, 344582708126408704, '[349776742455980032,350947173493932032]', '{\"items\":[{\"employeeId\":349776742455980032,\"name\":\"王柳\",\"gender\":\"女\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/349776742455980032/1764070174338_OIP.jpg\",\"status\":true,\"currentCompanyName\":\"公司1\",\"workYears\":1,\"profileCount\":1,\"latestOccupation\":\"财务\",\"occupationHistory\":[\"财务\"],\"averageScore\":3.8,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":4,\"专业技能\":3,\"团队协作\":4,\"创新能力\":4},\"evaluationCount\":1,\"avgAttendanceRate\":98,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":1},{\"tagId\":2,\"tagName\":\"创新能力\",\"tagType\":1,\"count\":1},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":1},{\"tagId\":6,\"tagName\":\"学习能力强\",\"tagType\":1,\"count\":1},{\"tagId\":7,\"tagName\":\"执行力强\",\"tagType\":1,\"count\":1}],\"topNeutralTags\":[{\"tagId\":18,\"tagName\":\"抗压能力需提升\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"责任心表现突出\",\"团队协作表现突出\",\"创新能力表现突出\"],\"disadvantages\":[\"专业技能有待提升\"]},{\"employeeId\":350947173493932032,\"name\":\"杨过\",\"gender\":\"男\",\"photoUrl\":\"https://appcover-1328558808.cos.ap-shanghai.myqcloud.com/employee/350947173338742784/1764673101822_微信图片_20231124092706.jpg\",\"status\":false,\"workYears\":0,\"profileCount\":2,\"latestOccupation\":\"产品经理\",\"occupationHistory\":[\"产品经理\",\"出纳\"],\"averageScore\":3.73,\"dimensionScores\":{\"工作业绩\":4,\"责任心\":3.67,\"专业技能\":3.67,\"团队协作\":3.67,\"创新能力\":3.67},\"evaluationCount\":3,\"avgAttendanceRate\":97,\"hasMajorIncident\":false,\"topPositiveTags\":[{\"tagId\":3,\"tagName\":\"责任心强\",\"tagType\":1,\"count\":3},{\"tagId\":5,\"tagName\":\"沟通能力强\",\"tagType\":1,\"count\":3},{\"tagId\":1,\"tagName\":\"团队协作\",\"tagType\":1,\"count\":2}],\"topNeutralTags\":[{\"tagId\":10,\"tagName\":\"需加强团队协作\",\"tagType\":2,\"count\":1},{\"tagId\":11,\"tagName\":\"专业技能需提升\",\"tagType\":2,\"count\":1}],\"advantages\":[\"专业技能表现突出\"],\"disadvantages\":[\"责任心有待提升\",\"团队协作有待提升\",\"创新能力有待提升\"]}],\"dimensionRadarData\":{\"工作业绩\":[4,4],\"责任心\":[4,3.67],\"专业技能\":[3,3.67],\"团队协作\":[4,3.67],\"创新能力\":[4,3.67]},\"dimensionNames\":[\"工作业绩\",\"责任心\",\"专业技能\",\"团队协作\",\"创新能力\"],\"aiAnalysisTaskId\":\"ai_task_472186811_1764938873729\"}', '```json\n{\n    \"comparison\": {\n        \"summary\": \"本次对比分析了两位候选人：王柳和杨过。两人均无重大违纪记录。从公司招聘偏好（Java、前端，偏好“专业技能突出”、“执行力强”，且“想要更稳定的员工”）来看，匹配度极低。两人均无任何技术开发背景，王柳有1年财务经验，杨过有产品经理和出纳经验。在工作表现上，王柳的综合评分略高（3.80分），软技能（团队协作、创新能力、责任心）评价突出，并拥有“执行力强”的正面标签，与公司偏好部分契合，但专业技能和抗压能力被指出需提升。杨过评价信息更丰富，沟通能力和责任心获得认可，但工作稳定性存疑（两段短期经历），且专业技能也被指出需提升。整体而言，两人在专业技能方面均未达到“突出”要求，且与目标岗位完全不匹配。在稳定性方面，王柳的职业记录相对更清晰稳定。\",\n        \"strengths\": {\n            \"王柳\": [\"综合评分较高（3.80分），整体评价更优\", \"软技能突出：团队协作、创新能力、责任心维度评分均为4分\", \"拥有“执行力强”的正面标签，与公司偏好直接匹配\", \"获得多项正面标签：团队协作、创新能力、沟通能力强、学习能力强\", \"出勤率良好（98%）\", \"离职原因明确为自主离职，记录清晰，职业轨迹相对稳定\"],\n            \"杨过\": [\"评价信息相对丰富（3条），来自HR和领导，视角多元\", \"沟通能力和责任心获得多次正面评价\", \"工作业绩维度评分稳定在4分\", \"出勤率表现良好（平均97%）\", \"无重大违纪记录\"]\n        },\n        \"weaknesses\": {\n            \"王柳\": [\"专业技能被明确指出需提升（维度评分3分），与“突出”偏好不符\", \"抗压能力需提升，被列为中性标签\", \"仅有1条评价信息，样本量较少\", \"工作经历单一，仅1年财务经验\", \"与公司偏好的Java、前端职位完全不匹配，无相关技能\"],\n            \"杨过\": [\"工作稳定性存疑，两段工作经历均较短（产品经理约1个月，出纳约6个月），与公司“想要更稳定的员工”偏好严重冲突\", \"团队协作能力存在波动，有“需加强团队协作”的标签\", \"专业技能被指出需提升\", \"工作年限为0年，经验尚浅\", \"与公司偏好的Java、前端职位完全不匹配，无相关技能\", \"一段离职原因为“公司缩编”，可能涉及被动离职\", \"缺乏“执行力强”的明确标签\"]\n        },\n        \"keyDifferences\": [\n            {\n                \"aspect\": \"与公司招聘偏好的匹配度\",\n                \"comparison\": \"王柳明确拥有“执行力强”的标签，与公司偏好之一直接匹配，这是其相对于杨过的显著优势。然而，两人均不具备“专业技能突出”的标签，且专业技能维度均被指出需提升，与另一核心偏好不符。在稳定性偏好上，王柳拥有连续一年的稳定工作记录，离职原因明确；杨过则有两段短期经历，稳定性明显不足。因此，王柳在“执行力强”和“稳定性”两个偏好上更匹配公司要求。\"\n            },\n            {\n                \"aspect\": \"工作稳定性与职业记录\",\n                \"comparison\": \"王柳拥有连续1年的单一财务岗位经历，离职原因为自主离职，职业记录相对简洁清晰，符合公司“想要更稳定的员工”的偏好。杨过则在短时间内有两段不同岗位的短期经历，其中一段离职原因为“公司缩编”，另一段为主动辞职，工作稳定性明显低于王柳，且职业方向（产品、出纳）与之前经历关联度不高，可能缺乏清晰的职业规划，与公司稳定性偏好严重冲突。\"\n            },\n            {\n                \"aspect\": \"能力评价与潜在风险\",\n                \"comparison\": \"王柳的评价虽然只有一条，但维度评分较高且一致，优势集中在软技能（团队协作、创新、责任心、执行力），弱项明确（专业技能、抗压能力）。杨过的评价更多元，但维度评分存在波动（如团队协作评分在3-4分间），标签也显示出矛盾（既有“团队协作”正面标签，也有“需加强团队协作”中性标签），说明其团队协作表现可能不稳定，专业技能的提升需求也被多次提及。王柳的“学习能力强”标签暗示了更好的发展潜力。\"\n            }\n        ],\n        \"recommendations\": [\n            {\n                \"candidateName\": \"王柳\",\n                \"score\": 62,\n                \"reason\": \"王柳在工作态度、团队协作和责任心方面表现突出，尤其拥有“执行力强”的标签，与公司偏好之一直接匹配。其学习能力强，出勤率高，离职原因明确为自主离职，职业记录清晰，在两人中稳定性相对最好，与公司“想要更稳定的员工”的偏好有一定契合度。然而，其最大的劣势是完全不具备Java或前端所需的专业技能和相关工作经验，与招聘岗位完全不匹配，且专业技能被明确标记为需提升，与“专业技能突出”的偏好不符。参考历史记录，其评分（60分、65分、58分、68分、65分）基本保持稳定，反映了其软技能优势与岗位不匹配的矛盾。在当前技术岗位招聘目标下，其适配性有限。\",\n                \"suitability\": \"不适合Java或前端技术岗位。如果公司有财务、行政、助理或任何需要强执行力、强沟通协作、高创新意识的非技术类岗位，王柳是合适的人选。\",\n                \"riskPoints\": [\"专业技能与目标技术岗位完全不匹配，需从零培养，成本极高\", \"抗压能力被指出需提升，可能不适应高强度、快节奏的技术开发环境\", \"缺乏技术背景，转型难度巨大\"]\n            },\n            {\n                \"candidateName\": \"杨过\",\n                \"score\": 58,\n                \"reason\": \"杨过在沟通能力和责任心方面获得较多正面评价，且评价来源更丰富。但其工作稳定性是显著风险点，两段短期经历和“公司缩编”的离职原因需要深入了解，这与公司“想要更稳定的员工”的偏好直接冲突。能力评价存在波动，团队协作和专业技能的稳定性存疑。同样，其背景（产品、出纳）与Java/前端开发职位毫无关联，匹配度为零，且未展示“执行力强”的标签。综合来看，其职业轨迹的清晰度和稳定性是两人中较差的。参考历史记录，其评分（55分、60分、55分、60分）保持稳定，反映了其沟通能力优势与稳定性差、岗位不匹配的综合评估。\",\n                \"suitability\": \"不适合Java或前端技术岗位。如果公司有初级产品助理、运营或需要强沟通能力的支持类岗位，且能接受其较短的任职经历和稳定性风险，可谨慎评估。\",\n                \"riskPoints\": [\"工作稳定性差，存在频繁离职风险，与公司偏好严重不符\", \"能力评价存在不一致性，团队协作能力可能不稳定\", \"与目标技术岗位完全不匹配\", \"“公司缩编”的离职原因需核实具体背景\", \"工作经验最浅（0年）\", \"缺乏“执行力强”的明确证明\"]\n            }\n        ],\n        \"finalSuggestion\": \"基于公司明确的招聘偏好（Java、前端，偏好“专业技能突出”、“执行力强”，且“想要更稳定的员工”），候选人王柳和杨过均不具备相关的专业背景、工作经历或“专业技能突出”的证明，因此都不推荐录用至目标技术岗位。强行录用将带来极高的培训成本、用人风险和岗位不匹配风险。在两人之间进行非技术岗位的对比：王柳在“执行力强”和“工作稳定性”两个关键偏好上更匹配公司要求，其软技能评价更聚焦、积极，职业记录更清晰，因此综合表现优于杨过。杨过的工作稳定性是其最大短板。建议：1. 坚持技术岗位招聘标准，继续寻找同时满足“有相关技术经验”、“专业技能突出”且“职业记录稳定”的候选人。2. 如果公司同时存在非技术类岗位（如财务、行政、产品助理等）的招聘需求，可以优先考虑王柳，其次谨慎评估杨过。在当前以技术岗位为核心的招聘目标下，不推荐为技术岗位录用任何一人。综合历史对比记录，此结论具有一致性。\"\n    }\n}\n```', '2025-12-05 20:47:53', '2025-12-05 20:47:53', 0);

-- ----------------------------
-- Table structure for talent_view_log
-- ----------------------------
DROP TABLE IF EXISTS `talent_view_log`;
CREATE TABLE `talent_view_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '浏览企业ID',
  `user_id` bigint NOT NULL COMMENT '浏览用户ID',
  `employee_id` bigint NOT NULL COMMENT '被浏览的员工ID',
  `view_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近浏览时间',
  `view_duration` int NULL DEFAULT 0 COMMENT '最近浏览时长（秒）',
  `view_count` int NULL DEFAULT 1 COMMENT '浏览次数',
  `view_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览来源（search=搜索结果，recommend=推荐，bookmark=收藏列表）',
  `search_keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '搜索关键词（如果来源是搜索）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_employee`(`company_id` ASC, `employee_id` ASC) USING BTREE COMMENT '同一企业对同一员工只保留一条浏览记录',
  INDEX `idx_company_time`(`company_id` ASC, `view_time` ASC) USING BTREE,
  INDEX `idx_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_view_time`(`view_time` ASC) USING BTREE,
  CONSTRAINT `fk_view_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_view_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 353723895461302273 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人才浏览记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of talent_view_log
-- ----------------------------
INSERT INTO `talent_view_log` VALUES (353673786081939456, 344582708126408704, 350528138855968768, 349776742455980032, '2025-12-04 20:29:14', 10, 1, 'search', NULL, NULL, '2025-12-04 20:29:13', 0);
INSERT INTO `talent_view_log` VALUES (353674260092817408, 344582708126408704, 350528138855968768, 349446284006400000, '2025-12-03 23:13:08', 2, 1, 'search', NULL, NULL, '2025-12-03 23:13:08', 0);
INSERT INTO `talent_view_log` VALUES (353674315080142848, 344582708126408704, 350528138855968768, 349694061756129280, '2025-12-03 11:56:28', 3, 2, 'search', NULL, NULL, '2025-12-03 11:56:27', 0);
INSERT INTO `talent_view_log` VALUES (353675815982587904, 344582708126408704, 350528138855968768, 351238108501110784, '2025-12-03 12:04:22', 2, 4, 'search', NULL, NULL, '2025-12-03 12:02:25', 0);
INSERT INTO `talent_view_log` VALUES (353676226663669760, 344582708126408704, 350528138855968768, 351129930824167424, '2025-12-03 12:04:03', 15, 2, 'search', NULL, NULL, '2025-12-03 12:04:03', 0);
INSERT INTO `talent_view_log` VALUES (353679747156930560, 344582708126408704, 344581859291447296, 350947173493932032, '2025-12-05 11:28:03', 9, 1, 'search', NULL, NULL, '2025-12-05 11:28:03', 0);
INSERT INTO `talent_view_log` VALUES (353721929960116224, 344592263740768256, 350550633466048512, 349776742455980032, '2025-12-03 15:33:07', 2, 1, 'search', NULL, NULL, '2025-12-03 15:33:07', 0);
INSERT INTO `talent_view_log` VALUES (353723895461302272, 344592263740768256, 350550633466048512, 350947173493932032, '2025-12-03 15:33:51', 27, 1, 'search', NULL, NULL, '2025-12-03 15:33:51', 0);

-- ----------------------------
-- Table structure for unlock_price_config
-- ----------------------------
DROP TABLE IF EXISTS `unlock_price_config`;
CREATE TABLE `unlock_price_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `evaluation_type` tinyint NOT NULL COMMENT '评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）',
  `points_cost` decimal(12, 2) NOT NULL COMMENT '解锁消耗积分',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_evaluation_type`(`evaluation_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解锁价格配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of unlock_price_config
-- ----------------------------
INSERT INTO `unlock_price_config` VALUES (1, 1, 3.00, '领导评价解锁', 1, '2025-12-03 11:49:50', '2025-12-03 11:49:50', 0);
INSERT INTO `unlock_price_config` VALUES (2, 2, 1.00, '同事评价解锁', 1, '2025-12-03 11:49:50', '2025-12-03 11:49:50', 0);
INSERT INTO `unlock_price_config` VALUES (3, 3, 5.00, 'HR评价解锁', 1, '2025-12-03 11:49:50', '2025-12-03 11:49:50', 0);
INSERT INTO `unlock_price_config` VALUES (4, 4, 0.50, '自评解锁', 1, '2025-12-03 11:49:50', '2025-12-03 11:49:50', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 359619316391104513 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (341422268628860928, 'admin', '30c2a4ec1a3c03290751c8d877a6df18', 'admin', NULL, '2025-10-30 16:31:12', '2025-11-24 19:22:16', 0, '管理员');
INSERT INTO `user` VALUES (341441376602845184, 'employee', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-10-30 17:47:07', '2025-10-30 17:47:07', 0, '员工1');
INSERT INTO `user` VALUES (344564694777683968, 'employee2', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-08 08:38:04', '2025-11-08 08:38:04', 0, '员工2');
INSERT INTO `user` VALUES (344581859291447296, 'company_admin1', '30c2a4ec1a3c03290751c8d877a6df18', 'company_admin', 344582708126408704, '2025-11-08 09:46:17', '2025-11-21 17:50:39', 0, '企业负责人1');
INSERT INTO `user` VALUES (344592157545185280, 'company_admin2', '30c2a4ec1a3c03290751c8d877a6df18', 'company_admin', 344592263740768256, '2025-11-08 10:27:12', '2025-11-21 17:50:51', 0, '企业负责人2');
INSERT INTO `user` VALUES (349419495334993920, '362121199912127548', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-21 18:09:19', '2025-11-25 22:14:50', 0, '张三');
INSERT INTO `user` VALUES (349446283922513920, '362121199912127348', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-21 19:55:46', '2025-11-21 19:55:46', 0, '李四');
INSERT INTO `user` VALUES (349694061701603328, '362121199910043478', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-22 12:20:21', '2025-11-22 12:20:21', 0, '赵武');
INSERT INTO `user` VALUES (349776742242070528, '362121199910046666', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-22 17:48:53', '2025-11-22 17:48:53', 0, '王柳');
INSERT INTO `user` VALUES (350528138855968768, 'company1_hr', '30c2a4ec1a3c03290751c8d877a6df18', 'hr', 344582708126408704, '2025-11-24 19:34:40', '2025-11-25 23:42:34', 0, 'company1_hr');
INSERT INTO `user` VALUES (350536592723017728, 'company1_hr2', '30c2a4ec1a3c03290751c8d877a6df18', 'hr', NULL, '2025-11-24 20:08:16', '2025-11-24 20:08:16', 0, 'company1_hr2');
INSERT INTO `user` VALUES (350550633466048512, 'company2_hr1', '30c2a4ec1a3c03290751c8d877a6df18', 'hr', 344592263740768256, '2025-11-24 21:04:03', '2025-11-27 14:13:10', 0, '公司2hr1');
INSERT INTO `user` VALUES (350929096651853824, '362202199602104477', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-25 22:07:56', '2025-11-25 22:17:34', 0, '李云');
INSERT INTO `user` VALUES (350947173338742784, '361102198802104477', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-25 23:19:46', '2025-11-25 23:19:46', 0, '杨过');
INSERT INTO `user` VALUES (351129930673172480, '654202198808177516', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-26 11:25:59', '2025-11-26 11:25:59', 0, '李云龙');
INSERT INTO `user` VALUES (351238108329144320, '654202198808161165', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-11-26 18:35:50', '2025-11-26 18:35:50', 0, '马保国');
INSERT INTO `user` VALUES (354398132337414144, '362202199912307564', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-12-05 11:52:39', '2025-12-05 11:52:39', 0, '李雪');
INSERT INTO `user` VALUES (354398132526157824, '421502199104059615', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-12-05 11:52:39', '2025-12-05 11:52:39', 0, '张无忌');
INSERT INTO `user` VALUES (354408883601141760, '362202199901235612', '30c2a4ec1a3c03290751c8d877a6df18', 'employee', NULL, '2025-12-05 12:35:22', '2025-12-05 12:35:22', 0, '赵东');
INSERT INTO `user` VALUES (359619316391104512, '2774680379@qq.com', '3ad51830d90e39ef9212bff7582ef6c8', 'company_admin', 359619316558876672, '2025-12-19 21:39:46', '2025-12-19 21:39:46', 0, '马化腾');

SET FOREIGN_KEY_CHECKS = 1;

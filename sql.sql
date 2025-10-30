-- 创建数据库
CREATE DATABASE IF NOT EXISTS cross_org_talent_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cross_org_talent_manager;

-- 1. company 表（无依赖，最先创建）
CREATE TABLE company (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL COMMENT '企业名称',
                         contact_person VARCHAR(50) NOT NULL COMMENT '联系人姓名',
                         phone VARCHAR(20) NOT NULL COMMENT '企业电话',
                         email VARCHAR(100) NOT NULL COMMENT '企业邮箱',
                         industry VARCHAR(50) NULL COMMENT '所属行业',
                         create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                         update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         is_delete TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息管理';

-- 2. employee 表（依赖 company，但不依赖其他表，第二创建）
CREATE TABLE employee (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(50) NOT NULL COMMENT '姓名',
                          gender CHAR(1) NULL COMMENT '性别',
                          phone VARCHAR(20) NULL COMMENT '电话',
                          email VARCHAR(100) NULL COMMENT '邮箱',
                          id_card_number VARCHAR(18) NOT NULL UNIQUE COMMENT '身份证号',
                          department_id BIGINT NULL COMMENT '所属部门',
                          status TINYINT(1) NOT NULL COMMENT '在职状态',
                          photo_url VARCHAR(255) NULL COMMENT '员工照片',
                          create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          is_delete TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工基本信息';

-- 3. department 表（依赖 company 和 employee，第三创建）
CREATE TABLE department (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL COMMENT '部门名称',
                            company_id BIGINT NOT NULL COMMENT '所属企业',
                            leader_id BIGINT NOT NULL COMMENT '部门领导',
                            create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            is_delete TINYINT(1) NOT NULL DEFAULT 0,
                            CONSTRAINT fk_department_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
                            CONSTRAINT fk_department_leader FOREIGN KEY (leader_id) REFERENCES employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录公司部门及部门领导';

-- 4. user 表（依赖 company，第四创建）
CREATE TABLE `user` (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        password VARCHAR(255) NOT NULL COMMENT '加密密码',
                        user_role VARCHAR(20) NOT NULL COMMENT '用户角色',
                        company_id BIGINT NULL COMMENT '所属企业',
                        create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        is_delete TINYINT(1) NOT NULL DEFAULT 0,
                        CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户管理';

-- 5. employee_profile 表（依赖 employee 和 company，第五创建）
CREATE TABLE employee_profile (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  employee_id BIGINT NOT NULL COMMENT '员工ID',
                                  company_id BIGINT NOT NULL COMMENT '所属企业',
                                  start_date DATE NOT NULL COMMENT '入职日期',
                                  end_date DATE NULL COMMENT '离职日期',
                                  performance_summary TEXT NULL COMMENT '绩效摘要',
                                  attendance_rate DECIMAL(5,2) NULL COMMENT '出勤率',
                                  has_major_incident TINYINT(1) NOT NULL COMMENT '重大违纪',
                                  reason_for_leaving VARCHAR(255) NULL COMMENT '离职原因',
                                  occupation VARCHAR(100) NULL COMMENT '职业',
                                  annual_salary DECIMAL(12,2) NULL COMMENT '年薪',
                                  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  is_delete TINYINT(1) NOT NULL DEFAULT 0,
                                  CONSTRAINT fk_profile_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_profile_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工档案信息';

-- 6. evaluation 表（依赖 employee_profile 和 user，第六创建）
CREATE TABLE evaluation (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            profile_id BIGINT NOT NULL COMMENT '员工档案ID',
                            evaluator_id BIGINT NOT NULL COMMENT '评价人',
                            score TINYINT NOT NULL CHECK (score BETWEEN 0 AND 100) COMMENT '评分（0-100分）',
                            comment TEXT NULL COMMENT '评价内容',
                            evaluation_date DATE NOT NULL COMMENT '评价日期',
                            is_leader_evaluation TINYINT(1) NOT NULL COMMENT '是否为领导评价',
                            create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            is_delete TINYINT(1) NOT NULL DEFAULT 0,
                            CONSTRAINT fk_evaluation_profile FOREIGN KEY (profile_id) REFERENCES employee_profile(id) ON DELETE CASCADE,
                            CONSTRAINT fk_evaluation_evaluator FOREIGN KEY (evaluator_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工评价记录';

-- 7. reward_punishment 表（依赖 employee_profile 和 employee，第七创建）
CREATE TABLE reward_punishment (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   profile_id BIGINT NOT NULL COMMENT '员工档案ID',
                                   employee_id BIGINT NOT NULL COMMENT '员工ID',
                                   type VARCHAR(20) NOT NULL COMMENT '类型',
                                   description TEXT NOT NULL COMMENT '详细描述',
                                   amount DECIMAL(12,2) NOT NULL COMMENT '金额',
                                   date DATE NOT NULL COMMENT '发生日期',
                                   create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                                   update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   is_delete TINYINT(1) NOT NULL DEFAULT 0,
                                   CONSTRAINT fk_reward_profile FOREIGN KEY (profile_id) REFERENCES employee_profile(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_reward_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖惩记录';

-- 8. company_points 表（依赖 company 和 evaluation，第八创建）
CREATE TABLE company_points (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                company_id BIGINT NOT NULL COMMENT '所属企业',
                                points DECIMAL(12,2) NOT NULL COMMENT '积分变动（正/负）',
                                change_reason TINYINT(1) NOT NULL COMMENT '变动原因（0=员工评价, 1=建立档案）',
                                evaluation_id BIGINT NULL COMMENT '评价ID（变动原因=0时有效）',
                                score TINYINT NULL COMMENT '评分（0-100，变动原因=0时有效）',
                                change_date DATE NOT NULL COMMENT '变动日期',
                                create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                is_delete TINYINT(1) NOT NULL DEFAULT 0,
                                CONSTRAINT fk_points_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
                                CONSTRAINT fk_points_evaluation FOREIGN KEY (evaluation_id) REFERENCES evaluation(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业积分变动记录';
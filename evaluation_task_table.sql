-- ----------------------------
-- Table structure for evaluation_task
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_task`;

CREATE TABLE `evaluation_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '被评价员工ID',
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
  CONSTRAINT `fk_task_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_evaluation` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价任务表' ROW_FORMAT = Dynamic;



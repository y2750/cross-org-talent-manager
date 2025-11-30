package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价任务表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("evaluation_task")
public class EvaluationTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 部门ID（创建任务时保存，避免员工离职后无法查询）
     */
    private Long departmentId;

    /**
     * 部门名称（创建任务时保存）
     */
    private String departmentName;

    /**
     * 评价人ID（HR或同事的userId）
     */
    private Long evaluatorId;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）
     */
    private Integer evaluationPeriod;

    /**
     * 评价年份（如2024）
     */
    private Integer periodYear;

    /**
     * 评价季度（1-4，仅季度评价时有效）
     */
    private Integer periodQuarter;

    /**
     * 任务状态（0=待评价，1=已完成，2=已过期）
     */
    private Integer status;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 关联的评价ID（完成后关联）
     */
    private Long evaluationId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}


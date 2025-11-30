package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价任务视图对象
 */
@Data
public class EvaluationTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 被评价员工姓名
     */
    private String employeeName;

    /**
     * 被评价员工部门名称
     */
    private String departmentName;

    /**
     * 被评价员工是否为部门主管
     */
    private Boolean isLeader;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 评价人姓名
     */
    private String evaluatorName;

    /**
     * 评价类型
     */
    private Integer evaluationType;

    /**
     * 评价类型文本
     */
    private String evaluationTypeText;

    /**
     * 评价周期
     */
    private Integer evaluationPeriod;

    /**
     * 评价周期文本
     */
    private String evaluationPeriodText;

    /**
     * 评价年份
     */
    private Integer periodYear;

    /**
     * 评价季度
     */
    private Integer periodQuarter;

    /**
     * 任务状态（0=待评价，1=已完成，2=已过期）
     */
    private Integer status;

    /**
     * 任务状态文本
     */
    private String statusText;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 关联的评价ID
     */
    private Long evaluationId;

    private LocalDateTime createTime;
}


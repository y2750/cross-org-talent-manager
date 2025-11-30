package com.crossorgtalentmanager.model.dto.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建评价任务请求
 */
@Data
public class EvaluationTaskCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被评价员工ID列表（批量创建）
     */
    private List<Long> employeeIds;

    /**
     * 单个被评价员工ID
     */
    private Long employeeId;

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
     * 截止时间
     */
    private LocalDateTime deadline;
}



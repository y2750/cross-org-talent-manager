package com.crossorgtalentmanager.model.dto.evaluation;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 查询评价任务请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluationTaskQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 评价类型
     */
    private Integer evaluationType;

    /**
     * 评价周期
     */
    private Integer evaluationPeriod;

    /**
     * 任务状态（0=待评价，1=已完成，2=已过期）
     */
    private Integer status;

    /**
     * 评价年份
     */
    private Integer periodYear;

    /**
     * 评价季度
     */
    private Integer periodQuarter;
}

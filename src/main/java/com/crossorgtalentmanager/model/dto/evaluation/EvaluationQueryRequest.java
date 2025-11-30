package com.crossorgtalentmanager.model.dto.evaluation;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 查询评价请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluationQueryRequest extends PageRequest implements Serializable {

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
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）
     */
    private Integer evaluationPeriod;

    /**
     * 评价年份
     */
    private Integer periodYear;

    /**
     * 评价季度（1-4）
     */
    private Integer periodQuarter;

    /**
     * 评价时员工所属公司ID
     */
    private Long companyId;
}

package com.crossorgtalentmanager.model.dto.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 新增评价请求
 */
@Data
public class EvaluationAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价日期（可选，默认今天）
     */
    private LocalDate evaluationDate;

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
     * 维度评分列表
     */
    private List<DimensionScoreRequest> dimensionScores;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 维度评分请求
     */
    @Data
    public static class DimensionScoreRequest implements Serializable {
        private Long dimensionId;
        private Integer score; // 1-5分
    }
}

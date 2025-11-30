package com.crossorgtalentmanager.model.dto.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 同事评价请求（离职时使用）
 */
@Data
public class EvaluationColleagueRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被评价员工ID（离职员工）
     */
    private Long employeeId;

    /**
     * 评价内容
     */
    private String comment;

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

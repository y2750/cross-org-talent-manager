package com.crossorgtalentmanager.model.dto.evaluation;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 更新评价请求
 */
@Data
public class EvaluationUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long id;

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

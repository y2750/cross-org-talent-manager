package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 评价维度评分视图对象（用于雷达图）
 */
@Data
public class EvaluationDimensionScoreVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维度ID
     */
    private Long dimensionId;

    /**
     * 维度名称
     */
    private String dimensionName;

    /**
     * 加权平均分（考虑评价类型权重）
     */
    private Double averageScore;
}

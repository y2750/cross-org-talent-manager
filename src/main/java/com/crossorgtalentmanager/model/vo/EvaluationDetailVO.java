package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 评价详情视图对象（包含所有关联信息）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EvaluationDetailVO extends EvaluationVO {

    private static final long serialVersionUID = 1L;

    /**
     * 平均维度评分
     */
    private Double averageDimensionScore;
}

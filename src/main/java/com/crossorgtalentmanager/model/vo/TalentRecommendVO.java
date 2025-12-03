package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 人才推荐VO
 *
 * @author y
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TalentRecommendVO extends TalentVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 推荐评分（0-100）
     */
    private BigDecimal recommendScore;

    /**
     * 推荐理由列表
     */
    private List<String> recommendReasons;

    /**
     * 匹配度百分比
     */
    private Integer matchPercentage;

    /**
     * 匹配的偏好职位
     */
    private List<String> matchedOccupations;

    /**
     * 匹配的偏好标签
     */
    private List<EvaluationTagVO> matchedTags;
}


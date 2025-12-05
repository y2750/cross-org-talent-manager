package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 人才评价VO（带解锁状态）
 *
 * @author y
 */
@Data
public class TalentEvaluationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 评价公司ID
     */
    private Long companyId;

    /**
     * 评价公司名称
     */
    private String companyName;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 评价类型名称
     */
    private String evaluationTypeName;

    /**
     * 评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）
     */
    private Integer evaluationPeriod;

    /**
     * 评价周期名称
     */
    private String evaluationPeriodName;

    /**
     * 评价日期
     */
    private LocalDate evaluationDate;

    /**
     * 评价年份
     */
    private Integer periodYear;

    /**
     * 评价季度
     */
    private Integer periodQuarter;

    /**
     * 是否已解锁（true=已解锁或免费可见，false=需要付费解锁）
     */
    private Boolean unlocked;

    /**
     * 解锁所需积分（未解锁时有值）
     */
    private BigDecimal unlockCost;

    // ========== 以下字段仅在unlocked=true时有值 ==========

    /**
     * 评价内容（未解锁时为null或脱敏）
     */
    private String comment;

    /**
     * 各维度评分
     */
    private Map<String, Integer> dimensionScores;

    /**
     * 平均评分
     */
    private BigDecimal averageScore;

    /**
     * 评价标签
     */
    private List<EvaluationTagVO> tags;

    /**
     * 评价人信息（脱敏）
     */
    private String evaluatorInfo;
}



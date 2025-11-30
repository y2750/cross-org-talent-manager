package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价视图对象
 */
@Data
public class EvaluationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 被评价员工所属公司ID（冗余字段，方便查询和离职后追溯）
     */
    private Long companyId;

    /**
     * 被评价员工姓名
     */
    private String employeeName;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 评价人姓名
     */
    private String evaluatorName;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价日期
     */
    private LocalDate evaluationDate;

    /**
     * 评价类型
     */
    private Integer evaluationType;

    /**
     * 评价类型文本
     */
    private String evaluationTypeText;

    /**
     * 评价周期
     */
    private Integer evaluationPeriod;

    /**
     * 评价周期文本
     */
    private String evaluationPeriodText;

    /**
     * 评价年份
     */
    private Integer periodYear;

    /**
     * 评价季度
     */
    private Integer periodQuarter;

    /**
     * 创建时间（评价时间）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 维度评分列表
     */
    private List<DimensionScoreVO> dimensionScores;

    /**
     * 标签列表
     */
    private List<TagVO> tags;

    /**
     * 维度评分视图对象
     */
    @Data
    public static class DimensionScoreVO implements Serializable {
        private Long dimensionId;
        private String dimensionName;
        private Integer score;
    }

    /**
     * 标签视图对象
     */
    @Data
    public static class TagVO implements Serializable {
        private Long tagId;
        private String tagName;
        private Integer tagType;
    }
}

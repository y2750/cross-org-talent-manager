package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 人才对比VO
 *
 * @author y
 */
@Data
public class TalentCompareVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对比项目列表
     */
    private List<CompareItemVO> items;

    /**
     * 对比维度评分雷达图数据
     */
    private Map<String, List<BigDecimal>> dimensionRadarData;

    /**
     * 维度名称列表（用于雷达图）
     */
    private List<String> dimensionNames;

    /**
     * 对比项目VO
     */
    @Data
    public static class CompareItemVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 员工ID
         */
        private Long employeeId;

        /**
         * 员工姓名
         */
        private String name;

        /**
         * 性别
         */
        private String gender;

        /**
         * 员工照片
         */
        private String photoUrl;

        /**
         * 当前状态（在职/离职）
         */
        private Boolean status;

        /**
         * 当前公司名称
         */
        private String currentCompanyName;

        /**
         * 工作年限（根据档案计算）
         */
        private Integer workYears;

        /**
         * 工作经历数
         */
        private Integer profileCount;

        /**
         * 最新职位
         */
        private String latestOccupation;

        /**
         * 曾任职位列表
         */
        private List<String> occupationHistory;

        /**
         * 综合评分
         */
        private BigDecimal averageScore;

        /**
         * 各维度评分
         */
        private Map<String, BigDecimal> dimensionScores;

        /**
         * 评价数量
         */
        private Integer evaluationCount;

        /**
         * 平均出勤率
         */
        private BigDecimal avgAttendanceRate;

        /**
         * 是否有重大违纪
         */
        private Boolean hasMajorIncident;

        /**
         * 正面标签列表（Top 5）
         */
        private List<TalentVO.TagStatVO> topPositiveTags;

        /**
         * 中性标签列表（Top 3）
         */
        private List<TalentVO.TagStatVO> topNeutralTags;

        /**
         * 优势（相比其他人）
         */
        private List<String> advantages;

        /**
         * 劣势（相比其他人）
         */
        private List<String> disadvantages;
    }
}


package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 评价统计视图对象
 */
@Data
public class EvaluationStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 评价总数
     */
    private Integer totalCount;

    /**
     * 各类型评价数量
     */
    private Map<Integer, Integer> countByType;

    /**
     * 各周期评价数量
     */
    private Map<Integer, Integer> countByPeriod;

    /**
     * 平均维度评分
     */
    private Map<String, Double> averageDimensionScores;

    /**
     * 季度评价趋势
     */
    private List<QuarterlyTrend> quarterlyTrends;

    /**
     * 季度趋势数据
     */
    @Data
    public static class QuarterlyTrend implements Serializable {
        private Integer year;
        private Integer quarter;
        private Double averageScore;
    }
}

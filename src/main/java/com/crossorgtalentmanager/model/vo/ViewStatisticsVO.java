package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 浏览统计VO
 *
 * @author y
 */
@Data
public class ViewStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总浏览次数
     */
    private Long totalViews;

    /**
     * 今日浏览次数
     */
    private Long todayViews;

    /**
     * 本周浏览次数
     */
    private Long weekViews;

    /**
     * 本月浏览次数
     */
    private Long monthViews;

    /**
     * 浏览人才数（去重）
     */
    private Long uniqueTalentCount;

    /**
     * 最常浏览的人才（Top 10）
     */
    private List<TalentViewCountVO> mostViewedTalents;

    /**
     * 最近浏览的人才（Top 10）
     */
    private List<TalentViewLogVO> recentViews;

    /**
     * 按日期统计的浏览趋势（最近30天）
     */
    private Map<String, Long> viewTrend;

    /**
     * 按来源统计的浏览分布
     */
    private Map<String, Long> viewSourceDistribution;

    /**
     * 最常搜索的关键词（Top 10）
     */
    private List<KeywordStatVO> topKeywords;

    /**
     * 人才浏览次数统计VO
     */
    @Data
    public static class TalentViewCountVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long employeeId;
        private String employeeName;
        private String employeePhotoUrl;
        private String latestOccupation;
        private Long viewCount;
    }

    /**
     * 关键词统计VO
     */
    @Data
    public static class KeywordStatVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String keyword;
        private Long count;
    }
}


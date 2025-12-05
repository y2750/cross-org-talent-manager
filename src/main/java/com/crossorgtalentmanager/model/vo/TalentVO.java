package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 人才市场列表展示VO
 *
 * @author y
 */
@Data
public class TalentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 员工照片URL
     */
    private String photoUrl;

    /**
     * 当前在职状态（true=在职，false=离职）
     */
    private Boolean status;

    /**
     * 当前所属公司名称（如果在职）
     */
    private String currentCompanyName;

    /**
     * 最新职位（从档案中获取）
     */
    private String latestOccupation;

    /**
     * 曾任职位列表（去重）
     */
    private List<String> occupationHistory;

    /**
     * 平均评分（所有维度评分的平均值）
     */
    private BigDecimal averageScore;

    /**
     * 评价数量
     */
    private Integer evaluationCount;

    /**
     * 正面标签统计（标签名 -> 次数）
     */
    private List<TagStatVO> positiveTags;

    /**
     * 中性标签统计（标签名 -> 次数）
     */
    private List<TagStatVO> neutralTags;

    /**
     * 工作经历数（档案数量）
     */
    private Integer profileCount;

    /**
     * 是否已收藏
     */
    private Boolean bookmarked;

    /**
     * 是否为本公司员工（当前或曾经）
     */
    private Boolean isOwnEmployee;

    /**
     * 标签统计VO
     */
    @Data
    public static class TagStatVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        
        private Long tagId;
        private String tagName;
        private Integer tagType;
        private Integer count;
    }
}



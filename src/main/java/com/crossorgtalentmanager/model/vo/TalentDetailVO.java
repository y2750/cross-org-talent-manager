package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 人才详情VO
 *
 * @author y
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TalentDetailVO extends TalentVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 电话（脱敏处理，非本公司员工不可见）
     */
    private String phone;

    /**
     * 邮箱（脱敏处理，非本公司员工不可见）
     */
    private String email;

    /**
     * 身份证号（脱敏处理，非本公司员工不可见）
     */
    private String idCardNumber;

    /**
     * 工作经历列表（按时间倒序）
     */
    private List<ProfileSummaryVO> profiles;

    /**
     * 评价列表（按时间倒序，根据权限显示）
     */
    private List<TalentEvaluationVO> evaluations;

    /**
     * 各维度平均评分
     */
    private Map<String, BigDecimal> dimensionScores;

    /**
     * 企业剩余积分
     */
    private BigDecimal companyPoints;

    /**
     * 免费可见评价数量
     */
    private Integer freeEvaluationCount;

    /**
     * 已解锁评价数量
     */
    private Integer unlockedEvaluationCount;

    /**
     * 需要解锁的评价数量
     */
    private Integer lockedEvaluationCount;

    /**
     * 是否可以申请查看联系方式
     */
    private Boolean canRequestContact;

    /**
     * 联系方式是否已授权
     */
    private Boolean contactAuthorized;

    /**
     * 工作经历摘要VO
     */
    @Data
    public static class ProfileSummaryVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long profileId;
        private Long companyId;
        private String companyName;
        private String occupation;
        private String startDate;
        private String endDate;
        private BigDecimal attendanceRate;
        private Boolean hasMajorIncident;
        private String reasonForLeaving;
        private String performanceSummary;
        private Integer visibility;

        /**
         * 是否可以查看详情
         */
        private Boolean canViewDetail;

        /**
         * 是否需要申请查看（visibility=1时）
         */
        private Boolean needRequest;

        /**
         * 是否已获得查看授权（针对visibility=1的档案）
         */
        private Boolean authorized;
    }
}


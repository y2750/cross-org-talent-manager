package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 企业招聘偏好VO
 *
 * @author y
 */
@Data
public class CompanyPreferenceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 偏好职位列表
     */
    private List<String> preferredOccupations;

    /**
     * 偏好标签列表
     */
    private List<EvaluationTagVO> preferredTags;

    /**
     * 排除标签列表
     */
    private List<EvaluationTagVO> excludedTags;

    /**
     * 最低评分要求
     */
    private BigDecimal minScore;

    /**
     * 是否排除有重大违纪记录
     */
    private Boolean excludeMajorIncident;

    /**
     * 最低出勤率要求
     */
    private BigDecimal minAttendanceRate;

    /**
     * 具体要求描述
     */
    private String requirementDescription;
}



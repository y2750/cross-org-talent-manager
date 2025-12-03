package com.crossorgtalentmanager.model.dto.talentmarket;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 企业招聘偏好请求
 *
 * @author y
 */
@Data
public class CompanyPreferenceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 偏好职位列表
     */
    private List<String> preferredOccupations;

    /**
     * 偏好标签ID列表
     */
    private List<Long> preferredTagIds;

    /**
     * 排除标签ID列表
     */
    private List<Long> excludedTagIds;

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
}


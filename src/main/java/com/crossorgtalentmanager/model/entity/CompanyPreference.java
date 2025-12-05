package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业招聘偏好 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("company_preference")
public class CompanyPreference implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 偏好职位（JSON数组）
     */
    private String preferredOccupations;

    /**
     * 偏好标签ID（JSON数组）
     */
    private String preferredTagIds;

    /**
     * 排除标签ID（JSON数组）
     */
    private String excludedTagIds;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}



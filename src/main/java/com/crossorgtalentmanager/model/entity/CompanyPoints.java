package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业积分变动记录 实体类。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("company_points")
public class CompanyPoints implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 所属企业
     */
    private Long companyId;

    /**
     * 积分变动（正/负）
     */
    private BigDecimal points;

    /**
     * 变动原因（1=建立档案，2=员工评价）
     */
    private Integer changeReason;

    /**
     * 关联员工id
     */
    private Long withEmployeeId;

    /**
     * 变动说明（后端自动生成）
     */
    private String changeDescription;

    /**
     * 变动日期
     */
    private LocalDate changeDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;

}

package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工档案信息 实体类。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("employee_profile")
public class EmployeeProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 所属企业
     */
    private Long companyId;

    /**
     * 入职日期
     */
    private Date startDate;

    /**
     * 离职日期
     */
    private Date endDate;

    /**
     * 绩效摘要
     */
    private String performanceSummary;

    /**
     * 出勤率
     */
    private BigDecimal attendanceRate;

    /**
     * 重大违纪
     */
    private Boolean hasMajorIncident;

    /**
     * 离职原因
     */
    private String reasonForLeaving;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 年薪
     */
    private BigDecimal annualSalary;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;

    /**
     * 操作人id
     */
    private Long operatorId;

}

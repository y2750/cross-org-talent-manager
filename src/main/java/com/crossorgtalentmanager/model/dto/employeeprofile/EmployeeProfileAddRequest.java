package com.crossorgtalentmanager.model.dto.employeeprofile;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.io.Serial;

/**
 * 员工档案添加请求。
 */
@Data
public class EmployeeProfileAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 员工 ID */
    private Long employeeId;

    /** 入职日期 */
    private Date startDate;

    /** 离职日期（可空） */
    private Date endDate;

    /** 绩效摘要 */
    private String performanceSummary;

    /** 出勤率（0-1，保留若干小数） */
    private BigDecimal attendanceRate;

    /** 是否存在重大违纪 */
    private Boolean hasMajorIncident;

    /** 离职原因 */
    private String reasonForLeaving;

    /** 职业/岗位 */
    private String occupation;

    /** 年薪（单位：元） */
    private BigDecimal annualSalary;
}

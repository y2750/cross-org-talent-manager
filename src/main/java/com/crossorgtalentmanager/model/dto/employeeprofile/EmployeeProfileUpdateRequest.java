package com.crossorgtalentmanager.model.dto.employeeprofile;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.io.Serial;

/**
 * 员工档案更新请求。
 * 
 * 注意：
 * - createTime、updateTime、isDelete 为系统维护字段，不接受前端修改
 * - employeeId、companyId 为身份字段，不允许修改
 * - id 为必填，用于标识要更新的档案
 * - 若前端传入的字段为 null，更新后该字段会被设为 null（覆盖操作）
 */
@Data
public class EmployeeProfileUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 档案 id（必填） */
    private Long id;

    /** 入职日期 */
    private Date startDate;

    /** 离职日期 */
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

    /** 公开范围（0=完全保密，1=对认证企业可见，2=公开） */
    private Integer visibility;

}

package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 员工档案包装类（VO）。
 */
@Data
public class EmployeeProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 员工 ID */
    private Long employeeId;

    /** 所属公司 ID */
    private Long companyId;

    /** 公司名称（用于返回给前端，用于模糊展示） */
    private String companyName;

    /** 入职日期 */
    private Date startDate;

    /** 离职日期 */
    private Date endDate;

    /** 绩效摘要 */
    private String performanceSummary;

    /** 出勤率 */
    private BigDecimal attendanceRate;

    /** 是否存在重大违纪 */
    private Boolean hasMajorIncident;

    /** 离职原因 */
    private String reasonForLeaving;

    /** 职业/岗位 */
    private String occupation;

    /** 年薪（单位：元） */
    private BigDecimal annualSalary;

    /** 创建时间 */
    private java.time.LocalDateTime createTime;

    /** 修改时间 */
    private java.time.LocalDateTime updateTime;

    /** 逻辑删除标志 */
    private Boolean isDelete;

    /** 操作人 ID */
    private Long operatorId;

    /** 该公司的奖惩记录列表（仅用于员工查看自己的档案时） */
    private java.util.List<com.crossorgtalentmanager.model.vo.RewardPunishmentVO> rewardPunishments;
}

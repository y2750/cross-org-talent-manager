package com.crossorgtalentmanager.model.dto.employeeprofile;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 员工档案查询请求（分页）。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmployeeProfileQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 id */
    private Long id;

    /** 员工 id */
    private Long employeeId;

    /** 所属公司 id（由系统或查询参数填充，用于过滤） */
    private Long companyId;

    /** 公司名称（模糊查询） */
    private String companyName;

    /** 入职日期 - 起 */
    private Date startDate;

    /** 入职日期 - 止 */
    private Date endDate;

    /** 职业/岗位（模糊查询） */
    private String occupation;

    /** 年薪下限 */
    private BigDecimal minAnnualSalary;

    /** 年薪上限 */
    private BigDecimal maxAnnualSalary;

    /** 排序字段 */
    private String sortField;

    /** 排序方向：ascend/descend */
    private String sortOrder;

}

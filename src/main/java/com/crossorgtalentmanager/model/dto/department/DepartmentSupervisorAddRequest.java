package com.crossorgtalentmanager.model.dto.department;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加/设置部门主管请求 DTO
 */
@Data
public class DepartmentSupervisorAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 部门 ID */
    private Long departmentId;

    /** 员工 ID (employee 表的 id) */
    private Long employeeId;

    /** true 表示设置为主管，false 表示取消主管 */
    private Boolean isSupervisor;
}

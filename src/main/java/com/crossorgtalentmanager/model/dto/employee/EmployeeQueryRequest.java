package com.crossorgtalentmanager.model.dto.employee;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 员工查询请求（分页）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmployeeQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工 ID
     */
    private Long id;

    /**
     * 姓名（支持模糊查询）
     */
    private String name;

    /**
     * 电话（支持模糊查询）
     */
    private String phone;

    /**
     * 身份证号（精确匹配）
     */
    private String idCardNumber;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 部门 ID
     */
    private Long departmentId;

    /**
     * 在职状态（true/false/null 表示不限）
     */
    private Boolean status;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder;
}

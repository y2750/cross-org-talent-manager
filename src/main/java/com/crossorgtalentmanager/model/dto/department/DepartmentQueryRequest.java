package com.crossorgtalentmanager.model.dto.department;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门查询请求（分页）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class DepartmentQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 所属企业 ID
     */
    private Long companyId;

    /**
     * 部门领导 ID
     */
    private Long leaderId;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder;

}

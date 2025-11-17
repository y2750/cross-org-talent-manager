package com.crossorgtalentmanager.model.dto.department;

import lombok.Data;

/**
 * ClassName: DepartmentAddRequest
 * Package: com.crossorgtalentmanager.model.dto.department
 * Description:
 *
 * @Author
 * @Create 2025/11/17 13:51
 * @Version 1.0
 */
@Data
public class DepartmentAddRequest {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 部门名称
     */
    private String name;

}

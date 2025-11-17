package com.crossorgtalentmanager.model.dto.employee;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * ClassName: EmployeeAddRequest
 * Package: com.crossorgtalentmanager.model.dto
 * Description:
 *
 * @Author
 * @Create 2025/11/21 11:59
 * @Version 1.0
 */
@Data
public class EmployeeCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 所属部门
     */
    private Long departmentId;

}

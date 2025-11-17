package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工基本信息包装类（VO）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class EmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工 ID
     */
    private Long id;

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
     * 所属公司 ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 所属部门 ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 在职状态
     */
    /**
     * 员工照片 URL
     */
    private String photoUrl;
}

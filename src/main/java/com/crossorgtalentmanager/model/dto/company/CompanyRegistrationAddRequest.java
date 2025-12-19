package com.crossorgtalentmanager.model.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 企业注册申请 创建请求
 */
@Data
public class CompanyRegistrationAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 企业名称
     */
    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    /**
     * 企业地址
     */
    @NotBlank(message = "企业地址不能为空")
    private String address;

    /**
     * 企业邮箱
     */
    @NotBlank(message = "企业邮箱不能为空")
    private String companyEmail;

    /**
     * 管理人姓名
     */
    @NotBlank(message = "管理人姓名不能为空")
    private String adminName;

    /**
     * 管理人电话
     */
    @NotBlank(message = "管理人电话不能为空")
    private String adminPhone;

    /**
     * 管理人邮箱
     */
    @NotBlank(message = "管理人邮箱不能为空")
    private String adminEmail;

    /**
     * 管理人身份证号
     */
    @NotBlank(message = "管理人身份证号不能为空")
    private String adminIdNumber;

    /**
     * 管理人账号名（用于登录）
     */
    @NotBlank(message = "管理人账号名不能为空")
    private String adminUsername;

    /**
     * 行业大类
     */
    @NotBlank(message = "行业大类不能为空")
    private String industryCategory;

    /**
     * 行业子类（前端多选）
     */
    private List<String> industries;

    /**
     * 证明材料图片 URL 列表
     */
    @NotNull(message = "证明材料不能为空")
    private List<String> proofImages;
}



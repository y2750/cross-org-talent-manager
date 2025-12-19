package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业注册申请 实体类。
 *
 * 用于企业注册人在前端提交企业信息，由系统管理员审批后正式创建企业与管理员账号。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("company_registration_request")
public class CompanyRegistrationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 企业邮箱
     */
    private String companyEmail;

    /**
     * 管理人姓名
     */
    private String adminName;

    /**
     * 管理人电话
     */
    private String adminPhone;

    /**
     * 管理人邮箱
     */
    private String adminEmail;

    /**
     * 管理人身份证号
     */
    private String adminIdNumber;

    /**
     * 管理人账号名（用于登录）
     */
    private String adminUsername;

    /**
     * 行业大类
     */
    private String industryCategory;

    /**
     * 行业子类（JSON 数组字符串）
     */
    private String industries;

    /**
     * 证明材料（JSON 数组字符串，存储图片 URL 列表）
     */
    private String proofMaterials;

    /**
     * 申请状态（0=待处理，1=已通过，2=已拒绝）
     */
    private Integer status;

    /**
     * 拒绝原因（当 status = 2 时必填）
     */
    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}



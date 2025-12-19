package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业注册申请 返回视图对象
 */
@Data
public class CompanyRegistrationRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String companyName;

    private String address;

    private String companyEmail;

    private String adminName;

    private String adminPhone;

    private String adminEmail;

    private String adminIdNumber;

    private String adminUsername;

    private String industryCategory;

    /**
     * 行业子类列表
     */
    private List<String> industries;

    /**
     * 证明材料图片 URL 列表
     */
    private List<String> proofImages;

    /**
     * 状态（0=待处理，1=已通过，2=已拒绝）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}



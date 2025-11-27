package com.crossorgtalentmanager.model.dto.company;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 企业更新请求
 */
@Data
public class CompanyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long id;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 联系人id
     */
    private Long contactPersonId;

    /**
     * 企业电话
     */
    private String phone;

    /**
     * 企业邮箱
     */
    private String email;

    /**
     * 行业大类
     */
    private String industryCategory;

    /**
     * 行业子类列表
     */
    private List<String> industries;
}


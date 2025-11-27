package com.crossorgtalentmanager.model.dto.company;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName: CompanyAddRequest
 * Package: com.crossorgtalentmanager.model.dto.company
 * Description:
 *
 * @Author
 * @Create 2025/11/8 09:09
 * @Version 1.0
 */
@Data
public class CompanyAddRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

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

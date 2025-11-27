package com.crossorgtalentmanager.model.dto.company;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CompanyQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 联系人姓名
     */
    private String contactPersonName;

    /**
     * 行业大类（用于筛选）
     */
    private String industryCategory;

    /**
     * 行业子类列表（用于筛选，可以选择多个）
     */
    private List<String> industries;

    private static final long serialVersionUID = 1L;
}

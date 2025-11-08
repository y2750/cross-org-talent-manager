package com.crossorgtalentmanager.model.dto.company;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

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
     * 行业
     */
    private String industry;

    private static final long serialVersionUID = 1L;
}

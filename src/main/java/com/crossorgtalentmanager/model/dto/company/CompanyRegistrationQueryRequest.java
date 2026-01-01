package com.crossorgtalentmanager.model.dto.company;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企业注册申请 查询请求
 */
@Data
public class CompanyRegistrationQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 企业名称（模糊查询）
     */
    private String companyName;

    /**
     * 审批状态（0=待处理，1=已通过，2=已拒绝）
     */
    private Integer status;

    private long pageNum = 1;

    private long pageSize = 10;

    private String sortField = "createTime";

    private String sortOrder = "descend";
}













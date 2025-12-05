package com.crossorgtalentmanager.model.dto.profileaccess;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 档案查阅请求查询请求
 */
@Data
public class ProfileAccessRequestQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     */
    private Long id;

    /**
     * 请求企业ID
     */
    private Long requestCompanyId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 档案ID
     */
    private Long employeeProfileId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序
     */
    private String sortOrder;
}


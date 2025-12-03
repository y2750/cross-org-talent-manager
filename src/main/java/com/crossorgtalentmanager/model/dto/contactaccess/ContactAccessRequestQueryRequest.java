package com.crossorgtalentmanager.model.dto.contactaccess;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 联系方式查看请求查询请求
 */
@Data
public class ContactAccessRequestQueryRequest implements Serializable {

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
     * 请求类型
     */
    private Integer requestType;

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

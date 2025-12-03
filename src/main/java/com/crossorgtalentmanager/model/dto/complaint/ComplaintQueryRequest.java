package com.crossorgtalentmanager.model.dto.complaint;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 投诉查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComplaintQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投诉ID
     */
    private Long id;

    /**
     * 投诉人ID
     */
    private Long complainantId;

    /**
     * 被投诉的评价ID
     */
    private Long evaluationId;

    /**
     * 被投诉的企业ID
     */
    private Long companyId;

    /**
     * 投诉类型（1=恶意评价，2=虚假信息，3=其他）
     */
    private Integer type;

    /**
     * 状态（0=待处理，1=处理中，2=已处理，3=已驳回）
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder;
}

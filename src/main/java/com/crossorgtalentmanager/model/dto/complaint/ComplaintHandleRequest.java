package com.crossorgtalentmanager.model.dto.complaint;

import lombok.Data;
import java.io.Serializable;

/**
 * 投诉处理请求
 */
@Data
public class ComplaintHandleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投诉ID
     */
    private Long id;

    /**
     * 处理结果（2=已处理，3=已驳回）
     */
    private Integer status;

    /**
     * 处理结果说明
     */
    private String handleResult;
}

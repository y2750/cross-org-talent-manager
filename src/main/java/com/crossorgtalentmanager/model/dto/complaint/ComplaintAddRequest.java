package com.crossorgtalentmanager.model.dto.complaint;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 新增投诉请求
 */
@Data
public class ComplaintAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被投诉的评价ID
     */
    private Long evaluationId;

    /**
     * 投诉类型（1=恶意评价，2=虚假信息，3=其他）
     */
    private Integer type;

    /**
     * 投诉标题
     */
    private String title;

    /**
     * 投诉内容
     */
    private String content;

    /**
     * 证据图片URL列表
     */
    private List<String> evidenceImages;
}

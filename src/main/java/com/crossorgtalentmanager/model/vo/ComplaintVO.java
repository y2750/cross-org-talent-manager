package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉视图对象
 */
@Data
public class ComplaintVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 投诉人ID
     */
    private Long complainantId;

    /**
     * 投诉人姓名
     */
    private String complainantName;

    /**
     * 被投诉的评价ID
     */
    private Long evaluationId;

    /**
     * 被投诉的企业ID
     */
    private Long companyId;

    /**
     * 被投诉的企业名称
     */
    private String companyName;

    /**
     * 投诉类型（1=恶意评价，2=虚假信息，3=其他）
     */
    private Integer type;

    /**
     * 投诉类型文本
     */
    private String typeText;

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

    /**
     * 状态（0=待处理，1=处理中，2=已处理，3=已驳回）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 被投诉的评价信息
     */
    private EvaluationVO evaluation;
}

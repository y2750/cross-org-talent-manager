package com.crossorgtalentmanager.model.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 评价标签统计视图对象
 */
@Data
public class EvaluationTagStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型（1=正面，2=中性）
     */
    private Integer tagType;

    /**
     * 出现次数
     */
    private Long count;
}


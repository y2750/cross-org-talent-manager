package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 评价标签库 VO
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class EvaluationTagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签类型（1=正面，2=中性）
     */
    private Integer type;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;
}

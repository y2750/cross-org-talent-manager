package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 解锁价格配置VO
 *
 * @author y
 */
@Data
public class UnlockPriceConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 评价类型名称
     */
    private String evaluationTypeName;

    /**
     * 解锁消耗积分
     */
    private BigDecimal pointsCost;

    /**
     * 描述
     */
    private String description;
}


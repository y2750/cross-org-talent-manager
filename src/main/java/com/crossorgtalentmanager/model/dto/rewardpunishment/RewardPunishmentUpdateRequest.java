package com.crossorgtalentmanager.model.dto.rewardpunishment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import lombok.Data;

/**
 * 奖惩记录更新请求 DTO。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class RewardPunishmentUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 奖惩记录 ID
     */
    private Long id;

    /**
     * 类型（1为奖励，2为惩罚）
     */
    private Integer type;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 发生日期
     */
    private Date date;
}

package com.crossorgtalentmanager.model.dto.rewardpunishment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import lombok.Data;

/**
 * 奖惩记录新增请求 DTO。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class RewardPunishmentAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工 ID
     */
    private Long employeeId;

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

package com.crossorgtalentmanager.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 奖惩记录值对象（VO）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class RewardPunishmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 奖惩记录 ID
     */
    private Long id;

    /**
     * 员工 ID
     */
    private Long employeeId;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 员工姓名
     */
    private String employeeName;

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

    /**
     * 操作人员 ID
     */
    private Long operatorId;

    /**
     * 操作人员名称
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

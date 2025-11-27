package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖惩记录 实体类。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("reward_punishment")
public class RewardPunishment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 公司ID
     */
    private Long companyId;

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

    private Long operatorId;

    /**
     * 发生日期
     */
    private Date date;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;

}

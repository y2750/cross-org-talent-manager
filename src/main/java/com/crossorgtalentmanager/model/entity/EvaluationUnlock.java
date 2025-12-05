package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价解锁记录 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("evaluation_unlock")
public class EvaluationUnlock implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 解锁企业ID
     */
    private Long companyId;

    /**
     * 被查看的员工ID
     */
    private Long employeeId;

    /**
     * 被解锁的评价ID
     */
    private Long evaluationId;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 消耗的积分
     */
    private BigDecimal pointsCost;

    /**
     * 解锁时间
     */
    private LocalDateTime unlockTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}



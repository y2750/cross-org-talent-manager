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
 * 解锁价格配置 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unlock_price_config")
public class UnlockPriceConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 解锁消耗积分
     */
    private BigDecimal pointsCost;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isActive;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}



package com.crossorgtalentmanager.model.entity;

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
import java.time.LocalDateTime;

/**
 * 维度评分记录表 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("evaluation_dimension_score")
public class EvaluationDimensionScore implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 评价ID
     */
    private Long evaluationId;

    /**
     * 维度ID
     */
    private Long dimensionId;

    /**
     * 评分（1-5分）
     */
    private Integer score;

    private LocalDateTime createTime;
}

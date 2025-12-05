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
import java.time.LocalDateTime;

/**
 * 人才对比记录 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("talent_compare_record")
public class TalentCompareRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 对比企业ID
     */
    private Long companyId;

    /**
     * 对比员工ID列表（JSON数组格式）
     */
    private String employeeIds;

    /**
     * 对比结果（JSON格式，包含完整的对比数据）
     */
    private String compareResult;

    /**
     * AI分析结果（JSON格式字符串）
     */
    private String aiAnalysisResult;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}


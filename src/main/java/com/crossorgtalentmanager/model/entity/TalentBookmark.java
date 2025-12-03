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
 * 人才收藏 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("talent_bookmark")
public class TalentBookmark implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 收藏企业ID
     */
    private Long companyId;

    /**
     * 被收藏的员工ID
     */
    private Long employeeId;

    /**
     * 收藏备注
     */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}


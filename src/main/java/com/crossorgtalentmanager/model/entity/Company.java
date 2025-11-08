package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业信息管理 实体类。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("company")
public class Company implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 联系人id
     */
    private Long contactPersonId;

    /**
     * 企业电话
     */
    private String phone;

    /**
     * 企业邮箱
     */
    private String email;

    /**
     * 所属行业
     */
    private String industry;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;

}

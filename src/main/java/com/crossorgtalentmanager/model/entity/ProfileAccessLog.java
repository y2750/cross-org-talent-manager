package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案查阅记录 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("profile_access_log")
public class ProfileAccessLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = com.mybatisflex.core.keygen.KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 请求ID
     */
    private Long requestId;

    /**
     * 查阅企业ID
     */
    private Long accessCompanyId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 被查阅的档案ID
     */
    private Long employeeProfileId;

    /**
     * 查阅时间
     */
    private LocalDateTime accessTime;

    /**
     * IP地址
     */
    private String ipAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

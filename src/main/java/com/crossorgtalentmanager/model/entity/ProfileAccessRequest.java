package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
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
 * 档案查阅请求 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("profile_access_request")
public class ProfileAccessRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = com.mybatisflex.core.keygen.KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 请求企业ID
     */
    private Long requestCompanyId;

    /**
     * 请求用户ID（创建请求的用户）
     */
    private Long requestUserId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 请求查阅的档案ID（可选，为空表示查阅所有档案）
     */
    private Long employeeProfileId;

    /**
     * 请求原因
     */
    private String requestReason;

    /**
     * 状态（0=待处理，1=已授权，2=已拒绝，3=已过期）
     */
    private Integer status;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 授权过期时间
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}

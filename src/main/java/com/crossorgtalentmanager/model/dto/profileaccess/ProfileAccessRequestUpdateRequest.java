package com.crossorgtalentmanager.model.dto.profileaccess;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案查阅请求更新请求（用于审批）
 */
@Data
public class ProfileAccessRequestUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     */
    private Long id;

    /**
     * 状态（1=已授权，2=已拒绝）
     */
    private Integer status;

    /**
     * 授权过期时间（授权时必填）
     */
    private LocalDateTime expireTime;
}

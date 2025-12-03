package com.crossorgtalentmanager.model.dto.profileaccess;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案查阅请求添加请求
 */
@Data
public class ProfileAccessRequestAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 授权过期时间（可选）
     */
    private LocalDateTime expireTime;
}

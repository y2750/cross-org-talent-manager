package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案查阅请求 VO
 */
@Data
public class ProfileAccessRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 请求企业ID
     */
    private Long requestCompanyId;

    /**
     * 请求企业名称
     */
    private String requestCompanyName;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

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
     * 状态文本
     */
    private String statusText;

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
}


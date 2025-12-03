package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 联系方式查看请求 VO
 */
@Data
public class ContactAccessRequestVO implements Serializable {

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
     * 请求类型（1=查看电话，2=查看邮箱，3=查看身份证号，4=查看所有联系方式，5=查看电话和邮箱）
     */
    private Integer requestType;

    /**
     * 请求类型文本
     */
    private String requestTypeText;

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

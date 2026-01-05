package com.crossorgtalentmanager.model.dto.company;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企业注册申请 审批请求
 */
@Data
public class CompanyRegistrationApproveRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请 ID
     */
    private Long id;

    /**
     * 审批是否通过（true=通过，false=拒绝）
     */
    private Boolean approved;

    /**
     * 拒绝原因（拒绝时必填）
     */
    private String rejectReason;
}














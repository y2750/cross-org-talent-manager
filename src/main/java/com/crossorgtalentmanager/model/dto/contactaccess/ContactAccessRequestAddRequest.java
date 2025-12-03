package com.crossorgtalentmanager.model.dto.contactaccess;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 联系方式查看请求添加请求
 */
@Data
public class ContactAccessRequestAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 请求类型（1=查看电话，2=查看邮箱，3=查看身份证号，4=查看所有联系方式，5=查看电话和邮箱）
     */
    private Integer requestType;

    /**
     * 请求原因
     */
    private String requestReason;

    /**
     * 授权过期时间（可选）
     */
    private LocalDateTime expireTime;
}

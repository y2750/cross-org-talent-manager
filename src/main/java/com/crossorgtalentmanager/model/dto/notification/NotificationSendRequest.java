package com.crossorgtalentmanager.model.dto.notification;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发布通知请求
 */
@Data
public class NotificationSendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送类型（1=指定用户名，2=指定角色，3=全体用户）
     */
    private Integer sendType;

    /**
     * 用户名（当sendType=1时使用）
     */
    private String username;

    /**
     * 用户角色（当sendType=2时使用，如：employee、hr、company_admin）
     */
    private String userRole;
}


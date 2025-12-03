package com.crossorgtalentmanager.model.dto.notification;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知更新请求
 */
@Data
public class NotificationUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 状态（0=未读，1=已读，2=已处理）
     */
    private Integer status;
}


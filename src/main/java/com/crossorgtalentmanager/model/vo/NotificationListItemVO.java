package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知列表项 VO（简化版，用于列表展示，减少响应内容）
 */
@Data
public class NotificationListItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 状态（0=未读，1=已读，2=已处理）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}


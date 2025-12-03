package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知 VO
 */
@Data
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 截止时间（仅评价任务类通知会使用）
     */
    private LocalDateTime deadline;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型（1=评价任务，2=查阅请求，3=系统通知，4=投诉处理）
     */
    private Integer type;

    /**
     * 通知类型文本
     */
    private String typeText;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联ID（如评价任务ID、请求ID等）
     */
    private Long relatedId;

    /**
     * 状态（0=未读，1=已读，2=已处理）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

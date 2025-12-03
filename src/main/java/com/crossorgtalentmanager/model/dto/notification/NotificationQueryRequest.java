package com.crossorgtalentmanager.model.dto.notification;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知查询请求
 */
@Data
public class NotificationQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型（1=评价任务，2=查阅请求，3=系统通知，4=投诉处理）
     */
    private Integer type;

    /**
     * 状态（0=未读，1=已读，2=已处理）
     */
    private Integer status;

    /**
     * 关联ID
     */
    private Long relatedId;

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField = "create_time";

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder = "descend";
}


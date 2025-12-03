package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notification")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = com.mybatisflex.core.keygen.KeyGenerators.snowFlakeId)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}

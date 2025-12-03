package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投诉实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("complaint")
public class Complaint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 投诉人ID（员工）
     */
    private Long complainantId;

    /**
     * 被投诉的评价ID
     */
    private Long evaluationId;

    /**
     * 被投诉的企业ID
     */
    private Long companyId;

    /**
     * 投诉类型（1=恶意评价，2=虚假信息，3=其他）
     */
    private Integer type;

    /**
     * 投诉标题
     */
    private String title;

    /**
     * 投诉内容
     */
    private String content;

    /**
     * 证据（JSON格式，可包含图片、链接等）
     */
    private String evidence;

    /**
     * 状态（0=待处理，1=处理中，2=已处理，3=已驳回）
     */
    private Integer status;

    /**
     * 处理人ID（系统管理员）
     */
    private Long handlerId;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}

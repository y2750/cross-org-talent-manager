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
 * 人才浏览记录 实体类
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("talent_view_log")
public class TalentViewLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 浏览企业ID
     */
    private Long companyId;

    /**
     * 浏览用户ID
     */
    private Long userId;

    /**
     * 被浏览的员工ID
     */
    private Long employeeId;

    /**
     * 浏览时间
     */
    private LocalDateTime viewTime;

    /**
     * 浏览时长（秒）
     */
    private Integer viewDuration;

    /**
     * 浏览来源（search=搜索结果，recommend=推荐，bookmark=收藏列表）
     */
    private String viewSource;

    /**
     * 搜索关键词（如果来源是搜索）
     */
    private String searchKeyword;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}


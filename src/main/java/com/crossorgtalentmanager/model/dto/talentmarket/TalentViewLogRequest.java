package com.crossorgtalentmanager.model.dto.talentmarket;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人才浏览记录请求
 *
 * @author y
 */
@Data
public class TalentViewLogRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 被浏览的员工ID
     */
    private Long employeeId;

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
}


package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人才浏览记录VO
 *
 * @author y
 */
@Data
public class TalentViewLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 被浏览的员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工照片
     */
    private String employeePhotoUrl;

    /**
     * 最新职位
     */
    private String latestOccupation;

    /**
     * 浏览时间
     */
    private LocalDateTime viewTime;

    /**
     * 浏览时长（秒）
     */
    private Integer viewDuration;

    /**
     * 浏览来源
     */
    private String viewSource;

    /**
     * 搜索关键词
     */
    private String searchKeyword;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 浏览用户ID
     */
    private Long userId;

    /**
     * 浏览用户姓名
     */
    private String userName;
}


package com.crossorgtalentmanager.model.dto.talentmarket;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 人才搜索请求
 *
 * @author y
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TalentSearchRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词搜索（姓名模糊匹配）
     */
    private String keyword;

    /**
     * 职位搜索（根据档案中的occupation字段模糊匹配）
     */
    private String occupation;

    /**
     * 职位列表搜索（多个职位，满足其一即可）
     */
    private List<String> occupations;

    /**
     * 最低平均评分
     */
    private BigDecimal minAverageScore;

    /**
     * 最高平均评分
     */
    private BigDecimal maxAverageScore;

    /**
     * 必须包含的评价标签ID列表（人才必须拥有这些标签中的至少一个）
     */
    private List<Long> includeTagIds;

    /**
     * 必须排除的评价标签ID列表（人才不能拥有这些标签中的任何一个）
     */
    private List<Long> excludeTagIds;

    /**
     * 评价内容模糊搜索（在评价comment字段中搜索）
     */
    private String evaluationKeyword;

    /**
     * 离职原因排除关键词列表（排除包含这些关键词的离职原因）
     */
    private List<String> excludeReasonKeywords;

    /**
     * 性别筛选
     */
    private String gender;

    /**
     * 行业大类筛选
     */
    private String industryCategory;

    /**
     * 是否只看离职人员（status=0）
     */
    private Boolean onlyLeft;

    /**
     * 是否只看在职人员（status=1）
     */
    private Boolean onlyWorking;

    /**
     * 是否排除本公司员工（true=排除，false=不限制）
     */
    private Boolean excludeOwnCompany;

    /**
     * 是否有重大违纪记录（true=排除有违纪的，false=不限制）
     */
    private Boolean excludeMajorIncident;

    /**
     * 最低出勤率
     */
    private BigDecimal minAttendanceRate;

    /**
     * 排序字段（averageScore=按评分排序，evaluationCount=按评价数排序，createTime=按时间排序）
     */
    private String sortField;

    /**
     * 排序方式（asc=升序，desc=降序）
     */
    private String sortOrder;

    /**
     * 是否跳过积分扣除（true=跳过，false或不设置=正常扣除）
     * 用于在恢复搜索条件时不重复扣除积分
     */
    private Boolean skipPointDeduction;
}


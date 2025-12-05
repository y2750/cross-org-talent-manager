package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才对比记录VO
 *
 * @author y
 */
@Data
public class TalentCompareRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 对比企业ID
     */
    private Long companyId;

    /**
     * 对比员工ID列表
     */
    private List<Long> employeeIds;

    /**
     * 对比员工姓名列表
     */
    private List<String> employeeNames;

    /**
     * 对比结果（完整的对比数据，JSON格式）
     */
    private TalentCompareVO compareResult;

    /**
     * AI分析结果（JSON格式字符串）
     */
    private String aiAnalysisResult;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


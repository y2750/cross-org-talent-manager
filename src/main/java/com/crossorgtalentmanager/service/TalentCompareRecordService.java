package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.entity.TalentCompareRecord;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 人才对比记录服务接口
 *
 * @author y
 */
public interface TalentCompareRecordService extends IService<TalentCompareRecord> {

    /**
     * 保存对比记录
     *
     * @param companyId        企业ID
     * @param employeeIds      员工ID列表
     * @param compareResult    对比结果（JSON格式）
     * @param aiAnalysisResult AI分析结果（JSON格式）
     * @return 记录ID
     */
    Long saveCompareRecord(Long companyId, List<Long> employeeIds, String compareResult, String aiAnalysisResult);

    /**
     * 更新AI分析结果
     *
     * @param recordId         记录ID
     * @param aiAnalysisResult AI分析结果（JSON格式）
     */
    void updateAiAnalysisResult(Long recordId, String aiAnalysisResult);

    /**
     * 查询企业的最新对比记录（按创建时间倒序）
     *
     * @param companyId 企业ID
     * @param limit     限制数量
     * @return 对比记录列表
     */
    List<TalentCompareRecord> getLatestRecords(Long companyId, int limit);

    /**
     * 检查是否存在相同的对比记录（相同的员工ID组合）
     *
     * @param companyId   企业ID
     * @param employeeIds 员工ID列表
     * @return 如果存在返回记录，否则返回null
     */
    TalentCompareRecord findExistingRecord(Long companyId, List<Long> employeeIds);

    /**
     * 查找包含任一相同对比人的历史记录（用于AI参考）
     * 相同对比人越多的数据优先级更高，最多返回5条
     *
     * @param companyId   企业ID
     * @param employeeIds 当前对比的员工ID列表
     * @return 历史对比记录列表（按匹配度排序，最多5条）
     */
    List<TalentCompareRecord> findRelatedHistoryRecords(Long companyId, List<Long> employeeIds);
}

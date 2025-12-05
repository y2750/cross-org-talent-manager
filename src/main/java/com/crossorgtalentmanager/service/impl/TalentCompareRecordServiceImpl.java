package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.crossorgtalentmanager.mapper.TalentCompareRecordMapper;
import com.crossorgtalentmanager.model.entity.TalentCompareRecord;
import com.crossorgtalentmanager.service.TalentCompareRecordService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 人才对比记录服务实现类
 *
 * @author y
 */
@Slf4j
@Service
public class TalentCompareRecordServiceImpl extends ServiceImpl<TalentCompareRecordMapper, TalentCompareRecord>
        implements TalentCompareRecordService {

    @Resource
    private TalentCompareRecordMapper talentCompareRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCompareRecord(Long companyId, List<Long> employeeIds, String compareResult, String aiAnalysisResult) {
        if (companyId == null || CollUtil.isEmpty(employeeIds)) {
            throw new IllegalArgumentException("企业ID和员工ID列表不能为空");
        }

        // 排序员工ID列表，确保相同组合的记录可以匹配
        List<Long> sortedIds = new ArrayList<>(employeeIds);
        Collections.sort(sortedIds);
        String employeeIdsJson = JSONUtil.toJsonStr(sortedIds);

        // 检查是否已存在相同的记录
        TalentCompareRecord existingRecord = findExistingRecord(companyId, employeeIds);
        if (existingRecord != null) {
            // 如果已存在，更新记录
            existingRecord.setCompareResult(compareResult);
            if (aiAnalysisResult != null) {
                existingRecord.setAiAnalysisResult(aiAnalysisResult);
            }
            boolean updated = this.updateById(existingRecord);
            if (!updated) {
                throw new RuntimeException("更新对比记录失败");
            }
            log.info("更新对比记录成功，记录ID={}, 企业ID={}, 员工数量={}", existingRecord.getId(), companyId, employeeIds.size());
            return existingRecord.getId();
        }

        // 创建新记录
        TalentCompareRecord record = TalentCompareRecord.builder()
                .companyId(companyId)
                .employeeIds(employeeIdsJson)
                .compareResult(compareResult)
                .aiAnalysisResult(aiAnalysisResult)
                .isDelete(false)
                .build();

        boolean saved = this.save(record);
        if (!saved) {
            throw new RuntimeException("保存对比记录失败");
        }

        log.info("保存对比记录成功，记录ID={}, 企业ID={}, 员工数量={}", record.getId(), companyId, employeeIds.size());
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiAnalysisResult(Long recordId, String aiAnalysisResult) {
        if (recordId == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }

        TalentCompareRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("对比记录不存在");
        }

        record.setAiAnalysisResult(aiAnalysisResult);
        boolean updated = this.updateById(record);
        if (!updated) {
            throw new RuntimeException("更新AI分析结果失败");
        }

        log.info("更新AI分析结果成功，记录ID={}", recordId);
    }

    @Override
    public List<TalentCompareRecord> getLatestRecords(Long companyId, int limit) {
        if (companyId == null) {
            return new ArrayList<>();
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("is_delete", false)
                .orderBy("create_time", false)
                .limit(limit);

        return this.list(queryWrapper);
    }

    @Override
    public TalentCompareRecord findExistingRecord(Long companyId, List<Long> employeeIds) {
        if (companyId == null || CollUtil.isEmpty(employeeIds)) {
            return null;
        }

        // 排序员工ID列表
        List<Long> sortedIds = new ArrayList<>(employeeIds);
        Collections.sort(sortedIds);
        String employeeIdsJson = JSONUtil.toJsonStr(sortedIds);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("employee_ids", employeeIdsJson)
                .eq("is_delete", false)
                .orderBy("create_time", false)
                .limit(1);

        List<TalentCompareRecord> records = this.list(queryWrapper);
        return CollUtil.isNotEmpty(records) ? records.get(0) : null;
    }

    @Override
    public List<TalentCompareRecord> findRelatedHistoryRecords(Long companyId, List<Long> employeeIds) {
        if (companyId == null || CollUtil.isEmpty(employeeIds)) {
            return new ArrayList<>();
        }

        // 查询该企业的所有历史对比记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("is_delete", false)
                .isNotNull("ai_analysis_result")
                .orderBy("create_time", false);

        List<TalentCompareRecord> allRecords = this.list(queryWrapper);
        if (CollUtil.isEmpty(allRecords)) {
            return new ArrayList<>();
        }

        // 将当前员工ID列表转换为Set，便于快速查找
        Set<Long> currentEmployeeIds = new HashSet<>(employeeIds);

        // 计算每条记录的匹配度（相同员工的数量）
        List<RecordWithMatchScore> recordsWithScore = new ArrayList<>();
        for (TalentCompareRecord record : allRecords) {
            try {
                List<Long> recordEmployeeIds = JSONUtil.toList(record.getEmployeeIds(), Long.class);
                if (CollUtil.isEmpty(recordEmployeeIds)) {
                    continue;
                }

                // 计算匹配的员工数量
                Set<Long> recordEmployeeIdsSet = new HashSet<>(recordEmployeeIds);
                recordEmployeeIdsSet.retainAll(currentEmployeeIds); // 求交集
                int matchCount = recordEmployeeIdsSet.size();

                // 只考虑至少有一个相同员工的记录
                if (matchCount > 0) {
                    recordsWithScore.add(new RecordWithMatchScore(record, matchCount));
                }
            } catch (Exception e) {
                log.warn("解析对比记录员工ID失败，记录ID={}, 错误：{}", record.getId(), e.getMessage());
            }
        }

        // 按匹配度降序排序，匹配度相同则按创建时间降序
        recordsWithScore.sort((a, b) -> {
            int scoreCompare = Integer.compare(b.matchScore, a.matchScore);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return b.record.getCreateTime().compareTo(a.record.getCreateTime());
        });

        // 返回最多5条记录
        return recordsWithScore.stream()
                .limit(5)
                .map(r -> r.record)
                .collect(Collectors.toList());
    }

    /**
     * 记录与匹配度的包装类
     */
    private static class RecordWithMatchScore {
        TalentCompareRecord record;
        int matchScore;

        RecordWithMatchScore(TalentCompareRecord record, int matchScore) {
            this.record = record;
            this.matchScore = matchScore;
        }
    }
}


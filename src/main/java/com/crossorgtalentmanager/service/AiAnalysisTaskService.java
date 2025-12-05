package com.crossorgtalentmanager.service;

import java.util.List;

/**
 * AI分析任务服务接口
 * 用于管理AI分析的异步任务
 *
 * @author y
 */
public interface AiAnalysisTaskService {
    
    /**
     * 提交AI分析任务并返回任务ID
     *
     * @param companyId 公司ID
     * @param employeeIds 员工ID列表
     * @param inputData AI输入数据
     * @return 任务ID
     */
    String submitTask(Long companyId, List<Long> employeeIds, String inputData);
    
    /**
     * 获取AI分析任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态（processing=处理中, completed=已完成, failed=失败）
     */
    String getTaskStatus(String taskId);
    
    /**
     * 获取AI分析结果
     *
     * @param taskId 任务ID
     * @return AI分析结果（如果任务未完成返回null）
     */
    String getTaskResult(String taskId);
    
    /**
     * 获取任务错误信息
     *
     * @param taskId 任务ID
     * @return 错误信息（如果任务失败）
     */
    String getTaskError(String taskId);
}

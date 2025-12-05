package com.crossorgtalentmanager.service.impl;

import com.crossorgtalentmanager.ai.AiTalentComparisonService;
import com.crossorgtalentmanager.service.AiAnalysisTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI分析任务服务实现类
 * 用于管理AI分析的异步任务
 *
 * @author y
 */
@Slf4j
@Service
public class AiAnalysisTaskServiceImpl implements AiAnalysisTaskService {
    
    @Resource
    private AiTalentComparisonService aiTalentComparisonService;
    
    // 任务状态和结果缓存（key: taskId, value: TaskInfo）
    private final ConcurrentHashMap<String, TaskInfo> taskCache = new ConcurrentHashMap<>();
    
    // 用于清理任务的线程池
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    
    // AI任务执行线程池（专门用于执行AI分析任务，避免阻塞）
    // 核心线程数：10，最大线程数：50，队列大小：100
    // 这样可以支持多个AI任务并发执行，不会互相阻塞
    private final ExecutorService aiTaskExecutor = new ThreadPoolExecutor(
            10, // 核心线程数
            50, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new LinkedBlockingQueue<>(100), // 任务队列
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ai-task-executor-" + (++counter));
                    thread.setDaemon(false);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
    );
    
    // 任务信息内部类
    private static class TaskInfo {
        String status; // processing, completed, failed
        String result;
        String error;
        long createTime;
        
        TaskInfo() {
            this.status = "processing";
            this.createTime = System.currentTimeMillis();
        }
    }
    
    @Override
    public String submitTask(Long companyId, List<Long> employeeIds, String inputData) {
        // 生成任务ID（基于公司ID和员工ID列表）
        String taskId = generateTaskId(companyId, employeeIds);
        
        // 创建任务信息
        TaskInfo taskInfo = new TaskInfo();
        taskCache.put(taskId, taskInfo);
        
        log.info("提交AI分析任务，任务ID={}, 公司ID={}, 员工数量={}", taskId, companyId, employeeIds.size());
        
        // 异步执行AI分析（使用专门的线程池，避免阻塞）
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行AI分析任务，任务ID={}", taskId);
                String aiResult = aiTalentComparisonService.compareTalents(inputData);
                
                if (aiResult != null && !aiResult.trim().isEmpty()) {
                    taskInfo.status = "completed";
                    taskInfo.result = aiResult;
                    log.info("AI分析任务完成，任务ID={}, 结果长度={}字符", taskId, aiResult.length());
                } else {
                    taskInfo.status = "failed";
                    taskInfo.error = "AI返回的分析结果为空";
                    log.warn("AI分析任务失败，任务ID={}, 原因：结果为空", taskId);
                }
            } catch (Exception e) {
                taskInfo.status = "failed";
                taskInfo.error = "AI分析失败: " + e.getMessage();
                log.error("AI分析任务异常，任务ID={}", taskId, e);
            } finally {
                // 设置清理任务（30分钟后清理）
                cleanupExecutor.schedule(() -> {
                    taskCache.remove(taskId);
                    log.info("清理AI分析任务缓存，任务ID={}", taskId);
                }, 30, TimeUnit.MINUTES);
            }
        }, aiTaskExecutor); // 使用专门的线程池执行，避免使用共享的ForkJoinPool
        
        return taskId;
    }
    
    @Override
    public String getTaskStatus(String taskId) {
        TaskInfo taskInfo = taskCache.get(taskId);
        if (taskInfo == null) {
            return "not_found";
        }
        return taskInfo.status;
    }
    
    @Override
    public String getTaskResult(String taskId) {
        TaskInfo taskInfo = taskCache.get(taskId);
        if (taskInfo == null || !"completed".equals(taskInfo.status)) {
            return null;
        }
        return taskInfo.result;
    }
    
    @Override
    public String getTaskError(String taskId) {
        TaskInfo taskInfo = taskCache.get(taskId);
        if (taskInfo == null || !"failed".equals(taskInfo.status)) {
            return null;
        }
        return taskInfo.error;
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId(Long companyId, List<Long> employeeIds) {
        // 使用公司ID和员工ID列表排序后的哈希值生成任务ID
        List<Long> sortedIds = employeeIds.stream().sorted().collect(java.util.stream.Collectors.toList());
        String key = companyId + "_" + sortedIds.toString();
        return "ai_task_" + Math.abs(key.hashCode()) + "_" + System.currentTimeMillis();
    }
}


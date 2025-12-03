package com.crossorgtalentmanager.schedule;

import com.crossorgtalentmanager.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知清理定时任务
 */
@Slf4j
@Component
public class NotificationCleanupScheduleTask {

    @Resource
    private NotificationService notificationService;

    /**
     * 自动删除已删除且超过7天的通知
     * 每7天的02:00执行一次
     * cron表达式：秒 分 时 日 月 周
     * 由于cron表达式的限制，使用每月的1、8、15、22、29号02:00执行（接近每7天）
     * 表达式：0 0 2 1,8,15,22,29 * ?
     */
    @Scheduled(cron = "0 0 2 1,8,15,22,29 * ?")
    public void cleanupDeletedNotifications() {
        log.info("开始执行通知清理任务：删除已删除且超过7天的通知");
        try {
            int deletedCount = notificationService.permanentlyDeleteOldNotifications(7);
            log.info("通知清理任务完成：成功删除{}条已删除且超过7天的通知记录", deletedCount);
        } catch (Exception e) {
            log.error("通知清理任务执行失败", e);
        }
    }
}


package com.crossorgtalentmanager.mapper;

import com.crossorgtalentmanager.model.entity.Notification;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 通知 Mapper
 */
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 物理删除已删除且超过指定天数的通知
     *
     * @param cutoffTime 截止时间（update_time小于此时间的已删除通知将被物理删除）
     * @return 删除的记录数
     */
    @Delete("DELETE FROM notification WHERE is_delete = 1 AND update_time < #{cutoffTime}")
    int permanentlyDeleteOldNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
}

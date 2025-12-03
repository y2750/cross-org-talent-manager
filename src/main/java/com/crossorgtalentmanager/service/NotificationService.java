package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.notification.NotificationQueryRequest;
import com.crossorgtalentmanager.model.dto.notification.NotificationUpdateRequest;
import com.crossorgtalentmanager.model.entity.Notification;
import com.crossorgtalentmanager.model.vo.NotificationVO;
import com.crossorgtalentmanager.model.vo.NotificationListItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 通知 服务层
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知（默认未读）
     *
     * @param userId    接收用户ID
     * @param type      通知类型
     * @param title     通知标题
     * @param content   通知内容
     * @param relatedId 关联业务ID
     * @return 通知ID
     */
    Long createNotification(Long userId, Integer type, String title, String content, Long relatedId);

    /**
     * 分页查询通知（列表，返回简化版VO）
     *
     * @param queryRequest 查询请求
     * @param loginUserId  登录用户ID
     * @return 分页结果
     */
    Page<NotificationListItemVO> pageNotifications(NotificationQueryRequest queryRequest, Long loginUserId);

    /**
     * 根据ID获取通知详情
     *
     * @param notificationId 通知ID（字符串类型，避免精度丢失）
     * @param loginUserId    登录用户ID
     * @return 通知详情VO
     */
    NotificationVO getNotificationById(String notificationId, Long loginUserId);

    /**
     * 更新通知状态
     *
     * @param updateRequest 更新请求
     * @param loginUserId   登录用户ID
     * @return 是否成功
     */
    Boolean updateNotificationStatus(NotificationUpdateRequest updateRequest, Long loginUserId);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean markAllAsRead(Long userId);

    /**
     * 转换 Notification 实体为 NotificationVO
     *
     * @param notification 通知实体
     * @return VO 对象
     */
    NotificationVO getNotificationVO(Notification notification);

    /**
     * 批量转换 Notification 列表为 NotificationVO 列表
     *
     * @param list 通知列表
     * @return VO 列表
     */
    List<NotificationVO> getNotificationVOList(List<Notification> list);

    /**
     * 根据查询条件构建 QueryWrapper
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper 对象
     */
    QueryWrapper getQueryWrapper(NotificationQueryRequest queryRequest);

    /**
     * 删除单条通知（只能删除已读通知）
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    Boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 批量删除已读通知
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteReadNotifications(Long userId);

    /**
     * 物理删除已删除且超过指定天数的通知（用于定时任务）
     *
     * @param days 天数（超过此天数的已删除通知将被物理删除）
     * @return 删除的记录数
     */
    Integer permanentlyDeleteOldNotifications(int days);

    /**
     * 系统管理员发布通知（批量创建）
     *
     * @param title    通知标题
     * @param content  通知内容
     * @param sendType 发送类型（1=指定用户名，2=指定角色，3=全体用户）
     * @param username 用户名（当sendType=1时使用）
     * @param userRole 用户角色（当sendType=2时使用）
     * @return 成功发送的通知数量
     */
    Integer sendNotification(String title, String content, Integer sendType, String username, String userRole);
}

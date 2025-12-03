package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.NotificationMapper;
import com.crossorgtalentmanager.model.dto.notification.NotificationQueryRequest;
import com.crossorgtalentmanager.model.dto.notification.NotificationUpdateRequest;
import com.crossorgtalentmanager.model.entity.EvaluationTask;
import com.crossorgtalentmanager.model.entity.Notification;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.NotificationStatusEnum;
import com.crossorgtalentmanager.model.enums.NotificationTypeEnum;
import com.crossorgtalentmanager.model.vo.NotificationVO;
import com.crossorgtalentmanager.model.vo.NotificationListItemVO;
import com.crossorgtalentmanager.service.EvaluationTaskService;
import com.crossorgtalentmanager.service.NotificationService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知 服务实现
 */
@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    @Lazy
    private EvaluationTaskService evaluationTaskService;

    @Resource
    private UserService userService;

    @Override
    public Long createNotification(Long userId, Integer type, String title, String content, Long relatedId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "通知接收用户不能为空");
        ThrowUtils.throwIf(type == null || !NotificationTypeEnum.isValidValue(type),
                ErrorCode.PARAMS_ERROR, "通知类型无效");
        ThrowUtils.throwIf(title == null || title.isBlank(), ErrorCode.PARAMS_ERROR, "通知标题不能为空");

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .status(NotificationStatusEnum.UNREAD.getValue())
                .isDelete(false)
                .build();

        boolean save = this.save(notification);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建通知失败");

        log.info("创建通知：userId={}, type={}, title={}, relatedId={}",
                userId, type, title, relatedId);
        return notification.getId();
    }

    @Override
    public Page<NotificationListItemVO> pageNotifications(NotificationQueryRequest queryRequest, Long loginUserId) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        ThrowUtils.throwIf(loginUserId == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        QueryWrapper qw = getQueryWrapper(queryRequest);
        // 只能查询自己的通知
        qw.eq("user_id", loginUserId);

        Page<Notification> page = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), qw);
        Page<NotificationListItemVO> voPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(),
                page.getTotalRow());
        List<NotificationListItemVO> voList = getNotificationListItemVOList(page.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public NotificationVO getNotificationById(String notificationId, Long loginUserId) {
        ThrowUtils.throwIf(notificationId == null || notificationId.isBlank(),
                ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        ThrowUtils.throwIf(loginUserId == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        Long id;
        try {
            id = Long.parseLong(notificationId);
        } catch (NumberFormatException e) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "通知ID格式错误");
            return null; // 不会执行到这里
        }

        Notification notification = this.getById(id);
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        ThrowUtils.throwIf(!notification.getUserId().equals(loginUserId),
                ErrorCode.NO_AUTH_ERROR, "只能查看自己的通知");

        return getNotificationVO(notification);
    }

    /**
     * 将 Notification 列表转换为 NotificationListItemVO 列表（简化版）
     */
    private List<NotificationListItemVO> getNotificationListItemVOList(List<Notification> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(notification -> {
            NotificationListItemVO vo = new NotificationListItemVO();
            vo.setId(notification.getId());
            vo.setTitle(notification.getTitle());
            vo.setStatus(notification.getStatus());

            // 填充状态文本
            NotificationStatusEnum statusEnum = NotificationStatusEnum.getEnumByValue(notification.getStatus());
            if (statusEnum != null) {
                vo.setStatusText(statusEnum.getText());
            }

            vo.setCreateTime(notification.getCreateTime());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateNotificationStatus(NotificationUpdateRequest updateRequest, Long loginUserId) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        ThrowUtils.throwIf(loginUserId == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(updateRequest.getStatus() == null,
                ErrorCode.PARAMS_ERROR, "状态不能为空");
        ThrowUtils.throwIf(!NotificationStatusEnum.isValidValue(updateRequest.getStatus()),
                ErrorCode.PARAMS_ERROR, "状态值无效");

        Notification notification = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        ThrowUtils.throwIf(!notification.getUserId().equals(loginUserId),
                ErrorCode.NO_AUTH_ERROR, "只能更新自己的通知");

        notification.setStatus(updateRequest.getStatus());
        boolean update = this.updateById(notification);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新通知状态失败");

        log.info("更新通知状态：notificationId={}, status={}, userId={}",
                updateRequest.getId(), updateRequest.getStatus(), loginUserId);
        return true;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        return this.count(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("status", NotificationStatusEnum.UNREAD.getValue()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAllAsRead(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        List<Notification> unreadNotifications = this.list(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("status", NotificationStatusEnum.UNREAD.getValue()));

        if (CollUtil.isEmpty(unreadNotifications)) {
            return true;
        }

        for (Notification notification : unreadNotifications) {
            notification.setStatus(NotificationStatusEnum.READ.getValue());
        }

        boolean update = this.updateBatch(unreadNotifications);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "批量更新通知状态失败");

        log.info("标记所有通知为已读：userId={}, count={}", userId, unreadNotifications.size());
        return true;
    }

    @Override
    public NotificationVO getNotificationVO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationVO vo = new NotificationVO();
        BeanUtil.copyProperties(notification, vo);

        // 如果是评价任务类通知，填充截止时间
        if (NotificationTypeEnum.EVALUATION_TASK.getValue().equals(notification.getType())
                && notification.getRelatedId() != null) {
            EvaluationTask task = evaluationTaskService.getById(notification.getRelatedId());
            if (task != null && task.getDeadline() != null) {
                vo.setDeadline(task.getDeadline());
            }
        }

        // 填充通知类型文本
        NotificationTypeEnum typeEnum = NotificationTypeEnum.getEnumByValue(notification.getType());
        if (typeEnum != null) {
            vo.setTypeText(typeEnum.getText());
        }

        // 填充状态文本
        NotificationStatusEnum statusEnum = NotificationStatusEnum.getEnumByValue(notification.getStatus());
        if (statusEnum != null) {
            vo.setStatusText(statusEnum.getText());
        }

        return vo;
    }

    @Override
    public List<NotificationVO> getNotificationVOList(List<Notification> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(this::getNotificationVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(NotificationQueryRequest queryRequest) {
        if (queryRequest == null) {
            return QueryWrapper.create();
        }

        QueryWrapper qw = QueryWrapper.create();

        if (queryRequest.getId() != null) {
            qw.eq("id", queryRequest.getId());
        }

        if (queryRequest.getUserId() != null) {
            qw.eq("user_id", queryRequest.getUserId());
        }

        if (queryRequest.getType() != null) {
            qw.eq("type", queryRequest.getType());
        }

        if (queryRequest.getStatus() != null) {
            qw.eq("status", queryRequest.getStatus());
        }

        if (queryRequest.getRelatedId() != null) {
            qw.eq("related_id", queryRequest.getRelatedId());
        }

        // 排序
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (sortField != null && !sortField.isEmpty()) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("create_time", false);
        }

        return qw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteNotification(Long notificationId, Long userId) {
        ThrowUtils.throwIf(notificationId == null, ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        Notification notification = this.getById(notificationId);
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        ThrowUtils.throwIf(!notification.getUserId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "只能删除自己的通知");
        // 只能删除已读通知
        ThrowUtils.throwIf(!NotificationStatusEnum.READ.getValue().equals(notification.getStatus()),
                ErrorCode.PARAMS_ERROR, "只能删除已读通知");

        boolean result = this.removeById(notificationId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除通知失败");

        log.info("删除通知：notificationId={}, userId={}", notificationId, userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteReadNotifications(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        List<Notification> readNotifications = this.list(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("status", NotificationStatusEnum.READ.getValue()));

        if (CollUtil.isEmpty(readNotifications)) {
            return true;
        }

        List<Long> ids = readNotifications.stream()
                .map(Notification::getId)
                .collect(java.util.stream.Collectors.toList());

        boolean result = this.removeByIds(ids);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量删除已读通知失败");

        log.info("批量删除已读通知：userId={}, count={}", userId, readNotifications.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer permanentlyDeleteOldNotifications(int days) {
        ThrowUtils.throwIf(days <= 0, ErrorCode.PARAMS_ERROR, "天数必须大于0");

        // 计算截止时间：当前时间减去指定天数
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);

        // 使用Mapper的物理删除方法
        int deletedCount = notificationMapper.permanentlyDeleteOldNotifications(cutoffTime);

        log.info("物理删除已删除且超过{}天的通知：删除{}条记录，截止时间：{}", days, deletedCount, cutoffTime);
        return deletedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer sendNotification(String title, String content, Integer sendType, String username, String userRole) {
        ThrowUtils.throwIf(title == null || title.isBlank(), ErrorCode.PARAMS_ERROR, "通知标题不能为空");
        ThrowUtils.throwIf(content == null || content.isBlank(), ErrorCode.PARAMS_ERROR, "通知内容不能为空");
        ThrowUtils.throwIf(sendType == null || sendType < 1 || sendType > 3,
                ErrorCode.PARAMS_ERROR, "发送类型无效（1=指定用户名，2=指定角色，3=全体用户）");

        List<User> targetUsers = new ArrayList<>();

        if (Integer.valueOf(1).equals(sendType)) {
            // 指定用户名
            ThrowUtils.throwIf(username == null || username.isBlank(),
                    ErrorCode.PARAMS_ERROR, "指定用户名时，用户名不能为空");
            User user = userService.list(
                    QueryWrapper.create().eq("username", username)).stream().findFirst().orElse(null);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在：" + username);
            targetUsers.add(user);
        } else if (Integer.valueOf(2).equals(sendType)) {
            // 指定角色
            ThrowUtils.throwIf(userRole == null || userRole.isBlank(),
                    ErrorCode.PARAMS_ERROR, "指定角色时，角色不能为空");
            List<User> users = userService.list(
                    QueryWrapper.create().eq("user_role", userRole));
            ThrowUtils.throwIf(CollUtil.isEmpty(users),
                    ErrorCode.NOT_FOUND_ERROR, "未找到角色为" + userRole + "的用户");
            targetUsers.addAll(users);
        } else if (Integer.valueOf(3).equals(sendType)) {
            // 全体用户
            List<User> allUsers = userService.list();
            ThrowUtils.throwIf(CollUtil.isEmpty(allUsers),
                    ErrorCode.NOT_FOUND_ERROR, "系统中没有用户");
            targetUsers.addAll(allUsers);
        }

        // 批量创建通知
        int successCount = 0;
        for (User user : targetUsers) {
            try {
                Long notificationId = createNotification(
                        user.getId(),
                        NotificationTypeEnum.SYSTEM.getValue(),
                        title,
                        content,
                        null);
                if (notificationId != null) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("为用户{}创建通知失败：{}", user.getUsername(), e.getMessage());
            }
        }

        log.info("系统管理员发布通知：发送类型={}, 目标用户数={}, 成功发送={}", sendType, targetUsers.size(), successCount);
        return successCount;
    }
}

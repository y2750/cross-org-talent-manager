package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.notification.NotificationQueryRequest;
import com.crossorgtalentmanager.model.dto.notification.NotificationSendRequest;
import com.crossorgtalentmanager.model.dto.notification.NotificationUpdateRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.NotificationVO;
import com.crossorgtalentmanager.model.vo.NotificationListItemVO;
import com.crossorgtalentmanager.service.NotificationService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.annotation.AuthCheck;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 通知 控制层
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    /**
     * 分页查询当前登录用户的通知（列表，返回简化版VO）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<NotificationListItemVO>> listNotifications(@RequestBody NotificationQueryRequest queryRequest,
                                                                HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Page<NotificationListItemVO> page = notificationService.pageNotifications(queryRequest, loginUser.getId());
        return ResultUtils.success(page);
    }

    /**
     * 根据ID获取通知详情（接受字符串类型ID，避免精度丢失）
     */
    @GetMapping("/get/vo")
    public BaseResponse<NotificationVO> getNotificationById(@RequestParam String id,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id.isBlank(), ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        NotificationVO notification = notificationService.getNotificationById(id, loginUser.getId());
        return ResultUtils.success(notification);
    }

    /**
     * 更新单条通知状态（已读 / 已处理）
     */
    @PutMapping("/update/status")
    public BaseResponse<Boolean> updateNotificationStatus(@RequestBody NotificationUpdateRequest updateRequest,
                                                          HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Boolean result = notificationService.updateNotificationStatus(updateRequest, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户未读通知数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long count = notificationService.getUnreadCount(loginUser.getId());
        return ResultUtils.success(count);
    }

    /**
     * 将当前登录用户的所有通知标记为已读
     */
    @PutMapping("/read/all")
    public BaseResponse<Boolean> markAllAsRead(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = notificationService.markAllAsRead(loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 删除单条通知（只能删除已读通知）
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNotification(@RequestBody com.crossorgtalentmanager.common.DeleteRequest deleteRequest,
                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "通知ID不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Boolean result = notificationService.deleteNotification(deleteRequest.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 批量删除已读通知
     */
    @PostMapping("/delete/read")
    public BaseResponse<Boolean> deleteReadNotifications(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Boolean result = notificationService.deleteReadNotifications(loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 系统管理员发布通知（支持指定用户名、指定角色、全体用户）
     */
    @PostMapping("/send")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> sendNotification(@RequestBody NotificationSendRequest sendRequest,
                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(sendRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "只有系统管理员可以发布通知");

        Integer count = notificationService.sendNotification(
                sendRequest.getTitle(),
                sendRequest.getContent(),
                sendRequest.getSendType(),
                sendRequest.getUsername(),
                sendRequest.getUserRole()
        );
        return ResultUtils.success(count);
    }
}



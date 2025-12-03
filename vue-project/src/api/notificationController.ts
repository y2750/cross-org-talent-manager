// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 分页查询当前登录用户的通知（列表，返回简化版VO） POST /notification/list/page/vo */
export async function listNotifications(
  body: API.NotificationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageNotificationListItemVO>('/notification/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新单条通知状态（已读 / 已处理） PUT /notification/update/status */
export async function updateNotificationStatus(
  body: API.NotificationUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/notification/update/status', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取当前登录用户未读通知数量 GET /notification/unread/count */
export async function getUnreadCount(options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/notification/unread/count', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 将当前登录用户的所有通知标记为已读 PUT /notification/read/all */
export async function markAllAsRead(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/notification/read/all', {
    method: 'PUT',
    ...(options || {}),
  })
}

/** 删除单条通知（只能删除已读通知） POST /notification/delete */
export async function deleteNotification(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/notification/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 批量删除已读通知 POST /notification/delete/read */
export async function deleteReadNotifications(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/notification/delete/read', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 根据ID获取通知详情（接受字符串类型ID，避免精度丢失） GET /notification/get/vo */
export async function getNotificationById(
  params: { id: string },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseNotificationVO>('/notification/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 系统管理员发布通知（支持指定用户名、指定角色、全体用户） POST /notification/send */
export async function sendNotification(
  body: API.NotificationSendRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInteger>('/notification/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}


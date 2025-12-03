// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 创建档案查阅请求 POST /profileAccessRequest/add */
export async function createProfileAccessRequest(
  body: {
    employeeId: string | number
    employeeProfileId?: string | number
    requestReason: string
    expireTime?: string
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/profileAccessRequest/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {
      ...body,
      employeeId: body.employeeId ? String(body.employeeId) : undefined,
      employeeProfileId: body.employeeProfileId ? String(body.employeeProfileId) : undefined,
    },
    ...(options || {}),
  })
}

/** 审批档案查阅请求 PUT /profileAccessRequest/approve */
export async function approveProfileAccessRequest(
  body: API.ProfileAccessRequestUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/profileAccessRequest/approve', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询档案查阅请求 POST /profileAccessRequest/list/page/vo */
export async function listProfileAccessRequestByPage(
  body: API.ProfileAccessRequestQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageProfileAccessRequestVO>('/profileAccessRequest/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}


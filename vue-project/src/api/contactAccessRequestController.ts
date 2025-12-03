// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 创建联系方式查看请求 POST /contactAccessRequest/add */
export async function createContactAccessRequest(
  body: {
    employeeId: string | number
    requestType: number
    requestReason?: string
    expireTime?: string
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/contactAccessRequest/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {
      ...body,
      employeeId: body.employeeId ? String(body.employeeId) : undefined,
    },
    ...(options || {}),
  })
}

/** 审批联系方式查看请求 PUT /contactAccessRequest/approve */
export async function approveContactAccessRequest(
  body: API.ContactAccessRequestUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/contactAccessRequest/approve', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询联系方式查看请求 POST /contactAccessRequest/list/page/vo */
export async function listContactAccessRequestByPage(
  body: API.ContactAccessRequestQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageContactAccessRequestVO>('/contactAccessRequest/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据ID获取联系方式查看请求详情 GET /contactAccessRequest/get/vo */
export async function getContactAccessRequestVOById(
  params: {
    id: string | number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseContactAccessRequestVO>('/contactAccessRequest/get/vo', {
    method: 'GET',
    params: {
      ...params,
      id: params.id ? String(params.id) : undefined, // 确保ID是字符串，避免精度丢失
    },
    ...(options || {}),
  })
}


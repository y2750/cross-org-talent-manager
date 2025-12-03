// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 提交投诉 POST /complaint/add */
export async function addComplaint(
  body: API.ComplaintAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/complaint/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询投诉列表 POST /complaint/list/page/vo */
export async function pageComplaint(
  body: API.ComplaintQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageComplaintVO>('/complaint/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取投诉详情 GET /complaint/detail */
export async function getComplaintDetail(
  params: {
    id: string
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseComplaintDetailVO>('/complaint/detail', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 处理投诉（管理员） POST /complaint/handle */
export async function handleComplaint(
  body: API.ComplaintHandleRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/complaint/handle', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 上传投诉证据图片 POST /complaint/upload/evidence */
export async function uploadEvidenceImages(
  files: File[],
  options?: { [key: string]: any },
) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return request<API.BaseResponseListString>('/complaint/upload/evidence', {
    method: 'POST',
    headers: {
      // 不设置 Content-Type，让浏览器自动设置 multipart/form-data 边界
    },
    data: formData,
    ...(options || {}),
  })
}


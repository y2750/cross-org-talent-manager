// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /department/add */
export async function addDepartment(
  body: API.DepartmentAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/department/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /department/addSupervisor */
export async function addSupervisor(
  body: API.DepartmentSupervisorAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/department/addSupervisor', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /department/get */
export async function getDepartmentById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDepartmentByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseDepartment>('/department/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /department/get/vo */
export async function getDepartmentVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDepartmentVOByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseDepartmentVO>('/department/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /department/list/page/vo */
export async function listDepartmentVoByPage(
  body: API.DepartmentQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDepartmentVO>('/department/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /department/toggle */
export async function toggleDepartmentStatus(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/department/toggle', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /department/update */
export async function updateDepartment(body: API.Department, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/department/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

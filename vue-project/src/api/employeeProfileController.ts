// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /employeeProfile/add */
export async function addEmployeeProfile(
  body: API.EmployeeProfileAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/employeeProfile/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employeeProfile/delete */
export async function deleteEmployeeProfile(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/employeeProfile/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /employeeProfile/get */
export async function getEmployeeProfileById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmployeeProfileByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEmployeeProfile>('/employeeProfile/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /employeeProfile/get/vo */
export async function getEmployeeProfileVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmployeeProfileVOByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEmployeeProfileVO>('/employeeProfile/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employeeProfile/list/page/vo */
export async function listEmployeeProfileVoByPage(
  body: API.EmployeeProfileQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageEmployeeProfileVO>('/employeeProfile/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取当前登录员工的档案列表 POST /employeeProfile/list/page/vo/me */
export async function listMyEmployeeProfileVoByPage(
  body: API.EmployeeProfileQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageEmployeeProfileVO>('/employeeProfile/list/page/vo/me', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /employeeProfile/update */
export async function updateEmployeeProfile(
  body: API.EmployeeProfileUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/employeeProfile/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

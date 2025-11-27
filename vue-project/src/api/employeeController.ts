// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /employee/create */
export async function employeeCreate(
  body: API.EmployeeCreateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/employee/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employee/fire */
export async function fireEmployee(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/employee/fire', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 确认解雇员工 POST /employee/fire/confirm */
export async function confirmFireEmployee(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/employee/fire/confirm', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /employee/get */
export async function getEmployeeById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmployeeByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEmployee>('/employee/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /employee/get/vo */
export async function getEmployeeVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmployeeVOByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEmployeeVO>('/employee/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employee/list/page/vo */
export async function listEmployeeVoByPage(
  body: API.EmployeeQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageEmployeeVO>('/employee/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employee/update */
export async function updateEmployee(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateEmployeeParams,
  body: {},
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/employee/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /employee/update/me */
export async function updateMyProfile(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateMyProfileParams,
  body: {},
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/employee/update/me', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 统计指定公司的员工数量 GET /employee/count */
export async function countEmployees(
  params: {
    companyId: number
    status?: boolean
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/employee/count', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 批量移出部门 POST /employee/batch/remove-from-department */
export async function batchRemoveFromDepartment(
  body: (string | number)[],
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInteger>('/employee/batch/remove-from-department', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 批量添加到部门 POST /employee/batch/add-to-department */
export async function batchAddToDepartment(
  body: {
    employeeIds: (string | number)[]
    departmentId: string | number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInteger>('/employee/batch/add-to-department', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取当前登录员工的员工信息 GET /employee/get/me/vo */
export async function getMyEmployeeVo(options?: { [key: string]: any }) {
  return request<API.BaseResponseEmployeeVO>('/employee/get/me/vo', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取部门同事列表（脱敏） GET /employee/colleagues/department */
export async function getDepartmentColleagues(options?: { [key: string]: any }) {
  return request<API.BaseResponseListEmployeeVO>('/employee/colleagues/department', {
    method: 'GET',
    ...(options || {}),
  })
}

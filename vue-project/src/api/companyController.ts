// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /company/add */
export async function addCompany(body: API.CompanyAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/company/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /company/get */
export async function getCompanyById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompanyByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCompany>('/company/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /company/get/vo */
export async function getCompanyVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompanyVOByIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCompanyVO>('/company/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /company/list/page/vo */
export async function listCompanyVoByPage(
  body: API.CompanyQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageCompanyVO>('/company/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /company/toggle */
export async function toggleCompanyStatus(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/company/toggle', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /company/update */
export async function update(
  body: API.CompanyUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/company/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取公司的积分 GET /company/points */
export async function getCompanyPoints(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompanyPointsParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseNumber>('/company/points', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询公司的积分变动记录 GET /company/points/history */
export async function getCompanyPointsHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompanyPointsHistoryParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageCompanyPointsVO>('/company/points/history', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

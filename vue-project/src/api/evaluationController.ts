// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 新增评价 POST /evaluation/add */
export async function addEvaluation(
  body: API.EvaluationAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/evaluation/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新评价 PUT /evaluation/update */
export async function updateEvaluation(
  body: API.EvaluationUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/evaluation/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除评价 POST /evaluation/delete */
export async function deleteEvaluation(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/evaluation/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取评价详情 GET /evaluation/detail */
export async function getEvaluationDetail(
  params: {
    id: number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEvaluationDetailVO>('/evaluation/detail', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询评价列表 POST /evaluation/list/page/vo */
export async function pageEvaluation(
  body: API.EvaluationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageEvaluationVO>('/evaluation/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 同事评价（离职时触发） POST /evaluation/colleague/add */
export async function addColleagueEvaluation(
  body: API.EvaluationColleagueRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/evaluation/colleague/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 创建离职评价任务 POST /evaluation/resignation/create */
export async function createResignationEvaluationTasks(
  params: {
    employeeId: number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/evaluation/resignation/create', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取待评价的同事列表 GET /evaluation/colleague/pending */
export async function getColleaguesToEvaluate(
  params: {
    employeeId: number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListLong>('/evaluation/colleague/pending', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取评价统计信息 GET /evaluation/statistics */
export async function getEvaluationStatistics(
  params: {
    employeeId: number
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseEvaluationStatisticsVO>('/evaluation/statistics', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 根据查询条件计算五维评价数据 POST /evaluation/dimension-scores */
export async function calculateDimensionScores(
  body: API.EvaluationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<{
    code?: number
    data?: API.EvaluationDimensionScoreVO[]
    message?: string
  }>('/evaluation/dimension-scores', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据查询条件统计标签出现次数 POST /evaluation/tag-statistics */
export async function countTagStatistics(
  body: API.EvaluationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<{
    code?: number
    data?: API.EvaluationTagStatisticsVO[]
    message?: string
  }>('/evaluation/tag-statistics', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}



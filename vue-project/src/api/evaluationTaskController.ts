// @ts-ignore
/* eslint-disable */
import request from '@/utils/request';

/** 创建评价任务 POST /evaluation/task/create */
export async function createEvaluationTasks(
  body: API.EvaluationTaskCreateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInteger>('/evaluation/task/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 创建季度评价任务 POST /evaluation/task/create/quarterly */
export async function createQuarterlyEvaluationTasks(
  params: {
    departmentId: number;
    periodYear: number;
    periodQuarter: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInteger>('/evaluation/task/create/quarterly', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 分页查询评价任务列表 POST /evaluation/task/list/page/vo */
export async function pageEvaluationTasks(
  body: API.EvaluationTaskQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageEvaluationTaskVO>('/evaluation/task/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取待评价任务数量 GET /evaluation/task/pending/count */
export async function getPendingTaskCount(options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/evaluation/task/pending/count', {
    method: 'GET',
    ...(options || {}),
  });
}



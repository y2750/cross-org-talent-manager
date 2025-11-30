// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 获取所有启用的评价标签列表 GET /evaluationTag/list/active */
export async function listActiveTags(options?: { [key: string]: any }) {
  return request<API.BaseResponseListEvaluationTagVO>('/evaluationTag/list/active', {
    method: 'GET',
    ...(options || {}),
  })
}


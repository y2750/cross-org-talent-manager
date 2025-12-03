// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 搜索人才 POST /talent-market/search */
export async function searchTalents(body: API.TalentSearchRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageTalentVO>('/talent-market/search', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 预估高级搜索消耗的积分 POST /talent-market/search/cost-preview */
export async function getAdvancedSearchCostPreview(body: API.TalentSearchRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBigDecimal>('/talent-market/search/cost-preview', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取人才详情 GET /talent-market/detail/${employeeId} */
export async function getTalentDetail(
  params: { employeeId: string },
  options?: { [key: string]: any },
) {
  const { employeeId } = params
  return request<API.BaseResponseTalentDetailVO>(`/talent-market/detail/${employeeId}`, {
    method: 'GET',
    ...(options || {}),
  })
}

/** 解锁评价 POST /talent-market/unlock */
export async function unlockEvaluation(
  body: API.EvaluationUnlockRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBigDecimal>('/talent-market/unlock', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 批量解锁评价 POST /talent-market/unlock/batch */
export async function batchUnlockEvaluations(
  body: API.EvaluationUnlockRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBigDecimal>('/talent-market/unlock/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 收藏人才 POST /talent-market/bookmark */
export async function bookmarkTalent(
  body: API.TalentBookmarkRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/talent-market/bookmark', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 取消收藏人才 DELETE /talent-market/bookmark/${employeeId} */
export async function unbookmarkTalent(
  params: { employeeId: string },
  options?: { [key: string]: any },
) {
  const { employeeId } = params
  return request<API.BaseResponseBoolean>(`/talent-market/bookmark/${employeeId}`, {
    method: 'DELETE',
    ...(options || {}),
  })
}

/** 获取收藏的人才列表 GET /talent-market/bookmarks */
export async function getBookmarkedTalents(
  params: { pageNum?: number; pageSize?: number },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTalentVO>('/talent-market/bookmarks', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取解锁价格配置 GET /talent-market/unlock-prices */
export async function getUnlockPriceConfigs(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUnlockPriceConfigVO>('/talent-market/unlock-prices', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取所有评价标签 GET /talent-market/tags */
export async function getAllTags(options?: { [key: string]: any }) {
  return request<API.BaseResponseListEvaluationTagVO>('/talent-market/tags', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 检查权限 GET /talent-market/check-permission */
export async function checkPermission(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/talent-market/check-permission', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 记录人才浏览 POST /talent-market/view/record */
export async function recordView(
  body: API.TalentViewLogRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/talent-market/view/record', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新浏览时长 POST /talent-market/view/duration */
export async function updateViewDuration(
  params: { viewLogId: string; viewDuration: number },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/talent-market/view/duration', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取浏览历史 GET /talent-market/view/history */
export async function getViewHistory(
  params: { pageNum?: number; pageSize?: number },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTalentViewLogVO>('/talent-market/view/history', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取浏览统计 GET /talent-market/view/statistics */
export async function getViewStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseViewStatisticsVO>('/talent-market/view/statistics', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 清除浏览记录 DELETE /talent-market/view/history */
export async function clearViewHistory(
  params?: { employeeId?: string },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInt>('/talent-market/view/history', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 保存企业偏好 POST /talent-market/preference */
export async function savePreference(
  body: API.CompanyPreferenceRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/talent-market/preference', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取企业偏好 GET /talent-market/preference */
export async function getPreference(options?: { [key: string]: any }) {
  return request<API.BaseResponseCompanyPreferenceVO>('/talent-market/preference', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取推荐人才 GET /talent-market/recommend */
export async function getRecommendedTalents(
  params: { pageNum?: number; pageSize?: number },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTalentRecommendVO>('/talent-market/recommend', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取相似人才 GET /talent-market/recommend/similar/${employeeId} */
export async function getSimilarTalents(
  params: { employeeId: string; limit?: number },
  options?: { [key: string]: any },
) {
  const { employeeId, ...queryParams } = params
  return request<API.BaseResponseListTalentRecommendVO>(
    `/talent-market/recommend/similar/${employeeId}`,
    {
      method: 'GET',
      params: {
        ...queryParams,
      },
      ...(options || {}),
    },
  )
}

/** 对比人才 POST /talent-market/compare */
export async function compareTalents(
  body: API.TalentCompareRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseTalentCompareVO>('/talent-market/compare', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}


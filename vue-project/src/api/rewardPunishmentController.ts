// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /rewardPunishment/add */
export async function addRewardPunishment(
  body: API.RewardPunishmentAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/rewardPunishment/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /rewardPunishment/delete */
export async function deleteRewardPunishment(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/rewardPunishment/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /rewardPunishment/list/page/vo */
export async function listRewardPunishmentVoByPage(
  body: API.RewardPunishmentQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageRewardPunishmentVO>('/rewardPunishment/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /rewardPunishment/query */
export async function queryByEmployeeId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryByEmployeeIdParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListRewardPunishment>('/rewardPunishment/query', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /rewardPunishment/update */
export async function updateRewardPunishment(
  body: API.RewardPunishmentUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/rewardPunishment/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

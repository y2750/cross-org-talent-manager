// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 提交企业注册申请 POST /company/registration/apply */
export async function applyCompanyRegistration(
  body: API.CompanyRegistrationAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong>('/company/registration/apply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员分页查看企业注册申请列表 POST /company/registration/list/page */
export async function listCompanyRegistrationPage(
  body: API.CompanyRegistrationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageCompanyRegistrationRequestVO>('/company/registration/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员审批企业注册申请 PUT /company/registration/approve */
export async function approveCompanyRegistration(
  body: API.CompanyRegistrationApproveRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/company/registration/approve', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 上传企业注册证明材料图片 POST /company/registration/upload/proof */
export async function uploadCompanyRegistrationProof(
  files: File[],
  options?: { [key: string]: any },
) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return request<API.BaseResponseListString>('/company/registration/upload/proof', {
    method: 'POST',
    data: formData,
    ...(options || {}),
  })
}





import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { showError } from '@/utils/messageHelper'
import { useUserStore } from '@/stores/userStore'
import router from '@/router'

// 直接连接到后端地址（后端已配置CORS）
const API_BASE_URL = 'http://localhost:8123/api'

const instance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 支持跨域请求时携带Cookie
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    console.log('[Request]', config.method?.toUpperCase(), config.url)
    // 从 localStorage 获取用户信息以获取 token
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        // 如果有 token，添加到请求头
        if (user.token) {
          config.headers.Authorization = `Bearer ${user.token}`
        }
      } catch (error) {
        console.error('Failed to parse user info:', error)
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 防止重复跳转登录页的标记
let isRedirectingToLogin = false

// 跳转到登录页的统一方法
const redirectToLogin = () => {
  if (isRedirectingToLogin) {
    return
  }
  isRedirectingToLogin = true
  
  const userStore = useUserStore()
  // 先清除用户数据（包括 localStorage 中的数据）
  userStore.clearUserData()
  
  // 显示错误提示
  showError('登录已过期，请重新登录')
  
  // 使用 router.push 跳转到登录页
  // 如果已经在登录页，重置标记
  if (window.location.pathname !== '/login') {
    // 使用 setTimeout 确保消息提示先显示，然后跳转
    setTimeout(() => {
      router.push('/login').finally(() => {
        // 跳转完成后重置标记
        setTimeout(() => {
          isRedirectingToLogin = false
        }, 500)
      })
    }, 100)
  } else {
    // 如果已经在登录页，重置标记
    setTimeout(() => {
      isRedirectingToLogin = false
    }, 1000)
  }
}

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // 处理业务层错误
    if (data?.code && data.code !== 0) {
      const errorMsg = data.message || '请求失败'

      // 如果是未登录错误（40100），重定向到登录页
      // 40100: 未登录
      // 401: HTTP 状态码（兼容处理）
      // 403: HTTP 状态码（兼容处理）
      if (data.code === 40100 || data.code === 401 || data.code === 403) {
        redirectToLogin()
        return Promise.reject(new Error(errorMsg))
      }

      // 对于员工离职、未分配部门等业务状态，不显示错误提示
      // 这些状态应该由页面静默处理，显示"无公司"、"无部门"等提示
      const silentErrors = [
        '员工未分配部门',
        '未分配部门',
        '未找到部门',
        '员工未分配公司',
        '未分配公司',
        '未找到公司',
        '离职',
        '员工已离职',
      ]
      
      const shouldSilent = silentErrors.some(keyword => errorMsg.includes(keyword))
      
      if (!shouldSilent) {
        showError(errorMsg)
      }
      
      return Promise.reject(new Error(errorMsg))
    }

    return response
  },
  (error) => {
    // 处理网络错误
    if (error.response?.status === 401 || error.response?.status === 403) {
      redirectToLogin()
    } else if (error.response?.status === 500) {
      showError('服务器错误，请稍后重试')
    } else if (error.code === 'ECONNABORTED') {
      showError('请求超时，请检查网络连接')
    } else {
      showError(error.message || '网络请求失败')
    }

    return Promise.reject(error)
  },
)

export default instance

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as userController from '@/api/userController'

export interface UserInfo {
  id: number
  username?: string
  nickname?: string
  userRole: string // backend values: admin, employee_admin, hr, employee
  companyId?: number
  createTime?: string
  updateTime?: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string>('')
  const isLoggedIn = ref(false)

  // 计算属性
  const userRole = computed(() => userInfo.value?.userRole || '')
  const userId = computed(() => userInfo.value?.id || 0)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // 权限检查方法
  const hasRole = (role: string | string[]) => {
    if (!isLoggedIn.value) return false
    const roles = Array.isArray(role) ? role : [role]
    return roles.includes(userRole.value)
  }

  const isSystemAdmin = computed(() => userRole.value === 'admin')
  // 后端可能使用 employee_admin 表示公司管理员，兼容两种写法
  const isCompanyAdmin = computed(() =>
    ['employee_admin', 'companyAdmin', 'company_admin'].includes(userRole.value),
  )
  const isHR = computed(() => userRole.value === 'hr')
  // 后端可能使用 employee 表示员工
  const isNormalUser = computed(() => userRole.value === 'user' || userRole.value === 'employee')

  // 登录
  const login = async (username: string, password: string) => {
    const result = await userController.userLogin({
      username,
      password,
    })

    if (result?.data?.code === 0 && result.data.data) {
      const loginData = result.data.data
      userInfo.value = {
        id: loginData.id || 0,
        username: loginData.username || username,
        nickname: loginData.nickname,
        userRole: loginData.userRole || 'user',
          companyId: loginData.companyId,
      }
      token.value = 'auth-token' // 从响应中获取真实 token
      isLoggedIn.value = true
      // 保存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      localStorage.setItem('isLoggedIn', 'true')
      return true
    }
    return false
  }

  // 登出
  const logout = async () => {
    try {
      await userController.userLogout()
    } catch (error) {
      console.error('Logout error:', error)
    }
    clearUserData()
  }

  // 清除用户数据
  const clearUserData = () => {
    userInfo.value = null
    token.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLoggedIn')
  }

  // 恢复登录状态（从 localStorage）
  const restoreLoginState = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    const savedLoginState = localStorage.getItem('isLoggedIn')

    if (savedUserInfo && savedLoginState === 'true') {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
        isLoggedIn.value = true
        token.value = 'auth-token'
      } catch (error) {
        console.error('Failed to restore login state:', error)
        clearUserData()
      }
    }
  }

  // 获取当前登录用户信息
  const fetchCurrentUser = async () => {
    try {
      const result = await userController.getLoginUser()
      if (result?.data?.code === 0 && result.data.data) {
        const loginData = result.data.data
        userInfo.value = {
          id: loginData.id || 0,
          username: loginData.username || '',
          nickname: loginData.nickname,
          userRole: loginData.userRole || 'user',
          companyId: loginData.companyId,
        }
        isLoggedIn.value = true
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        localStorage.setItem('isLoggedIn', 'true')
      }
    } catch (error) {
      console.error('Failed to fetch current user:', error)
      clearUserData()
    }
  }

  return {
    // 状态
    userInfo,
    token,
    isLoggedIn,
    // 计算属性
    userRole,
    userId,
    username,
    nickname,
    isSystemAdmin,
    isCompanyAdmin,
    isHR,
    isNormalUser,
    // 方法
    hasRole,
    login,
    logout,
    clearUserData,
    restoreLoginState,
    fetchCurrentUser,
  }
})

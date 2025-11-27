import { useUserStore } from '@/stores/userStore'

/**
 * 权限检查组合式函数
 * 用于检查当前用户是否拥有特定的角色权限
 */
export function useRole() {
  const userStore = useUserStore()

  /**
   * 检查用户是否拥有指定的角色
   */
  const hasRole = (role: string | string[]): boolean => {
    return userStore.hasRole(role)
  }

  /**
   * 检查用户是否是系统管理员
   */
  const isSystemAdmin = (): boolean => {
    return userStore.isSystemAdmin
  }

  /**
   * 检查用户是否是公司管理员
   */
  const isCompanyAdmin = (): boolean => {
    return userStore.isCompanyAdmin
  }

  /**
   * 检查用户是否是 HR
   */
  const isHR = (): boolean => {
    return userStore.isHR
  }

  /**
   * 检查用户是否是普通用户
   */
  const isNormalUser = (): boolean => {
    return userStore.isNormalUser
  }

  /**
   * 检查用户是否已登录
   */
  const isLoggedIn = (): boolean => {
    return userStore.isLoggedIn
  }

  /**
   * 获取当前用户角色
   */
  const getCurrentRole = (): string => {
    return userStore.userRole
  }

  /**
   * 获取当前用户名
   */
  const getCurrentUsername = (): string => {
    return userStore.username
  }

  /**
   * 检查是否拥有操作权限（用于系统管理员操作）
   */
  const canManageUsers = (): boolean => {
    return isSystemAdmin()
  }

  /**
   * 检查是否拥有添加 HR 权限
   */
  const canAddHR = (): boolean => {
    return isSystemAdmin() || isCompanyAdmin()
  }

  /**
   * 检查是否拥有修改密码权限
   */
  const canChangePassword = (): boolean => {
    return isLoggedIn()
  }

  /**
   * 检查是否可以访问用户列表
   */
  const canViewUserList = (): boolean => {
    return isSystemAdmin()
  }

  /**
   * 检查是否可以访问本公司 HR 列表
   */
  const canViewCompanyHRs = (): boolean => {
    return isCompanyAdmin() || isSystemAdmin()
  }

  return {
    hasRole,
    isSystemAdmin,
    isCompanyAdmin,
    isHR,
    isNormalUser,
    isLoggedIn,
    getCurrentRole,
    getCurrentUsername,
    canManageUsers,
    canAddHR,
    canChangePassword,
    canViewUserList,
    canViewCompanyHRs,
  }
}

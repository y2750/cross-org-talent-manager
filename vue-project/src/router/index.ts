import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import HomeView from '../views/HomeView.vue'

// 不需要登录的页面
const publicPages = new Set(['/login', '/diagnostic', '/register-company'])

const routes: RouteRecordRaw[] = [
  {
    path: '/register-company',
    name: 'companyRegistrationApply',
    component: () => import('../views/CompanyRegistrationApplyView.vue'),
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
  },
  {
    path: '/diagnostic',
    name: 'diagnostic',
    component: () => import('../views/DiagnosticView.vue'),
  },
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/home',
    name: 'homePath',
    component: HomeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/users',
    name: 'userManagement',
    component: () => import('../views/UserManagementView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'employee_admin', 'company_admin', 'companyAdmin'] },
  },
  {
    path: '/companies',
    name: 'companyManagement',
    component: () => import('../views/CompanyManagementView.vue'),
    meta: { requiresAuth: true, roles: ['admin'] },
  },
  {
    path: '/companies/:id',
    name: 'companyDetail',
    component: () => import('../views/CompanyDetailView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/employees/:employeeId/profile',
    name: 'employeeProfile',
    component: () => import('../views/EmployeeProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/employees',
    name: 'employeeManagement',
    component: () => import('../views/EmployeeManagementView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/employees/:id/detail',
    name: 'employeeDetail',
    component: () => import('../views/EmployeeDetailView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/departments/:id',
    name: 'departmentDetail',
    component: () => import('../views/DepartmentDetailView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/my-company',
    name: 'myCompany',
    component: () => import('../views/MyCompanyView.vue'),
    meta: { requiresAuth: true, roles: ['employee'] },
  },
  {
    path: '/my-profile',
    name: 'myProfile',
    component: () => import('../views/MyProfileView.vue'),
    meta: { requiresAuth: true, roles: ['employee'] },
  },
  {
    path: '/update-profile',
    name: 'updateProfile',
    component: () => import('../views/UpdateProfileView.vue'),
    meta: { requiresAuth: true, roles: ['employee'] },
  },
  {
    path: '/evaluation/tasks',
    name: 'evaluationTasks',
    component: () => import('../views/EvaluationTaskView.vue'),
    meta: { requiresAuth: true, roles: ['hr', 'employee'] }, // admin不应该访问评价任务
  },
  {
    path: '/evaluation/my',
    name: 'myEvaluation',
    component: () => import('../views/MyEvaluationView.vue'),
    meta: { requiresAuth: true, roles: ['employee'] },
  },
  {
    path: '/evaluation/create-quarterly',
    name: 'createQuarterlyEvaluation',
    component: () => import('../views/CreateQuarterlyEvaluationView.vue'),
    meta: { requiresAuth: true, roles: ['employee', 'hr', 'admin'] },
  },
  {
    path: '/notifications',
    name: 'notificationList',
    component: () => import('../views/NotificationListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications/:id',
    name: 'notificationDetail',
    component: () => import('../views/NotificationDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/complaints/management',
    name: 'complaintManagement',
    component: () => import('../views/ComplaintManagementView.vue'),
    meta: { requiresAuth: true, roles: ['admin'] },
  },
  {
    path: '/company/registration/requests',
    name: 'companyRegistrationApproval',
    component: () => import('../views/CompanyRegistrationApprovalView.vue'),
    meta: { requiresAuth: true, roles: ['admin'] },
  },
  {
    path: '/talent-market',
    name: 'talentMarket',
    component: () => import('../views/TalentMarketView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/talent-market/detail/:employeeId',
    name: 'talentMarketDetail',
    component: () => import('../views/TalentMarketDetailView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
  {
    path: '/talent-market/compare',
    name: 'talentCompare',
    component: () => import('../views/TalentCompareView.vue'),
    meta: { requiresAuth: true, roles: ['admin', 'company_admin', 'hr'] },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 检查用户是否已登录且恢复登录状态
const ensureUserLoggedIn = (userStore: ReturnType<typeof useUserStore>): void => {
  if (!userStore.isLoggedIn && localStorage.getItem('isLoggedIn')) {
    userStore.restoreLoginState()
  }
}

// 处理登录重定向
const handleLoginRedirect = (
  userStore: ReturnType<typeof useUserStore>,
  next: Function,
): boolean => {
  if (userStore.isLoggedIn) {
    next('/home')
    return true
  }
  return false
}

// 处理权限检查
const checkRolePermission = (
  to: any,
  userStore: ReturnType<typeof useUserStore>,
  next: Function,
): boolean => {
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const hasRole = to.meta.roles.includes(userStore.userRole)
    if (!hasRole) {
      next('/home')
      return false
    }
  }
  return true
}

// 导航守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 恢复登录状态
  ensureUserLoggedIn(userStore)

  // 检查是否是公开页面
  if (publicPages.has(to.path)) {
    // 已登录用户访问登录页时，重定向到首页
    if (to.path === '/login' && userStore.isLoggedIn) {
      next('/home')
      return
    }
    next()
    return
  }

  // 所有非公开页面都需要登录
  if (!userStore.isLoggedIn) {
    // 未登录，清空可能存在的过期用户数据，然后重定向到登录页
    userStore.clearUserData()
    next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
    return
  }

  // 已登录，检查权限
  if (to.meta.requiresAuth || to.meta.roles) {
    if (checkRolePermission(to, userStore, next)) {
      // 权限检查通过
      next()
    }
  } else {
    // 没有权限要求，直接通过
    next()
  }
})

export default router

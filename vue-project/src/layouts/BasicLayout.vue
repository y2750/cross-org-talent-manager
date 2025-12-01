<template>
  <div v-if="isLoginPage" class="login-layout">
    <router-view />
  </div>
  <a-layout v-else class="app-layout" style="min-height: 100vh">
    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 主体内容区域：左侧固定菜单 + 右侧内容 -->
    <a-layout style="margin-bottom: 80px">
      <!-- 只在非公司详情页面和员工档案页面显示主功能菜单 -->
      <a-layout-sider
        v-if="!shouldHideMainMenu"
        width="220"
        style="background: #fff; border-right: 1px solid #e8e8e8; flex: 0 0 220px"
      >
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0">
          <strong>功能菜单</strong>
        </div>
        <div style="padding: 12px">
          <a-menu mode="inline" :style="{ border: 'none' }" @click="handleSiderClick">
            <a-menu-item key="home">首页</a-menu-item>
            <template v-if="isEmployee">
              <a-menu-item key="myCompany">我的公司</a-menu-item>
              <a-menu-item key="myProfile">我的档案</a-menu-item>
              <a-menu-item key="updateProfile">更新资料</a-menu-item>
              <a-menu-item key="evaluationTasks">
                <a-badge v-if="pendingCount > 0" :count="pendingCount" :number-style="{ backgroundColor: '#ff4d4f' }">
                  <span>评价任务</span>
                </a-badge>
                <span v-else>评价任务</span>
              </a-menu-item>
              <a-menu-item key="myEvaluation">我的评价</a-menu-item>
            </template>
            <template v-else>
              <a-menu-item v-if="canManageUsers" key="users">{{ userManagementLabel }}</a-menu-item>
              <a-menu-item v-if="isSystemAdmin" key="companies">公司管理</a-menu-item>
              <a-menu-item v-if="canAccessCompanyDetail" key="companyDetail">公司管理</a-menu-item>
              <a-menu-item v-if="canManageEmployees" key="employees">员工管理</a-menu-item>
              <a-menu-item v-if="isHR" key="evaluationTasks">
                <a-badge v-if="pendingCount > 0" :count="pendingCount" :number-style="{ backgroundColor: '#ff4d4f' }">
                  <span>评价任务</span>
                </a-badge>
                <span v-else>评价任务</span>
              </a-menu-item>
            </template>
          </a-menu>
        </div>
      </a-layout-sider>

      <!-- 内容区 -->
      <a-layout-content style="padding: 24px">
        <router-view />
      </a-layout-content>
    </a-layout>

    <!-- 底部页脚 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import GlobalHeader from '@/components/layout/GlobalHeader.vue'
import GlobalFooter from '@/components/layout/GlobalFooter.vue'
import { useUserStore } from '@/stores/userStore'
import * as evaluationTaskController from '@/api/evaluationTaskController'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 待评价数量
const pendingCount = ref(0)
let refreshTimer: number | null = null

// 刷新待评价数量
const refreshPendingCount = async () => {
  try {
    const response = await evaluationTaskController.getPendingTaskCount()
    if (response?.data?.code === 0) {
      pendingCount.value = response.data.data || 0
    }
  } catch (error) {
    console.error('Failed to refresh pending count:', error)
  }
}

const isSystemAdmin = computed(() => userStore.userRole === 'admin')
const isCompanyAdmin = computed(() =>
  ['employee_admin', 'company_admin', 'companyAdmin'].includes(userStore.userRole),
)
const isHR = computed(() => userStore.userRole === 'hr')
const isEmployee = computed(() => userStore.userRole === 'employee')
const canManageUsers = computed(() => isSystemAdmin.value || isCompanyAdmin.value)
const canManageEmployees = computed(() => isSystemAdmin.value || isCompanyAdmin.value || isHR.value)
const canAccessCompanyDetail = computed(() => {
  // company_admin 和 hr 可以访问自己的公司详情页面
  return (isCompanyAdmin.value || isHR.value) && userStore.userInfo?.companyId
})
const userManagementLabel = computed(() => (isCompanyAdmin.value ? 'HR管理' : '用户管理'))
const isLoginPage = computed(() => route.path === '/login')
const isCompanyDetailPage = computed(() => route.path.startsWith('/companies/') && route.params.id)
const isEmployeeProfilePage = computed(() => route.path.startsWith('/employees/') && route.name === 'employeeProfile')
const isDepartmentDetailPage = computed(() => route.path.startsWith('/departments/') && route.params.id)
const shouldHideMainMenu = computed(() => isCompanyDetailPage.value || isEmployeeProfilePage.value || isDepartmentDetailPage.value)

const handleSiderClick = (e: any) => {
  if (e.key === 'companyDetail') {
    // company_admin 和 hr 访问自己的公司详情页面
    const companyId = userStore.userInfo?.companyId
    if (companyId) {
      router.push(`/companies/${companyId}`)
    } else {
      console.error('用户没有关联的公司ID')
    }
    return
  }
  
  const map: Record<string, string> = {
    home: '/home',
    users: '/users',
    companies: '/companies',
    employees: '/employees',
    myCompany: '/my-company',
    myProfile: '/my-profile',
    updateProfile: '/update-profile',
    evaluationTasks: '/evaluation/tasks',
    myEvaluation: '/evaluation/my',
    createQuarterlyEvaluation: '/evaluation/create-quarterly',
  }
  const path = map[e.key]
  if (path) router.push(path)
}

// 初始化时加载待评价数量
onMounted(() => {
  // 只有员工、HR或系统管理员才需要显示待评价数量
  if (isEmployee.value || isHR.value || isSystemAdmin.value) {
    refreshPendingCount()
    // 每30秒刷新一次
    refreshTimer = window.setInterval(refreshPendingCount, 30000)
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 监听路由变化，当进入评价任务页面时刷新数量
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/evaluation/tasks' && (isEmployee.value || isHR.value || isSystemAdmin.value)) {
      refreshPendingCount()
    }
  }
)
</script>

<style scoped>
.login-layout {
  min-height: 100vh;
}

.app-layout {
  display: flex;
  flex-direction: column;
}

.app-layout .ant-layout-content {
  flex: 1;
  background: #ffffff;
  min-height: calc(100vh - 64px - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-layout .ant-layout-content {
    padding: 16px !important;
    min-height: calc(100vh - 56px - 80px);
  }
}
</style>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useRole } from '@/composables/useRole'
import {
  UserOutlined,
  TeamOutlined,
  BankOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  BellOutlined,
  SettingOutlined,
  TrophyOutlined,
  BarChartOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import * as notificationController from '@/api/notificationController'
import * as evaluationTaskController from '@/api/evaluationTaskController'
import * as companyRegistrationController from '@/api/companyRegistrationController'

const router = useRouter()
const userStore = useUserStore()
const roleHelpers = useRole()

// 统计数据
const unreadNotificationCount = ref(0)
const pendingTaskCount = ref(0)
const pendingRegistrationCount = ref(0)
const loading = ref(false)

// 角色检查（直接使用 userStore 的 computed 属性，它们已经是 computed 了）
const isSystemAdmin = computed(() => userStore.isSystemAdmin)
const isCompanyAdmin = computed(() => userStore.isCompanyAdmin)
const isHR = computed(() => userStore.isHR)
// 只有 employee 角色才是员工，严格限制，只允许 employee 角色
const isEmployee = computed(() => {
  const role = userStore.userRole
  // 只有明确的 employee 角色才是员工，排除所有其他角色
  return role === 'employee'
})

// 获取未读通知数量
const loadUnreadCount = async () => {
  try {
    const response = await notificationController.getUnreadCount()
    if (response?.data?.code === 0) {
      unreadNotificationCount.value = response.data.data || 0
    }
  } catch (error) {
    console.error('Failed to load unread count:', error)
  }
}

// 获取待评价任务数量
const loadPendingCount = async () => {
  // 系统管理员不能查看评价任务，直接返回
  if (userStore.userRole === 'admin') {
    return
  }
  
  try {
    const response = await evaluationTaskController.getPendingTaskCount()
    if (response?.data?.code === 0) {
      pendingTaskCount.value = response.data.data || 0
    }
  } catch (error: any) {
    // 如果是权限错误（系统管理员不能查看评价任务），静默处理
    if (error?.response?.data?.code === 40101 || error?.message?.includes('系统管理员不能查看评价任务')) {
      return
    }
    console.error('Failed to load pending count:', error)
  }
}

// 获取待处理企业注册申请数量
const loadPendingRegistrationCount = async () => {
  // 只有系统管理员才能查看企业注册申请
  if (userStore.userRole !== 'admin') {
    return
  }
  
  try {
    const response = await companyRegistrationController.listCompanyRegistrationPage({
      pageNum: 1,
      pageSize: 1,
      status: 0, // 待处理状态
    })
    if (response?.data?.code === 0 && response.data.data) {
      pendingRegistrationCount.value = response.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('Failed to load pending registration count:', error)
  }
}

// 快捷入口配置
const quickLinks = computed(() => {
  const links: Array<{
    title: string
    icon: any
    path: string
    color: string
    badge?: number
    visible: boolean
  }> = []

  // 获取当前角色（在 computed 内部直接访问 userStore 的 computed 属性）
  const currentRole = userStore.userRole
  const isSysAdmin = userStore.isSystemAdmin
  const isCompAdmin = userStore.isCompanyAdmin
  const isHr = userStore.isHR
  const isEmp = currentRole === 'employee'

  // 通知
  links.push({
    title: '通知中心',
    icon: BellOutlined,
    path: '/notifications',
    color: '#1890ff',
    badge: unreadNotificationCount.value > 0 ? unreadNotificationCount.value : undefined,
    visible: true,
  })

  // 评价任务（员工和HR可见）
  if (isEmp || isHr) {
    links.push({
      title: '评价任务',
      icon: CheckCircleOutlined,
      path: '/evaluation/tasks',
      color: '#52c41a',
      badge: pendingTaskCount.value > 0 ? pendingTaskCount.value : undefined,
      visible: true,
    })
  }

  // 我的档案（仅员工可见）
  if (isEmp) {
    links.push({
      title: '我的档案',
      icon: FileTextOutlined,
      path: '/my-profile',
      color: '#722ed1',
      visible: true,
    })
  }

  // 我的评价（仅员工可见）
  if (isEmp) {
    links.push({
      title: '我的评价',
      icon: TrophyOutlined,
      path: '/evaluation/my',
      color: '#faad14',
      visible: true,
    })
  }

  // 更新资料（仅员工可见）
  if (isEmp) {
    links.push({
      title: '更新资料',
      icon: SettingOutlined,
      path: '/update-profile',
      color: '#722ed1',
      visible: true,
    })
  }

  // 我的公司（仅员工可见）
  if (isEmp) {
    links.push({
      title: '我的公司',
      icon: BankOutlined,
      path: '/my-company',
      color: '#13c2c2',
      visible: true,
    })
  }

  // 投诉处理（仅系统管理员可见）
  if (isSysAdmin) {
    links.push({
      title: '投诉处理',
      icon: WarningOutlined,
      path: '/complaints/management',
      color: '#ff7875',
      visible: true,
    })
  }

  // 企业注册申请（仅系统管理员可见）
  if (isSysAdmin) {
    links.push({
      title: '企业注册申请',
      icon: BankOutlined,
      path: '/company/registration/requests',
      color: '#1890ff',
      badge: pendingRegistrationCount.value > 0 ? pendingRegistrationCount.value : undefined,
      visible: true,
    })
  }

  // 人才市场（HR、公司管理员、系统管理员可见）
  if (isHr || isCompAdmin || isSysAdmin) {
    links.push({
      title: '人才市场',
      icon: SearchOutlined,
      path: '/talent-market',
      color: '#722ed1',
      visible: true,
    })
  }

  // 员工管理（HR和公司管理员可见，且左侧菜单有显示）
  if (isHr || isCompAdmin || isSysAdmin) {
    links.push({
      title: '员工管理',
      icon: TeamOutlined,
      path: '/employees',
      color: '#fa8c16',
      visible: true,
    })
  }

  // 公司管理（HR、公司管理员和系统管理员可见，且左侧菜单有显示）
  if (isHr || isCompAdmin || isSysAdmin) {
    links.push({
      title: isSysAdmin ? '公司管理' : '公司详情',
      icon: BankOutlined,
      path: isSysAdmin ? '/companies' : `/companies/${userStore.userInfo?.companyId}`,
      color: '#eb2f96',
      visible: true,
    })
  }

  // 用户管理（系统管理员和公司管理员可见，且左侧菜单有显示）
  if (isSysAdmin || isCompAdmin) {
    links.push({
      title: isSysAdmin ? '用户管理' : 'HR管理',
      icon: UserOutlined,
      path: '/users',
      color: '#13c2c2',
      visible: true,
    })
  }

  return links.filter(link => link.visible)
})

// 获取当前时间问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 获取用户角色文本
const roleText = computed(() => {
  if (isSystemAdmin.value) return '系统管理员'
  if (isCompanyAdmin.value) return '公司管理员'
  if (isHR.value) return 'HR'
  if (isEmployee.value) return '员工'
  return '用户'
})

onMounted(async () => {
  loading.value = true
  await Promise.all([
    loadUnreadCount(),
    loadPendingCount(),
    loadPendingRegistrationCount(),
  ])
  loading.value = false
})
</script>

<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="welcome-text">
          <h1 class="greeting">{{ greeting }}，{{ userStore.nickname || userStore.username }}！</h1>
          <p class="subtitle">欢迎使用跨组织人才管理系统</p>
          <a-tag :color="isSystemAdmin.value ? 'red' : isCompanyAdmin.value ? 'blue' : isHR.value ? 'green' : 'default'" class="role-tag">
            {{ roleText }}
          </a-tag>
        </div>
        <div class="banner-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="stat-card" :loading="loading" @click="router.push('/notifications')">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6f7ff; color: #1890ff;">
                <BellOutlined />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ unreadNotificationCount }}</div>
                <div class="stat-label">未读通知</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col v-if="(isEmployee.value || isHR.value) && !isSystemAdmin.value" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="stat-card" :loading="loading" @click="router.push('/evaluation/tasks')">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f6ffed; color: #52c41a;">
                <CheckCircleOutlined />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ pendingTaskCount }}</div>
                <div class="stat-label">待评价任务</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links-section">
      <h2 class="section-title">
        <BarChartOutlined />
        <span>快捷入口</span>
      </h2>
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="link in quickLinks"
          :key="link.path"
          :xs="12"
          :sm="8"
          :md="6"
          :lg="4"
        >
          <a-card
            class="quick-link-card"
            hoverable
            @click="router.push(link.path)"
          >
            <div class="link-content">
              <div class="link-icon" :style="{ color: link.color }">
                <component :is="link.icon" />
              </div>
              <div class="link-title">{{ link.title }}</div>
              <a-badge
                v-if="link.badge"
                :count="link.badge"
                :number-style="{ backgroundColor: '#ff4d4f' }"
                class="link-badge"
              />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 系统信息 -->
    <div class="system-info-section">
      <a-card>
        <template #title>
          <SettingOutlined />
          <span>系统信息</span>
        </template>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="用户名" :span="1">
            {{ userStore.username }}
          </a-descriptions-item>
          <a-descriptions-item label="昵称" :span="1">
            {{ userStore.nickname || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="角色" :span="1">
            {{ roleText }}
          </a-descriptions-item>
          <a-descriptions-item label="登录时间" :span="1">
            {{ new Date().toLocaleString('zh-CN') }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 24px;
  background: linear-gradient(to bottom, #f0f2f5 0%, #ffffff 100%);
  min-height: calc(100vh - 64px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 48px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.banner-content {
  position: relative;
  z-index: 1;
}

.welcome-text {
  color: #1f2937;
}

.greeting {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #1f2937;
}

.subtitle {
  font-size: 16px;
  margin: 0 0 16px 0;
  color: #6b7280;
}

.role-tag {
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 12px;
}

.banner-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(24, 144, 255, 0.05);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  right: -50px;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: -30px;
  right: 100px;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 50%;
  right: 50px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) scale(1.1);
    opacity: 0.5;
  }
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 32px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

/* 快捷入口 */
.quick-links-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-link-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
}

.quick-link-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.link-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  min-height: 120px;
}

.link-icon {
  font-size: 48px;
  margin-bottom: 16px;
  transition: transform 0.3s ease;
}

.quick-link-card:hover .link-icon {
  transform: scale(1.1);
}

.link-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  text-align: center;
}

.link-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

/* 系统信息 */
.system-info-section {
  margin-bottom: 24px;
}

.system-info-section :deep(.ant-card-head-title) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 32px 24px;
  }

  .greeting {
    font-size: 24px;
  }

  .stat-value {
    font-size: 24px;
  }

  .link-icon {
    font-size: 36px;
  }
}
</style>

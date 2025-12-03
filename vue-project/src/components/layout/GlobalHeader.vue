<template>
  <a-layout-header
    :style="{ background: '#fff', padding: '0 24px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }"
  >
    <div style="display: flex; align-items: center; height: 100%; justify-content: space-between">
      <!-- 左侧: Logo 和标题 -->
      <div style="display: flex; align-items: center; gap: 16px">
        <img 
          v-if="logoUrl" 
          :src="logoUrl" 
          alt="Logo" 
          style="height: 48px; width: auto; object-fit: contain; cursor: pointer"
          @click="router.push('/home')"
        />
        <h1 style="margin: 0; font-size: 18px; font-weight: 600; color: #1890ff; cursor: pointer" @click="router.push('/home')">
          {{ title }}
        </h1>
      </div>

      <!-- 中间: 菜单项 -->
      <div style="flex: 1; margin-left: 48px">
        <a-menu
          v-if="isLoggedIn"
          :mode="isMobile ? 'inline' : 'horizontal'"
          :selected-keys="[activeKey]"
          :items="menuItems"
          :inline-collapsed="isMobile"
          style="border-bottom: none; background: transparent"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧: 用户操作 -->
      <div style="display: flex; align-items: center; gap: 24px">
        <template v-if="isLoggedIn">
          <!-- 通知图标（使用阿里巴巴矢量图标库 SVG） -->
          <a-badge
            :count="unreadCount"
            :number-style="{
              backgroundColor: '#ff4d4f',
              fontSize: '9px',
              minWidth: '12px',
              height: '12px',
              lineHeight: '12px',
              padding: '0 3px',
            }"
            :offset="[4, 0]"
          >
            <span class="notification-icon" @click="handleNotificationClick">
              <svg
                t="1764597414635"
                class="icon"
                viewBox="0 0 1024 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="1604"
              >
                <path
                  d="M512 213.333333V128h42.666667v85.333333c145.066667 12.8 256 132.266667 256 277.333334V810.666667H256v-320C256 345.6 366.933333 226.133333 512 213.333333zM298.666667 640v128h469.333333v-256-21.333333c0-128-106.666667-234.666667-234.666667-234.666667S298.666667 362.666667 298.666667 490.666667V640z m128 213.333333h213.333333v42.666667h-213.333333v-42.666667z"
                  fill="#2c2c2c"
                  p-id="1605"
                ></path>
              </svg>
            </span>
          </a-badge>

          <a-dropdown>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item key="profile">
                  <template #icon>
                    <span>👤</span>
                  </template>
                  个人信息
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">
                  <template #icon>
                    <span>🚪</span>
                  </template>
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
            <div style="display: flex; align-items: center; gap: 8px; cursor: pointer">
              <a-avatar>{{ userInitial }}</a-avatar>
              <span>{{ username }}</span>
            </div>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="primary" @click="handleLogin"> 登录 </a-button>
        </template>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useRole } from '@/composables/useRole'
import { message } from 'ant-design-vue'
import * as notificationController from '@/api/notificationController'

interface MenuItem {
  key: string
  label: string
  icon?: any
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isSystemAdmin } = useRole()

const title = ref('跨组织人才管理系统')
const logoUrl = ref('/logo.jpeg')
const isMobile = ref(false)
const activeKey = ref('home')

const isLoggedIn = computed(() => userStore.isLoggedIn)
const username = computed(() => userStore.nickname || userStore.username || '')
const userInitial = computed(() => {
  const name = userStore.nickname || userStore.username || '用户'
  return name.charAt(0).toUpperCase()
})

// 未读通知数量
const unreadCount = ref(0)
let refreshTimer: number | null = null

// 刷新未读通知数量
const refreshUnreadCount = async () => {
  if (!isLoggedIn.value) {
    return
  }
  try {
    const response = await notificationController.getUnreadCount()
    if (response?.data?.code === 0) {
      unreadCount.value = response.data.data || 0
    }
  } catch (error) {
    console.error('Failed to refresh unread count:', error)
  }
}

// 通知图标点击
const handleNotificationClick = () => {
  router.push('/notifications')
}

// 菜单项配置 - 顶部导航只保留非功能性菜单
const menuItems = computed<MenuItem[]>(() => {
  const items: MenuItem[] = [
    { key: 'home', label: '首页' },
    { key: 'about', label: '关于' },
  ]

  return items
})

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  
  // 如果已登录，加载未读通知数量并设置定时刷新
  if (isLoggedIn.value) {
    refreshUnreadCount()
    // 每30秒刷新一次未读数量
    refreshTimer = window.setInterval(() => {
      refreshUnreadCount()
    }, 30000)
  }

  // 监听刷新未读数量事件
  window.addEventListener('refreshUnreadCount', refreshUnreadCount)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('refreshUnreadCount', refreshUnreadCount)
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 监听登录状态变化
watch(
  () => isLoggedIn.value,
  (newValue) => {
    if (newValue) {
      refreshUnreadCount()
      if (!refreshTimer) {
        refreshTimer = window.setInterval(() => {
          refreshUnreadCount()
        }, 30000)
      }
    } else {
      unreadCount.value = 0
      if (refreshTimer) {
        clearInterval(refreshTimer)
        refreshTimer = null
      }
    }
  }
)

// 菜单点击处理
const handleMenuClick = (e: any) => {
  activeKey.value = e.key
  const routeMap: Record<string, string> = {
    home: '/home',
    about: '/about',
  }
  const path = routeMap[e.key]
  if (path) {
    router.push(path)
  }
}

// 用户菜单点击处理
const handleUserMenuClick = async (e: any) => {
  if (e.key === 'profile') {
    router.push('/profile')
  } else if (e.key === 'logout') {
    await userStore.logout()
    message.success('已退出登录')
    router.push('/login')
  }
}

// 登录按钮点击
const handleLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
:deep(.ant-menu) {
  background: transparent;
}

:deep(.ant-menu-item) {
  color: rgba(0, 0, 0, 0.85) !important;
}

:deep(.ant-menu-item-selected) {
  color: #1890ff !important;
  border-bottom-color: #1890ff !important;
}

/* 响应式 */
@media (max-width: 768px) {
  :deep(.ant-menu-horizontal) {
    display: none;
  }
}

.notification-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  cursor: pointer;
}

.notification-icon .icon {
  width: 20px;
  height: 20px;
}

/* 通知红点样式优化 - 缩小至75%并更靠近图标 */
:deep(.ant-badge-count) {
  font-size: 9px !important;
  min-width: 12px !important;
  height: 12px !important;
  line-height: 12px !important;
  padding: 0 3px !important;
  box-shadow: 0 0 0 1px #fff !important;
  border-radius: 6px !important;
}
</style>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useRole } from '@/composables/useRole'
import { message } from 'ant-design-vue'

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
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

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
</style>

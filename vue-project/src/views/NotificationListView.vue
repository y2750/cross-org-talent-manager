<template>
  <div class="notification-list-container">
    <a-card>
      <template #title>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>通知列表</span>
          <div style="display: flex; gap: 8px">
            <a-button
              v-if="isSystemAdmin"
              type="primary"
              @click="showSendModal"
            >
              发布通知
            </a-button>
            <a-button @click="handleMarkAllAsRead" :loading="markAllAsReadLoading">
              一键已读
            </a-button>
            <a-button danger @click="handleDeleteAllRead" :loading="deleteAllReadLoading">
              一键删除
            </a-button>
          </div>
        </div>
      </template>

      <a-list
        :data-source="notificationList"
        :loading="loading"
        :pagination="paginationConfig"
      >
        <template #renderItem="{ item }">
          <a-list-item
            :class="{ 'unread-item': item.status === 0 }"
            style="cursor: pointer; padding: 16px"
            @click="handleItemClick(item)"
          >
            <a-list-item-meta>
              <template #title>
                <div style="display: flex; align-items: center; gap: 8px">
                  <span :style="{ fontWeight: item.status === 0 ? 'bold' : 'normal' }">
                    {{ item.title }}
                  </span>
                  <a-tag v-if="item.status === 0" color="red" size="small">未读</a-tag>
                </div>
              </template>
              <template #description>
                <span style="color: #999; font-size: 12px">
                  {{ formatTime(item.createTime) }}
                </span>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>

        <template #empty>
          <a-empty description="暂无通知" />
        </template>
      </a-list>
    </a-card>

    <!-- 发布通知对话框 -->
    <a-modal
      v-model:open="sendModalVisible"
      title="发布通知"
      :confirm-loading="sendLoading"
      @ok="handleSendNotification"
      @cancel="handleCancelSend"
      width="600px"
    >
      <a-form :model="sendForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="通知标题" :rules="[{ required: true, message: '请输入通知标题' }]">
          <a-input v-model:value="sendForm.title" placeholder="请输入通知标题" :maxlength="100" show-count />
        </a-form-item>

        <a-form-item label="通知内容" :rules="[{ required: true, message: '请输入通知内容' }]">
          <a-textarea
            v-model:value="sendForm.content"
            placeholder="请输入通知内容"
            :rows="6"
            :maxlength="1000"
            show-count
          />
        </a-form-item>

        <a-form-item label="发送方式" :rules="[{ required: true, message: '请选择发送方式' }]">
          <a-radio-group v-model:value="sendForm.sendType">
            <a-radio :value="1">指定用户</a-radio>
            <a-radio :value="2">指定角色</a-radio>
            <a-radio :value="3">全体用户</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item
          v-if="sendForm.sendType === 1"
          label="用户"
          :rules="[{ required: true, message: '请选择或输入用户' }]"
        >
          <a-select
            v-model:value="sendForm.username"
            show-search
            :options="userOptions"
            :filter-option="false"
            :loading="userSearchLoading"
            :not-found-content="userSearchLoading ? '加载中...' : '暂无匹配用户'"
            placeholder="请选择或搜索用户"
            @search="handleUserSearch"
            @focus="handleUserFocus"
            allow-clear
            style="width: 100%"
          >
            <template #option="{ nickname, username }">
              <div style="display: flex; gap: 8px;">
                <span style="font-weight: 500;">{{ nickname }}</span>
                <span style="color: #999;">{{ username }}</span>
              </div>
            </template>
          </a-select>
        </a-form-item>

        <a-form-item
          v-if="sendForm.sendType === 2"
          label="用户角色"
          :rules="[{ required: true, message: '请选择用户角色' }]"
        >
          <a-select v-model:value="sendForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="employee">员工</a-select-option>
            <a-select-option value="hr">HR</a-select-option>
            <a-select-option value="company_admin">公司管理员</a-select-option>
            <a-select-option value="admin">系统管理员</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-if="sendForm.sendType === 3" :wrapper-col="{ offset: 6, span: 18 }">
          <a-alert message="将发送给系统中的所有用户" type="info" show-icon />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import * as notificationController from '@/api/notificationController'
import * as userController from '@/api/userController'
import type { NotificationListItemVO } from '@/api/typings'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const markAllAsReadLoading = ref(false)
const deleteAllReadLoading = ref(false)
const notificationList = ref<NotificationListItemVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 发布通知相关
const isSystemAdmin = computed(() => userStore.userRole === 'admin')
const sendModalVisible = ref(false)
const sendLoading = ref(false)
const sendForm = ref({
  title: '',
  content: '',
  sendType: 1 as number, // 1=指定用户，2=指定角色，3=全体用户
  username: '',
  userRole: '',
})

// 用户搜索相关
const userOptions = ref<Array<{ value: string; label: string; nickname: string; username: string }>>([])
const userSearchLoading = ref(false)
const userSearchTimer = ref<number | null>(null)
const allUsers = ref<Array<{ value: string; label: string; nickname: string; username: string }>>([])
const hasLoadedAllUsers = ref(false)

// 分页配置
const paginationConfig = computed(() => ({
  current: currentPage.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50'],
  onChange: (page: number, size: number) => {
    currentPage.value = page
    pageSize.value = size
    loadNotifications()
  },
  onShowSizeChange: (current: number, size: number) => {
    currentPage.value = current
    pageSize.value = size
    loadNotifications()
  },
}))

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await notificationController.listNotifications({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      sortField: 'create_time',
      sortOrder: 'descend',
    })
    if (response?.data?.code === 0 && response.data.data) {
      notificationList.value = response.data.data.records || []
      total.value = response.data.data.totalRow || 0
    } else {
      message.error(response?.data?.message || '加载通知列表失败')
    }
  } catch (error) {
    console.error('Failed to load notifications:', error)
    message.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 点击通知项
const handleItemClick = (item: NotificationListItemVO) => {
  // 使用字符串类型ID，避免精度丢失
  router.push(`/notifications/${item.id?.toString()}`)
}

// 一键已读
const handleMarkAllAsRead = async () => {
  markAllAsReadLoading.value = true
  try {
    const response = await notificationController.markAllAsRead()
    if (response?.data?.code === 0) {
      message.success('已标记所有通知为已读')
      loadNotifications()
      // 刷新Header中的未读数量
      setTimeout(() => {
        window.dispatchEvent(new Event('refreshUnreadCount'))
      }, 100)
    } else {
      message.error(response?.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('Failed to mark all as read:', error)
    message.error('操作失败')
  } finally {
    markAllAsReadLoading.value = false
  }
}

// 一键删除已读通知
const handleDeleteAllRead = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除所有已读通知吗？此操作不可恢复。',
    onOk: async () => {
      deleteAllReadLoading.value = true
      try {
        const response = await notificationController.deleteReadNotifications()
        if (response?.data?.code === 0) {
          message.success('已删除所有已读通知')
          loadNotifications()
          // 刷新Header中的未读数量
          setTimeout(() => {
            window.dispatchEvent(new Event('refreshUnreadCount'))
          }, 100)
        } else {
          message.error(response?.data?.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete read notifications:', error)
        message.error('删除失败')
      } finally {
        deleteAllReadLoading.value = false
      }
    },
  })
}

// 加载所有用户（用于下拉选择）
const loadAllUsers = async () => {
  if (hasLoadedAllUsers.value) return
  
  userSearchLoading.value = true
  try {
    const response = await userController.listUserVoByPage({
      pageNum: 1,
      pageSize: 1000, // 加载足够多的用户
      sortField: 'createTime',
      sortOrder: 'descend',
    })

    if (response?.data?.code === 0 && response.data.data) {
      const users = response.data.data.records || []
      allUsers.value = users.map((user: any) => ({
        value: user.username || '',
        label: `${user.nickname || ''} ${user.username || ''}`.trim(),
        nickname: user.nickname || '',
        username: user.username || '',
      }))
      userOptions.value = [...allUsers.value]
      hasLoadedAllUsers.value = true
    }
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 用户聚焦时显示全部列表
const handleUserFocus = () => {
  if (!hasLoadedAllUsers.value) {
    loadAllUsers()
  } else {
    // 聚焦时显示全部用户
    userOptions.value = [...allUsers.value]
  }
}

// 用户搜索（前端过滤）
const handleUserSearch = (searchText: string) => {
  if (userSearchTimer.value) {
    clearTimeout(userSearchTimer.value)
  }

  // 如果还没加载用户列表，先加载
  if (!hasLoadedAllUsers.value) {
    loadAllUsers()
    return
  }

  if (!searchText || !searchText.trim()) {
    userOptions.value = [...allUsers.value]
    return
  }

  userSearchTimer.value = window.setTimeout(() => {
    const keyword = searchText.trim().toLowerCase()
    // 在已加载的用户列表中进行前端模糊匹配
    userOptions.value = allUsers.value.filter((user) => {
      return (
        user.nickname.toLowerCase().includes(keyword) ||
        user.username.toLowerCase().includes(keyword)
      )
    })
  }, 100) // 前端过滤，防抖时间可以更短
}

// 显示发布通知对话框
const showSendModal = async () => {
  sendForm.value = {
    title: '',
    content: '',
    sendType: 1,
    username: '',
    userRole: '',
  }
  sendModalVisible.value = true
  // 打开对话框时预加载用户列表
  await loadAllUsers()
}

// 取消发布
const handleCancelSend = () => {
  sendModalVisible.value = false
  sendForm.value = {
    title: '',
    content: '',
    sendType: 1,
    username: '',
    userRole: '',
  }
  if (userSearchTimer.value) {
    clearTimeout(userSearchTimer.value)
    userSearchTimer.value = null
  }
}

// 发布通知
const handleSendNotification = async () => {
  // 表单验证
  if (!sendForm.value.title || !sendForm.value.title.trim()) {
    message.error('请输入通知标题')
    return
  }
  if (!sendForm.value.content || !sendForm.value.content.trim()) {
    message.error('请输入通知内容')
    return
  }
  if (sendForm.value.sendType === 1 && !sendForm.value.username?.trim()) {
    message.error('请选择或输入用户')
    return
  }
  if (sendForm.value.sendType === 2 && !sendForm.value.userRole) {
    message.error('请选择用户角色')
    return
  }

  sendLoading.value = true
  try {
    const response = await notificationController.sendNotification({
      title: sendForm.value.title.trim(),
      content: sendForm.value.content.trim(),
      sendType: sendForm.value.sendType,
      username: sendForm.value.sendType === 1 ? sendForm.value.username.trim() : undefined,
      userRole: sendForm.value.sendType === 2 ? sendForm.value.userRole : undefined,
    })

    if (response?.data?.code === 0) {
      const count = response.data.data || 0
      message.success(`通知发布成功，已发送给 ${count} 个用户`)
      sendModalVisible.value = false
      handleCancelSend()
      // 刷新通知列表
      loadNotifications()
    } else {
      message.error(response?.data?.message || '发布通知失败')
    }
  } catch (error: any) {
    console.error('Failed to send notification:', error)
    message.error(error?.response?.data?.message || '发布通知失败')
  } finally {
    sendLoading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notification-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

.unread-item {
  background-color: #f0f7ff;
  border-left: 3px solid #1890ff;
}

:deep(.ant-list-item) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-list-item:hover) {
  background-color: #fafafa;
}
</style>


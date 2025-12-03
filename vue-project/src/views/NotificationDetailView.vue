<template>
  <div class="notification-detail-container">
    <a-card v-if="notification" :loading="loading">
      <template #title>
        <div style="display: flex; align-items: center; gap: 8px">
          <span>{{ notification.title }}</span>
          <a-tag v-if="notification.status === 0" color="red" size="small">未读</a-tag>
          <a-tag v-else-if="notification.status === 1" color="green" size="small">已读</a-tag>
          <a-tag v-else color="blue" size="small">已处理</a-tag>
        </div>
      </template>

      <div class="notification-content">
        <div class="content-section">
          <h3>通知内容</h3>
          <p style="white-space: pre-wrap; line-height: 1.8">{{ notification.content }}</p>
        </div>

        <div class="info-section">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="通知类型">
              {{ notification.typeText || '未知类型' }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ formatTime(notification.createTime) }}
            </a-descriptions-item>
            <!-- 联系方式查看请求通知显示请求过期时间 -->
            <a-descriptions-item
              v-if="isContactAccessRequestNotification && contactRequest && contactRequest.expireTime"
              label="请求过期时间"
            >
              {{ formatTime(contactRequest.expireTime) }}
            </a-descriptions-item>
            <!-- 有截止时间的显示截止时间，所有通知都不显示更新时间 -->
            <a-descriptions-item
              v-if="notification.deadline"
              label="截止时间"
            >
              {{ formatTime(notification.deadline) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>

      <div class="action-buttons">
        <!-- 联系方式查看请求的同意/拒绝按钮 -->
        <template v-if="showApproveRejectButtons">
          <a-button
            type="primary"
            @click="handleApproveContactRequest"
            :loading="approveLoading"
          >
            同意
          </a-button>
          <a-button
            danger
            @click="handleRejectContactRequest"
            :loading="rejectLoading"
          >
            拒绝
          </a-button>
        </template>
        <!-- 其他类型通知的处理按钮（当不显示同意/拒绝按钮时） -->
        <template v-else>
          <a-button
            v-if="canHandle"
            type="primary"
            @click="handleGoToAction"
            :loading="actionLoading"
          >
            {{ 
              notification.title && notification.title.includes('联系方式查看请求结果通知') && 
              notification.content && notification.content.includes('已同意')
                ? '去查看'
                : '去处理'
            }}
          </a-button>
          <a-button
            v-if="notification.status === 1"
            danger
            @click="handleDelete"
            :loading="deleteLoading"
          >
            删除
          </a-button>
        </template>
      </div>
      
      <!-- 请求已处理提示 -->
      <div 
        v-if="isContactAccessRequestNotification && contactRequest && contactRequest.status !== 0" 
        style="margin-top: 16px;"
      >
        <a-alert
          :message="contactRequest.status === 1 ? '该请求已被同意' : '该请求已被拒绝'"
          type="info"
          show-icon
        />
      </div>
    </a-card>

    <a-card v-else-if="!loading">
      <a-empty description="通知不存在或已被删除" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import * as notificationController from '@/api/notificationController'
import * as contactAccessRequestController from '@/api/contactAccessRequestController'
import type { NotificationVO } from '@/api/typings'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const actionLoading = ref(false)
const deleteLoading = ref(false)
const approveLoading = ref(false)
const rejectLoading = ref(false)
const notification = ref<NotificationVO | null>(null)
const contactRequest = ref<any>(null)

// 是否可以处理（根据通知类型判断）
// 注意：联系方式查看请求通知不在此处处理，会显示专门的同意/拒绝按钮
const canHandle = computed(() => {
  if (!notification.value || !notification.value.relatedId) {
    return false
  }
  // 如果是待处理的联系方式查看请求通知，不显示"去处理"按钮
  if (showApproveRejectButtons.value) {
    return false
  }
  const type = notification.value.type
  // 1=评价任务，2=查阅请求，3=系统通知，4=投诉处理
  // 对于查阅请求（type=2），如果是联系方式查看请求的结果通知，可以跳转
  if (type === 2) {
    // 如果是审批结果通知，可以跳转
    return notification.value.title && notification.value.title.includes('结果通知')
  }
  return type === 1 || type === 4
})

// 是否是联系方式查看请求通知
// 通过标题判断：标题为"联系方式查看请求通知"的是联系方式查看请求
const isContactAccessRequestNotification = computed(() => {
  if (!notification.value) {
    return false
  }
  
  // 检查通知类型
  if (notification.value.type !== 2) {
    return false
  }
  
  // 检查标题（使用更宽松的匹配）
  const title = notification.value.title || ''
  if (!title.includes('联系方式查看请求通知')) {
    return false
  }
  
  // 检查 relatedId（支持字符串和数字）
  const relatedId = notification.value.relatedId
  if (!relatedId && relatedId !== 0 && relatedId !== '0') {
    return false
  }
  
  // 只要标题匹配且有 relatedId，就认为是联系方式查看请求通知
  // 不再检查内容，因为新版本的通知内容可能不包含"联系方式"这几个字
  return true
})

// 是否显示同意/拒绝按钮
// 显示条件：是联系方式查看请求通知，且请求状态为待处理（status === 0）
const showApproveRejectButtons = computed(() => {
  if (!notification.value) {
    return false
  }
  
  // 直接判断是否是联系方式查看请求通知
  // 标题必须包含"联系方式查看请求通知"
  const titleMatch = notification.value.title && 
    notification.value.title.includes('联系方式查看请求通知')
  
  // 内容判断更宽松：只要标题匹配，就认为是联系方式查看请求
  // 不需要检查内容是否包含"联系方式"，因为新版本的通知内容可能不包含这几个字
  const isContactRequest = 
    notification.value.type === 2 &&
    titleMatch &&
    notification.value.relatedId
  
  if (!isContactRequest) {
    return false
  }
  
  // 如果已加载请求详情，检查请求状态是否为待处理（status === 0）
  if (contactRequest.value) {
    return contactRequest.value.status === 0
  }
  
  // 如果还未加载请求详情，先显示按钮（加载后会根据状态更新）
  return true
})

// 加载通知详情
const loadNotificationDetail = async () => {
  const notificationId = route.params.id as string
  if (!notificationId) {
    message.error('通知ID不能为空')
    router.back()
    return
  }

  loading.value = true
  try {
    // 使用新的详情接口，传递字符串类型ID
    const response = await notificationController.getNotificationById({
      id: notificationId,
    })
    if (response?.data?.code === 0 && response.data.data) {
      notification.value = response.data.data

      // 如果是联系方式查看请求通知，加载请求详情
      if (
        notification.value.type === 2 &&
        notification.value.title &&
        notification.value.title.includes('联系方式查看请求通知') &&
        notification.value.relatedId
      ) {
        await loadContactRequestDetail(notification.value.relatedId)
      }

      // 如果通知未读，自动标记为已读
      if (notification.value.status === 0) {
        await markAsRead()
      }
    } else {
      message.error('通知不存在')
      router.back()
    }
  } catch (error) {
    console.error('Failed to load notification detail:', error)
    message.error('加载通知详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 标记为已读
const markAsRead = async () => {
  if (!notification.value) return

  try {
    const response = await notificationController.updateNotificationStatus({
      id: notification.value.id,
      status: 1, // 已读
    })
    if (response?.data?.code === 0) {
      if (notification.value) {
        notification.value.status = 1
        notification.value.statusText = '已读'
      }
      // 刷新Header中的未读数量
      window.dispatchEvent(new Event('refreshUnreadCount'))
    }
  } catch (error) {
    console.error('Failed to mark as read:', error)
  }
}

// 去处理
const handleGoToAction = () => {
  if (!notification.value || !notification.value.relatedId) {
    message.warning('该通知没有关联的业务')
    return
  }

  const type = notification.value.type
  const relatedId = notification.value.relatedId

  actionLoading.value = true
  try {
    if (type === 1) {
      // 评价任务 - 跳转到评价任务页面
      router.push('/evaluation/tasks')
    } else if (type === 2) {
      // 查阅请求
      // 如果是联系方式查看请求的结果通知（已同意），跳转到人才详情页面
      if (
        notification.value.title &&
        notification.value.title.includes('联系方式查看请求结果通知') &&
        notification.value.content &&
        notification.value.content.includes('已同意')
      ) {
        // relatedId 在同意后存储的是员工ID，跳转到人才详情页面
        router.push(`/talent-market/detail/${relatedId}`)
      } else if (
        notification.value.content &&
        notification.value.content.includes('联系方式')
      ) {
        // 联系方式查看请求通知，跳转到我的档案页面
        router.push('/my-profile')
      } else {
        // 其他查阅请求，跳转到我的档案页面查看请求
        router.push('/my-profile')
      }
    } else if (type === 4) {
      // 投诉处理 - 跳转到投诉处理页面
      router.push('/complaints/management')
    } else {
      message.warning('该通知类型暂不支持跳转')
    }
  } catch (error) {
    console.error('Failed to navigate:', error)
    message.error('跳转失败')
  } finally {
    actionLoading.value = false
  }
}

// 加载联系方式查看请求详情
const loadContactRequestDetail = async (requestId: string | number) => {
  try {
    const response = await contactAccessRequestController.getContactAccessRequestVOById({
      id: requestId, // 直接传递，API函数会将其转换为字符串
    })
    if (response?.data?.code === 0 && response.data.data) {
      contactRequest.value = response.data.data
    }
  } catch (error) {
    console.error('Failed to load contact request detail:', error)
  }
}

// 同意联系方式查看请求
const handleApproveContactRequest = () => {
  if (!contactRequest.value) {
    message.error('请求信息不存在')
    return
  }

  Modal.confirm({
    title: '确认同意',
    content: '确定要同意该联系方式查看请求吗？同意后将授权对方查看您的联系方式。',
    onOk: async () => {
      if (!contactRequest.value) return

      approveLoading.value = true
      try {
        const response = await contactAccessRequestController.approveContactAccessRequest({
          id: contactRequest.value.id,
          status: 1, // 已授权
          expireTime: contactRequest.value.expireTime || undefined, // 使用请求中的过期时间
        })
        if (response?.data?.code === 0) {
          message.success('已同意该请求')
          // 刷新请求详情，更新按钮显示状态
          if (notification.value?.relatedId) {
            await loadContactRequestDetail(notification.value.relatedId)
          }
          // 刷新通知列表的未读数量
          window.dispatchEvent(new Event('refreshUnreadCount'))
        } else {
          message.error(response?.data?.message || '操作失败')
        }
      } catch (error: any) {
        console.error('Failed to approve contact request:', error)
        message.error(error?.response?.data?.message || '操作失败')
      } finally {
        approveLoading.value = false
      }
    },
  })
}

// 拒绝联系方式查看请求
const handleRejectContactRequest = () => {
  if (!contactRequest.value) {
    message.error('请求信息不存在')
    return
  }

  Modal.confirm({
    title: '确认拒绝',
    content: '确定要拒绝该联系方式查看请求吗？拒绝后该请求将无法再次处理。',
    onOk: async () => {
      if (!contactRequest.value) return

      rejectLoading.value = true
      try {
        const response = await contactAccessRequestController.approveContactAccessRequest({
          id: contactRequest.value.id,
          status: 2, // 已拒绝
        })
        if (response?.data?.code === 0) {
          message.success('已拒绝该请求')
          // 刷新请求详情，更新按钮显示状态
          if (notification.value?.relatedId) {
            await loadContactRequestDetail(notification.value.relatedId)
          }
          // 刷新通知列表的未读数量
          window.dispatchEvent(new Event('refreshUnreadCount'))
        } else {
          message.error(response?.data?.message || '操作失败')
        }
      } catch (error: any) {
        console.error('Failed to reject contact request:', error)
        message.error(error?.response?.data?.message || '操作失败')
      } finally {
        rejectLoading.value = false
      }
    },
  })
}

// 删除通知
const handleDelete = () => {
  if (!notification.value) return

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条通知吗？此操作不可恢复。',
    onOk: async () => {
      if (!notification.value) return

      deleteLoading.value = true
      try {
        const response = await notificationController.deleteNotification({
          id: notification.value.id,
        })
        if (response?.data?.code === 0) {
          message.success('删除成功')
          router.push('/notifications')
        } else {
          message.error(response?.data?.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete notification:', error)
        message.error('删除失败')
      } finally {
        deleteLoading.value = false
      }
    },
  })
}

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

onMounted(() => {
  loadNotificationDetail()
})
</script>

<style scoped>
.notification-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.notification-content {
  margin-bottom: 24px;
}

.content-section {
  margin-bottom: 24px;
}

.content-section h3 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.content-section p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.info-section {
  margin-top: 24px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
}
</style>


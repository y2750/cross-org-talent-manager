import { message } from 'ant-design-vue'

/**
 * 优化的消息提示工具
 * 功能：
 * 1. 限制同时存在的消息数量（最多2条）
 * 2. 限制一定时间内的消息频率（防止短时间内大量弹窗）
 */

interface MessageItem {
  key: string
  timestamp: number
}

// 当前显示的消息列表（最多2条）
const activeMessages: MessageItem[] = []

// 消息时间窗口记录（用于限制频率）
const messageTimeWindow: number[] = []
const TIME_WINDOW_MS = 3000 // 3秒时间窗口
const MAX_MESSAGES_PER_WINDOW = 5 // 每个时间窗口最多5条消息

/**
 * 清理最早的消息
 */
const removeOldestMessage = () => {
  if (activeMessages.length > 0) {
    const oldest = activeMessages[0]
    message.destroy(oldest.key)
    activeMessages.shift()
  }
}

/**
 * 检查是否超过频率限制
 */
const checkRateLimit = (): boolean => {
  const now = Date.now()
  
  // 清理过期的时间窗口记录
  while (messageTimeWindow.length > 0 && messageTimeWindow[0] < now - TIME_WINDOW_MS) {
    messageTimeWindow.shift()
  }
  
  // 检查是否超过频率限制
  if (messageTimeWindow.length >= MAX_MESSAGES_PER_WINDOW) {
    return false // 超过频率限制，不显示消息
  }
  
  // 记录当前消息时间
  messageTimeWindow.push(now)
  return true
}

/**
 * 显示错误消息（优化版）
 */
export const showError = (content: string, duration?: number) => {
  // 检查频率限制
  if (!checkRateLimit()) {
    console.warn('消息频率过高，已忽略:', content)
    return
  }
  
  // 如果已有2条消息，移除最早的一条
  if (activeMessages.length >= 2) {
    removeOldestMessage()
  }
  
  // 显示新消息
  const key = `error_${Date.now()}_${Math.random()}`
  message.error({
    content,
    duration: duration || 3,
    key,
    onClose: () => {
      // 消息关闭时从列表中移除
      const index = activeMessages.findIndex(item => item.key === key)
      if (index > -1) {
        activeMessages.splice(index, 1)
      }
    },
  })
  
  // 添加到活动消息列表
  activeMessages.push({
    key,
    timestamp: Date.now(),
  })
}

/**
 * 显示成功消息（优化版）
 */
export const showSuccess = (content: string, duration?: number) => {
  // 检查频率限制
  if (!checkRateLimit()) {
    console.warn('消息频率过高，已忽略:', content)
    return
  }
  
  // 如果已有2条消息，移除最早的一条
  if (activeMessages.length >= 2) {
    removeOldestMessage()
  }
  
  // 显示新消息
  const key = `success_${Date.now()}_${Math.random()}`
  message.success({
    content,
    duration: duration || 3,
    key,
    onClose: () => {
      // 消息关闭时从列表中移除
      const index = activeMessages.findIndex(item => item.key === key)
      if (index > -1) {
        activeMessages.splice(index, 1)
      }
    },
  })
  
  // 添加到活动消息列表
  activeMessages.push({
    key,
    timestamp: Date.now(),
  })
}

/**
 * 显示警告消息（优化版）
 */
export const showWarning = (content: string, duration?: number) => {
  // 检查频率限制
  if (!checkRateLimit()) {
    console.warn('消息频率过高，已忽略:', content)
    return
  }
  
  // 如果已有2条消息，移除最早的一条
  if (activeMessages.length >= 2) {
    removeOldestMessage()
  }
  
  // 显示新消息
  const key = `warning_${Date.now()}_${Math.random()}`
  message.warning({
    content,
    duration: duration || 3,
    key,
    onClose: () => {
      // 消息关闭时从列表中移除
      const index = activeMessages.findIndex(item => item.key === key)
      if (index > -1) {
        activeMessages.splice(index, 1)
      }
    },
  })
  
  // 添加到活动消息列表
  activeMessages.push({
    key,
    timestamp: Date.now(),
  })
}

/**
 * 显示信息消息（优化版）
 */
export const showInfo = (content: string, duration?: number) => {
  // 检查频率限制
  if (!checkRateLimit()) {
    console.warn('消息频率过高，已忽略:', content)
    return
  }
  
  // 如果已有2条消息，移除最早的一条
  if (activeMessages.length >= 2) {
    removeOldestMessage()
  }
  
  // 显示新消息
  const key = `info_${Date.now()}_${Math.random()}`
  message.info({
    content,
    duration: duration || 3,
    key,
    onClose: () => {
      // 消息关闭时从列表中移除
      const index = activeMessages.findIndex(item => item.key === key)
      if (index > -1) {
        activeMessages.splice(index, 1)
      }
    },
  })
  
  // 添加到活动消息列表
  activeMessages.push({
    key,
    timestamp: Date.now(),
  })
}

/**
 * 清除所有消息
 */
export const clearAll = () => {
  message.destroy()
  activeMessages.length = 0
  messageTimeWindow.length = 0
}

/**
 * 导出一个优化的 message 对象，可以替换原有的 message
 * 使用方式：import { optimizedMessage } from '@/utils/messageHelper'
 * 然后使用 optimizedMessage.error() 等
 */
export const optimizedMessage = {
  error: showError,
  success: showSuccess,
  warning: showWarning,
  info: showInfo,
  clearAll,
}


<script setup lang="ts">
import { onBeforeMount } from 'vue'
import { ConfigProvider } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import BasicLayout from './layouts/BasicLayout.vue'
import { useUserStore } from '@/stores/userStore'

// 设置 dayjs 为中文
dayjs.locale('zh-cn')

const userStore = useUserStore()

// 应用启动时恢复登录状态
onBeforeMount(() => {
  userStore.restoreLoginState()
})
</script>

<template>
  <ConfigProvider :locale="zhCN">
    <BasicLayout />
  </ConfigProvider>
</template>

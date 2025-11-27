<script setup lang="ts">
import { onBeforeMount } from 'vue'
import BasicLayout from './layouts/BasicLayout.vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

// 应用启动时恢复登录状态
onBeforeMount(() => {
  userStore.restoreLoginState()
})
</script>

<template>
  <BasicLayout />
</template>

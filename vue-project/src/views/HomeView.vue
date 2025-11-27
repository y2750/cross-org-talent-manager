<script setup lang="ts">
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
</script>

<template>
  <div class="home-container">
    <div class="content-header">
      <h2>欢迎使用跨组织人才管理系统</h2>
      <p>当前用户: {{ userStore.nickname || userStore.username }}</p>
    </div>

    <div class="content-area">
      <a-empty description="请使用左侧功能菜单选择功能" style="margin-top: 50px" />
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 60vh;
  background-color: #f5f5f5;
}

.content-area {
  flex: 1;
  overflow-y: auto;
}

.content-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.content-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.content-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.content-area {
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-height: 300px;
}
</style>

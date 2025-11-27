<template>
  <div class="diagnostic-container">
    <h2>后端连接诊断</h2>

    <div class="test-section">
      <h3>1. 测试基础连接</h3>
      <button @click="testBasicConnection">测试后端是否在线</button>
      <p>{{ basicResult }}</p>
    </div>

    <div class="test-section">
      <h3>2. 测试 /user/login 路由</h3>
      <button @click="testLoginRoute">测试登录路由</button>
      <p>{{ loginRouteResult }}</p>
    </div>

    <div class="test-section">
      <h3>3. 测试 /api/user/login 路由</h3>
      <button @click="testApiLoginRoute">测试API登录路由</button>
      <p>{{ apiLoginRouteResult }}</p>
    </div>

    <div class="test-section">
      <h3>4. 测试实际登录请求</h3>
      <button @click="testActualLogin">测试实际登录（admin/admin123）</button>
      <p>{{ actualLoginResult }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

const basicResult = ref('')
const loginRouteResult = ref('')
const apiLoginRouteResult = ref('')
const actualLoginResult = ref('')

const testBasicConnection = async () => {
  basicResult.value = '测试中...'
  try {
    const response = await axios.get('http://localhost:8123/user/get', {
      params: { id: 1 },
      timeout: 5000,
    })
    basicResult.value = `✅ 后端在线 | 状态码: ${response.status}`
  } catch (error: any) {
    basicResult.value = `❌ 连接失败: ${error.message || '未知错误'}`
  }
}

const testLoginRoute = async () => {
  loginRouteResult.value = '测试中...'
  try {
    const response = await axios.post(
      'http://localhost:8123/user/login',
      { username: 'admin', password: 'admin123' },
      { timeout: 5000, withCredentials: true },
    )
    loginRouteResult.value = `✅ 路由存在 | 响应: ${JSON.stringify(response.data)}`
  } catch (error: any) {
    loginRouteResult.value = `❌ 错误: ${error.response?.status || error.code} - ${error.response?.statusText || error.message}`
  }
}

const testApiLoginRoute = async () => {
  apiLoginRouteResult.value = '测试中...'
  try {
    const response = await axios.post(
      'http://localhost:8123/api/user/login',
      { username: 'admin', password: 'admin123' },
      { timeout: 5000, withCredentials: true },
    )
    apiLoginRouteResult.value = `✅ 路由存在 | 响应: ${JSON.stringify(response.data)}`
  } catch (error: any) {
    apiLoginRouteResult.value = `❌ 错误: ${error.response?.status || error.code} - ${error.response?.statusText || error.message}`
  }
}

const testActualLogin = async () => {
  actualLoginResult.value = '测试中...'
  try {
    const response = await axios.post(
      'http://localhost:8123/user/login',
      { username: 'admin', password: 'admin123' },
      {
        timeout: 5000,
        withCredentials: true,
        headers: { 'Content-Type': 'application/json' },
      },
    )
    actualLoginResult.value = `✅ 登录成功 | 响应: ${JSON.stringify(response.data)}`
  } catch (error: any) {
    actualLoginResult.value = `❌ 错误: ${error.response?.status} - ${error.response?.data || error.message}`
  }
}
</script>

<style scoped>
.diagnostic-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.test-section {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

button:hover {
  background-color: #0050b3;
}

p {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  word-break: break-all;
}
</style>

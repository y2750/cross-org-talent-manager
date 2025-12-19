<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()

    loading.value = true
    const success = await userStore.login(formState.username, formState.password)

    if (success) {
      message.success('登录成功')
      // 如果有重定向参数，跳转到目标页面，否则跳转到首页
      const redirect = (route.query.redirect as string) || '/home'
      router.push(redirect)
    } else {
      message.error('登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('Login error:', error)
    message.error('登录出错，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.username = ''
  formState.password = ''
}

const handleGoRegister = () => {
  router.push('/register-company')
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="login-title">跨组织人才管理系统</h1>
      <p class="login-subtitle">系统登录</p>

      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleLogin"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formState.username" placeholder="请输入用户名" size="large" />
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password
            v-model:value="formState.password"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block size="large" :loading="loading">
            登录
          </a-button>
        </a-form-item>

        <a-form-item>
          <a-button block size="large" @click="handleReset"> 重置 </a-button>
        </a-form-item>

        <a-form-item>
          <a-button type="link" block size="large" @click="handleGoRegister">
            注册企业
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <p>© 2025 跨组织人才管理系统</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #ffffff;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.login-title {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
}

.login-subtitle {
  margin: 0 0 30px 0;
  font-size: 14px;
  color: #6b7280;
  text-align: center;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

.login-footer p {
  margin: 0;
}
</style>

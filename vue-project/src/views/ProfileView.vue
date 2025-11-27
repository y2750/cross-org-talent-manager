<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRole } from '@/composables/useRole'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import * as companyController from '@/api/companyController'

const userStore = useUserStore()
const { isCompanyAdmin, isHR } = useRole()
const formRef = ref<FormInstance>()
const isEditing = ref(false)
const loading = ref(false)
const companyName = ref<string>('')
const companyLoading = ref(false)

// 判断是否需要显示公司名称
const shouldShowCompanyName = computed(() => {
  return isCompanyAdmin() || isHR()
})

const profileForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const formRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_: any, value: string) => {
        if (value === profileForm.newPassword) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur',
    },
  ],
}

const handleEdit = () => {
  isEditing.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    // 这里调用修改密码的API
    message.success('密码已更新')
    isEditing.value = false
    profileForm.oldPassword = ''
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
  } catch (error) {
    console.error('Validation failed:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  profileForm.oldPassword = ''
  profileForm.newPassword = ''
  profileForm.confirmPassword = ''
  isEditing.value = false
}

// 获取公司名称
const fetchCompanyName = async () => {
  if (!shouldShowCompanyName.value || !userStore.userInfo?.companyId) {
    return
  }

  try {
    companyLoading.value = true
    const result = await companyController.getCompanyVoById({
      id: userStore.userInfo.companyId,
    })

    if (result?.data?.code === 0 && result.data.data) {
      companyName.value = result.data.data.name || ''
    }
  } catch (error) {
    console.error('Failed to fetch company name:', error)
  } finally {
    companyLoading.value = false
  }
}

onMounted(() => {
  fetchCompanyName()
})
</script>

<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人信息</h2>
    </div>

    <div class="profile-card">
      <a-form ref="formRef" layout="vertical" :model="profileForm" :rules="formRules">
        <a-form-item label="用户名">
          <a-input :value="userStore.username" disabled />
        </a-form-item>

        <a-form-item v-if="shouldShowCompanyName" label="所属公司">
          <a-spin :spinning="companyLoading">
            <a-input :value="companyName" disabled />
          </a-spin>
        </a-form-item>

        <template v-if="isEditing">
          <a-form-item label="原密码" name="oldPassword">
            <a-input-password v-model:value="profileForm.oldPassword" placeholder="请输入原密码" />
          </a-form-item>

          <a-form-item label="新密码" name="newPassword">
            <a-input-password v-model:value="profileForm.newPassword" placeholder="请输入新密码" />
          </a-form-item>

          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password
              v-model:value="profileForm.confirmPassword"
              placeholder="请确认新密码"
            />
          </a-form-item>
        </template>

        <a-form-item>
          <template v-if="!isEditing">
            <a-button type="primary" @click="handleEdit"> 修改密码 </a-button>
          </template>
          <template v-else>
            <a-space>
              <a-button type="primary" @click="handleSave" :loading="loading"> 保存 </a-button>
              <a-button @click="handleCancel"> 取消 </a-button>
            </a-space>
          </template>
        </a-form-item>
      </a-form>
    </div>

    <div class="profile-footer">
      <p>账户安全提示：请勿向他人透露您的密码和个人信息</p>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.profile-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 30px;
}

.profile-footer {
  text-align: center;
  padding: 15px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.profile-footer p {
  margin: 0;
}
</style>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'
import * as employeeController from '@/api/employeeController'

const userStore = useUserStore()

// 当前员工信息
const myEmployeeInfo = ref<API.EmployeeVO | null>(null)

// 表单数据
const formData = ref({
  phone: '',
  email: '',
})

// 照片上传
const photoFile = ref<File | null>(null)
const photoUrl = ref<string>('')
const uploading = ref(false)

// 加载中
const loading = ref(false)

// 计算年龄（从身份证号）
const calculateAge = (idCardNumber?: string): number | null => {
  if (!idCardNumber || idCardNumber.length !== 18) return null
  
  try {
    const year = parseInt(idCardNumber.substring(6, 10))
    const month = parseInt(idCardNumber.substring(10, 12))
    const day = parseInt(idCardNumber.substring(12, 14))
    const birthDate = new Date(year, month - 1, day)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    return age >= 0 ? age : null
  } catch (error) {
    return null
  }
}

const employeeAge = computed(() => {
  return calculateAge(myEmployeeInfo.value?.idCardNumber)
})

// 获取当前员工信息
const fetchMyEmployeeInfo = async () => {
  try {
    loading.value = true
    const result = await employeeController.getMyEmployeeVo()
    if (result?.data?.code === 0 && result.data.data) {
      const employee = result.data.data
      myEmployeeInfo.value = employee
      formData.value.phone = employee.phone || ''
      formData.value.email = employee.email || ''
      photoUrl.value = employee.photoUrl || ''
    } else {
      message.error('获取员工信息失败')
    }
  } catch (error) {
    console.error('Failed to fetch employee info:', error)
    message.error('获取员工信息失败')
  } finally {
    loading.value = false
  }
}

// 头像文件列表（用于上传组件）
const avatarFileList = ref<any[]>([])

// 头像上传前处理
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB！')
    return false
  }
  
  // 保存文件引用
  photoFile.value = file
  
  // 预览图片
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    photoUrl.value = reader.result as string
    // 更新文件列表用于显示
    avatarFileList.value = [{
      uid: Date.now().toString(),
      name: file.name,
      status: 'done',
      url: photoUrl.value,
    }]
  }
  reader.onerror = () => {
    message.error('图片读取失败')
  }
  
  return false // 阻止自动上传，在保存时统一上传
}

// 提交表单
const handleSubmit = async () => {
  try {
    uploading.value = true
    
    const formDataToSend = new FormData()
    if (formData.value.phone) {
      formDataToSend.append('phone', formData.value.phone)
    }
    if (formData.value.email) {
      formDataToSend.append('email', formData.value.email)
    }
    if (photoFile.value) {
      formDataToSend.append('photo', photoFile.value)
    }
    
    // 使用 FormData 上传文件（后端使用 @RequestParam 和 @RequestPart）
    const API_BASE_URL = 'http://localhost:8123/api'
    const userInfo = localStorage.getItem('userInfo')
    let headers: any = {}
    
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        if (user.token) {
          headers.Authorization = `Bearer ${user.token}`
        }
      } catch (error) {
        console.error('Failed to parse user info:', error)
      }
    }
    
    const response = await axios.post(`${API_BASE_URL}/employee/update/me`, formDataToSend, {
      headers: {
        ...headers,
        // 不设置 Content-Type，让浏览器自动设置 multipart/form-data 边界
      },
      withCredentials: true,
    })
    
    if (response.data?.code === 0) {
      message.success('更新成功')
      // 重新获取员工信息
      await fetchMyEmployeeInfo()
      photoFile.value = null
      avatarFileList.value = []
    } else {
      message.error(response.data?.message || '更新失败')
    }
  } catch (error: any) {
    console.error('Failed to update profile:', error)
    message.error(error?.response?.data?.message || error?.message || '更新失败')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  fetchMyEmployeeInfo()
})
</script>

<template>
  <div class="update-profile-view">
    <a-spin :spinning="loading">
      <a-card>
        <template #title>
          <h2>更新资料</h2>
        </template>

        <a-spin :spinning="loading">
          <!-- 员工头像和基本信息（只读） -->
          <div style="display: flex; gap: 24px; margin-bottom: 24px" v-if="myEmployeeInfo">
            <!-- 左侧头像 -->
            <div style="flex-shrink: 0">
              <a-avatar
                v-if="photoUrl"
                :src="photoUrl"
                :size="180"
                shape="square"
                style="width: 135px; height: 180px; border-radius: 8px; object-fit: cover"
              />
              <a-avatar
                v-else
                :size="180"
                shape="square"
                style="width: 135px; height: 180px; border-radius: 8px; background-color: #1890ff; font-size: 60px; line-height: 180px"
              >
                {{ myEmployeeInfo.name?.charAt(0) }}
              </a-avatar>
            </div>

            <!-- 右侧基本信息（只读） -->
            <div style="flex: 1">
              <a-descriptions bordered :column="2">
                <a-descriptions-item label="姓名">{{ myEmployeeInfo.name || '-' }}</a-descriptions-item>
                <a-descriptions-item label="性别">{{ myEmployeeInfo.gender || '-' }}</a-descriptions-item>
                <a-descriptions-item label="年龄">
                  {{ employeeAge !== null ? `${employeeAge}岁` : '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="身份证号">{{ myEmployeeInfo.idCardNumber || '-' }}</a-descriptions-item>
                <a-descriptions-item label="所属公司">{{ myEmployeeInfo.companyName || '-' }}</a-descriptions-item>
                <a-descriptions-item label="所属部门">{{ myEmployeeInfo.departmentName || '-' }}</a-descriptions-item>
              </a-descriptions>
            </div>
          </div>

          <a-divider />

          <a-form :model="formData" layout="vertical" @submit.prevent="handleSubmit">
            <!-- 照片上传 -->
            <a-form-item label="照片">
              <a-upload
                v-model:file-list="avatarFileList"
                :before-upload="beforeAvatarUpload"
                list-type="picture-card"
                accept="image/*"
                :max-count="1"
                :show-upload-list="true"
              >
                <div v-if="avatarFileList.length === 0">
                  <span style="font-size: 24px">📷</span>
                  <div style="margin-top: 8px">上传头像</div>
                </div>
              </a-upload>
              <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px">
                支持 JPG、PNG 格式，文件大小不超过 2MB，建议尺寸 135×180 像素（3:4比例）
              </div>
            </a-form-item>

            <!-- 电话 -->
            <a-form-item label="电话" name="phone">
              <a-input v-model:value="formData.phone" placeholder="请输入电话" />
            </a-form-item>

            <!-- 邮箱 -->
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
            </a-form-item>

            <!-- 提交按钮 -->
            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="uploading">
                保存
              </a-button>
            </a-form-item>
          </a-form>
        </a-spin>
      </a-card>
    </a-spin>
  </div>
</template>

<style scoped>
.update-profile-view {
  padding: 0;
}
</style>


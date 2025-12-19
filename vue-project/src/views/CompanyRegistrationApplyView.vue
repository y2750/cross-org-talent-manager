<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, type UploadProps, type UploadFile } from 'ant-design-vue'
import IndustrySelector from '@/components/IndustrySelector.vue'
import * as companyRegistrationController from '@/api/companyRegistrationController'
import * as userController from '@/api/userController'
import { INDUSTRY_CATEGORIES } from '@/constants/industry'

const router = useRouter()

const formState = reactive<{
  companyName: string
  address: string
  companyEmail: string
  adminName: string
  adminPhone: string
  adminEmail: string
  adminIdNumber: string
  adminUsername: string
  industryCategory: string
  industries: string[]
  proofImages: string[]
}>({
  companyName: '',
  address: '',
  companyEmail: '',
  adminName: '',
  adminPhone: '',
  adminEmail: '',
  adminIdNumber: '',
  adminUsername: '',
  industryCategory: '',
  industries: [],
  proofImages: [],
})

const submitting = ref(false)
const fileList = ref<UploadProps['fileList']>([])
const checkingUsername = ref(false)
const usernameStatus = ref<'success' | 'error' | 'validating' | ''>('')
const usernameHelp = ref('')

// 格式校验状态
const phoneStatus = ref<'success' | 'error' | ''>('')
const phoneHelp = ref('')
const emailStatus = ref<'success' | 'error' | ''>('')
const emailHelp = ref('')
const companyEmailStatus = ref<'success' | 'error' | ''>('')
const companyEmailHelp = ref('')
const idNumberStatus = ref<'success' | 'error' | ''>('')
const idNumberHelp = ref('')

// 行业大类选项
const industryCategories = ref<string[]>(
  INDUSTRY_CATEGORIES.map((cat) => cat.value)
)

// 监听行业大类变化，清空已选的子类
watch(
  () => formState.industryCategory,
  (newVal) => {
    if (!newVal) {
      formState.industries = []
    } else {
      // 过滤掉不属于当前大类的子类
      const currentCategory = INDUSTRY_CATEGORIES.find((cat) => cat.value === newVal)
      if (currentCategory) {
        formState.industries = formState.industries.filter((ind) =>
          currentCategory.children.some((child) => child.value === ind)
        )
      } else {
        formState.industries = []
      }
    }
  }
)

// 账号名格式验证
const validateUsernameFormat = (username: string): boolean => {
  if (!username || username.length < 4 || username.length > 20) {
    return false
  }
  // 只能包含字母、数字、下划线
  return /^[a-zA-Z0-9_]+$/.test(username)
}

// 身份证号格式验证（18位，最后一位可能是X）
const validateIdNumberFormat = (idNumber: string): boolean => {
  if (!idNumber) return false
  // 18位身份证号，前17位数字，最后一位数字或X
  return /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(idNumber)
}

// 手机号格式验证（11位，以1开头）
const validatePhoneFormat = (phone: string): boolean => {
  if (!phone) return false
  // 11位手机号，以1开头
  return /^1[3-9]\d{9}$/.test(phone)
}

// 邮箱格式验证
const validateEmailFormat = (email: string): boolean => {
  if (!email) return false
  // 标准邮箱格式
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

// 校验身份证号
const validateIdNumber = (idNumber: string) => {
  if (!idNumber?.trim()) {
    idNumberStatus.value = ''
    idNumberHelp.value = ''
    return
  }
  const trimmed = idNumber.trim()
  if (trimmed.length < 18) {
    idNumberStatus.value = 'error'
    idNumberHelp.value = `还差 ${18 - trimmed.length} 位，请输入完整的18位身份证号`
  } else if (trimmed.length > 18) {
    idNumberStatus.value = 'error'
    idNumberHelp.value = '身份证号应为18位，请检查输入'
  } else if (!validateIdNumberFormat(trimmed)) {
    idNumberStatus.value = 'error'
    idNumberHelp.value = '身份证号格式有误，请检查输入（最后一位可以是数字或X）'
  } else {
    idNumberStatus.value = 'success'
    idNumberHelp.value = '✓ 格式正确'
  }
}

// 校验手机号
const validatePhone = (phone: string) => {
  if (!phone?.trim()) {
    phoneStatus.value = ''
    phoneHelp.value = ''
    return
  }
  const trimmed = phone.trim()
  if (trimmed.length === 0) {
    phoneStatus.value = ''
    phoneHelp.value = ''
    return
  }
  if (trimmed.length < 11) {
    phoneStatus.value = 'error'
    phoneHelp.value = `还差 ${11 - trimmed.length} 位，请输入完整的11位手机号`
  } else if (trimmed.length > 11) {
    phoneStatus.value = 'error'
    phoneHelp.value = '手机号应为11位，请检查输入'
  } else if (!trimmed.startsWith('1')) {
    phoneStatus.value = 'error'
    phoneHelp.value = '手机号应以1开头，请检查输入'
  } else if (!validatePhoneFormat(trimmed)) {
    phoneStatus.value = 'error'
    phoneHelp.value = '手机号格式有误，请输入有效的11位手机号（如：13800138000）'
  } else {
    phoneStatus.value = 'success'
    phoneHelp.value = '✓ 格式正确'
  }
}

// 校验管理人邮箱
const validateAdminEmail = (email: string) => {
  if (!email?.trim()) {
    emailStatus.value = ''
    emailHelp.value = ''
    return
  }
  const trimmed = email.trim()
  if (!trimmed.includes('@')) {
    emailStatus.value = 'error'
    emailHelp.value = '邮箱地址应包含 @ 符号，如：example@email.com'
  } else if (trimmed.indexOf('@') === 0 || trimmed.indexOf('@') === trimmed.length - 1) {
    emailStatus.value = 'error'
    emailHelp.value = '邮箱格式有误，@ 符号位置不正确'
  } else if (!trimmed.includes('.')) {
    emailStatus.value = 'error'
    emailHelp.value = '邮箱地址应包含域名，如：example@email.com'
  } else if (!validateEmailFormat(trimmed)) {
    emailStatus.value = 'error'
    emailHelp.value = '邮箱格式有误，请输入有效的邮箱地址（如：example@email.com）'
  } else {
    emailStatus.value = 'success'
    emailHelp.value = '✓ 格式正确'
  }
}

// 校验企业邮箱
const validateCompanyEmail = (email: string) => {
  if (!email?.trim()) {
    companyEmailStatus.value = ''
    companyEmailHelp.value = ''
    return
  }
  const trimmed = email.trim()
  if (!trimmed.includes('@')) {
    companyEmailStatus.value = 'error'
    companyEmailHelp.value = '邮箱地址应包含 @ 符号，如：company@email.com'
  } else if (trimmed.indexOf('@') === 0 || trimmed.indexOf('@') === trimmed.length - 1) {
    companyEmailStatus.value = 'error'
    companyEmailHelp.value = '邮箱格式有误，@ 符号位置不正确'
  } else if (!trimmed.includes('.')) {
    companyEmailStatus.value = 'error'
    companyEmailHelp.value = '邮箱地址应包含域名，如：company@email.com'
  } else if (!validateEmailFormat(trimmed)) {
    companyEmailStatus.value = 'error'
    companyEmailHelp.value = '邮箱格式有误，请输入有效的邮箱地址（如：company@email.com）'
  } else {
    companyEmailStatus.value = 'success'
    companyEmailHelp.value = '✓ 格式正确'
  }
}

// 检查账号名是否重复
const checkUsernameExists = async (username: string) => {
  if (!username || !validateUsernameFormat(username)) {
    usernameStatus.value = ''
    usernameHelp.value = ''
    return
  }

  checkingUsername.value = true
  usernameStatus.value = 'validating'
  usernameHelp.value = '正在检查...'

  try {
    const res = await userController.checkUsername({ username })
    if (res?.data?.code === 0) {
      const exists = res.data.data
      if (exists) {
        usernameStatus.value = 'error'
        usernameHelp.value = '该账号名已被占用，请更换其他账号名'
      } else {
        usernameStatus.value = 'success'
        usernameHelp.value = '该账号名可用'
      }
    } else {
      usernameStatus.value = 'error'
      usernameHelp.value = res?.data?.message || '检查失败，请稍后重试'
    }
  } catch (e) {
    console.error('Failed to check username:', e)
    usernameStatus.value = 'error'
    usernameHelp.value = '检查失败，请稍后重试'
  } finally {
    checkingUsername.value = false
  }
}

const handleUploadChange: UploadProps['onChange'] = ({ fileList: newList }) => {
  fileList.value = newList
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type?.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB')
    return false
  }
  // 阻止自动上传，在提交时统一上传
  return false
}

const validateForm = (): boolean => {
  if (!formState.companyName?.trim()) {
    message.error('请输入企业名称')
    return false
  }
  if (!formState.address?.trim()) {
    message.error('请输入企业地址')
    return false
  }
  if (!formState.companyEmail?.trim()) {
    message.error('请输入企业邮箱')
    return false
  }
  if (!validateEmailFormat(formState.companyEmail)) {
    message.error('企业邮箱格式不正确')
    return false
  }
  if (!formState.adminName?.trim()) {
    message.error('请输入公司管理人姓名')
    return false
  }
  if (!formState.adminPhone?.trim()) {
    message.error('请输入管理人电话')
    return false
  }
  if (!validatePhoneFormat(formState.adminPhone)) {
    message.error('管理人电话格式不正确，请输入11位有效手机号')
    return false
  }
  if (!formState.adminEmail?.trim()) {
    message.error('请输入管理人邮箱')
    return false
  }
  if (!validateEmailFormat(formState.adminEmail)) {
    message.error('管理人邮箱格式不正确')
    return false
  }
  if (!formState.adminIdNumber?.trim()) {
    message.error('请输入管理人身份证号')
    return false
  }
  if (!validateIdNumberFormat(formState.adminIdNumber)) {
    message.error('身份证号格式不正确，请输入18位有效身份证号')
    return false
  }
  if (!formState.adminUsername?.trim()) {
    message.error('请输入管理人账号名')
    return false
  }
  if (!validateUsernameFormat(formState.adminUsername)) {
    message.error('账号名格式不正确：4-20个字符，只能包含字母、数字和下划线')
    return false
  }
  if (usernameStatus.value === 'error') {
    message.error('账号名已被占用，请更换其他账号名')
    return false
  }
  if (usernameStatus.value !== 'success') {
    message.error('请先确认账号名可用')
    return false
  }
  if (!formState.industryCategory) {
    message.error('请选择行业大类')
    return false
  }
  if (!fileList.value || fileList.value.length === 0) {
    message.error('请至少上传一张证明材料图片')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }
  try {
    submitting.value = true

    // 先上传图片到服务器
    const filesToUpload: File[] = []
    for (const file of fileList.value) {
      if (file.originFileObj) {
        filesToUpload.push(file.originFileObj)
      }
    }

    if (filesToUpload.length === 0) {
      message.error('请选择要上传的证明材料图片')
      return
    }

    // 上传图片
    let proofImageUrls: string[] = []
    try {
      const uploadResult = await companyRegistrationController.uploadCompanyRegistrationProof(
        filesToUpload
      )
      if (uploadResult?.data?.code === 0 && uploadResult.data.data) {
        proofImageUrls = uploadResult.data.data
      } else {
        message.error(uploadResult?.data?.message || '图片上传失败')
        return
      }
    } catch (uploadError: any) {
      console.error('Failed to upload images:', uploadError)
      const errorMsg =
        uploadError?.response?.data?.message ||
        uploadError?.message ||
        '图片上传失败，请稍后重试'
      message.error(errorMsg)
      return
    }

    // 提交申请
    const res = await companyRegistrationController.applyCompanyRegistration({
      companyName: formState.companyName.trim(),
      address: formState.address.trim(),
      companyEmail: formState.companyEmail.trim(),
      adminName: formState.adminName.trim(),
      adminPhone: formState.adminPhone.trim(),
      adminEmail: formState.adminEmail.trim(),
      adminIdNumber: formState.adminIdNumber.trim(),
      adminUsername: formState.adminUsername.trim(),
      industryCategory: formState.industryCategory,
      industries: formState.industries.length ? formState.industries : undefined,
      proofImages: proofImageUrls,
    })
    if (res?.data?.code === 0) {
      message.success('申请已提交，请等待管理员审核')
      router.push('/login')
    } else {
      message.error(res?.data?.message || '提交申请失败')
    }
  } catch (e: any) {
    console.error(e)
    const errorMsg = e?.response?.data?.message || e?.message || '提交申请失败，请稍后重试'
    message.error(errorMsg)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="company-registration-container">
    <a-card class="registration-card" title="企业注册申请">
      <a-alert
        message="说明"
        type="info"
        show-icon
        style="margin-bottom: 16px"
        description="请填写真实的企业与联系人信息，并上传相关证明材料（如营业执照等），提交后由系统管理员审核。"
      />

      <a-form layout="vertical">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="12">
            <a-form-item label="企业名称" required>
              <a-input v-model:value="formState.companyName" placeholder="请输入企业名称" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              label="企业邮箱"
              required
              :validate-status="companyEmailStatus"
              :help="companyEmailHelp"
            >
              <a-input
                v-model:value="formState.companyEmail"
                placeholder="例如：company@example.com"
                @blur="validateCompanyEmail(formState.companyEmail)"
                @input="() => { if (companyEmailStatus === 'error') { companyEmailStatus = ''; companyEmailHelp = '' } }"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="企业地址" required>
          <a-input v-model:value="formState.address" placeholder="请输入企业注册地址或办公地址" />
        </a-form-item>

        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="8">
            <a-form-item label="管理人姓名" required>
              <a-input v-model:value="formState.adminName" placeholder="请输入公司管理人姓名" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item
              label="管理人电话"
              required
              :validate-status="phoneStatus"
              :help="phoneHelp"
            >
              <a-input
                v-model:value="formState.adminPhone"
                placeholder="例如：13800138000"
                :maxlength="11"
                @blur="validatePhone(formState.adminPhone)"
                @input="() => { 
                  const value = formState.adminPhone;
                  if (phoneStatus === 'error' && value.length < 11) {
                    phoneStatus = '';
                    phoneHelp = '';
                  } else if (value.length === 11) {
                    validatePhone(value);
                  }
                }"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item
              label="管理人邮箱"
              required
              :validate-status="emailStatus"
              :help="emailHelp"
            >
              <a-input
                v-model:value="formState.adminEmail"
                placeholder="例如：admin@example.com"
                @blur="validateAdminEmail(formState.adminEmail)"
                @input="() => { if (emailStatus === 'error') { emailStatus = ''; emailHelp = '' } }"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item
          label="管理人身份证号"
          required
          :validate-status="idNumberStatus"
          :help="idNumberHelp"
        >
          <a-input
            v-model:value="formState.adminIdNumber"
            placeholder="例如：110101199001011234"
            :maxlength="18"
            @blur="validateIdNumber(formState.adminIdNumber)"
            @input="() => { 
              const value = formState.adminIdNumber;
              if (idNumberStatus === 'error' && value.length < 18) {
                idNumberStatus = '';
                idNumberHelp = '';
              } else if (value.length === 18) {
                validateIdNumber(value);
              }
            }"
          />
        </a-form-item>

        <a-form-item
          label="管理人账号名"
          required
          :validate-status="usernameStatus"
          :help="usernameHelp"
        >
          <a-input
            v-model:value="formState.adminUsername"
            placeholder="4-20个字符，只能包含字母、数字和下划线"
            :maxlength="20"
            @blur="checkUsernameExists(formState.adminUsername)"
            @input="() => { usernameStatus = ''; usernameHelp = '' }"
          >
            <template #suffix>
              <span v-if="checkingUsername" class="loading-icon">⏳</span>
            </template>
          </a-input>
          <div style="margin-top: 4px; font-size: 12px; color: #999">
            账号名将用于登录系统，请妥善保管
          </div>
        </a-form-item>

        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="12">
            <a-form-item label="行业大类" required>
              <a-select
                v-model:value="formState.industryCategory"
                placeholder="请选择行业大类"
                allow-clear
              >
                <a-select-option v-for="item in industryCategories" :key="item" :value="item">
                  {{ item }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="行业子类（可多选）">
              <IndustrySelector
                v-model="formState.industries"
                :category="formState.industryCategory"
                placeholder="请先选择行业大类"
                :max-tag-count="4"
                :disabled="!formState.industryCategory"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="证明材料（营业执照等）" required>
          <a-upload
            multiple
            :file-list="fileList"
            :before-upload="beforeUpload"
            :on-change="handleUploadChange"
            list-type="picture-card"
            :max-count="10"
          >
            <div v-if="(!fileList || fileList.length < 10)">
              <span>选择图片</span>
            </div>
          </a-upload>
          <div style="margin-top: 8px; color: #666; font-size: 12px">
            最多可上传10张图片，单张图片不超过5MB
          </div>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="submitting" @click="handleSubmit">
              提交申请
            </a-button>
            <a-button @click="router.push('/login')">返回登录</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.company-registration-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 32px 16px;
}

.registration-card {
  width: 100%;
  max-width: 900px;
}

/* 错误提示平滑过渡动画 */
:deep(.ant-form-item-explain) {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

:deep(.ant-form-item-explain-connected) {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.ant-form-item-explain > div) {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideDownFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideDownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>




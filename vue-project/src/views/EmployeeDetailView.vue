<script setup lang="ts">
import { onMounted, onBeforeUnmount, reactive, ref, computed } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import * as employeeController from '@/api/employeeController'
import * as userController from '@/api/userController'
import * as departmentController from '@/api/departmentController'
import * as employeeProfileController from '@/api/employeeProfileController'
import * as companyController from '@/api/companyController'
import * as rewardPunishmentController from '@/api/rewardPunishmentController'
import { useRole } from '@/composables/useRole'
import { useUserStore } from '@/stores/userStore'
import type { FormInstance } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const { isSystemAdmin, isCompanyAdmin, isHR } = useRole()
const userStore = useUserStore()

// 使用字符串类型，避免 JavaScript 数字精度丢失
const employeeId = ref<string>(String(route.params.id))
const loading = ref(false)
const activeTab = ref('profile') // 解雇模式下默认显示档案标签页
const editMode = ref(false)

// 解雇流程相关
const isFireMode = ref(false) // 是否为解雇流程模式
const fireModeCompanyId = ref<string | number | undefined>(undefined) // 解雇流程中的公司ID（保持原始类型，避免精度丢失）
const fireConfirmLoading = ref(false) // 确认解雇加载状态
const fireProcessOperations = ref<Array<{ 
  type: 'add' | 'update' | 'delete'; 
  profileId: number | undefined; 
  originalData?: any | null; // 用于 update 和 delete 操作，存储原始数据
}>>([]) // 解雇流程中的操作记录

// 员工信息
const employeeInfo = ref<API.EmployeeVO | null>(null)
const employeeFormRef = ref<FormInstance>()
const employeeForm = reactive({
  name: '',
  idCardNumber: '',
  phone: '',
  email: '',
  departmentId: undefined as number | undefined,
  photoUrl: undefined as string | undefined,
})

// 头像上传相关
const avatarFileList = ref<any[]>([])
const avatarUploading = ref(false)

// 员工档案
const profileList = ref<API.EmployeeProfileVO[]>([])
const profileDetailModalVisible = ref(false)
const profileEditModalVisible = ref(false)
const isEditProfile = ref(false)
const currentProfile = ref<API.EmployeeProfileVO | null>(null)

// 按公司分组的档案列表
interface GroupedProfile {
  companyId: string // 使用字符串类型，避免精度丢失
  companyName: string
  profiles: API.EmployeeProfileVO[]
}

// 按公司分组的奖惩记录
interface GroupedRewardPunishment {
  companyId: string
  companyName: string
  records: API.RewardPunishmentVO[]
}

// 奖惩记录相关
const rewardPunishmentModalVisible = ref(false)
const currentRewardCompanyId = ref<string | undefined>(undefined)
const currentRewardCompanyName = ref<string>('')
const rewardPunishmentList = ref<API.RewardPunishmentVO[]>([])
const rewardPunishmentLoading = ref<Map<string, boolean>>(new Map())
const rewardPunishmentCompanies = ref<Array<{ companyId: string; companyName: string }>>([]) // 有奖惩记录的公司列表
const rewardPunishmentEditModalVisible = ref(false)
const isEditRewardPunishment = ref(false)
const currentRewardPunishment = ref<API.RewardPunishmentVO | null>(null)
const rewardPunishmentFormRef = ref<FormInstance>()
const rewardPunishmentForm = reactive({
  id: undefined as number | undefined,
  type: undefined as number | undefined,
  description: '',
  amount: undefined as number | undefined,
  date: '',
})
const rewardPunishmentAddModalVisible = ref(false)
const isAddRewardPunishment = ref(false)
const rewardPunishmentAddFormRef = ref<FormInstance>()
const rewardPunishmentAddForm = reactive({
  type: undefined as number | undefined,
  description: '',
  amount: undefined as number | undefined,
  date: '',
  companyId: undefined as string | undefined, // 用于新增时指定公司
})

const groupedProfiles = computed<GroupedProfile[]>(() => {
  console.log('=== groupedProfiles 计算属性执行 ===')
  console.log('profileList.value:', profileList.value)
  console.log('profileList.value 长度:', profileList.value.length)
  console.log('isFireMode.value:', isFireMode.value)
  console.log('fireModeCompanyId.value:', fireModeCompanyId.value, '类型:', typeof fireModeCompanyId.value)
  
  const groups = new Map<string | number, GroupedProfile>()
  
  // 在解雇模式下，只显示当前公司的档案
  let filteredProfiles = profileList.value
  if (isFireMode.value && fireModeCompanyId.value) {
    console.log('开始过滤档案...')
    filteredProfiles = profileList.value.filter(p => {
      if (!p.companyId) {
        console.log('档案没有 companyId:', p)
        return false
      }
      // 统一转换为字符串进行比较，避免类型不匹配
      const profileCompanyId = String(p.companyId)
      const fireCompanyId = String(fireModeCompanyId.value)
      const match = profileCompanyId === fireCompanyId
      console.log(`档案 companyId: ${profileCompanyId} (${typeof p.companyId}), 解雇模式 companyId: ${fireCompanyId} (${typeof fireModeCompanyId.value}), 匹配: ${match}`)
      return match
    })
    console.log('过滤后的档案数量:', filteredProfiles.length)
    console.log('过滤后的档案:', filteredProfiles)
  } else {
    console.log('非解雇模式，显示所有档案')
    
    // 应用筛选条件
    const currentUserCompanyId = userStore.userInfo?.companyId
    const currentUserCompanyIdStr = currentUserCompanyId ? String(currentUserCompanyId) : undefined
    
    // 仅查看本公司筛选
    if (showCurrentCompanyOnly.value && currentUserCompanyIdStr) {
      filteredProfiles = filteredProfiles.filter(p => {
        if (!p.companyId) return false
        return String(p.companyId) === currentUserCompanyIdStr
      })
    }
    
    // 公司筛选下拉框
    if (selectedCompanyId.value) {
      filteredProfiles = filteredProfiles.filter(p => {
        if (!p.companyId) return false
        return String(p.companyId) === selectedCompanyId.value
      })
    }
  }
  
  filteredProfiles.forEach((profile) => {
    const companyId = profile.companyId
    if (!companyId) {
      console.log('跳过没有 companyId 的档案:', profile)
      return
    }
    
    // 使用字符串作为key，避免类型问题
    const companyIdKey = String(companyId)
    
    if (!groups.has(companyIdKey)) {
      groups.set(companyIdKey, {
        companyId: companyIdKey, // 保持字符串类型，避免精度丢失
        companyName: profile.companyName || `公司${companyId}`,
        profiles: [],
      })
      console.log('创建新的公司分组:', companyIdKey, profile.companyName)
    }
    
    groups.get(companyIdKey)!.profiles.push(profile)
  })
  
  // 转换为数组并排序：本公司优先，然后按公司名排序
  const currentUserCompanyId = userStore.userInfo?.companyId
  const currentUserCompanyIdStr = currentUserCompanyId ? String(currentUserCompanyId) : undefined
  
  const result = Array.from(groups.values()).sort((a, b) => {
    // 如果当前用户有公司ID，则本公司排在前面
    if (currentUserCompanyIdStr) {
      const aIsCurrent = a.companyId === currentUserCompanyIdStr
      const bIsCurrent = b.companyId === currentUserCompanyIdStr
      if (aIsCurrent && !bIsCurrent) return -1
      if (!aIsCurrent && bIsCurrent) return 1
    }
    // 按公司名排序，如果公司名相同则按ID排序（字符串比较）
    if (a.companyName !== b.companyName) {
      return a.companyName.localeCompare(b.companyName)
    }
    return a.companyId.localeCompare(b.companyId) // 使用字符串比较，避免精度丢失
  })
  
  console.log('最终 groupedProfiles 结果:', result)
  console.log('最终 groupedProfiles 数量:', result.length)
  return result
})
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  id: undefined as number | undefined,
  companyId: undefined as number | undefined,
  startDate: '',
  endDate: '',
  performanceSummary: '',
  attendanceRate: undefined as number | undefined,
  hasMajorIncident: false,
  reasonForLeaving: '',
  occupation: '',
  annualSalary: undefined as number | undefined,
})

// 部门选项
const departmentOptions = ref<{ label: string; value: number }[]>([])

// 公司选项（管理员使用）
const companyOptions = ref<{ label: string; value: number }[]>([])
const companyLoading = ref(false)

// 档案筛选相关
const showCurrentCompanyOnly = ref(false) // 仅查看本公司
const selectedCompanyId = ref<string | undefined>(undefined) // 选中的公司ID（用于筛选）
const companyFilterOptions = ref<Array<{ label: string; value: string; companyId: string }>>([]) // 公司筛选选项
const companySearchText = ref('') // 公司搜索文本

// 身份证号验证规则
const idCardNumberRules = [
  {
    validator: (_rule: any, value: string) => {
      if (!value) {
        return Promise.reject(new Error('请输入身份证号'))
      }
      if (!validateIdCardNumber(value)) {
        return Promise.reject(new Error('请输入正确的18位身份证号（包含有效的出生日期）'))
      }
      return Promise.resolve()
    },
    trigger: 'blur',
  },
]

// 获取员工详情
const fetchEmployeeDetail = async () => {
  try {
    loading.value = true
    const result = await employeeController.getEmployeeVoById({ id: employeeId.value as any })
    if (result?.data?.code === 0 && result.data.data) {
      employeeInfo.value = result.data.data
      // 初始化表单
      employeeForm.name = employeeInfo.value.name || ''
      employeeForm.idCardNumber = employeeInfo.value.idCardNumber || ''
      employeeForm.phone = employeeInfo.value.phone || ''
      employeeForm.email = employeeInfo.value.email || ''
      employeeForm.departmentId = employeeInfo.value.departmentId
      employeeForm.photoUrl = employeeInfo.value.photoUrl
      // 重置头像上传列表
      avatarFileList.value = []
    } else {
      message.error('获取员工信息失败')
      router.back()
    }
  } catch (error) {
    console.error('Failed to fetch employee detail:', error)
    message.error('获取员工信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 查看奖惩记录（弹窗模式）
const viewRewardPunishment = async (companyId: string | number, companyName: string) => {
  // 统一转换为字符串，避免精度丢失
  const companyIdStr = String(companyId)
  try {
    currentRewardCompanyId.value = companyIdStr
    currentRewardCompanyName.value = companyName
    rewardPunishmentModalVisible.value = true
    rewardPunishmentLoading.value.set(companyIdStr, true)
    
    const result = await rewardPunishmentController.listRewardPunishmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      employeeId: employeeId.value as any,
      companyId: companyIdStr as any, // 使用字符串类型，避免精度丢失
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      rewardPunishmentList.value = result.data.data.records || []
    } else {
      message.error('获取奖惩记录失败')
      rewardPunishmentList.value = []
    }
  } catch (error) {
    console.error('Failed to fetch reward punishment:', error)
    message.error('获取奖惩记录失败')
    rewardPunishmentList.value = []
  } finally {
    rewardPunishmentLoading.value.set(companyIdStr, false)
  }
}

// 判断是否可以删除奖惩记录
const canDeleteRewardPunishment = (record: API.RewardPunishmentVO): boolean => {
  // 系统管理员可以删除任意记录
  if (isSystemAdmin()) {
    return true
  }
  
  // HR和公司管理员只能删除本公司奖惩记录
  if (isHR() || isCompanyAdmin()) {
    const currentUserCompanyId = userStore.userInfo?.companyId
    if (!currentUserCompanyId) return false
    
    // 检查奖惩记录的公司ID是否等于当前用户的公司ID
    // 使用奖惩记录本身的 companyId（记录产生时的公司ID），而不是员工当前的公司ID
    const recordCompanyId = record.companyId
    if (recordCompanyId && String(recordCompanyId) === String(currentUserCompanyId)) {
      return true
    }
  }
  
  return false
}

// 判断是否可以修改奖惩记录（仅HR或公司管理员对本公司产生的奖惩记录可用）
const canEditRewardPunishment = (record: API.RewardPunishmentVO): boolean => {
  // 系统管理员不能修改奖惩记录
  if (isSystemAdmin()) {
    return false
  }
  
  // HR和公司管理员只能修改本公司产生的奖惩记录
  if (isHR() || isCompanyAdmin()) {
    const currentUserCompanyId = userStore.userInfo?.companyId
    if (!currentUserCompanyId) return false
    
    // 检查奖惩记录的公司ID是否等于当前用户的公司ID
    // 使用奖惩记录本身的 companyId（记录产生时的公司ID），而不是员工当前的公司ID
    const recordCompanyId = record.companyId
    if (recordCompanyId && String(recordCompanyId) === String(currentUserCompanyId)) {
      return true
    }
  }
  
  return false
}

// 删除奖惩记录
const handleDeleteRewardPunishment = async (record: API.RewardPunishmentVO) => {
  if (!canDeleteRewardPunishment(record)) {
    message.error('您没有权限删除该奖惩记录')
    return
  }
  
  try {
    const result = await rewardPunishmentController.deleteRewardPunishment({
      id: record.id as any,
    })
    
    if (result?.data?.code === 0) {
      message.success('删除成功')
      // 更新有奖惩记录的公司列表
      await fetchRewardPunishmentCompanies()
      // 更新公司筛选选项
      updateCompanyFilterOptions()
      // 重新获取该公司的奖惩记录
      if (currentRewardCompanyId.value) {
        await viewRewardPunishment(currentRewardCompanyId.value, currentRewardCompanyName.value)
      }
    } else {
      message.error(result?.data?.message || '删除失败')
    }
  } catch (error: any) {
    console.error('Failed to delete reward punishment:', error)
    message.error(error?.response?.data?.message || '删除失败')
  }
}

// 打开新增奖惩记录弹窗
const openAddRewardPunishment = async () => {
  // 只有HR和公司管理员可以新增
  if (!isHR() && !isCompanyAdmin()) {
    message.error('您没有权限新增奖惩记录')
    return
  }
  
  isAddRewardPunishment.value = true
  rewardPunishmentAddForm.type = undefined
  rewardPunishmentAddForm.description = ''
  rewardPunishmentAddForm.amount = undefined
  rewardPunishmentAddForm.date = ''
  
  // 设置公司ID（使用员工当前所属公司）
  if (employeeInfo.value?.companyId) {
    rewardPunishmentAddForm.companyId = String(employeeInfo.value.companyId)
  } else {
    // 如果没有公司信息，使用当前用户的公司ID
    const currentUserCompanyId = userStore.userInfo?.companyId
    if (currentUserCompanyId) {
      rewardPunishmentAddForm.companyId = String(currentUserCompanyId)
    }
  }
  
  rewardPunishmentAddModalVisible.value = true
}

// 打开编辑奖惩记录弹窗
const openEditRewardPunishment = (record: API.RewardPunishmentVO) => {
  if (!canEditRewardPunishment(record)) {
    message.error('您没有权限修改该奖惩记录')
    return
  }
  
  isEditRewardPunishment.value = true
  currentRewardPunishment.value = record
  rewardPunishmentForm.id = record.id
  rewardPunishmentForm.type = record.type
  rewardPunishmentForm.description = record.description || ''
  rewardPunishmentForm.amount = record.amount ? Number(record.amount) : undefined
  rewardPunishmentForm.date = record.date || ''
  rewardPunishmentEditModalVisible.value = true
}

// 保存新增奖惩记录
const handleSaveAddRewardPunishment = async () => {
  try {
    await rewardPunishmentAddFormRef.value?.validate()
    
    if (!isAddRewardPunishment.value) {
      message.error('无效的操作')
      return
    }
    
    // 权限检查
    if (!isHR() && !isCompanyAdmin()) {
      message.error('您没有权限新增奖惩记录')
      return
    }
    
    const result = await rewardPunishmentController.addRewardPunishment({
      employeeId: employeeId.value as any,
      type: rewardPunishmentAddForm.type,
      description: rewardPunishmentAddForm.description || undefined,
      amount: rewardPunishmentAddForm.amount,
      date: rewardPunishmentAddForm.date || undefined,
    } as any)
    
    if (result?.data?.code === 0) {
      message.success('新增成功')
      rewardPunishmentAddModalVisible.value = false
      
      // 更新有奖惩记录的公司列表
      await fetchRewardPunishmentCompanies()
      // 更新公司筛选选项
      updateCompanyFilterOptions()
      
      // 确定要查看的公司ID和公司名称
      let targetCompanyId: string | undefined
      let targetCompanyName: string = ''
      
      if (currentRewardCompanyId.value && rewardPunishmentModalVisible.value) {
        // 如果当前有打开的奖惩记录弹窗，刷新列表
        targetCompanyId = currentRewardCompanyId.value
        targetCompanyName = currentRewardCompanyName.value
      } else if (employeeInfo.value?.companyId && employeeInfo.value?.companyName) {
        // 如果没有打开的弹窗，自动打开当前公司的奖惩记录弹窗
        targetCompanyId = String(employeeInfo.value.companyId)
        targetCompanyName = employeeInfo.value.companyName
      }
      
      if (targetCompanyId) {
        await viewRewardPunishment(targetCompanyId, targetCompanyName)
      }
    } else {
      message.error(result?.data?.message || '新增失败')
    }
  } catch (error: any) {
    console.error('Failed to add reward punishment:', error)
    if (error?.errorFields) {
      // 表单验证错误
      return
    }
    message.error(error?.response?.data?.message || '新增失败')
  }
}

// 保存奖惩记录（修改）
const handleSaveRewardPunishment = async () => {
  try {
    await rewardPunishmentFormRef.value?.validate()
    
    if (!isEditRewardPunishment.value || !rewardPunishmentForm.id) {
      message.error('无效的操作')
      return
    }
    
    // 权限检查
    if (currentRewardPunishment.value && !canEditRewardPunishment(currentRewardPunishment.value)) {
      message.error('您没有权限修改该奖惩记录')
      return
    }
    
    const result = await rewardPunishmentController.updateRewardPunishment({
      id: rewardPunishmentForm.id,
      type: rewardPunishmentForm.type,
      description: rewardPunishmentForm.description || undefined,
      amount: rewardPunishmentForm.amount,
      date: rewardPunishmentForm.date || undefined,
    } as any)
    
    if (result?.data?.code === 0) {
      message.success('修改成功')
      rewardPunishmentEditModalVisible.value = false
      // 重新获取该公司的奖惩记录
      if (currentRewardCompanyId.value) {
        await viewRewardPunishment(currentRewardCompanyId.value, currentRewardCompanyName.value)
      }
    } else {
      message.error(result?.data?.message || '修改失败')
    }
  } catch (error: any) {
    console.error('Failed to save reward punishment:', error)
    if (error?.errorFields) {
      // 表单验证错误
      return
    }
    message.error(error?.response?.data?.message || '修改失败')
  }
}

// 获取奖惩类型文本
const getRewardPunishmentTypeText = (type: number | undefined): string => {
  if (type === 1) return '奖励'
  if (type === 2) return '惩罚'
  return '-'
}

// 获取奖惩类型颜色
const getRewardPunishmentTypeColor = (type: number | undefined): string => {
  if (type === 1) return 'green'
  if (type === 2) return 'red'
  return 'default'
}

// 获取有奖惩记录的公司列表
const fetchRewardPunishmentCompanies = async () => {
  try {
    // 获取该员工的所有奖惩记录（不指定companyId），以获取所有有奖惩记录的公司
    const result = await rewardPunishmentController.listRewardPunishmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      employeeId: employeeId.value as any,
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      // 收集所有有奖惩记录的公司ID
      const companyIdSet = new Set<string>()
      records.forEach((record: API.RewardPunishmentVO) => {
        const recordCompanyId = record.companyId
        if (recordCompanyId) {
          companyIdSet.add(String(recordCompanyId))
        }
      })
      
      // 批量查询公司名称
      const companyMap = new Map<string, string>()
      if (companyIdSet.size > 0) {
        try {
          const companyListResult = await companyController.listCompanyVoByPage({
            pageNum: 1,
            pageSize: 1000,
          } as any)
          
          if (companyListResult?.data?.code === 0 && companyListResult.data.data) {
            const companies = companyListResult.data.data.records || []
            companies.forEach((company: API.CompanyVO) => {
              if (company.id) {
                const companyIdStr = String(company.id)
                if (companyIdSet.has(companyIdStr)) {
                  companyMap.set(companyIdStr, company.name || `公司${companyIdStr}`)
                }
              }
            })
          }
        } catch (error) {
          console.error('Failed to fetch company names:', error)
        }
      }
      
      // 构建公司列表，如果查询不到公司名称则使用默认名称
      rewardPunishmentCompanies.value = Array.from(companyIdSet).map((companyId) => ({
        companyId,
        companyName: companyMap.get(companyId) || `公司${companyId}`,
      }))
      
      // 更新公司筛选选项
      updateCompanyFilterOptions()
    } else {
      rewardPunishmentCompanies.value = []
    }
  } catch (error) {
    console.error('Failed to fetch reward punishment companies:', error)
    rewardPunishmentCompanies.value = []
  }
}

// 获取员工档案列表
const fetchProfileList = async () => {
  try {
    const params: any = {
      employeeId: employeeId.value as any,
      pageNum: 1,
      pageSize: 100,
    }
    
    // 解雇模式下，只获取当前公司的档案
    if (isFireMode.value && fireModeCompanyId.value) {
      params.companyId = fireModeCompanyId.value
    }
    
    console.log('=== 获取档案列表 ===')
    console.log('解雇模式:', isFireMode.value)
    console.log('解雇模式公司ID:', fireModeCompanyId.value)
    console.log('请求参数:', params)
    
    const result = await employeeProfileController.listEmployeeProfileVoByPage(params)
    console.log('后端返回结果:', result)
    
    if (result?.data?.code === 0 && result.data.data) {
      profileList.value = result.data.data.records || []
      console.log('设置后的 profileList.value:', profileList.value)
      console.log('profileList.value 长度:', profileList.value.length)
      if (profileList.value.length > 0 && profileList.value[0]) {
        console.log('第一条档案的 companyId:', profileList.value[0].companyId, '类型:', typeof profileList.value[0].companyId)
      }
      
      // 更新公司筛选选项
      updateCompanyFilterOptions()
    } else {
      console.warn('获取档案失败，返回码:', result?.data?.code, '消息:', result?.data?.message)
    }
  } catch (error) {
    console.error('Failed to fetch profile list:', error)
    message.error('获取员工档案失败')
  }
}

// 获取部门列表
const fetchDepartmentOptions = async () => {
  if (!employeeInfo.value?.companyId) return

  try {
    const response = await departmentController.listDepartmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      companyId: employeeInfo.value.companyId,
      sortField: 'create_time',
      sortOrder: 'descend',
    })
    if (response?.data?.code === 0) {
      const records = response.data?.data?.records || []
      departmentOptions.value = records
        .filter((item: API.DepartmentVO) => item.id)
        .map((item: API.DepartmentVO) => ({
          label: item.name || `部门${item.id}`,
          value: item.id as number,
        }))
    }
  } catch (error) {
    console.error('Failed to fetch department list:', error)
  }
}

// 保存员工信息
const handleSaveEmployee = async () => {
  try {
    await employeeFormRef.value?.validate()
    
    // 使用 FormData 上传文件
    const formData = new FormData()
    formData.append('id', employeeId.value)
    
    // 管理员可以修改姓名和身份证号
    if (isSystemAdmin()) {
      if (employeeForm.name) {
        formData.append('name', employeeForm.name)
      }
      if (employeeForm.idCardNumber) {
        formData.append('idCardNumber', employeeForm.idCardNumber)
      }
    }
    
    // 只有当 departmentId 有有效值（不是 null 或 undefined）时才添加
    if (employeeForm.departmentId !== undefined && employeeForm.departmentId !== null) {
      formData.append('departmentId', String(employeeForm.departmentId))
    }
    if (employeeForm.phone) {
      formData.append('phone', employeeForm.phone)
    }
    if (employeeForm.email) {
      formData.append('email', employeeForm.email)
    }
    
    // 如果有新上传的头像文件，添加到 FormData
    if (avatarFile.value) {
      formData.append('photo', avatarFile.value)
    }
    
    // 使用 axios 发送 multipart/form-data 请求
    const API_BASE_URL = 'http://localhost:8123/api'
    const userInfo = localStorage.getItem('userInfo')
    let headers: any = {}
    
    // 添加认证头（如果需要）
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
    
    const response = await axios.post(`${API_BASE_URL}/employee/update`, formData, {
      headers: {
        ...headers,
        // 不设置 Content-Type，让浏览器自动设置 multipart/form-data 边界
      },
      withCredentials: true,
    })

    if (response?.data?.code === 0) {
      message.success('保存成功')
      editMode.value = false
      avatarFile.value = null
      avatarFileList.value = []
      await fetchEmployeeDetail()
    } else {
      message.error(response?.data?.message || '保存失败')
    }
  } catch (error: any) {
    console.error('Failed to save employee:', error)
    message.error(error?.response?.data?.message || error?.message || '保存失败')
  }
}

// 根据身份证号推断性别（第17位，奇数为男性，偶数为女性）
const inferGenderFromIdCard = (idCardNumber: string): string | null => {
  if (!idCardNumber || idCardNumber.length !== 18) return null
  
  // 18位身份证：第17位（索引16）是性别位
  const genderDigit = idCardNumber.charAt(16)
  const digit = parseInt(genderDigit)
  if (isNaN(digit)) return null
  
  // 奇数为男性，偶数为女性
  return digit % 2 === 1 ? '男' : '女'
}

// 验证身份证号格式（18位，出生日期格式）
const validateIdCardNumber = (idCardNumber: string): boolean => {
  if (!idCardNumber) return false
  
  // 必须是18位
  if (idCardNumber.length !== 18) return false
  
  // 前17位必须是数字，最后一位可以是数字或X
  if (!/^\d{17}[\dXx]$/.test(idCardNumber)) return false
  
  // 验证出生日期（第7-14位：YYYYMMDD）
  const year = parseInt(idCardNumber.substring(6, 10))
  const month = parseInt(idCardNumber.substring(10, 12))
  const day = parseInt(idCardNumber.substring(12, 14))
  
  // 年份范围：1900-当前年份
  const currentYear = new Date().getFullYear()
  if (year < 1900 || year > currentYear) return false
  
  // 月份范围：1-12
  if (month < 1 || month > 12) return false
  
  // 日期范围：1-31（简单验证，不验证具体月份的天数）
  if (day < 1 || day > 31) return false
  
  // 验证日期是否有效
  const date = new Date(year, month - 1, day)
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return false
  }
  
  return true
}

// 身份证号输入时自动推断性别
const handleIdCardNumberBlur = () => {
  if (employeeForm.idCardNumber && isSystemAdmin()) {
    const inferredGender = inferGenderFromIdCard(employeeForm.idCardNumber)
    if (inferredGender && employeeInfo.value) {
      // 更新显示的性别（但实际更新需要后端处理）
      employeeInfo.value.gender = inferredGender
    }
  }
}

// 计算年龄（从身份证号）
const calculateAge = (idCardNumber?: string): number | null => {
  if (!idCardNumber || idCardNumber.length !== 18) return null
  
  try {
    // 18位身份证：第7-14位为出生日期（YYYYMMDD）
    const year = idCardNumber.substring(6, 10)
    const month = idCardNumber.substring(10, 12)
    const day = idCardNumber.substring(12, 14)
    const birthDateStr = `${year}-${month}-${day}`
    
    const birthDate = new Date(birthDateStr)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    
    // 如果还没过生日，年龄减1
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    
    return age >= 0 ? age : null
  } catch (error) {
    console.error('Failed to calculate age:', error)
    return null
  }
}

// 员工年龄（计算属性）
const employeeAge = computed(() => {
  return calculateAge(employeeInfo.value?.idCardNumber)
})

// 头像文件
const avatarFile = ref<File | null>(null)

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
  avatarFile.value = file
  
  // 预览图片
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    employeeForm.photoUrl = reader.result as string
    avatarFileList.value = [
      {
        uid: Date.now().toString(),
        name: file.name,
        status: 'done',
        url: reader.result as string,
      } as any,
    ]
  }
  reader.onerror = () => {
    message.error('图片读取失败')
  }
  
  return false // 阻止自动上传，在保存时统一上传
}

// 取消编辑
const handleCancelEdit = () => {
  editMode.value = false
  // 恢复原始值
  if (employeeInfo.value) {
    employeeForm.name = employeeInfo.value.name || ''
    employeeForm.idCardNumber = employeeInfo.value.idCardNumber || ''
    employeeForm.phone = employeeInfo.value.phone || ''
    employeeForm.email = employeeInfo.value.email || ''
    employeeForm.departmentId = employeeInfo.value.departmentId
    employeeForm.photoUrl = employeeInfo.value.photoUrl
    avatarFileList.value = []
    avatarFile.value = null
  }
}

// 开始解雇流程（跳转到解雇模式）
const handleStartFireProcess = async () => {
  try {
    console.log('=== 开始解雇流程 ===')
    // 获取员工信息，确定公司ID
    const employeeDetail = await employeeController.getEmployeeVoById({ id: employeeId.value as any })
    console.log('员工详情:', employeeDetail)
    
    if (employeeDetail?.data?.code === 0 && employeeDetail.data.data) {
      const employee = employeeDetail.data.data
      console.log('员工信息:', employee)
      console.log('员工 companyId:', employee.companyId, '类型:', typeof employee.companyId)
      console.log('用户 companyId:', userStore.userInfo?.companyId, '类型:', typeof userStore.userInfo?.companyId)
      console.log('是否为系统管理员:', isSystemAdmin())
      
      // 保持 companyId 为原始类型（string 或 number），避免大整数精度丢失
      const currentCompanyId = isSystemAdmin() 
        ? employee.companyId 
        : (userStore.userInfo?.companyId || employee.companyId)
      
      console.log('计算出的 currentCompanyId:', currentCompanyId, '类型:', typeof currentCompanyId)
      
      if (!currentCompanyId) {
        message.error('无法确定员工所属公司')
        return
      }
      
      // 设置解雇模式
      isFireMode.value = true
      fireModeCompanyId.value = currentCompanyId
      fireProcessOperations.value = [] // 清空操作记录
      activeTab.value = 'profile' // 切换到档案标签页
      
      console.log('设置解雇模式完成:')
      console.log('  isFireMode.value:', isFireMode.value)
      console.log('  fireModeCompanyId.value:', fireModeCompanyId.value, '类型:', typeof fireModeCompanyId.value)
      console.log('  activeTab.value:', activeTab.value)
      
      // 重新获取档案列表（只显示当前公司的）
      await fetchProfileList()
      
      message.info('请完成员工档案编辑后点击确认解雇')
    } else {
      console.error('获取员工信息失败:', employeeDetail)
      message.error('获取员工信息失败')
    }
  } catch (error) {
    console.error('Failed to start fire process:', error)
    message.error('启动解雇流程失败')
  }
}

// 确认解雇（在档案编辑完成后）
const handleConfirmFire = async () => {
  try {
    fireConfirmLoading.value = true
    
    // 调用后端确认解雇接口
    const result = await employeeController.confirmFireEmployee({ 
      id: employeeId.value as any 
    })
    
    if (result?.data?.code === 0) {
      // 清空操作记录
      fireProcessOperations.value = []
      message.success('员工已成功解雇')
      router.push('/employees')
    } else {
      message.error(result?.data?.message || '解雇失败')
    }
  } catch (error: any) {
    console.error('Failed to confirm fire employee:', error)
    message.error(error?.response?.data?.message || '解雇失败')
  } finally {
    fireConfirmLoading.value = false
  }
}

// 回滚解雇流程中的所有操作（内部函数，不显示确认对话框）
const rollbackFireProcessOperations = async (): Promise<boolean> => {
  if (fireProcessOperations.value.length === 0) {
    return true
  }
  
  try {
    console.log('开始回滚解雇流程操作，操作数量:', fireProcessOperations.value.length)
    for (const op of fireProcessOperations.value) {
      if (op.type === 'add' && op.profileId) {
        // 删除新增的档案
        console.log('回滚：删除新增的档案，ID:', op.profileId)
        await employeeProfileController.deleteEmployeeProfile({ id: op.profileId as any })
      } else if (op.type === 'update' && op.profileId && op.originalData) {
        // 恢复原始数据
        console.log('回滚：恢复档案原始数据，ID:', op.profileId)
        await employeeProfileController.updateEmployeeProfile({
          id: op.profileId,
          startDate: op.originalData.startDate || undefined,
          endDate: op.originalData.endDate || undefined,
          performanceSummary: op.originalData.performanceSummary || undefined,
          attendanceRate: op.originalData.attendanceRate,
          hasMajorIncident: op.originalData.hasMajorIncident,
          reasonForLeaving: op.originalData.reasonForLeaving || undefined,
          occupation: op.originalData.occupation || undefined,
          annualSalary: op.originalData.annualSalary,
        })
      } else if (op.type === 'delete' && op.originalData) {
        // 恢复删除的档案（重新创建）
        console.log('回滚：恢复删除的档案，原始数据:', op.originalData)
        await employeeProfileController.addEmployeeProfile({
          employeeId: employeeId.value as any,
          companyId: op.originalData.companyId,
          startDate: op.originalData.startDate || undefined,
          endDate: op.originalData.endDate || undefined,
          performanceSummary: op.originalData.performanceSummary || undefined,
          attendanceRate: op.originalData.attendanceRate,
          hasMajorIncident: op.originalData.hasMajorIncident,
          reasonForLeaving: op.originalData.reasonForLeaving || undefined,
          occupation: op.originalData.occupation || undefined,
          annualSalary: op.originalData.annualSalary,
        })
      }
    }
    console.log('回滚操作完成')
    return true
  } catch (error) {
    console.error('Failed to rollback operations:', error)
    return false
  }
}

// 取消解雇流程（回滚所有操作）
const handleCancelFireProcess = async () => {
  if (fireProcessOperations.value.length > 0) {
    const { Modal } = await import('ant-design-vue')
    Modal.confirm({
      title: '确认取消解雇流程？',
      content: `解雇流程中有 ${fireProcessOperations.value.length} 项档案操作未完成，取消后将回滚这些操作。是否确认取消？`,
      okText: '确认取消',
      cancelText: '继续编辑',
      onOk: async () => {
        // 回滚所有操作
        const success = await rollbackFireProcessOperations()
        if (success) {
          message.success('已取消解雇流程，所有操作已回滚')
        } else {
          message.error('回滚操作失败，请手动检查')
        }
        
        // 清空解雇模式
        isFireMode.value = false
        fireModeCompanyId.value = undefined
        fireProcessOperations.value = []
        activeTab.value = 'info'
        await fetchProfileList() // 重新获取所有档案
      },
    })
  } else {
    // 没有操作记录，直接取消
    isFireMode.value = false
    fireModeCompanyId.value = undefined
    fireProcessOperations.value = []
    activeTab.value = 'info'
    await fetchProfileList() // 重新获取所有档案
  }
}

// 冻结/恢复账户
const handleToggleUserStatus = async () => {
  try {
    // 获取员工关联的用户ID
    const employeeDetail = await employeeController.getEmployeeById({ id: employeeId.value as any })
    if (employeeDetail?.data?.code === 0 && employeeDetail.data.data) {
      const employee = employeeDetail.data.data as any
      if (!employee.userId) {
        message.error('该员工未关联用户账户')
        return
      }

      const result = await userController.toggleUserStatus({ id: employee.userId })
      if (result?.data?.code === 0) {
        message.success('操作成功')
        // 更新账户状态，使按钮立即更新
        await fetchUserAccountStatus()
        await fetchEmployeeDetail()
      } else {
        message.error('操作失败')
      }
    }
  } catch (error) {
    console.error('Failed to toggle user status:', error)
    message.error('操作失败')
  }
}

// 获取用户账户状态
const userAccountStatus = ref<'normal' | 'frozen' | 'none'>('none')
const fetchUserAccountStatus = async () => {
  // 只有系统管理员才能访问用户账户状态
  if (!isSystemAdmin()) {
    userAccountStatus.value = 'none'
    return
  }
  
  try {
    const employeeDetail = await employeeController.getEmployeeById({ id: employeeId.value as any })
    if (employeeDetail?.data?.code === 0 && employeeDetail.data.data) {
      const employee = employeeDetail.data.data as any
      if (employee.userId) {
        const userDetail = await userController.getUserById({ id: employee.userId })
        if (userDetail?.data?.code === 0 && userDetail.data.data) {
          const user = userDetail.data.data as any
          userAccountStatus.value = user.isDelete ? 'frozen' : 'normal'
          return
        }
      }
    }
    userAccountStatus.value = 'none'
  } catch (error) {
    console.error('Failed to fetch user status:', error)
    userAccountStatus.value = 'none'
  }
}

// 账户状态按钮文本
const accountButtonText = computed(() => {
  if (userAccountStatus.value === 'frozen') return '恢复账户'
  if (userAccountStatus.value === 'normal') return '冻结账户'
  return '无关联账户'
})

// 账户状态按钮类型
const accountButtonType = computed(() => {
  if (userAccountStatus.value === 'frozen') return 'primary'
  return 'default'
})

// 离职日期验证规则
const endDateRules = [
  {
    validator: (_rule: any, value: any) => {
      // 离职日期可以为空
      if (!value || (typeof value === 'string' && !value.trim())) {
        return Promise.resolve()
      }
      
      // 只有当入职日期也有值时才进行日期比较
      const startDate = profileForm.startDate
      if (startDate && typeof startDate === 'string' && startDate.trim() && value < startDate) {
        return Promise.reject('离职日期不能早于入职日期')
      }
      
      return Promise.resolve()
    },
    trigger: 'change',
  },
]

// 获取公司列表（管理员使用）
const fetchCompanyOptions = async () => {
  if (!isSystemAdmin()) {
    return
  }

  try {
    companyLoading.value = true
    const response = await companyController.listCompanyVoByPage({
      pageNum: 1,
      pageSize: 1000,
      sortField: 'create_time',
      sortOrder: 'descend',
    })
    if (response?.data?.code === 0) {
      const records = response.data?.data?.records || []
      companyOptions.value = records
        .filter((item: API.CompanyVO) => item.id)
        .map((item: API.CompanyVO) => ({
          label: item.name || `公司${item.id}`,
          value: item.id as number,
        }))
    }
  } catch (error) {
    console.error('Failed to fetch company list:', error)
  } finally {
    companyLoading.value = false
  }
}

// 获取公司筛选选项（从档案列表中提取所有公司）
const updateCompanyFilterOptions = () => {
  const companyMap = new Map<string, { label: string; companyId: string }>()
  
  // 从档案列表中提取公司
  profileList.value.forEach((profile) => {
    if (profile.companyId && profile.companyName) {
      const companyIdStr = String(profile.companyId)
      if (!companyMap.has(companyIdStr)) {
        companyMap.set(companyIdStr, {
          label: profile.companyName,
          companyId: companyIdStr,
        })
      }
    }
  })
  
  // 从奖惩记录公司列表中提取公司
  rewardPunishmentCompanies.value.forEach((company) => {
    if (!companyMap.has(company.companyId)) {
      companyMap.set(company.companyId, {
        label: company.companyName,
        companyId: company.companyId,
      })
    }
  })
  
  companyFilterOptions.value = Array.from(companyMap.values()).map((item) => ({
    label: item.label,
    value: item.companyId,
    companyId: item.companyId,
  }))
}

// 公司筛选过滤函数
const filteredCompanyFilterOptions = computed(() => {
  if (!companySearchText.value) {
    return companyFilterOptions.value
  }
  const searchLower = companySearchText.value.toLowerCase()
  return companyFilterOptions.value.filter(
    (option) => option.label.toLowerCase().includes(searchLower),
  )
})

// 根据筛选条件过滤后的奖惩记录公司列表
const filteredRewardPunishmentCompanies = computed(() => {
  let filtered = rewardPunishmentCompanies.value
  
  // 如果选择了公司筛选，只显示选中公司的奖惩记录
  if (selectedCompanyId.value) {
    filtered = filtered.filter(company => company.companyId === selectedCompanyId.value)
  }
  // 如果开启了"仅查看本公司"，只显示当前公司的奖惩记录
  else if (showCurrentCompanyOnly.value) {
    const currentUserCompanyId = userStore.userInfo?.companyId
    if (currentUserCompanyId) {
      const currentUserCompanyIdStr = String(currentUserCompanyId)
      filtered = filtered.filter(company => company.companyId === currentUserCompanyIdStr)
    }
  }
  
  return filtered
})

// 切换仅查看本公司
const handleToggleCurrentCompanyOnly = () => {
  showCurrentCompanyOnly.value = !showCurrentCompanyOnly.value
  // 如果开启仅查看本公司，清除公司筛选
  if (showCurrentCompanyOnly.value) {
    selectedCompanyId.value = undefined
  }
}

// 公司筛选变化
const handleCompanyFilterChange = (value: string | undefined) => {
  selectedCompanyId.value = value
  // 如果选择了公司，关闭仅查看本公司
  if (value) {
    showCurrentCompanyOnly.value = false
  }
}

// 公司搜索文本变化
const handleCompanyFilterSearch = (value: string) => {
  companySearchText.value = value
  // 如果搜索文本为空，清除选中
  if (!value) {
    // 不清除 selectedCompanyId，保持用户的选择
  }
}

// 公司搜索过滤函数
const filterCompanyOption = (input: string, option: any) => {
  const label = option.label?.toString().toLowerCase() || ''
  return label.includes(input.toLowerCase())
}

// 打开新增档案弹窗
const openAddProfile = async () => {
  isEditProfile.value = false
  profileForm.id = undefined
  profileForm.companyId = undefined
  profileForm.startDate = ''
  profileForm.endDate = ''
  profileForm.performanceSummary = ''
  profileForm.attendanceRate = undefined
  profileForm.hasMajorIncident = false
  profileForm.reasonForLeaving = ''
  profileForm.occupation = ''
  profileForm.annualSalary = undefined
  
  // 解雇模式下，自动设置当前公司ID
  if (isFireMode.value && fireModeCompanyId.value) {
    profileForm.companyId = typeof fireModeCompanyId.value === 'string' ? Number(fireModeCompanyId.value) : fireModeCompanyId.value
  } else if (isSystemAdmin()) {
    // 管理员需要加载公司列表
    await fetchCompanyOptions()
  }
  
  profileEditModalVisible.value = true
}

// 打开查看档案详情弹窗
const openViewProfile = (profile: API.EmployeeProfileVO) => {
  currentProfile.value = profile
  profileDetailModalVisible.value = true
}

// 判断是否可以编辑档案
const canEditProfile = (profile: API.EmployeeProfileVO | null): boolean => {
  if (!profile) return false
  
  // 系统管理员可以编辑所有档案
  if (isSystemAdmin()) {
    return true
  }
  
  // company_admin 或 hr 只能编辑本公司员工在本公司产生的档案
  if (isCompanyAdmin() || isHR()) {
    const currentUserCompanyId = userStore.userInfo?.companyId
    if (!currentUserCompanyId) return false
    
    // 检查档案的 companyId 是否等于当前用户的 companyId
    const profileCompanyId = profile.companyId
    if (profileCompanyId && String(profileCompanyId) === String(currentUserCompanyId)) {
      // 检查员工的 companyId 是否等于当前用户的 companyId
      const employeeCompanyId = employeeInfo.value?.companyId
      if (employeeCompanyId && String(employeeCompanyId) === String(currentUserCompanyId)) {
        return true
      }
    }
    return false
  }
  
  return false
}

// 打开编辑档案弹窗（从详情页）
const openEditProfileFromDetail = () => {
  if (!currentProfile.value) return
  
  // 权限检查
  if (!canEditProfile(currentProfile.value)) {
    message.error('您没有权限编辑该档案')
    return
  }
  
  isEditProfile.value = true
  profileForm.id = currentProfile.value.id
  profileForm.startDate = currentProfile.value.startDate || ''
  profileForm.endDate = currentProfile.value.endDate || ''
  profileForm.performanceSummary = currentProfile.value.performanceSummary || ''
  profileForm.attendanceRate = currentProfile.value.attendanceRate
  profileForm.hasMajorIncident = currentProfile.value.hasMajorIncident || false
  profileForm.reasonForLeaving = currentProfile.value.reasonForLeaving || ''
  profileForm.occupation = currentProfile.value.occupation || ''
  profileForm.annualSalary = currentProfile.value.annualSalary
  
  profileDetailModalVisible.value = false
  profileEditModalVisible.value = true
}

// 保存档案
const handleSaveProfile = async () => {
  try {
    await profileFormRef.value?.validate()

    // 编辑时进行权限检查
    if (isEditProfile.value && profileForm.id) {
      // 查找当前编辑的档案
      const editingProfile = profileList.value.find(p => p.id === profileForm.id)
      if (editingProfile && !canEditProfile(editingProfile)) {
        message.error('您没有权限编辑该档案')
        return
      }
    }

    if (isEditProfile.value && profileForm.id) {
      // 更新档案
      // 如果是解雇流程，记录原始数据
      let originalData: any = null
      if (isFireMode.value) {
        const existingProfile = profileList.value.find(p => p.id === profileForm.id)
        if (existingProfile) {
          originalData = {
            startDate: existingProfile.startDate,
            endDate: existingProfile.endDate,
            performanceSummary: existingProfile.performanceSummary,
            attendanceRate: existingProfile.attendanceRate,
            hasMajorIncident: existingProfile.hasMajorIncident,
            reasonForLeaving: existingProfile.reasonForLeaving,
            occupation: existingProfile.occupation,
            annualSalary: existingProfile.annualSalary,
          }
        }
      }
      
      const result = await employeeProfileController.updateEmployeeProfile({
        id: profileForm.id,
        startDate: profileForm.startDate || undefined,
        endDate: profileForm.endDate || undefined,
        performanceSummary: profileForm.performanceSummary || undefined,
        attendanceRate: profileForm.attendanceRate,
        hasMajorIncident: profileForm.hasMajorIncident,
        reasonForLeaving: profileForm.reasonForLeaving || undefined,
        occupation: profileForm.occupation || undefined,
        annualSalary: profileForm.annualSalary,
      })

      if (result?.data?.code === 0) {
        message.success('档案更新成功')
        profileEditModalVisible.value = false
        
        // 如果是解雇流程，记录操作
        if (isFireMode.value) {
          // 检查是否已存在该档案的操作记录
          const existingOpIndex = fireProcessOperations.value.findIndex(
            op => op.type === 'update' && op.profileId === profileForm.id
          )
          if (originalData) {
            if (existingOpIndex >= 0 && fireProcessOperations.value[existingOpIndex]) {
              // 更新操作记录
              fireProcessOperations.value[existingOpIndex].originalData = originalData
            } else {
              // 新增操作记录
              fireProcessOperations.value.push({
                type: 'update',
                profileId: profileForm.id,
                originalData: originalData,
              })
            }
          }
        }
        
        await fetchProfileList()
      } else {
        message.error('档案更新失败')
      }
    } else {
      // 新增档案
      const addPayload: API.EmployeeProfileAddRequest = {
        employeeId: employeeId.value as any,
        startDate: profileForm.startDate || undefined,
        endDate: profileForm.endDate || undefined,
        performanceSummary: profileForm.performanceSummary || undefined,
        attendanceRate: profileForm.attendanceRate,
        hasMajorIncident: profileForm.hasMajorIncident,
        reasonForLeaving: profileForm.reasonForLeaving || undefined,
        occupation: profileForm.occupation || undefined,
        annualSalary: profileForm.annualSalary,
      }
      
      // 设置公司ID
      if (isSystemAdmin() && !isFireMode.value) {
        // 系统管理员新增时必须指定公司
        addPayload.companyId = profileForm.companyId
      } else {
        // 非系统管理员（HR和company_admin）使用当前用户的公司ID
        const currentCompanyId = userStore.userInfo?.companyId
        if (currentCompanyId) {
          addPayload.companyId = typeof currentCompanyId === 'string' ? Number(currentCompanyId) : currentCompanyId
        } else if (isFireMode.value && fireModeCompanyId.value) {
          // 解雇模式下使用解雇流程中的公司ID
          addPayload.companyId = typeof fireModeCompanyId.value === 'string' ? Number(fireModeCompanyId.value) : fireModeCompanyId.value
        }
      }
      
      const result = await employeeProfileController.addEmployeeProfile(addPayload)

      if (result?.data?.code === 0) {
        message.success('档案创建成功')
        profileEditModalVisible.value = false
        
        // 如果是解雇流程，记录操作
        if (isFireMode.value && result.data.data) {
          fireProcessOperations.value.push({
            type: 'add',
            profileId: result.data.data as number,
          })
        }
        
        await fetchProfileList()
      } else {
        message.error('档案创建失败')
      }
    }
  } catch (error) {
    console.error('Failed to save profile:', error)
  }
}

// 删除档案（从详情页）
const handleDeleteProfileFromDetail = async () => {
  if (!currentProfile.value?.id) return
  
  // 权限检查
  if (!canEditProfile(currentProfile.value)) {
    message.error('您没有权限删除该档案')
    return
  }
  
  try {
    // 如果是解雇流程，记录删除操作和完整档案数据
    let profileDataForRollback: any = null
    if (isFireMode.value && currentProfile.value) {
      profileDataForRollback = {
        companyId: currentProfile.value.companyId,
        startDate: currentProfile.value.startDate,
        endDate: currentProfile.value.endDate,
        performanceSummary: currentProfile.value.performanceSummary,
        attendanceRate: currentProfile.value.attendanceRate,
        hasMajorIncident: currentProfile.value.hasMajorIncident,
        reasonForLeaving: currentProfile.value.reasonForLeaving,
        occupation: currentProfile.value.occupation,
        annualSalary: currentProfile.value.annualSalary,
      }
    }
    
    const result = await employeeProfileController.deleteEmployeeProfile({ id: currentProfile.value.id as any })
    if (result?.data?.code === 0) {
      message.success('档案已删除')
      profileDetailModalVisible.value = false
      
      // 如果是解雇流程，记录删除操作
      if (isFireMode.value && profileDataForRollback) {
        fireProcessOperations.value.push({
          type: 'delete',
          profileId: currentProfile.value.id,
          originalData: profileDataForRollback,
        })
        console.log('解雇流程中删除档案，已记录操作:', {
          type: 'delete',
          profileId: currentProfile.value.id,
          originalData: profileDataForRollback,
        })
      }
      
      await fetchProfileList()
    } else {
      message.error('删除失败')
    }
  } catch (error) {
    console.error('Failed to delete profile:', error)
    message.error('删除失败')
  }
}

// 返回列表
const handleBack = () => {
  router.push('/employees')
}

// 路由守卫：离开页面时检查是否需要回滚
onBeforeRouteLeave((to, from, next) => {
  if (isFireMode.value && fireProcessOperations.value.length > 0) {
    console.log('检测到解雇流程未完成，准备回滚操作')
    // 自动回滚操作
    rollbackFireProcessOperations().then((success) => {
      if (success) {
        message.warning('已自动回滚解雇流程中的档案操作')
      } else {
        message.error('自动回滚失败，请手动检查')
      }
      // 清空解雇模式
      isFireMode.value = false
      fireModeCompanyId.value = undefined
      fireProcessOperations.value = []
      next() // 允许离开
    }).catch((error) => {
      console.error('回滚操作出错:', error)
      message.error('回滚操作出错')
      next() // 即使出错也允许离开
    })
  } else {
    next() // 没有未完成的操作，直接允许离开
  }
})

// 页面卸载前检查是否需要回滚
onBeforeUnmount(() => {
  if (isFireMode.value && fireProcessOperations.value.length > 0) {
    console.log('页面卸载，检测到解雇流程未完成，准备回滚操作')
    // 同步回滚（页面卸载时无法显示消息）
    rollbackFireProcessOperations().catch((error) => {
      console.error('页面卸载时回滚操作出错:', error)
    })
  }
})

onMounted(async () => {
  await fetchEmployeeDetail()
  await fetchDepartmentOptions()
  await fetchProfileList()
  await fetchRewardPunishmentCompanies() // 获取有奖惩记录的公司列表
  // 只有系统管理员才需要获取用户账户状态
  if (isSystemAdmin()) {
    await fetchUserAccountStatus()
  }
})
</script>

<template>
  <div class="employee-detail">
    <a-card>
      <!-- 页面头部 -->
      <template #title>
        <a-space>
          <a-button type="text" @click="handleBack">
            <template #icon>←</template>
          </a-button>
          <span>员工详情</span>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <template v-if="!isFireMode && !isSystemAdmin()">
            <a-popconfirm
              title="确认解雇？"
              description="确定要解雇该员工吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleStartFireProcess"
            >
              <a-button danger>解雇员工</a-button>
            </a-popconfirm>
          </template>
          <template v-else-if="isFireMode && !isSystemAdmin()">
            <a-button @click="handleCancelFireProcess">取消解雇</a-button>
          </template>
          
          <a-popconfirm
            v-if="isSystemAdmin() && userAccountStatus !== 'none'"
            :title="userAccountStatus === 'frozen' ? '确认恢复账户？' : '确认冻结账户？'"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleToggleUserStatus"
          >
            <a-button :type="accountButtonType">{{ accountButtonText }}</a-button>
          </a-popconfirm>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <!-- 标签页 -->
        <a-tabs v-model:activeKey="activeTab">
          <!-- 基本信息 -->
          <a-tab-pane key="info" tab="基本信息">
            <!-- 员工头像和基本信息 -->
            <div style="display: flex; gap: 24px; margin-bottom: 24px">
              <!-- 左侧头像 -->
              <div style="flex-shrink: 0">
                <!-- 查看模式：显示头像 -->
                <a-avatar
                  v-if="!editMode"
                  v-show="employeeInfo?.photoUrl"
                  :src="employeeInfo?.photoUrl"
                  :size="180"
                  shape="square"
                  style="width: 135px; height: 180px; border-radius: 8px; object-fit: cover"
                />
                <a-avatar
                  v-if="!editMode && !employeeInfo?.photoUrl"
                  :size="180"
                  shape="square"
                  style="width: 135px; height: 180px; border-radius: 8px; background-color: #1890ff; font-size: 60px; line-height: 180px"
                >
                  {{ employeeInfo?.name?.charAt(0) }}
                </a-avatar>
                <!-- 编辑模式：显示头像预览 -->
                <div v-if="editMode">
                  <a-avatar
                    v-if="employeeForm.photoUrl"
                    :src="employeeForm.photoUrl"
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
                    {{ employeeInfo?.name?.charAt(0) }}
                  </a-avatar>
                </div>
              </div>

              <!-- 右侧基本信息 -->
              <div style="flex: 1">
                <a-descriptions bordered :column="2">
                  <a-descriptions-item label="姓名">{{ employeeInfo?.name }}</a-descriptions-item>
                  <a-descriptions-item label="性别">{{ employeeInfo?.gender }}</a-descriptions-item>
                  <a-descriptions-item label="年龄">
                    {{ employeeAge !== null ? `${employeeAge}岁` : '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="身份证号">{{
                    employeeInfo?.idCardNumber || '-'
                  }}</a-descriptions-item>
                  <a-descriptions-item label="所属公司">{{
                    employeeInfo?.companyName || '-'
                  }}</a-descriptions-item>
                </a-descriptions>
              </div>
            </div>

            <a-divider />

            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
              <h3>可编辑信息</h3>
              <a-space v-if="!editMode">
                <a-button type="primary" @click="editMode = true">编辑</a-button>
              </a-space>
              <a-space v-else>
                <a-button @click="handleCancelEdit">取消</a-button>
                <a-button type="primary" @click="handleSaveEmployee">保存</a-button>
              </a-space>
            </div>

            <a-form
              ref="employeeFormRef"
              :model="employeeForm"
              layout="vertical"
              :disabled="!editMode"
            >
              <a-row :gutter="16">
                <!-- 管理员可以编辑姓名和身份证号 -->
                <template v-if="isSystemAdmin()">
                  <a-col :span="12">
                    <a-form-item label="姓名" name="name">
                      <a-input v-model:value="employeeForm.name" placeholder="请输入姓名" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item 
                      label="身份证号" 
                      name="idCardNumber"
                      :rules="idCardNumberRules"
                    >
                      <a-input
                        v-model:value="employeeForm.idCardNumber"
                        placeholder="请输入18位身份证号"
                        maxlength="18"
                        @blur="handleIdCardNumberBlur"
                      />
                    </a-form-item>
                  </a-col>
                </template>
                <a-col :span="12">
                  <a-form-item label="手机号" name="phone">
                    <a-input v-model:value="employeeForm.phone" placeholder="请输入手机号" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="邮箱" name="email">
                    <a-input v-model:value="employeeForm.email" placeholder="请输入邮箱" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="部门" name="departmentId">
                    <a-select
                      v-model:value="employeeForm.departmentId"
                      :options="departmentOptions"
                      placeholder="请选择部门"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
            
            <!-- 头像上传区域（编辑模式） -->
            <div v-if="editMode" style="margin-top: 16px">
              <a-form-item label="头像" name="photoUrl">
                <a-upload
                  v-model:file-list="avatarFileList"
                  name="photo"
                  list-type="picture-card"
                  :show-upload-list="true"
                  :before-upload="beforeAvatarUpload"
                  accept="image/*"
                  :max-count="1"
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
            </div>
          </a-tab-pane>

          <!-- 员工档案 -->
          <a-tab-pane key="profile" tab="员工档案">
            <!-- 解雇模式提示 -->
            <a-alert
              v-if="isFireMode"
              type="warning"
              message="解雇流程进行中"
              description='请完成员工在本公司的档案编辑工作。完成档案编辑后，点击右下角的"确认解雇"按钮完成解雇操作。如果关闭页面，解雇操作将不会执行。'
              show-icon
              style="margin-bottom: 16px"
            />
            
            <div style="margin-bottom: 16px">
              <a-space>
                <a-button 
                  v-if="isFireMode" 
                  type="primary" 
                  @click="openAddProfile"
                >
                  新增档案
                </a-button>
                <a-button 
                  v-else 
                  type="primary" 
                  @click="openAddProfile"
                >
                  新增档案
                </a-button>
                <a-button
                  v-if="(isHR() || isCompanyAdmin()) && !isFireMode"
                  type="primary"
                  @click="openAddRewardPunishment"
                >
                  新增奖惩记录
                </a-button>
              </a-space>
            </div>
            
            <!-- 筛选控件（非解雇模式） -->
            <div v-if="!isFireMode && (groupedProfiles.length > 0 || rewardPunishmentCompanies.length > 0 || profileList.length > 0)" style="margin-bottom: 16px">
              <a-row :gutter="[16, 16]">
                <a-col :xs="24" :sm="12" :md="8">
                  <a-checkbox :checked="showCurrentCompanyOnly" @change="handleToggleCurrentCompanyOnly">
                    仅查看本公司
                  </a-checkbox>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8">
                  <a-select
                    v-model:value="selectedCompanyId"
                    placeholder="筛选公司"
                    show-search
                    allow-clear
                    style="width: 100%"
                    :options="filteredCompanyFilterOptions"
                    :filter-option="false"
                    @change="handleCompanyFilterChange"
                    @search="handleCompanyFilterSearch"
                  />
                </a-col>
              </a-row>
            </div>

            <!-- 合并有档案的公司和有奖惩记录的公司 -->
            <div v-if="groupedProfiles.length === 0 && filteredRewardPunishmentCompanies.length === 0" style="text-align: center; padding: 40px; color: #8c8c8c">
              <template v-if="isFireMode">
                暂无当前公司的档案信息，请先新增档案
              </template>
              <template v-else>
                暂无档案信息
              </template>
            </div>
            
            <!-- 合并显示：有档案的公司和有奖惩记录的公司 -->
            <div v-else>
              <!-- 创建合并后的公司列表（包含档案的公司 + 只有奖惩记录的公司） -->
              <template v-for="group in groupedProfiles" :key="group.companyId">
                <div style="margin-bottom: 32px">
                  <!-- 公司标题 -->
                  <h3 style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #262626">
                    {{ group.companyName }}
                    <a-tag 
                      v-if="(isFireMode && fireModeCompanyId && group.companyId === String(fireModeCompanyId)) || 
                             (!isFireMode && userStore.userInfo?.companyId && group.companyId === String(userStore.userInfo.companyId))" 
                      color="orange" 
                      style="margin-left: 8px"
                    >
                      当前公司
                    </a-tag>
                  </h3>
                  
                  <!-- 该公司的档案表格 -->
                  <a-table
                    :columns="[
                      { title: '入职日期', dataIndex: 'startDate', width: 120 },
                      { title: '离职日期', dataIndex: 'endDate', width: 120 },
                      { title: '职位', dataIndex: 'occupation', width: 120 },
                      { title: '年薪(万元)', dataIndex: 'annualSalary', width: 120 },
                      { title: '出勤率(%)', dataIndex: 'attendanceRate', width: 100 },
                      { title: '操作', key: 'action', width: 100, align: 'center' },
                    ]"
                    :data-source="group.profiles"
                    :pagination="false"
                    size="middle"
                    :bordered="true"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.dataIndex === 'annualSalary'">
                        {{ record.annualSalary ? record.annualSalary.toFixed(2) : '-' }}
                      </template>
                      <template v-else-if="column.key === 'action'">
                        <a-button type="link" size="small" @click="openViewProfile(record)">
                          查看
                        </a-button>
                      </template>
                    </template>
                  </a-table>
                  
                  <!-- 查看奖惩记录按钮 -->
                  <div style="margin-top: 16px; text-align: right">
                    <a-button type="primary" @click="viewRewardPunishment(group.companyId, group.companyName)">
                      查看奖惩记录
                    </a-button>
                  </div>
                </div>
              </template>
              
              <!-- 显示只有奖惩记录但没有档案的公司（根据筛选条件过滤） -->
              <template v-for="company in filteredRewardPunishmentCompanies" :key="company.companyId">
                <div v-if="!groupedProfiles.some(g => g.companyId === company.companyId)" style="margin-bottom: 32px">
                  <!-- 公司标题 -->
                  <h3 style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #262626">
                    {{ company.companyName }}
                    <a-tag 
                      v-if="userStore.userInfo?.companyId && company.companyId === String(userStore.userInfo.companyId)" 
                      color="orange" 
                      style="margin-left: 8px"
                    >
                      当前公司
                    </a-tag>
                  </h3>
                  
                  <!-- 提示信息 -->
                  <a-alert
                    message="暂无档案信息"
                    description="该员工在此公司暂无档案记录"
                    type="info"
                    show-icon
                    style="margin-bottom: 16px"
                  />
                  
                  <!-- 查看奖惩记录按钮 -->
                  <div style="text-align: right">
                    <a-button type="primary" @click="viewRewardPunishment(company.companyId, company.companyName)">
                      查看奖惩记录
                    </a-button>
                  </div>
                </div>
              </template>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-spin>
    </a-card>

    <!-- 解雇模式确认按钮（固定在右下角） -->
    <div v-if="isFireMode" style="position: fixed; bottom: 80px; right: 40px; z-index: 1000; padding: 16px; background: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15)">
      <a-popconfirm
        title="确认解雇？"
        description="确定要完成解雇操作吗？"
        ok-text="确认解雇"
        cancel-text="取消"
        @confirm="handleConfirmFire"
      >
        <a-button 
          type="primary" 
          danger 
          size="large"
          :loading="fireConfirmLoading"
          style="width: 120px"
        >
          确认解雇
        </a-button>
      </a-popconfirm>
    </div>

    <!-- 档案详情弹窗 -->
    <a-modal
      v-model:visible="profileDetailModalVisible"
      title="档案详情"
      :footer="null"
      width="800px"
      :maskClosable="false"
    >
      <template #extra>
        <a-space v-if="canEditProfile(currentProfile)">
          <a-button type="primary" @click="openEditProfileFromDetail">编辑</a-button>
          <a-popconfirm
            title="确认删除？"
            description="确定要删除该档案吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleDeleteProfileFromDetail"
          >
            <a-button danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
      
      <a-descriptions v-if="currentProfile" bordered :column="2">
        <a-descriptions-item label="公司">{{ currentProfile.companyName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="职位">{{ currentProfile.occupation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="入职日期">{{ currentProfile.startDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="离职日期">{{ currentProfile.endDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="年薪(万元)">
          {{ currentProfile.annualSalary ? currentProfile.annualSalary.toFixed(2) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="出勤率(%)">{{ currentProfile.attendanceRate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="是否有重大事故">
          <a-tag :color="currentProfile.hasMajorIncident ? 'red' : 'green'">
            {{ currentProfile.hasMajorIncident ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentProfile.createTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="绩效总结" :span="2">
          {{ currentProfile.performanceSummary || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="离职原因" :span="2">
          {{ currentProfile.reasonForLeaving || '-' }}
        </a-descriptions-item>
      </a-descriptions>
      
      <div v-if="canEditProfile(currentProfile)" style="margin-top: 16px; text-align: right">
        <a-space>
          <a-button type="primary" @click="openEditProfileFromDetail">编辑</a-button>
          <a-popconfirm
            title="确认删除？"
            description="确定要删除该档案吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleDeleteProfileFromDetail"
          >
            <a-button danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </a-modal>

    <!-- 档案编辑弹窗 -->
    <a-modal
      v-model:visible="profileEditModalVisible"
      :title="isEditProfile ? '编辑档案' : '新增档案'"
      ok-text="保存"
      cancel-text="取消"
      width="800px"
      :maskClosable="false"
      @ok="handleSaveProfile"
    >
      <a-form ref="profileFormRef" :model="profileForm" layout="vertical">
        <a-row :gutter="16">
          <!-- 管理员新增档案时必须指定公司（解雇模式下不显示，已自动设置） -->
          <a-col v-if="isSystemAdmin() && !isEditProfile && !isFireMode" :span="24">
            <a-form-item 
              label="所属公司" 
              name="companyId"
              :rules="[
                { required: true, message: '请选择所属公司' }
              ]"
            >
              <a-select
                v-model:value="profileForm.companyId"
                :options="companyOptions"
                placeholder="请选择或搜索公司"
                :loading="companyLoading"
                show-search
                :filter-option="filterCompanyOption"
              />
            </a-form-item>
          </a-col>
          <!-- 解雇模式下显示公司信息（只读） -->
          <a-col v-if="isFireMode && !isEditProfile && fireModeCompanyId" :span="24">
            <a-form-item label="所属公司">
              <a-input :value="employeeInfo?.companyName || '当前公司'" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              label="入职日期" 
              name="startDate"
              :rules="[
                { required: true, message: '请选择入职日期' }
              ]"
            >
              <a-input v-model:value="profileForm.startDate" type="date" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item 
              label="离职日期" 
              name="endDate"
              :rules="endDateRules"
            >
              <a-input v-model:value="profileForm.endDate" type="date" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职位" name="occupation">
              <a-input v-model:value="profileForm.occupation" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="年薪(万元)" name="annualSalary">
              <a-input-number
                v-model:value="profileForm.annualSalary"
                :min="0"
                :step="10"
                style="width: 100%"
                placeholder="请输入年薪（万元）"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="出勤率(%)" name="attendanceRate">
              <a-input-number
                v-model:value="profileForm.attendanceRate"
                :min="0"
                :max="100"
                :step="0.1"
                style="width: 100%"
                placeholder="请输入出勤率"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="是否有重大事故" name="hasMajorIncident">
              <a-switch v-model:checked="profileForm.hasMajorIncident" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="绩效总结" name="performanceSummary">
              <a-textarea
                v-model:value="profileForm.performanceSummary"
                :rows="3"
                placeholder="请输入绩效总结"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="离职原因" name="reasonForLeaving">
              <a-textarea
                v-model:value="profileForm.reasonForLeaving"
                :rows="3"
                placeholder="请输入离职原因"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 奖惩记录对话框 -->
    <a-modal
      v-model:open="rewardPunishmentModalVisible"
      :title="`${currentRewardCompanyName} - 奖惩记录`"
      :footer="null"
      width="900"
    >
      <a-spin :spinning="rewardPunishmentLoading.get(currentRewardCompanyId || '') || false">
        <a-table
          :columns="[
            { title: '类型', dataIndex: 'type', width: 80, align: 'center' },
            { title: '发生日期', dataIndex: 'date', width: 120 },
            { title: '描述', dataIndex: 'description', width: 300 },
            { title: '金额', dataIndex: 'amount', width: 120, align: 'right' },
            { title: '操作人', dataIndex: 'operatorName', width: 120 },
            { title: '操作', key: 'action', width: 150, align: 'center' },
          ]"
          :data-source="rewardPunishmentList"
          :loading="rewardPunishmentLoading.get(currentRewardCompanyId || '') || false"
          :pagination="false"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'type'">
              <a-tag :color="getRewardPunishmentTypeColor(record.type)">
                {{ getRewardPunishmentTypeText(record.type) }}
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'amount'">
              {{ record.amount ? `¥${Number(record.amount).toFixed(2)}` : '-' }}
            </template>
            <template v-else-if="column.dataIndex === 'date'">
              {{ record.date || '-' }}
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button
                  v-if="canEditRewardPunishment(record)"
                  type="link"
                  size="small"
                  @click="openEditRewardPunishment(record)"
                >
                  修改
                </a-button>
                <a-popconfirm
                  v-if="canDeleteRewardPunishment(record)"
                  title="确认删除？"
                  description="确定要删除该奖惩记录吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteRewardPunishment(record)"
                >
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
                <span v-if="!canEditRewardPunishment(record) && !canDeleteRewardPunishment(record)" style="color: #d9d9d9">-</span>
              </a-space>
            </template>
          </template>
        </a-table>
        
        <a-empty
          v-if="!rewardPunishmentLoading.get(currentRewardCompanyId || '') && rewardPunishmentList.length === 0"
          description="暂无奖惩记录"
          style="padding: 40px"
        />
      </a-spin>
    </a-modal>

    <!-- 奖惩记录编辑对话框 -->
    <a-modal
      v-model:open="rewardPunishmentEditModalVisible"
      title="编辑奖惩记录"
      ok-text="保存"
      cancel-text="取消"
      width="600"
      :maskClosable="false"
      @ok="handleSaveRewardPunishment"
    >
      <a-form ref="rewardPunishmentFormRef" :model="rewardPunishmentForm" layout="vertical">
        <a-form-item
          label="类型"
          name="type"
          :rules="[{ required: true, message: '请选择类型' }]"
        >
          <a-radio-group v-model:value="rewardPunishmentForm.type">
            <a-radio :value="1">奖励</a-radio>
            <a-radio :value="2">惩罚</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          label="发生日期"
          name="date"
          :rules="[{ required: true, message: '请选择发生日期' }]"
        >
          <a-input v-model:value="rewardPunishmentForm.date" type="date" />
        </a-form-item>
        <a-form-item
          label="描述"
          name="description"
        >
          <a-textarea
            v-model:value="rewardPunishmentForm.description"
            :rows="4"
            placeholder="请输入描述"
          />
        </a-form-item>
        <a-form-item
          label="金额"
          name="amount"
          :rules="[{ required: true, message: '请输入金额' }]"
        >
          <a-input-number
            v-model:value="rewardPunishmentForm.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 新增奖惩记录对话框 -->
    <a-modal
      v-model:open="rewardPunishmentAddModalVisible"
      title="新增奖惩记录"
      ok-text="保存"
      cancel-text="取消"
      width="600"
      :maskClosable="false"
      @ok="handleSaveAddRewardPunishment"
    >
      <a-form ref="rewardPunishmentAddFormRef" :model="rewardPunishmentAddForm" layout="vertical">
        <a-form-item
          label="类型"
          name="type"
          :rules="[{ required: true, message: '请选择类型' }]"
        >
          <a-radio-group v-model:value="rewardPunishmentAddForm.type">
            <a-radio :value="1">奖励</a-radio>
            <a-radio :value="2">惩罚</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          label="发生日期"
          name="date"
          :rules="[{ required: true, message: '请选择发生日期' }]"
        >
          <a-input v-model:value="rewardPunishmentAddForm.date" type="date" />
        </a-form-item>
        <a-form-item
          label="描述"
          name="description"
        >
          <a-textarea
            v-model:value="rewardPunishmentAddForm.description"
            :rows="4"
            placeholder="请输入描述"
          />
        </a-form-item>
        <a-form-item
          label="金额"
          name="amount"
          :rules="[{ required: true, message: '请输入金额' }]"
        >
          <a-input-number
            v-model:value="rewardPunishmentAddForm.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.employee-detail {
  padding: 20px;
}
</style>


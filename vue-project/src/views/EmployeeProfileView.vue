<script setup lang="ts">
import { onMounted, reactive, ref, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import * as companyController from '@/api/companyController'
import * as employeeController from '@/api/employeeController'
import * as employeeProfileController from '@/api/employeeProfileController'
import * as rewardPunishmentController from '@/api/rewardPunishmentController'
import * as profileAccessRequestController from '@/api/profileAccessRequestController'
import type { FormInstance } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()

// 获取员工ID
const employeeId = computed(() => {
  const id = route.params.employeeId
  if (typeof id === 'string') {
    return id
  }
  return String(id || '')
})

// 获取当前公司ID（从查询参数，用于筛选和菜单）
const currentCompanyId = computed(() => {
  const id = route.query.companyId
  if (typeof id === 'string') {
    return id
  }
  return undefined
})

// 辅助函数：获取公司ID（保持为字符串，避免精度丢失）
const getCompanyId = (): string | undefined => {
  return currentCompanyId.value
}

// 当前选中的档案详情（用于查看详细信息）
const currentProfileDetail = ref<API.EmployeeProfileVO | null>(null)
const profileDetailModalVisible = ref(false)

// 员工信息
const employeeInfo = ref<API.EmployeeVO | null>(null)
const employeeLoading = ref(false)

// 档案列表
const profileList = ref<API.EmployeeProfileVO[]>([])
const profileLoading = ref(false)

// 所有档案（未筛选）
const allProfiles = ref<API.EmployeeProfileVO[]>([])

// 按公司分组的档案
interface GroupedProfile {
  companyId: string
  companyName: string
  profiles: API.EmployeeProfileVO[]
}

// 展开的行keys（用于控制哪些行展开）
const expandedRowKeys = ref<string[]>([])

const groupedProfiles = computed<GroupedProfile[]>(() => {
  const groups = new Map<string, GroupedProfile>()
  
  profileList.value.forEach((profile) => {
    const companyId = profile.companyId
    if (!companyId) return
    
    const companyIdStr = String(companyId)
    
    if (!groups.has(companyIdStr)) {
      groups.set(companyIdStr, {
        companyId: companyIdStr,
        companyName: profile.companyName || `公司${companyId}`,
        profiles: [],
      })
    }
    
    groups.get(companyIdStr)!.profiles.push(profile)
  })
  
  // 转换为数组并按公司名称排序
  return Array.from(groups.values()).sort((a, b) => {
    return a.companyName.localeCompare(b.companyName)
  })
})

// 奖惩记录相关
const rewardPunishmentModalVisible = ref(false)
const currentRewardCompanyId = ref<string | undefined>(undefined)
const currentRewardCompanyName = ref<string>('')
const rewardPunishmentList = ref<API.RewardPunishmentVO[]>([])
const rewardPunishmentLoading = ref(false)

// 请求授权相关
const requestAccessModalVisible = ref(false)
const requestAccessFormRef = ref<FormInstance>()
const currentRequestProfile = ref<API.EmployeeProfileVO | null>(null)
const requestAccessForm = reactive({
  requestReason: '',
  expireDays: 7 as number,
})
const requestAccessLoading = ref(false)

// 过期时间选项
const expireDaysOptions = [
  { label: '7天', value: 7 },
  { label: '30天', value: 30 },
  { label: '3个月', value: 90 },
]

// 筛选和搜索
const showCurrentCompanyOnly = ref(false)
const companySearchText = ref('')
const selectedCompanyId = ref<string | undefined>(undefined)
const companyOptions = ref<{ label: string; value: string; companyId: string }[]>([])
const filteredCompanyOptions = computed(() => {
  if (!companySearchText.value) {
    return companyOptions.value
  }
  const searchLower = companySearchText.value.toLowerCase()
  return companyOptions.value.filter(
    (option) => option.label.toLowerCase().includes(searchLower),
  )
})

// 排序
const sortOrder = ref<'ascend' | 'descend'>('descend')
const sortField = ref<'startDate' | 'createTime' | 'annualSalary' | 'attendanceRate'>('startDate')

// 计算年龄（从身份证号）
const calculateAge = (idCardNumber?: string): number | null => {
  if (!idCardNumber || idCardNumber.length < 14) {
    return null
  }
  try {
    // 身份证号格式：前6位地区码，7-10位年份，11-12位月份，13-14位日期
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
    return age
  } catch (error) {
    console.error('Failed to calculate age:', error)
    return null
  }
}

// 获取员工信息
const fetchEmployeeInfo = async () => {
  try {
    employeeLoading.value = true
    const id = employeeId.value
    if (!id) {
      message.error('无效的员工ID')
      return
    }
    
    const result = await employeeController.getEmployeeVoById({ id: id as any })
    if (result?.data?.code === 0 && result.data.data) {
      employeeInfo.value = result.data.data
    } else {
      const errorMsg = result?.data?.message || '请求数据不存在'
      message.error(errorMsg)
    }
  } catch (error: any) {
    console.error('Failed to fetch employee info:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '获取员工信息失败'
    message.error(errorMsg)
  } finally {
    employeeLoading.value = false
  }
}

// 获取所有档案
const fetchAllProfiles = async () => {
  try {
    profileLoading.value = true
    const id = employeeId.value
    if (!id) {
      return
    }
    
    // 获取所有档案（不分页）
    // 后端已配置Long类型序列化为字符串，Spring会自动将字符串转换为long
    const result = await employeeProfileController.listEmployeeProfileVoByPage({
      pageNum: 1,
      pageSize: 10000, // 获取所有档案
      employeeId: id as any, // 直接传递字符串，避免精度丢失
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      allProfiles.value = result.data.data.records || []
      
      // 自动展开所有无法查看的档案行
      expandedRowKeys.value = allProfiles.value
        .filter(p => !canViewDetail(p))
        .map(p => String(p.id || ''))
        .filter(id => id !== '')
      
      // 提取公司列表
      const companyMap = new Map<string, { label: string; companyId: string }>()
      allProfiles.value.forEach((profile) => {
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
      companyOptions.value = Array.from(companyMap.values()).map((item) => ({
        label: item.label,
        value: item.companyId,
        companyId: item.companyId,
      }))
      
      applyFilters()
    }
  } catch (error: any) {
    console.error('Failed to fetch profiles:', error)
    message.error('获取档案列表失败')
  } finally {
    profileLoading.value = false
  }
}

// 应用筛选和排序
const applyFilters = () => {
  let filtered = [...allProfiles.value]
  
  // 筛选：仅查看本公司档案
  if (showCurrentCompanyOnly.value && currentCompanyId.value) {
    filtered = filtered.filter(
      (profile) => String(profile.companyId) === currentCompanyId.value,
    )
  }
  
  // 筛选：按公司搜索
  if (selectedCompanyId.value) {
    filtered = filtered.filter(
      (profile) => String(profile.companyId) === selectedCompanyId.value,
    )
  } else if (companySearchText.value) {
    // 模糊匹配
    const searchLower = companySearchText.value.toLowerCase()
    filtered = filtered.filter(
      (profile) => profile.companyName?.toLowerCase().includes(searchLower),
    )
  }
  
  // 排序
  filtered.sort((a, b) => {
    let aValue: any
    let bValue: any
    
    if (sortField.value === 'startDate') {
      aValue = a.startDate ? new Date(a.startDate).getTime() : 0
      bValue = b.startDate ? new Date(b.startDate).getTime() : 0
    } else if (sortField.value === 'createTime') {
      aValue = a.createTime ? new Date(a.createTime).getTime() : 0
      bValue = b.createTime ? new Date(b.createTime).getTime() : 0
    } else if (sortField.value === 'annualSalary') {
      aValue = a.annualSalary ? Number(a.annualSalary) : 0
      bValue = b.annualSalary ? Number(b.annualSalary) : 0
    } else if (sortField.value === 'attendanceRate') {
      aValue = a.attendanceRate ? Number(a.attendanceRate) : 0
      bValue = b.attendanceRate ? Number(b.attendanceRate) : 0
    } else {
      aValue = 0
      bValue = 0
    }
    
    if (sortOrder.value === 'ascend') {
      return aValue - bValue
    } else {
      return bValue - aValue
    }
  })
  
  profileList.value = filtered
}

// 切换筛选
const handleToggleFilter = () => {
  showCurrentCompanyOnly.value = !showCurrentCompanyOnly.value
  applyFilters()
}

// 公司搜索变化
const handleCompanySearchChange = (value: string | undefined) => {
  selectedCompanyId.value = value
  // 清除搜索文本，因为已经选择了
  if (value) {
    companySearchText.value = ''
  }
  applyFilters()
}

// 公司搜索文本变化
const handleCompanySearch = (value: string) => {
  companySearchText.value = value
  // 如果搜索文本为空，清除选中
  if (!value) {
    selectedCompanyId.value = undefined
  }
  applyFilters()
}

// 排序变化
const handleSortChange = (field: 'startDate' | 'createTime' | 'annualSalary' | 'attendanceRate', order: 'ascend' | 'descend') => {
  sortField.value = field
  sortOrder.value = order
  applyFilters()
}

// 查看档案详细信息
const viewProfileDetail = (profile: API.EmployeeProfileVO) => {
  // 检查是否可以查看详情
  if (profile.canViewDetail === false) {
    message.warning('您没有权限查看此档案的详细信息')
    return
  }
  currentProfileDetail.value = profile
  profileDetailModalVisible.value = true
}

// 判断是否可以查看档案详情
const canViewDetail = (profile: API.EmployeeProfileVO): boolean => {
  return profile.canViewDetail !== false
}

// 判断是否需要显示请求授权按钮
const shouldShowRequestAccess = (profile: API.EmployeeProfileVO): boolean => {
  // 如果已经可以查看，不需要请求授权
  if (profile.canViewDetail === true) {
    return false
  }
  // 只有"对认证企业可见"的档案才需要请求授权
  return profile.visibility === 1
}

// 获取权限提示文本
const getPermissionTip = (profile: API.EmployeeProfileVO): string => {
  if (profile.visibility === 0) {
    return '此档案已设置为完全保密，不可查看'
  }
  if (profile.visibility === 1 && profile.canViewDetail === false) {
    return '此档案需要授权后才能查看，请点击"请求授权"按钮'
  }
  return ''
}

// 打开请求授权弹窗
const openRequestAccessModal = (profile: API.EmployeeProfileVO) => {
  currentRequestProfile.value = profile
  requestAccessForm.requestReason = ''
  requestAccessForm.expireDays = 7
  requestAccessModalVisible.value = true
}

// 提交授权请求
const submitAccessRequest = async () => {
  if (!currentRequestProfile.value || !employeeId.value) {
    return
  }

  try {
    await requestAccessFormRef.value?.validate()
    requestAccessLoading.value = true

    // 计算过期时间
    const expireDate = new Date()
    expireDate.setDate(expireDate.getDate() + requestAccessForm.expireDays)
    const expireTime = expireDate.toISOString().replace('T', ' ').substring(0, 19)

    const result = await profileAccessRequestController.createProfileAccessRequest({
      employeeId: employeeId.value,
      employeeProfileId: currentRequestProfile.value.id,
      requestReason: requestAccessForm.requestReason,
      expireTime: expireTime,
    })

    if (result?.data?.code === 0) {
      message.success('授权请求已提交，等待员工审批')
      requestAccessModalVisible.value = false
      // 刷新档案列表
      await fetchProfileList()
    } else {
      message.error(result?.data?.message || '提交失败')
    }
  } catch (error: any) {
    console.error('Failed to submit access request:', error)
    if (error?.errorFields) {
      // 表单验证错误
      return
    }
    message.error(error?.response?.data?.message || '提交失败')
  } finally {
    requestAccessLoading.value = false
  }
}

// 查看奖惩记录
const viewRewardPunishment = async (companyId: string, companyName: string) => {
  try {
    currentRewardCompanyId.value = companyId
    currentRewardCompanyName.value = companyName
    rewardPunishmentModalVisible.value = true
    rewardPunishmentLoading.value = true
    
    const result = await rewardPunishmentController.listRewardPunishmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      employeeId: employeeId.value as any,
      companyId: companyId as any,
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
    rewardPunishmentLoading.value = false
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

// 返回上一级（返回到公司员工详情界面）
const handleGoBack = () => {
  const companyId = getCompanyId()
  if (companyId) {
    // 返回到公司的员工详情界面
    router.push({ path: `/companies/${companyId}`, query: { menu: 'employees' } })
  } else {
    router.back()
  }
}

// 初始化
onMounted(async () => {
  await fetchEmployeeInfo()
  await fetchAllProfiles()
})
</script>

<template>
  <div class="employee-profile-view">
    <a-layout style="min-height: calc(100vh - 200px)">
      <!-- 右侧内容 -->
      <a-layout-content style="padding: 24px; background: #fff">
        <!-- 返回按钮 -->
        <div style="margin-bottom: 16px">
          <a-button type="text" @click="handleGoBack">
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            返回
          </a-button>
        </div>

        <!-- 员工基本信息 -->
        <a-card :loading="employeeLoading" style="margin-bottom: 24px">
          <template #title>员工基本信息</template>
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :sm="8" :md="6">
              <div style="text-align: center">
                <img
                  v-if="employeeInfo?.photoUrl"
                  :src="employeeInfo.photoUrl"
                  :alt="employeeInfo.name"
                  style="width: 180px; height: 240px; object-fit: cover; border-radius: 4px; margin-bottom: 8px;"
                />
                <div
                  v-else
                  style="width: 180px; height: 240px; background: #f0f0f0; border-radius: 4px; margin: 0 auto 8px; display: flex; align-items: center; justify-content: center; font-size: 72px; color: #999;"
                >
                  {{ employeeInfo?.name?.charAt(0) || '?' }}
                </div>
              </div>
            </a-col>
            <a-col :xs="24" :sm="16" :md="18">
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="姓名">
                  {{ employeeInfo?.name || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="身份证号">
                  {{ employeeInfo?.idCardNumber || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="年龄">
                  {{ calculateAge(employeeInfo?.idCardNumber) || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="性别">
                  {{ employeeInfo?.gender || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="电话">
                  {{ employeeInfo?.phone || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="邮箱">
                  {{ employeeInfo?.email || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="当前公司">
                  {{ employeeInfo?.companyName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="当前部门">
                  {{ employeeInfo?.departmentName || '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-col>
          </a-row>
        </a-card>

        <!-- 档案列表 -->
        <a-card>
          <template #title>员工档案</template>
          
          <!-- 筛选和搜索 -->
          <div style="margin-bottom: 16px">
            <a-row :gutter="[16, 16]">
              <a-col :xs="24" :sm="12" :md="8">
                <a-checkbox :checked="showCurrentCompanyOnly" @change="handleToggleFilter">
                  仅查看本公司档案
                </a-checkbox>
              </a-col>
              <a-col :xs="24" :sm="12" :md="8">
                <a-select
                  v-model:value="selectedCompanyId"
                  placeholder="搜索或选择公司"
                  show-search
                  allow-clear
                  style="width: 100%"
                  :options="filteredCompanyOptions"
                  :filter-option="false"
                  @change="handleCompanySearchChange"
                  @search="handleCompanySearch"
                />
              </a-col>
              <a-col :xs="24" :sm="24" :md="8">
                <a-space>
                  <span>排序：</span>
                  <a-radio-group
                    :value="sortField"
                    @change="(e: any) => handleSortChange(e.target.value, sortOrder)"
                  >
                    <a-radio-button value="startDate">入职日期</a-radio-button>
                    <a-radio-button value="createTime">创建时间</a-radio-button>
                    <a-radio-button value="annualSalary">年薪</a-radio-button>
                    <a-radio-button value="attendanceRate">出勤率</a-radio-button>
                  </a-radio-group>
                  <a-button
                    :type="sortOrder === 'descend' ? 'primary' : 'default'"
                    @click="handleSortChange(sortField, sortOrder === 'descend' ? 'ascend' : 'descend')"
                  >
                    {{ sortOrder === 'descend' ? '降序' : '升序' }}
                  </a-button>
                </a-space>
              </a-col>
            </a-row>
          </div>

          <!-- 按公司分组展示档案 -->
          <div v-if="!profileLoading && groupedProfiles.length > 0">
            <div v-for="group in groupedProfiles" :key="group.companyId" style="margin-bottom: 32px">
              <!-- 公司标题 -->
              <h3 style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #262626">
                {{ group.companyName }}
              </h3>
              
              <!-- 该公司的档案表格 -->
              <a-table
                :columns="[
                  { title: '职位', dataIndex: 'occupation', width: 150 },
                  { title: '入职日期', dataIndex: 'startDate', width: 120 },
                  { title: '离职日期', dataIndex: 'endDate', width: 120 },
                  { title: '年薪', dataIndex: 'annualSalary', width: 120 },
                  { title: '出勤率', dataIndex: 'attendanceRate', width: 100 },
                  { title: '重大事故', dataIndex: 'hasMajorIncident', width: 100 },
                  { title: '操作', key: 'operation', width: 150, align: 'center' },
                ]"
                :data-source="group.profiles"
                :loading="profileLoading"
                :pagination="false"
                row-key="id"
                size="middle"
                :bordered="true"
                :row-expandable="(record: API.EmployeeProfileVO) => !canViewDetail(record)"
                v-model:expandedRowKeys="expandedRowKeys"
                @expand="(expanded: boolean, record: API.EmployeeProfileVO) => {
                  if (!expanded && !canViewDetail(record)) {
                    // 如果用户尝试收起无法查看的档案行，立即重新展开
                    nextTick(() => {
                      const key = String(record.id || '')
                      if (key && !expandedRowKeys.includes(key)) {
                        expandedRowKeys.push(key)
                      }
                    })
                  }
                }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'annualSalary'">
                    {{ record.annualSalary ? `${Number(record.annualSalary).toFixed(2)}万元` : '-' }}
                  </template>
                  <template v-else-if="column.dataIndex === 'attendanceRate'">
                    {{ record.attendanceRate ? `${Number(record.attendanceRate).toFixed(2)}%` : '-' }}
                  </template>
                  <template v-else-if="column.dataIndex === 'hasMajorIncident'">
                    {{ record.hasMajorIncident ? '是' : '否' }}
                  </template>
                  <template v-else-if="column.dataIndex === 'startDate' || column.dataIndex === 'endDate'">
                    {{ record[column.dataIndex] || '-' }}
                  </template>
                  <template v-else-if="column.key === 'operation'">
                    <div style="display: flex; gap: 8px; justify-content: center">
                      <a-button
                        v-if="canViewDetail(record)"
                        type="link"
                        size="small"
                        @click="viewProfileDetail(record)"
                      >
                        查看详情
                      </a-button>
                      <a-button
                        v-if="shouldShowRequestAccess(record)"
                        type="primary"
                        size="small"
                        @click="openRequestAccessModal(record)"
                      >
                        请求授权
                      </a-button>
                    </div>
                  </template>
                </template>
                <template #expandedRowRender="{ record }">
                  <a-alert
                    v-if="!canViewDetail(record)"
                    type="warning"
                    :message="getPermissionTip(record)"
                    show-icon
                    style="margin: 8px 0"
                    :closable="false"
                  />
                </template>
              </a-table>
              
              <!-- 查看奖惩记录按钮 -->
              <div style="margin-top: 16px; text-align: right">
                <a-button type="primary" @click="viewRewardPunishment(group.companyId, group.companyName)">
                  查看奖惩记录
                </a-button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <a-empty
            v-if="!profileLoading && groupedProfiles.length === 0"
            description="暂无档案记录"
            style="padding: 40px"
          />
        </a-card>
      </a-layout-content>
    </a-layout>

    <!-- 请求授权对话框 -->
    <a-modal
      v-model:open="requestAccessModalVisible"
      title="请求授权查看档案"
      :confirm-loading="requestAccessLoading"
      @ok="submitAccessRequest"
      @cancel="requestAccessModalVisible = false"
      width="600"
    >
      <a-form
        ref="requestAccessFormRef"
        :model="requestAccessForm"
        layout="vertical"
        :rules="{
          requestReason: [{ required: true, message: '请输入请求原因', trigger: 'blur' }],
          expireDays: [{ required: true, message: '请选择授权过期时间', trigger: 'change' }],
        }"
      >
        <a-form-item label="档案信息">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="公司">
              {{ currentRequestProfile?.companyName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="职位">
              {{ currentRequestProfile?.occupation || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="入职日期">
              {{ currentRequestProfile?.startDate || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-form-item>
        <a-form-item label="请求原因" name="requestReason">
          <a-textarea
            v-model:value="requestAccessForm.requestReason"
            placeholder="请输入请求查看此档案的原因"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
        <a-form-item label="授权过期时间" name="expireDays">
          <a-radio-group v-model:value="requestAccessForm.expireDays">
            <a-radio
              v-for="option in expireDaysOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 档案详细信息对话框 -->
    <a-modal
      v-model:open="profileDetailModalVisible"
      title="档案详细信息"
      :footer="null"
      width="800"
    >
      <a-descriptions v-if="currentProfileDetail" :column="2" bordered>
        <a-descriptions-item label="公司名称">
          {{ currentProfileDetail.companyName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="职位">
          {{ currentProfileDetail.occupation || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="入职日期">
          {{ currentProfileDetail.startDate || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="离职日期">
          {{ currentProfileDetail.endDate || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="年薪">
          {{ currentProfileDetail.annualSalary ? `${Number(currentProfileDetail.annualSalary).toFixed(2)}万元` : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="出勤率">
          {{ currentProfileDetail.attendanceRate ? `${Number(currentProfileDetail.attendanceRate).toFixed(2)}%` : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="重大事故">
          {{ currentProfileDetail.hasMajorIncident ? '是' : '否' }}
        </a-descriptions-item>
        <a-descriptions-item label="绩效总结" :span="2">
          {{ currentProfileDetail.performanceSummary || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="离职原因" :span="2">
          {{ currentProfileDetail.reasonForLeaving || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 奖惩记录对话框 -->
    <a-modal
      v-model:open="rewardPunishmentModalVisible"
      :title="`${currentRewardCompanyName} - 奖惩记录`"
      :footer="null"
      width="900"
    >
      <a-spin :spinning="rewardPunishmentLoading">
        <a-table
          :columns="[
            { title: '类型', dataIndex: 'type', width: 80, align: 'center' },
            { title: '发生日期', dataIndex: 'date', width: 120 },
            { title: '描述', dataIndex: 'description', width: 300 },
            { title: '金额', dataIndex: 'amount', width: 120, align: 'right' },
            { title: '操作人', dataIndex: 'operatorName', width: 120 },
            { title: '创建时间', dataIndex: 'createTime', width: 160 },
          ]"
          :data-source="rewardPunishmentList"
          :loading="rewardPunishmentLoading"
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
            <template v-else-if="column.dataIndex === 'createTime'">
              {{ record.createTime || '-' }}
            </template>
          </template>
        </a-table>
        
        <a-empty
          v-if="!rewardPunishmentLoading && rewardPunishmentList.length === 0"
          description="暂无奖惩记录"
          style="padding: 40px"
        />
      </a-spin>
    </a-modal>
  </div>
</template>

<style scoped>
.employee-profile-view {
  /* 与 CompanyDetailView 保持一致，移除 BasicLayout 的 padding */
  margin: -24px;
  min-height: calc(100vh - 200px);
}
</style>


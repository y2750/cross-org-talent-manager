<script setup lang="ts">
import { onMounted, ref, computed, reactive, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import * as employeeController from '@/api/employeeController'
import * as employeeProfileController from '@/api/employeeProfileController'
import * as profileAccessRequestController from '@/api/profileAccessRequestController'

// 当前员工信息
const myEmployeeInfo = ref<API.EmployeeVO | null>(null)
const employeeLoading = ref(false)

// 档案列表
const profileList = ref<API.EmployeeProfileVO[]>([])
const profileLoading = ref(false)

// 所有档案（未筛选）
const allProfiles = ref<API.EmployeeProfileVO[]>([])

// 筛选
const selectedCompanyId = ref<string | undefined>(undefined)
const companyOptions = ref<{ label: string; value: string }[]>([])

// 档案查阅请求相关
const activeTab = ref<'profile' | 'requests'>('profile')
const accessRequestList = ref<API.ProfileAccessRequestVO[]>([])
const accessRequestLoading = ref(false)
const accessRequestPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
// 用于闪烁效果的档案ID
const highlightProfileId = ref<number | string | null>(null)

// 按公司分组的档案列表
interface GroupedProfile {
  companyId: string // 使用字符串类型，避免精度丢失
  companyName: string
  profiles: API.EmployeeProfileVO[]
}

// 有奖惩记录的公司列表（即使没有档案）- 从档案数据中提取
// 包括虚拟档案（id为null，表示只有奖惩记录没有档案）
const rewardPunishmentCompanies = computed<Array<{ companyId: string; companyName: string }>>(() => {
  try {
    const companyMap = new Map<string, { companyId: string; companyName: string }>()
    
    // 从档案数据中提取有奖惩记录的公司（包括虚拟档案）
    const profiles = allProfiles.value || []
    profiles.forEach((profile) => {
      if (profile.companyId && profile.rewardPunishments && profile.rewardPunishments.length > 0) {
        const companyIdStr = String(profile.companyId)
        if (!companyMap.has(companyIdStr)) {
          companyMap.set(companyIdStr, {
            companyId: companyIdStr,
            companyName: profile.companyName || `公司${companyIdStr}`,
          })
        }
      }
    })
    
    return Array.from(companyMap.values())
  } catch (error) {
    console.error('Error in rewardPunishmentCompanies computed:', error)
    return []
  }
})

const groupedProfiles = computed<GroupedProfile[]>(() => {
  try {
    const groups = new Map<string, GroupedProfile>()
    
    let filtered = allProfiles.value || []
    if (selectedCompanyId.value) {
      filtered = filtered.filter(
        (profile) => profile.companyId && String(profile.companyId) === selectedCompanyId.value
      )
    }
    
    filtered.forEach((profile) => {
      const companyId = profile.companyId
      if (!companyId) return
      
      // 跳过虚拟档案（id为null），它们会在 rewardPunishmentCompanies 中显示
      // 虚拟档案只用于存储奖惩记录，不应该显示在档案列表中
      if (profile.id === null || profile.id === undefined) {
        return
      }
      
      const companyIdKey = String(companyId)
      
      if (!groups.has(companyIdKey)) {
        groups.set(companyIdKey, {
          companyId: companyIdKey, // 保持字符串类型
          companyName: profile.companyName || `公司${companyId}`,
          profiles: [],
        })
      }
      
      groups.get(companyIdKey)!.profiles.push(profile)
    })
    
    // 获取当前员工的当前公司ID
    const currentCompanyId = myEmployeeInfo.value?.companyId
    const currentCompanyIdStr = currentCompanyId ? String(currentCompanyId) : undefined
    
    // 排序：当前公司优先，然后按公司名排序
    return Array.from(groups.values()).sort((a, b) => {
      if (currentCompanyIdStr) {
        const aIsCurrent = a.companyId === currentCompanyIdStr
        const bIsCurrent = b.companyId === currentCompanyIdStr
        if (aIsCurrent && !bIsCurrent) return -1
        if (!aIsCurrent && bIsCurrent) return 1
      }
      // 按公司名排序，如果公司名相同则按ID排序（字符串比较）
      if (a.companyName !== b.companyName) {
        return a.companyName.localeCompare(b.companyName)
      }
      return a.companyId.localeCompare(b.companyId)
    })
  } catch (error) {
    console.error('Error in groupedProfiles computed:', error)
    return []
  }
})

// 当前选中的档案详情
const currentProfileDetail = ref<API.EmployeeProfileVO | null>(null)
const profileDetailModalVisible = ref(false)

// 奖惩记录相关
const rewardPunishmentModalVisible = ref(false)
const currentRewardCompanyId = ref<string | undefined>(undefined)
const currentRewardCompanyName = ref<string>('')
const rewardPunishmentList = ref<API.RewardPunishmentVO[]>([])
// 不再需要 loading 状态，因为数据已经在档案中

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
    employeeLoading.value = true
    const result = await employeeController.getMyEmployeeVo()
    if (result?.data?.code === 0 && result.data.data) {
      myEmployeeInfo.value = result.data.data
      // 获取档案列表
      if (myEmployeeInfo.value.id) {
        await fetchProfileList()
      }
    } else {
      message.error('获取员工信息失败')
    }
  } catch (error) {
    console.error('Failed to fetch employee info:', error)
    message.error('获取员工信息失败')
  } finally {
    employeeLoading.value = false
  }
}

// 获取档案列表
const fetchProfileList = async () => {
  if (!myEmployeeInfo.value?.id) {
    return
  }
  try {
    profileLoading.value = true
    const result = await employeeProfileController.listMyEmployeeProfileVoByPage({
      pageNum: 1,
      pageSize: 1000,
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      console.log('获取到的档案数据:', records)
      allProfiles.value = records
      
      // 更新公司筛选选项
      updateCompanyFilterOptions()
    } else {
      console.error('获取档案列表失败:', result?.data)
      message.error(result?.data?.message || '获取档案列表失败')
    }
  } catch (error) {
    console.error('Failed to fetch profile list:', error)
    message.error('获取档案列表失败')
  } finally {
    profileLoading.value = false
  }
}

// 查看档案详情
const openProfileDetail = (profile: API.EmployeeProfileVO) => {
  currentProfileDetail.value = profile
  profileDetailModalVisible.value = true
}

// 更新档案公开范围
const updateProfileVisibility = async (profileId: number | string | undefined, visibility: number) => {
  if (!profileId) {
    message.error('档案ID不能为空')
    return
  }
  try {
    // 使用字符串ID，避免JavaScript精度丢失，后端JsonConfig会自动转换为Long
    const result = await employeeProfileController.updateMyProfileVisibility({
      id: String(profileId),
      visibility: visibility,
    } as any)
    if (result?.data?.code === 0) {
      message.success('公开范围更新成功')
      // 刷新档案列表
      await fetchProfileList()
      // 如果当前查看的档案详情，也更新一下
      if (currentProfileDetail.value && currentProfileDetail.value.id === profileId) {
        currentProfileDetail.value.visibility = visibility
      }
    } else {
      message.error(result?.data?.message || '更新失败')
    }
  } catch (error: any) {
    console.error('Failed to update profile visibility:', error)
    message.error(error?.response?.data?.message || '更新失败')
  }
}

// 获取公开范围文本
const getVisibilityText = (visibility: number | undefined): string => {
  if (visibility === 0) return '完全保密'
  if (visibility === 1) return '对认证企业可见'
  if (visibility === 2) return '公开'
  return '未设置'
}

// 获取公开范围选项
const visibilityOptions = [
  { label: '完全保密', value: 0 },
  { label: '对认证企业可见', value: 1 },
  { label: '公开', value: 2 },
]

// 查看奖惩记录（弹窗模式）- 从档案数据中获取，不调用接口
const viewRewardPunishment = (companyId: string | number, companyName: string) => {
  // 统一转换为字符串，避免精度丢失
  const companyIdStr = String(companyId)
  currentRewardCompanyId.value = companyIdStr
  currentRewardCompanyName.value = companyName
  rewardPunishmentModalVisible.value = true
  
  // 从档案数据中查找该公司的奖惩记录
  const companyProfiles = allProfiles.value.filter(
    (profile) => profile.companyId && String(profile.companyId) === companyIdStr
  )
  
  // 从第一个档案中获取奖惩记录（后端已经按公司分组填充）
  if (companyProfiles.length > 0 && companyProfiles[0].rewardPunishments) {
    rewardPunishmentList.value = companyProfiles[0].rewardPunishments || []
  } else {
    rewardPunishmentList.value = []
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

// 从档案数据中提取有奖惩记录但没有档案的公司
const extractRewardPunishmentOnlyCompanies = () => {
  // 这个方法现在不需要了，因为 rewardPunishmentCompanies 已经改为计算属性
  // 从档案数据中自动提取有奖惩记录的公司
  // 但是，如果某个公司只有奖惩记录但没有档案，我们需要单独处理
  // 由于后端只返回有档案的公司的奖惩记录，所以这里暂时不需要额外处理
}

// 更新公司筛选选项（包含档案和奖惩记录的公司）
const updateCompanyFilterOptions = () => {
  const companyMap = new Map<string, { label: string; companyId: string }>()
  
  // 从档案列表中提取公司
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
  
  // 从奖惩记录公司列表中提取公司（现在是计算属性）
  rewardPunishmentCompanies.value.forEach((company) => {
    if (!companyMap.has(company.companyId)) {
      companyMap.set(company.companyId, {
        label: company.companyName,
        companyId: company.companyId,
      })
    }
  })
  
  companyOptions.value = Array.from(companyMap.values()).map((item) => ({
    label: item.label,
    value: item.companyId,
  }))
}

// 获取档案查阅请求列表
const fetchAccessRequests = async (page: number = 1) => {
  try {
    accessRequestLoading.value = true
    const result = await profileAccessRequestController.listProfileAccessRequestByPage({
      pageNum: page,
      pageSize: accessRequestPagination.pageSize,
      status: undefined, // 查询所有状态的请求
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      accessRequestList.value = result.data.data.records || []
      accessRequestPagination.total = Number(result.data.data.totalRow) || 0
      accessRequestPagination.current = page
    } else {
      message.error(result?.data?.message || '获取请求列表失败')
    }
  } catch (error: any) {
    console.error('Failed to fetch access requests:', error)
    message.error(error?.response?.data?.message || '获取请求列表失败')
  } finally {
    accessRequestLoading.value = false
  }
}

// 处理请求（批准或拒绝）
const handleRequest = async (request: API.ProfileAccessRequestVO, approved: boolean) => {
  if (!request.id) {
    message.error('请求ID不能为空')
    return
  }
  
  try {
    // 如果批准，需要传递过期时间（使用请求中的过期时间）
    const expireTime = approved && request.expireTime ? request.expireTime : undefined
    
    const result = await profileAccessRequestController.approveProfileAccessRequest({
      id: request.id,
      status: approved ? 1 : 2, // 1=已授权，2=已拒绝
      expireTime: expireTime,
    } as any)
    
    if (result?.data?.code === 0) {
      message.success(approved ? '已批准请求' : '已拒绝请求')
      // 刷新请求列表
      await fetchAccessRequests(accessRequestPagination.current)
    } else {
      message.error(result?.data?.message || '操作失败')
    }
  } catch (error: any) {
    console.error('Failed to handle request:', error)
    message.error(error?.response?.data?.message || '操作失败')
  }
}

// 获取状态文本
const getStatusText = (status: number | undefined): string => {
  if (status === 0) return '待处理'
  if (status === 1) return '已授权'
  if (status === 2) return '已拒绝'
  if (status === 3) return '已过期'
  return '未知'
}

// 获取状态颜色
const getStatusColor = (status: number | undefined): string => {
  if (status === 0) return 'orange'
  if (status === 1) return 'green'
  if (status === 2) return 'red'
  if (status === 3) return 'default'
  return 'default'
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined): string => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch (error) {
    return dateTime
  }
}

// 切换标签页
const handleTabChange = (key: string) => {
  activeTab.value = key as 'profile' | 'requests'
  if (key === 'requests') {
    fetchAccessRequests(1)
  }
}

// 获取表格行的类名（用于高亮）
const getRowClassName = (record: API.EmployeeProfileVO | null | undefined): string => {
  try {
    if (!record || !record.id) {
      return ''
    }
    const currentHighlightId = highlightProfileId.value
    if (!currentHighlightId) {
      return ''
    }
    const recordIdStr = String(record.id)
    const highlightIdStr = String(currentHighlightId)
    if (recordIdStr === highlightIdStr) {
      return 'highlight-profile-row'
    }
    return ''
  } catch (error) {
    console.error('Error in getRowClassName:', error)
    return ''
  }
}

// 根据档案ID获取档案信息
const getProfileById = (profileId: number | string | undefined): API.EmployeeProfileVO | null => {
  if (!profileId) return null
  return allProfiles.value.find(p => p.id && String(p.id) === String(profileId)) || null
}

// 格式化档案显示文本
const formatProfileDisplay = (request: API.ProfileAccessRequestVO): string => {
  if (!request.employeeProfileId) {
    return '所有档案'
  }
  
  const profile = getProfileById(request.employeeProfileId)
  if (!profile) {
    return `档案ID: ${request.employeeProfileId}`
  }
  
  const companyName = profile.companyName || `公司${profile.companyId || ''}`
  const startDate = profile.startDate || ''
  const endDate = profile.endDate || ''
  
  if (endDate) {
    return `${companyName}：${startDate}-${endDate}`
  } else {
    return `${companyName}：${startDate}`
  }
}

// 跳转到档案并高亮
const jumpToProfile = async (request: API.ProfileAccessRequestVO) => {
  if (!request.employeeProfileId) {
    message.info('该请求针对所有档案')
    return
  }
  
  // 先清除之前的高亮
  highlightProfileId.value = null
  await nextTick()
  
  // 切换到档案列表标签页
  activeTab.value = 'profile'
  
  // 等待标签页切换完成和表格渲染
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 300))
  
  // 先清除，再设置，确保动画能重新触发
  highlightProfileId.value = null
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 50))
  
  // 设置高亮的档案ID
  highlightProfileId.value = request.employeeProfileId
  
  // 等待DOM更新，确保类名已应用
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 100))
  
  // 滚动到对应位置
  const profileId = String(request.employeeProfileId)
  const tableRows = document.querySelectorAll('.ant-table-tbody tr[data-row-key]')
  tableRows.forEach((row) => {
    const rowKey = row.getAttribute('data-row-key')
    if (rowKey === profileId) {
      row.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
  
  // 2秒后清除高亮
  setTimeout(() => {
    highlightProfileId.value = null
  }, 2000)
}

onMounted(async () => {
  await fetchMyEmployeeInfo()
  // 不再需要单独调用 fetchRewardPunishmentCompanies，因为奖惩记录已经在档案数据中
})
</script>

<template>
  <div class="my-profile-view">
    <a-spin :spinning="employeeLoading">
      <a-card>
        <template #title>
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="profile-tabs">
            <a-tab-pane key="profile" tab="我的档案" />
            <a-tab-pane key="requests" tab="档案查阅请求" />
          </a-tabs>
        </template>

        <!-- 档案列表 -->
        <div v-if="activeTab === 'profile'">
          <!-- 员工基本信息 -->
          <div style="margin-bottom: 24px">
            <h3>基本信息</h3>
            <div style="display: flex; gap: 24px; margin-bottom: 24px" v-if="myEmployeeInfo">
              <!-- 左侧照片 -->
              <div style="flex-shrink: 0">
                <a-avatar
                  v-if="myEmployeeInfo.photoUrl"
                  :src="myEmployeeInfo.photoUrl"
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
              
              <!-- 右侧基本信息 -->
              <div style="flex: 1">
                <a-descriptions bordered :column="2">
                  <a-descriptions-item label="姓名">{{ myEmployeeInfo.name || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="性别">{{ myEmployeeInfo.gender || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="年龄">
                    {{ employeeAge !== null ? `${employeeAge}岁` : '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="身份证号">{{ myEmployeeInfo.idCardNumber || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="电话">{{ myEmployeeInfo.phone || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="邮箱">{{ myEmployeeInfo.email || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="公司">{{ myEmployeeInfo.companyName || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="部门">{{ myEmployeeInfo.departmentName || '-' }}</a-descriptions-item>
                </a-descriptions>
              </div>
            </div>
            <a-empty v-else description="暂无员工信息" />
          </div>

          <a-divider />
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
            <h3>档案列表</h3>
            <a-select
              v-model:value="selectedCompanyId"
              placeholder="按公司筛选"
              allow-clear
              style="width: 200px"
              :options="companyOptions"
            />
          </div>
          
          <a-spin :spinning="profileLoading">
            <div v-if="groupedProfiles.length === 0 && rewardPunishmentCompanies.length === 0" style="text-align: center; padding: 40px; color: #8c8c8c">
              暂无档案信息
            </div>
            
            <div v-else-if="groupedProfiles.length > 0 || rewardPunishmentCompanies.length > 0">
              <!-- 显示有档案的公司 -->
              <div v-for="group in groupedProfiles" :key="group.companyId" style="margin-bottom: 32px">
                <!-- 公司标题 -->
                <h4 style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #262626">
                  {{ group.companyName }}
                  <a-tag 
                    v-if="myEmployeeInfo?.companyId && group.companyId === String(myEmployeeInfo.companyId)" 
                    color="orange" 
                    style="margin-left: 8px"
                  >
                    当前公司
                  </a-tag>
                </h4>
                
                <!-- 该公司的档案表格 -->
                <a-table
                  :columns="[
                    { title: '入职日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
                    { title: '离职日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
                    { title: '职位', dataIndex: 'occupation', key: 'occupation', width: 150 },
                    { title: '年薪(万元)', dataIndex: 'annualSalary', key: 'annualSalary', width: 120 },
                    { title: '出勤率(%)', dataIndex: 'attendanceRate', key: 'attendanceRate', width: 100 },
                    { title: '公开范围', key: 'visibility', width: 150 },
                    { title: '操作', key: 'operation', width: 150 },
                  ]"
                  :data-source="group.profiles"
                  :pagination="false"
                  size="middle"
                  :bordered="true"
                  row-key="id"
                  :row-class-name="getRowClassName"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'annualSalary'">
                      {{ record.annualSalary ? record.annualSalary.toFixed(2) : '-' }}
                    </template>
                    <template v-else-if="column.key === 'attendanceRate'">
                      {{ record.attendanceRate ? `${record.attendanceRate.toFixed(2)}%` : '-' }}
                    </template>
                    <template v-else-if="column.key === 'visibility'">
                      <a-select
                        :value="record.visibility !== undefined ? record.visibility : 2"
                        :options="visibilityOptions"
                        size="small"
                        style="width: 100%"
                        @change="(value: number) => updateProfileVisibility(record.id, value)"
                      />
                    </template>
                    <template v-else-if="column.key === 'operation'">
                      <a-button type="link" size="small" @click="openProfileDetail(record)">查看详情</a-button>
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
              
              <!-- 显示只有奖惩记录但没有档案的公司（虚拟档案，id为null） -->
              <template v-for="profile in allProfiles" :key="`virtual-${profile.companyId}`">
                <div 
                  v-if="(profile.id === null || profile.id === undefined) && profile.companyId && profile.rewardPunishments && profile.rewardPunishments.length > 0"
                  style="margin-bottom: 32px"
                >
                  <!-- 公司标题 -->
                  <h4 style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #262626">
                    {{ profile.companyName || `公司${profile.companyId}` }}
                    <a-tag 
                      v-if="myEmployeeInfo?.companyId && String(profile.companyId) === String(myEmployeeInfo.companyId)" 
                      color="orange" 
                      style="margin-left: 8px"
                    >
                      当前公司
                    </a-tag>
                  </h4>
                  
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
                    <a-button type="primary" @click="viewRewardPunishment(profile.companyId, profile.companyName || `公司${profile.companyId}`)">
                      查看奖惩记录
                    </a-button>
                  </div>
                </div>
              </template>
            </div>
          </a-spin>
        </div>

        <!-- 档案查阅请求 -->
        <div v-if="activeTab === 'requests'">
          <a-spin :spinning="accessRequestLoading">
            <a-table
              :columns="[
                { title: '请求公司', dataIndex: 'requestCompanyName', key: 'requestCompanyName', width: 150 },
                { title: '请求的档案', key: 'profileInfo', width: 200 },
                { title: '请求理由', dataIndex: 'requestReason', key: 'requestReason', width: 250 },
                { title: '请求时间', key: 'requestTime', width: 160 },
                { title: '授权过期时间', key: 'expireTime', width: 160 },
                { title: '状态', key: 'status', width: 100, align: 'center' },
                { title: '操作', key: 'operation', width: 150, align: 'center' },
              ]"
              :data-source="accessRequestList"
              :pagination="{
                current: accessRequestPagination.current,
                pageSize: accessRequestPagination.pageSize,
                total: accessRequestPagination.total,
                showSizeChanger: true,
                showTotal: (total) => `共 ${total} 条`,
              }"
              row-key="id"
              size="middle"
              :bordered="true"
              @change="(pagination) => {
                if (pagination.current) {
                  fetchAccessRequests(pagination.current)
                }
                if (pagination.pageSize) {
                  accessRequestPagination.pageSize = pagination.pageSize
                  fetchAccessRequests(1)
                }
              }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'profileInfo'">
                  <a
                    v-if="record.employeeProfileId"
                    href="javascript:void(0)"
                    style="color: #1890ff; cursor: pointer"
                    @click="jumpToProfile(record)"
                  >
                    {{ formatProfileDisplay(record) }}
                  </a>
                  <span v-else style="color: #8c8c8c">所有档案</span>
                </template>
                <template v-else-if="column.key === 'requestTime'">
                  {{ formatDateTime(record.requestTime) }}
                </template>
                <template v-else-if="column.key === 'expireTime'">
                  {{ formatDateTime(record.expireTime) }}
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="getStatusColor(record.status)">
                    {{ getStatusText(record.status) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'operation'">
                  <a-space v-if="record.status === 0">
                    <a-button type="primary" size="small" @click="handleRequest(record, true)">
                      批准
                    </a-button>
                    <a-button danger size="small" @click="handleRequest(record, false)">
                      拒绝
                    </a-button>
                  </a-space>
                  <span v-else style="color: #8c8c8c">-</span>
                </template>
              </template>
            </a-table>
            
            <a-empty
              v-if="!accessRequestLoading && accessRequestList.length === 0"
              description="暂无档案查阅请求"
              style="padding: 40px"
            />
          </a-spin>
        </div>
      </a-card>
    </a-spin>

    <!-- 档案详情弹窗 -->
    <a-modal
      v-model:open="profileDetailModalVisible"
      title="档案详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2" v-if="currentProfileDetail">
        <a-descriptions-item label="公司">{{ currentProfileDetail.companyName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="职位">{{ currentProfileDetail.occupation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="入职日期">{{ currentProfileDetail.startDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="离职日期">{{ currentProfileDetail.endDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="年薪">
          {{ currentProfileDetail.annualSalary ? currentProfileDetail.annualSalary.toFixed(2) : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="出勤率">
          {{ currentProfileDetail.attendanceRate ? `${currentProfileDetail.attendanceRate.toFixed(2)}%` : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="是否有重大事故">
          <a-tag :color="currentProfileDetail.hasMajorIncident ? 'red' : 'green'">
            {{ currentProfileDetail.hasMajorIncident ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="公开范围">
          <a-select
            :value="currentProfileDetail.visibility !== undefined ? currentProfileDetail.visibility : 2"
            :options="visibilityOptions"
            style="width: 200px"
            @change="(value: number) => updateProfileVisibility(currentProfileDetail?.id, value)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="离职原因" :span="2">
          {{ currentProfileDetail.reasonForLeaving || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="绩效总结" :span="2">
          {{ currentProfileDetail.performanceSummary || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 奖惩记录对话框（只读） -->
    <a-modal
      v-model:open="rewardPunishmentModalVisible"
      :title="`${currentRewardCompanyName} - 奖惩记录`"
      :footer="null"
      width="900"
    >
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
        v-if="rewardPunishmentList.length === 0"
        description="暂无奖惩记录"
        style="padding: 40px"
      />
    </a-modal>
  </div>
</template>

<style scoped>
.my-profile-view {
  padding: 0;
}

.my-profile-view h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.profile-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
}

/* 档案高亮闪烁动画 - 使用深度选择器穿透 scoped 样式 */
:deep(.highlight-profile-row) {
  animation: highlight-flash 2s ease-in-out !important;
  background-color: rgba(128, 128, 128, 0.3) !important;
}

:deep(.highlight-profile-row td) {
  background-color: inherit !important;
}

@keyframes highlight-flash {
  0% {
    background-color: rgba(128, 128, 128, 0.5) !important;
  }
  12.5% {
    background-color: rgba(255, 255, 255, 1) !important;
  }
  25% {
    background-color: rgba(128, 128, 128, 0.5) !important;
  }
  37.5% {
    background-color: rgba(255, 255, 255, 1) !important;
  }
  50% {
    background-color: rgba(128, 128, 128, 0.5) !important;
  }
  62.5% {
    background-color: rgba(255, 255, 255, 1) !important;
  }
  75% {
    background-color: rgba(128, 128, 128, 0.5) !important;
  }
  87.5% {
    background-color: rgba(255, 255, 255, 1) !important;
  }
  100% {
    background-color: transparent !important;
  }
}
</style>


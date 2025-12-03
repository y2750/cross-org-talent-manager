<script setup lang="ts">
import { onMounted, reactive, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import * as companyController from '@/api/companyController'
import * as userController from '@/api/userController'
import * as employeeController from '@/api/employeeController'
import * as departmentController from '@/api/departmentController'
import * as employeeProfileController from '@/api/employeeProfileController'
import IndustrySelector from '@/components/IndustrySelector.vue'
import type { FormInstance } from 'ant-design-vue'
import { useRole } from '@/composables/useRole'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const { isSystemAdmin, isCompanyAdmin, isHR } = useRole()
const userStore = useUserStore()

// 获取公司ID，后端已配置Long类型序列化为字符串，Spring会自动将字符串转换为long
const companyId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string' && id) {
    return id
  }
  // 如果是 company_admin 或 hr，且路由参数中没有公司ID，使用当前用户的 companyId
  if ((isCompanyAdmin() || isHR()) && userStore.userInfo?.companyId) {
    return String(userStore.userInfo.companyId)
  }
  return String(id || '')
})

// 辅助函数：获取公司ID（保持为字符串，避免精度丢失）
// 后端JsonConfig已配置Long序列化为字符串，Spring会自动将字符串反序列化为Long
const getCompanyId = (): string | undefined => {
  const id = companyId.value
  return id || undefined
}

// 当前选中的菜单项
const selectedMenu = ref('home')

// 公司信息
const companyInfo = ref<API.CompanyVO | null>(null)
const companyLoading = ref(false)

// 首页统计信息
const employeeCount = ref(0)
const departmentCount = ref(0)
const managerCount = ref(0)

// 员工列表
const employeeList = ref<API.EmployeeVO[]>([])
const employeeTotal = ref(0)
const employeeLoading = ref(false)
const employeeSearchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  departmentId: undefined as string | undefined,
  idCardNumber: '',
  sortField: 'create_time',
  sortOrder: 'descend',
  companyId: undefined as any,
})

// 部门选项（用于员工搜索）
const departmentOptionsForSearch = ref<{ label: string; value: string }[]>([])

// 部门列表
const departmentList = ref<API.DepartmentVO[]>([])
const departmentTotal = ref(0)
const departmentLoading = ref(false)
const departmentSearchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyId: undefined as any,
})

// 管理人列表
const managerList = ref<API.UserVO[]>([])
const managerLoading = ref(false)

// 积分相关（hr、company_admin和admin可见）
const canViewPoints = computed(() => isHR() || isCompanyAdmin() || isSystemAdmin())
const totalPoints = ref<number>(0)
const pointsLoading = ref(false)
const pointsHistory = ref<API.CompanyPointsVO[]>([])
const pointsHistoryLoading = ref(false)
const pointsHistoryPageNum = ref(1)
const pointsHistoryPageSize = ref(10)
const pointsHistoryTotal = ref(0)

// 表单引用
const companyFormRef = ref<FormInstance>()
const departmentFormRef = ref<FormInstance>()
const managerFormRef = ref<FormInstance>()

// 公司编辑表单
const companyFormState = reactive({
  name: '',
  contactPersonId: undefined as number | undefined,
  phone: '',
  email: '',
  industries: [] as string[],
})

// 部门编辑表单
const departmentFormState = reactive({
  id: undefined as string | undefined,
  name: '',
  leaderId: undefined as string | undefined,
})

// 是否为新增部门模式
const isAddDepartmentMode = computed(() => !departmentFormState.id)

// 管理人变更表单
const managerFormState = reactive({
  userId: undefined as number | undefined,
  action: 'assign' as 'assign' | 'remove',
})

// 对话框状态
const companyModalVisible = ref(false)
const departmentModalVisible = ref(false)
const managerModalVisible = ref(false)

// 选项数据
const managerOptions = ref<{ label: string; value: number }[]>([])
const contactPersonOptions = ref<{ label: string; value: number }[]>([])
const employeeOptions = ref<{ label: string; value: string }[]>([])
const departmentOptions = ref<{ label: string; value: number }[]>([])

// 部门详情相关
const departmentDetailModalVisible = ref(false)
const currentDepartmentDetail = ref<API.DepartmentVO | null>(null)
const departmentEmployeeList = ref<API.EmployeeVO[]>([])
const departmentEmployeeLoading = ref(false)

// 获取主管姓名（从员工列表中）
const getLeaderName = computed(() => {
  if (!currentDepartmentDetail.value) return '-'
  if (currentDepartmentDetail.value.leaderName) {
    return currentDepartmentDetail.value.leaderName
  }
  if (currentDepartmentDetail.value.leaderId) {
    const leader = departmentEmployeeList.value.find(
      (emp: API.EmployeeVO) => String(emp.id) === String(currentDepartmentDetail.value!.leaderId)
    )
    return leader?.name || '-'
  }
  return '-'
})

// 获取公司信息
const fetchCompanyInfo = async () => {
  try {
    companyLoading.value = true
    let id = companyId.value
    
    // 如果是 company_admin 或 hr，且路由参数中没有公司ID，使用当前用户的 companyId
    if ((isCompanyAdmin() || isHR()) && (!id || id === 'undefined')) {
      if (userStore.userInfo?.companyId) {
        id = String(userStore.userInfo.companyId)
        // 更新路由参数（不触发导航）
        router.replace({ params: { id } })
      } else {
        message.error('您没有关联的公司')
        router.push('/home')
        return
      }
    }
    
    if (!id || id === 'undefined') {
      message.error('无效的公司ID')
      return
    }
    
    // 权限检查：company_admin 和 hr 只能访问自己的公司
    if ((isCompanyAdmin() || isHR()) && userStore.userInfo?.companyId) {
      const userCompanyId = String(userStore.userInfo.companyId)
      if (id !== userCompanyId) {
        message.error('您只能访问自己公司的信息')
        router.push('/home')
        return
      }
    }
    
    // Spring会自动将字符串参数转换为long类型
    const result = await companyController.getCompanyVoById({ id: id as any })
    if (result?.data?.code === 0 && result.data.data) {
      companyInfo.value = result.data.data
      companyFormState.name = result.data.data.name || ''
      companyFormState.contactPersonId = result.data.data.contactPersonId
      companyFormState.phone = result.data.data.phone || ''
      companyFormState.email = result.data.data.email || ''
      companyFormState.industries = result.data.data.industries || []
      
      // 更新积分显示（如果可见）
      if (canViewPoints.value && result.data.data.totalPoints !== undefined) {
        totalPoints.value = Number(result.data.data.totalPoints) || 0
      }
    } else {
      const errorMsg = result?.data?.message || '请求数据不存在'
      message.error(errorMsg)
    }
  } catch (error: any) {
    console.error('Failed to fetch company info:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '获取公司信息失败'
    message.error(errorMsg)
  } finally {
    companyLoading.value = false
  }
}

// 获取首页统计信息
const fetchHomeStats = async () => {
  try {
    // 获取员工数量
    const employeeResult = await employeeController.listEmployeeVoByPage({
      pageNum: 1,
      pageSize: 1,
      companyId: getCompanyId(),
    } as any)
    if (employeeResult?.data?.code === 0 && employeeResult.data.data) {
      employeeCount.value = employeeResult.data.data.totalRow || 0
    }

    // 获取部门数量
    const departmentResult = await departmentController.listDepartmentVoByPage({
      pageNum: 1,
      pageSize: 1,
      companyId: getCompanyId(),
    } as any)
    if (departmentResult?.data?.code === 0 && departmentResult.data.data) {
      departmentCount.value = departmentResult.data.data.totalRow || 0
    }

    // 获取管理人数量：role为company_admin且companyId等于当前公司id
    // 只有系统管理员或公司管理员可以访问此接口
    if (isSystemAdmin() || isCompanyAdmin()) {
      try {
        const managerResult = await userController.listUserVoByPage({
          pageNum: 1,
          pageSize: 1,
          userRole: 'company_admin',
          companyId: getCompanyId(),
        } as any)
        if (managerResult?.data?.code === 0 && managerResult.data.data) {
          managerCount.value = managerResult.data.data.totalRow || 0
        }
      } catch (error) {
        console.error('Failed to fetch manager count:', error)
        // HR 角色无法访问此接口，静默失败
      }
    }
  } catch (error) {
    console.error('Failed to fetch home stats:', error)
  }
}

// 获取积分
const fetchPoints = async () => {
  if (!canViewPoints.value) {
    return
  }
  try {
    pointsLoading.value = true
    
    // 从companyInfo中获取积分（已在fetchCompanyInfo中加载）
    if (companyInfo.value && companyInfo.value.totalPoints !== undefined) {
      totalPoints.value = Number(companyInfo.value.totalPoints) || 0
    } else {
      // 如果companyInfo中没有，通过积分接口获取
      let targetCompanyId: number | undefined
      if (isSystemAdmin()) {
        // admin可以查看任意公司
        targetCompanyId = companyId.value ? Number(companyId.value) : undefined
      } else {
        // hr和company_admin查看自己的公司
        targetCompanyId = userStore.userInfo?.companyId ? Number(userStore.userInfo.companyId) : undefined
      }
      
      if (targetCompanyId) {
        const result = await companyController.getCompanyPoints({
          companyId: targetCompanyId,
        })
        if (result?.data?.code === 0 && result.data.data !== undefined) {
          totalPoints.value = Number(result.data.data) || 0
        }
      }
    }
  } catch (error: any) {
    console.error('Failed to fetch points:', error)
  } finally {
    pointsLoading.value = false
  }
}

// 获取积分变动记录
const fetchPointsHistory = async (page: number = 1) => {
  if (!canViewPoints.value) {
    return
  }
  
  try {
    pointsHistoryLoading.value = true
    
    // 确定要查询的公司ID
    let targetCompanyId: number | undefined
    if (isSystemAdmin()) {
      // admin可以查看任意公司，使用路由参数中的companyId
      targetCompanyId = companyId.value ? Number(companyId.value) : undefined
    } else {
      // hr和company_admin查看自己的公司
      targetCompanyId = userStore.userInfo?.companyId ? Number(userStore.userInfo.companyId) : undefined
    }
    
    if (!targetCompanyId) {
      pointsHistory.value = []
      pointsHistoryTotal.value = 0
      return
    }
    
    const result = await companyController.getCompanyPointsHistory({
      pageNum: page,
      pageSize: pointsHistoryPageSize.value,
      companyId: targetCompanyId,
    })
    
    if (result?.data?.code === 0 && result.data.data) {
      pointsHistory.value = result.data.data.records || []
      pointsHistoryTotal.value = result.data.data.totalRow || 0
      pointsHistoryPageNum.value = page
    } else {
      const errorMsg = result?.data?.message || '获取积分变动记录失败'
      message.error(errorMsg)
    }
  } catch (error: any) {
    console.error('Failed to fetch points history:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '获取积分变动记录失败'
    message.error(errorMsg)
  } finally {
    pointsHistoryLoading.value = false
  }
}

// 格式化积分显示
const formatPoints = (points: number | undefined) => {
  const pointsValue = Number(points || 0)
  const isPositive = pointsValue >= 0
  return {
    text: `${isPositive ? '+' : ''}${pointsValue.toFixed(2)}`,
    color: isPositive ? '#52c41a' : '#ff4d4f'
  }
}

// 获取员工列表
const fetchEmployeeList = async () => {
  try {
    employeeLoading.value = true
    const params: API.EmployeeQueryRequest = {
      pageNum: 1,
      pageSize: 1000, // 获取所有员工用于前端筛选
      companyId: getCompanyId(),
      sortField: employeeSearchParams.sortField,
      sortOrder: employeeSearchParams.sortOrder,
    } as any
    if (employeeSearchParams.name) params.name = employeeSearchParams.name
    if (employeeSearchParams.idCardNumber) params.idCardNumber = employeeSearchParams.idCardNumber
    if (employeeSearchParams.departmentId) params.departmentId = employeeSearchParams.departmentId as any

    const result = await employeeController.listEmployeeVoByPage(params)
    if (result?.data?.code === 0 && result.data.data) {
      let employees = result.data.data.records || []
      
      // 确保只显示当前公司的员工（按employee的companyId过滤）
      const currentCompanyId = getCompanyId()
      if (currentCompanyId) {
        employees = employees.filter(
          (emp: API.EmployeeVO) => String(emp.companyId) === currentCompanyId,
        )
      }
      
      // 分页处理
      const start = (employeeSearchParams.pageNum - 1) * employeeSearchParams.pageSize
      const end = start + employeeSearchParams.pageSize
      employeeList.value = employees.slice(start, end)
      employeeTotal.value = employees.length
    }
  } catch (error) {
    console.error('Failed to fetch employee list:', error)
    message.error('获取员工列表失败')
  } finally {
    employeeLoading.value = false
  }
}

// 获取部门列表
const fetchDepartmentList = async () => {
  try {
    departmentLoading.value = true
    const result = await departmentController.listDepartmentVoByPage({
      pageNum: departmentSearchParams.pageNum,
      pageSize: departmentSearchParams.pageSize,
      companyId: getCompanyId(),
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const departments = result.data.data.records || []
      // 为每个部门获取员工数量和主管姓名
      for (const dept of departments) {
        if (dept.id) {
          try {
            const empResult = await employeeController.listEmployeeVoByPage({
              pageNum: 1,
              pageSize: 1000,
              companyId: getCompanyId(),
              departmentId: dept.id as any,
            } as any)
            if (empResult?.data?.code === 0 && empResult.data.data) {
              // 确保只统计当前公司的员工
              const currentCompanyId = getCompanyId()
              const allEmployees = empResult.data.data.records || []
              const filtered = currentCompanyId
                ? allEmployees.filter((emp: API.EmployeeVO) => String(emp.companyId) === currentCompanyId)
                : allEmployees
              ;(dept as any).employeeCount = filtered.length
            } else {
              ;(dept as any).employeeCount = 0
            }
          } catch (error) {
            console.error('Failed to fetch department employee count:', error)
            ;(dept as any).employeeCount = 0
          }
        } else {
          ;(dept as any).employeeCount = 0
        }
        
        // 主管姓名已由后端批量查询并填充，无需前端再次查询
        // 如果后端返回的leaderName为空但leaderId存在，说明员工可能已被删除，保持undefined即可
      }
      departmentList.value = departments
      departmentTotal.value = result.data.data.totalRow || 0
      
      // 更新部门选项（用于员工搜索）
      departmentOptionsForSearch.value = departments.map((dept: API.DepartmentVO) => ({
        label: dept.name || '',
        value: dept.id ? String(dept.id) : '',
      }))
    }
  } catch (error) {
    console.error('Failed to fetch department list:', error)
    message.error('获取部门列表失败')
  } finally {
    departmentLoading.value = false
  }
}

// 获取管理人列表：role为company_admin且companyId等于当前公司id
// 只有系统管理员或公司管理员可以访问此接口
const fetchManagerList = async () => {
  // HR 角色无法访问此接口，直接返回
  if (!isSystemAdmin() && !isCompanyAdmin()) {
    managerList.value = []
    managerOptions.value = []
    return
  }
  
  try {
    managerLoading.value = true
    const result = await userController.listUserVoByPage({
      pageNum: 1,
      pageSize: 1000,
      userRole: 'company_admin',
      companyId: getCompanyId(),
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      managerList.value = records
      managerOptions.value = records.map((user: API.UserVO) => ({
        label: `${user.username} (${user.nickname})`,
        value: user.id as number,
      }))
    }
  } catch (error) {
    console.error('Failed to fetch manager list:', error)
    message.error('获取管理人列表失败')
  } finally {
    managerLoading.value = false
  }
}

// 获取联系人选项（本公司的管理人）：role为company_admin且companyId等于当前公司id
// 只有系统管理员或公司管理员可以访问此接口
const fetchContactPersonOptions = async () => {
  // HR 角色无法访问此接口，直接返回
  if (!isSystemAdmin() && !isCompanyAdmin()) {
    contactPersonOptions.value = []
    return
  }
  
  try {
    const result = await userController.listUserVoByPage({
      pageNum: 1,
      pageSize: 1000,
      userRole: 'company_admin',
      companyId: getCompanyId(),
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      contactPersonOptions.value = records.map((user: API.UserVO) => ({
        label: `${user.username} (${user.nickname})`,
        value: user.id as number,
      }))
    }
  } catch (error) {
    console.error('Failed to fetch contact person options:', error)
  }
}

// 获取可任命的管理人选项（只有company_admin角色）
// 只有系统管理员或公司管理员可以访问此接口
const fetchAvailableManagers = async () => {
  // HR 角色无法访问此接口，直接返回
  if (!isSystemAdmin() && !isCompanyAdmin()) {
    managerOptions.value = []
    return
  }
  
  try {
    const result = await userController.listUserVoByPage({
      pageNum: 1,
      pageSize: 1000,
      userRole: 'company_admin',
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      // 过滤掉已经有公司的管理人
      const currentCompanyId = getCompanyId()
      const available = records.filter(
        (user: API.UserVO) => !user.companyId || String(user.companyId) === currentCompanyId,
      )
      managerOptions.value = available.map((user: API.UserVO) => ({
        label: `${user.username} (${user.nickname})`,
        value: user.id as number,
      }))
    }
  } catch (error) {
    console.error('Failed to fetch available managers:', error)
  }
}

// 查看员工档案
const viewEmployeeProfile = async (employeeId: number) => {
  // 跳转到员工档案详情页，传递当前公司ID用于筛选
  router.push({
    name: 'employeeProfile',
    params: { employeeId: String(employeeId) },
    query: { companyId: getCompanyId() },
  })
}

// 获取部门员工列表（用于选择主管）
const fetchDepartmentEmployees = async (departmentId: string) => {
  try {
    if (!departmentId) {
      employeeOptions.value = []
      return
    }
    
    const params: any = {
      pageNum: 1,
      pageSize: 1000,
      companyId: getCompanyId(),
      departmentId: departmentId as any, // 只获取该部门的员工
    }
    
    const result = await employeeController.listEmployeeVoByPage(params)
    if (result?.data?.code === 0 && result.data.data) {
      const employees = result.data.data.records || []
      // 确保只显示当前公司的员工
      const currentCompanyId = getCompanyId()
      const filtered = currentCompanyId
        ? employees.filter((emp: API.EmployeeVO) => String(emp.companyId) === currentCompanyId)
        : employees
      employeeOptions.value = filtered.map((emp: API.EmployeeVO) => ({
        label: emp.name || `员工${emp.id}`,
        value: String(emp.id),
      }))
    } else {
      employeeOptions.value = []
    }
  } catch (error) {
    console.error('Failed to fetch department employees:', error)
    employeeOptions.value = []
  }
}

// 打开部门编辑对话框
const openDepartmentModal = async (department?: API.DepartmentVO) => {
  if (department) {
    departmentFormState.id = department.id ? String(department.id) : undefined
    departmentFormState.name = department.name || ''
    // 先获取该部门的员工列表（主管只能由本部门员工担任）
    if (department.id) {
      await fetchDepartmentEmployees(String(department.id))
    } else {
      employeeOptions.value = []
    }
    // 在员工列表加载完成后再设置 leaderId，并验证主管是否在列表中
    const leaderIdStr = department.leaderId ? String(department.leaderId) : undefined
    if (leaderIdStr) {
      // 检查主管是否在员工选项列表中
      const leaderExists = employeeOptions.value.some(opt => opt.value === leaderIdStr)
      if (leaderExists) {
        departmentFormState.leaderId = leaderIdStr
      } else {
        // 如果主管不在列表中，清空（可能是已离职或已调离该部门）
        departmentFormState.leaderId = undefined
      }
    } else {
      departmentFormState.leaderId = undefined
    }
  } else {
    departmentFormState.id = undefined
    departmentFormState.name = ''
    departmentFormState.leaderId = undefined
    // 新增部门时，暂时不加载员工列表（因为还没有部门ID）
    employeeOptions.value = []
  }
  departmentModalVisible.value = true
}

// 查看部门详情
const viewDepartmentDetail = async (department: API.DepartmentVO) => {
  if (department.id) {
    // 跳转到部门详情页面
    router.push({
      name: 'departmentDetail',
      params: { id: String(department.id) },
      query: { 
        from: 'company',
        companyId: getCompanyId(),
      },
    })
  }
}

// 获取部门员工列表（用于详情显示）
const fetchDepartmentEmployeeList = async (departmentId: string) => {
  try {
    departmentEmployeeLoading.value = true
    const result = await employeeController.listEmployeeVoByPage({
      pageNum: 1,
      pageSize: 1000,
      companyId: getCompanyId(),
      departmentId: departmentId as any,
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const employees = result.data.data.records || []
      // 确保只显示当前公司的员工
      const currentCompanyId = getCompanyId()
      let filtered = currentCompanyId
        ? employees.filter((emp: API.EmployeeVO) => String(emp.companyId) === currentCompanyId)
        : employees
      
      // 如果有主管，将主管放在第一位
      if (currentDepartmentDetail.value?.leaderId) {
        const leaderId = String(currentDepartmentDetail.value.leaderId)
        const leaderIndex = filtered.findIndex((emp: API.EmployeeVO) => String(emp.id) === leaderId)
        if (leaderIndex > -1 && filtered[leaderIndex]) {
          // 将主管移到第一位
          const leader = filtered[leaderIndex]
          filtered.splice(leaderIndex, 1)
          filtered.unshift(leader)
        }
      }
      
      departmentEmployeeList.value = filtered
    }
  } catch (error) {
    console.error('Failed to fetch department employee list:', error)
    departmentEmployeeList.value = []
  } finally {
    departmentEmployeeLoading.value = false
  }
}

// 保存部门
const handleSaveDepartment = async () => {
  try {
    await departmentFormRef.value?.validate()
    if (departmentFormState.id) {
      // 更新部门
      // 先获取原始部门信息，用于比较 leaderId 是否变化
      const originalDepartment = departmentList.value.find(
        (dept: API.DepartmentVO) => String(dept.id) === departmentFormState.id
      )
      const originalLeaderId = originalDepartment?.leaderId ? String(originalDepartment.leaderId) : undefined
      const newLeaderId = departmentFormState.leaderId
      
      // 1. 更新部门名称（不包含 leaderId）
      const updateResult = await departmentController.updateDepartment({
        id: departmentFormState.id as any,
        name: departmentFormState.name,
        companyId: getCompanyId() as any,
        // 不在这里更新 leaderId，使用 addSupervisor 接口
      } as API.Department)
      
      if (updateResult?.data?.code !== 0) {
        message.error('部门更新失败')
        return
      }
      
      // 2. 如果 leaderId 有变化，使用 addSupervisor 接口更新主管
      if (originalLeaderId !== newLeaderId) {
        if (newLeaderId) {
          // 设置新主管
          const supervisorResult = await departmentController.addSupervisor({
            departmentId: departmentFormState.id as any,
            employeeId: newLeaderId as any,
            isSupervisor: true,
          } as API.DepartmentSupervisorAddRequest)
          
          if (supervisorResult?.data?.code !== 0) {
            message.error(supervisorResult?.data?.message || '设置主管失败')
            // 即使设置主管失败，部门名称已更新，所以继续
          }
        } else if (originalLeaderId) {
          // 取消原主管（从有主管变为无主管）
          const supervisorResult = await departmentController.addSupervisor({
            departmentId: departmentFormState.id as any,
            employeeId: originalLeaderId as any,
            isSupervisor: false,
          } as API.DepartmentSupervisorAddRequest)
          
          if (supervisorResult?.data?.code !== 0) {
            message.error(supervisorResult?.data?.message || '取消主管失败')
            // 即使取消主管失败，部门名称已更新，所以继续
          }
        }
      }
      
      message.success('部门更新成功')
      departmentModalVisible.value = false
      await fetchDepartmentList()
      await fetchHomeStats()
    } else {
      // 新增部门
      const result = await departmentController.addDepartment({
        name: departmentFormState.name,
      } as API.DepartmentAddRequest)
      if (result?.data?.code === 0) {
        const newDepartmentId = result.data.data
        // 如果新增部门时指定了主管，设置主管
        if (newDepartmentId && departmentFormState.leaderId) {
          try {
            await departmentController.addSupervisor({
              departmentId: newDepartmentId as any,
              employeeId: departmentFormState.leaderId as any,
              isSupervisor: true,
            } as API.DepartmentSupervisorAddRequest)
          } catch (error) {
            console.error('Failed to set supervisor for new department:', error)
            // 即使设置主管失败，部门已创建，所以继续
          }
        }
        message.success('部门创建成功')
        departmentModalVisible.value = false
        await fetchDepartmentList()
        await fetchHomeStats()
      } else {
        message.error(result?.data?.message || '部门创建失败')
      }
    }
  } catch (error) {
    console.error('Save department error:', error)
    message.error('操作失败')
  }
}

// 切换部门状态（启用/弃用）
const handleToggleDepartmentStatus = async (department: API.DepartmentVO) => {
  const isDelete = (department as any).isDelete
  const action = isDelete ? '启用' : '弃用'
  
  const handleConfirm = async () => {
    try {
      const result = await departmentController.toggleDepartmentStatus({
        id: department.id ? String(department.id) : undefined,
      } as any)
      
      if (result?.data?.code === 0) {
        if (!isDelete) {
          // 弃用操作：将本部门员工的departmentId置为null
          // 需要调用后端批量更新接口
          const employees = departmentEmployeeList.value
          if (employees.length > 0) {
            // 这里需要后端提供批量更新接口，暂时先提示
            message.info('部门已弃用，请手动处理部门员工')
          }
        }
        message.success(`部门${action}成功`)
        await fetchDepartmentList()
        await fetchHomeStats()
        if (departmentDetailModalVisible.value) {
          await fetchDepartmentEmployeeList(department.id ? String(department.id) : '')
        }
      } else {
        message.error(`部门${action}失败`)
      }
    } catch (error) {
      console.error(`Toggle department status error:`, error)
      message.error(`部门${action}失败`)
    }
  }
  
  if (!isDelete) {
    // 弃用操作，显示确认对话框
    const { Modal } = await import('ant-design-vue')
    Modal.confirm({
      title: '确认弃用部门',
      content: '此操作将把本部门员工移出本部门，是否继续？',
      okText: '确定',
      cancelText: '取消',
      onOk: handleConfirm,
    })
  } else {
    // 启用操作，直接执行
    handleConfirm()
  }
}

// 打开管理人变更对话框
const openManagerModal = (action: 'assign' | 'remove') => {
  managerFormState.action = action
  managerFormState.userId = undefined
  if (action === 'assign') {
    fetchAvailableManagers()
  } else {
    fetchManagerList()
  }
  managerModalVisible.value = true
}

// 保存管理人变更
const handleSaveManager = async () => {
  try {
    await managerFormRef.value?.validate()
    if (managerFormState.action === 'assign') {
      // 任命管理人
      const result = await userController.updateUser({
        id: managerFormState.userId,
        userRole: 'company_admin',
        companyId: getCompanyId(),
      } as API.UserUpdateRequest)
      if (result?.data?.code === 0) {
        message.success('任命管理人成功')
        managerModalVisible.value = false
        await fetchManagerList()
        await fetchHomeStats()
      } else {
        message.error('任命管理人失败')
      }
    } else {
      // 解除管理人
      const result = await userController.updateUser({
        id: managerFormState.userId,
        userRole: 'hr',
        companyId: undefined,
      } as API.UserUpdateRequest)
      if (result?.data?.code === 0) {
        message.success('解除管理人成功')
        managerModalVisible.value = false
        await fetchManagerList()
        await fetchHomeStats()
      } else {
        message.error('解除管理人失败')
      }
    }
  } catch (error) {
    console.error('Save manager error:', error)
  }
}

// 保存公司信息
const handleSaveCompany = async () => {
  try {
    await companyFormRef.value?.validate()
    
    // 根据角色构建更新请求
    const updateRequest: API.CompanyUpdateRequest = {
      id: getCompanyId(),
      phone: companyFormState.phone,
      email: companyFormState.email,
    } as API.CompanyUpdateRequest
    
    // 系统管理员可以更新所有字段
    if (isSystemAdmin()) {
      updateRequest.name = companyFormState.name
      updateRequest.contactPersonId = companyFormState.contactPersonId
      updateRequest.industries = companyFormState.industries
    }
    // 公司管理员只能更新电话和邮箱（已在模板中限制）
    
    const result = await companyController.update(updateRequest)
    
    if (result?.data?.code === 0 && result.data.data === true) {
      message.success('公司信息更新成功')
      companyModalVisible.value = false
      await fetchCompanyInfo()
    } else {
      const errorMsg = result?.data?.message || '公司信息更新失败'
      message.error(errorMsg)
    }
  } catch (error: any) {
    console.error('Save company error:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '公司信息更新失败'
    message.error(errorMsg)
  }
}

// 删除公司
const handleDeleteCompany = async () => {
  try {
    const result = await companyController.toggleCompanyStatus({ id: getCompanyId() } as any)
    if (result?.data?.code === 0) {
      message.success('公司删除成功')
      router.push('/companies')
    } else {
      message.error('公司删除失败')
    }
  } catch (error) {
    console.error('Delete company error:', error)
    message.error('公司删除失败')
  }
}

// 员工搜索
const handleEmployeeSearch = () => {
  employeeSearchParams.pageNum = 1
  fetchEmployeeList()
}

// 员工重置
const handleEmployeeReset = () => {
  employeeSearchParams.name = ''
  employeeSearchParams.departmentId = undefined
  employeeSearchParams.idCardNumber = ''
  employeeSearchParams.pageNum = 1
  fetchEmployeeList()
}

// 员工表格变化
const handleEmployeeTableChange = (pagination: any, filters: any, sorter: any) => {
  employeeSearchParams.pageNum = pagination.current
  employeeSearchParams.pageSize = pagination.pageSize
  if (sorter.field) {
    // 将前端字段名映射为数据库列名
    const fieldMap: Record<string, string> = {
      createTime: 'create_time',
      updateTime: 'update_time',
      name: 'name',
      phone: 'phone',
    }
    employeeSearchParams.sortField = fieldMap[sorter.field] || sorter.field
    employeeSearchParams.sortOrder = sorter.order || 'descend'
  } else {
    // 没有排序时使用默认排序
    employeeSearchParams.sortField = 'create_time'
    employeeSearchParams.sortOrder = 'descend'
  }
  fetchEmployeeList()
}

// 部门表格变化
const handleDepartmentTableChange = (pagination: any) => {
  departmentSearchParams.pageNum = pagination.current
  departmentSearchParams.pageSize = pagination.pageSize
  fetchDepartmentList()
}

// 菜单切换
const handleMenuChange = (key: string) => {
  selectedMenu.value = key
  if (key === 'points') {
    // 切换到积分变动记录时，加载数据
    fetchPointsHistory(1)
  } else if (key === 'employees') {
    // 先获取部门列表，以便在搜索时使用
    fetchDepartmentList().then(() => {
      fetchEmployeeList()
    })
  } else if (key === 'departments') {
    fetchDepartmentList()
  } else if (key === 'managers') {
    fetchManagerList()
  } else if (key === 'settings') {
    fetchContactPersonOptions()
  }
}

// 表单规则（根据角色动态生成）
const companyFormRules = computed(() => {
  const rules: any = {}
  if (isSystemAdmin()) {
    rules.name = [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
    rules.contactPersonId = [{ required: true, message: '请选择联系人', trigger: 'change' }]
  }
  rules.phone = [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
  rules.email = [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
  return rules
})

const departmentFormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

const managerFormRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
}

// 监听菜单变化，自动加载积分变动记录
watch(selectedMenu, (newMenu) => {
  if (newMenu === 'points' && canViewPoints.value) {
    fetchPointsHistory(1)
  }
})

// 初始化
onMounted(async () => {
  await fetchCompanyInfo()
  await fetchHomeStats()
  await fetchContactPersonOptions()
  // 如果是hr、company_admin或admin，获取积分信息（仅获取总积分，变动记录在菜单切换时加载）
  if (canViewPoints.value) {
    await fetchPoints()
    // 如果默认菜单是积分变动记录，则加载数据
    if (selectedMenu.value === 'points') {
      fetchPointsHistory(1)
    }
  }
})
</script>

<template>
  <div class="company-detail-wrapper">
    <a-layout style="min-height: calc(100vh - 200px)">
      <!-- 左侧菜单 -->
      <a-layout-sider 
        width="220" 
        style="background: #fff; border-right: 1px solid #e8e8e8; flex: 0 0 220px"
      >
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0">
          <a-button 
            type="text" 
            size="small" 
            @click="isSystemAdmin() ? router.push('/companies') : router.push('/home')" 
            style="padding: 0"
          >
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            返回
          </a-button>
        </div>
        <div style="padding: 12px">
          <a-menu
            :selectedKeys="[selectedMenu]"
            mode="inline"
            :style="{ border: 'none' }"
            @click="(e: any) => handleMenuChange(e.key as string)"
          >
            <a-menu-item key="home">首页</a-menu-item>
            <a-menu-item key="employees">员工详情</a-menu-item>
            <a-menu-item key="departments">部门详情</a-menu-item>
            <a-menu-item v-if="canViewPoints" key="points">积分变动记录</a-menu-item>
            <a-menu-item v-if="isSystemAdmin() || isCompanyAdmin()" key="managers">管理人变更</a-menu-item>
            <a-menu-item v-if="isSystemAdmin() || isCompanyAdmin()" key="settings">公司资料更新</a-menu-item>
          </a-menu>
        </div>
      </a-layout-sider>

      <!-- 右侧内容 -->
      <a-layout-content style="padding: 24px; background: #fff" class="company-content">
        <!-- 首页 -->
        <div v-if="selectedMenu === 'home'" class="home-content">
          <a-card :loading="companyLoading">
            <template #title>公司概览</template>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="公司名称">
                {{ companyInfo?.name || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="注册时间">
                {{ companyInfo?.createTime || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="员工人数">{{ employeeCount }}</a-descriptions-item>
              <a-descriptions-item label="部门数量">{{ departmentCount }}</a-descriptions-item>
              <a-descriptions-item v-if="!isHR()" label="管理人数量">{{ managerCount }}</a-descriptions-item>
              <a-descriptions-item v-if="canViewPoints" label="当前积分">
                <a-spin :spinning="pointsLoading">
                  <span style="font-size: 16px; font-weight: bold; color: #1890ff;">
                    {{ totalPoints.toFixed(2) }}
                  </span>
                </a-spin>
              </a-descriptions-item>
              <a-descriptions-item label="联系人">
                {{ companyInfo?.contactPersonName || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="联系电话">
                {{ companyInfo?.phone || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="邮箱">
                {{ companyInfo?.email || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="行业大类">
                {{ companyInfo?.industryCategory || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="行业子类" :span="2">
                <template v-if="companyInfo?.industries && companyInfo.industries.length > 0">
                  <a-tag v-for="(industry, index) in companyInfo.industries" :key="index" color="blue">
                    {{ industry }}
                  </a-tag>
                </template>
                <span v-else>-</span>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </div>

        <!-- 员工详情 -->
        <div v-if="selectedMenu === 'employees'" class="employee-content">
          <a-card>
            <template #title>员工列表</template>
            <a-row :gutter="[16, 16]" style="margin-bottom: 16px">
              <a-col :xs="24" :sm="8">
                <a-input
                  v-model:value="employeeSearchParams.name"
                  placeholder="按姓名搜索"
                  allow-clear
                  @pressEnter="handleEmployeeSearch"
                />
              </a-col>
              <a-col :xs="24" :sm="8">
                <a-select
                  v-model:value="employeeSearchParams.departmentId"
                  placeholder="按部门搜索"
                  allow-clear
                  style="width: 100%"
                  :options="departmentOptionsForSearch"
                  @change="handleEmployeeSearch"
                />
              </a-col>
              <a-col :xs="24" :sm="8">
                <a-input
                  v-model:value="employeeSearchParams.idCardNumber"
                  placeholder="按身份证号搜索"
                  allow-clear
                  @pressEnter="handleEmployeeSearch"
                />
              </a-col>
            </a-row>
            <a-space style="margin-bottom: 16px">
              <a-button type="primary" @click="handleEmployeeSearch">查询</a-button>
              <a-button @click="handleEmployeeReset">重置</a-button>
            </a-space>
            <a-table
              :columns="[
                { title: '姓名', dataIndex: 'name', width: 120 },
                { title: '性别', dataIndex: 'gender', width: 80 },
                { title: '电话', dataIndex: 'phone', width: 150 },
                { title: '邮箱', dataIndex: 'email', width: 200 },
                { title: '部门', dataIndex: 'departmentName', width: 150 },
                {
                  title: '操作',
                  key: 'operation',
                  width: 120,
                  align: 'center',
                },
              ]"
              :data-source="employeeList"
              :loading="employeeLoading"
              :pagination="{
                current: employeeSearchParams.pageNum,
                pageSize: employeeSearchParams.pageSize,
                total: employeeTotal,
              }"
              @change="handleEmployeeTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'operation'">
                  <a-button type="link" size="small" @click="viewEmployeeProfile(record.id)">
                    查看档案
                  </a-button>
                </template>
              </template>
            </a-table>
          </a-card>
        </div>

        <!-- 部门详情 -->
        <div v-if="selectedMenu === 'departments'" class="department-content">
          <a-card>
            <template #title>部门列表</template>
            <template #extra>
              <a-button type="primary" @click="openDepartmentModal()">新增部门</a-button>
            </template>
            <a-table
              :columns="[
                { title: '部门名称', dataIndex: 'name', width: 200 },
                { title: '主管', dataIndex: 'leaderName', width: 150 },
                { title: '人数', key: 'employeeCount', width: 100 },
                { title: '状态', key: 'status', width: 100 },
                {
                  title: '操作',
                  key: 'operation',
                  width: 200,
                  align: 'center',
                },
              ]"
              :data-source="departmentList"
              :loading="departmentLoading"
              :pagination="{
                current: departmentSearchParams.pageNum,
                pageSize: departmentSearchParams.pageSize,
                total: departmentTotal,
              }"
              @change="handleDepartmentTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'leaderName'">
                  {{ record.leaderName || '-' }}
                </template>
                <template v-else-if="column.key === 'employeeCount'">
                  {{ (record as any).employeeCount || 0 }}
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="(record as any).isDelete ? 'red' : 'green'">
                    {{ (record as any).isDelete ? '已弃用' : '启用中' }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'operation'">
                  <a-space>
                    <a-button type="link" size="small" @click="viewDepartmentDetail(record)">
                      查看详情
                    </a-button>
                    <a-button type="link" size="small" @click="openDepartmentModal(record)">
                      编辑
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      :danger="!(record as any).isDelete"
                      @click="handleToggleDepartmentStatus(record)"
                    >
                      {{ (record as any).isDelete ? '启用' : '弃用' }}
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </div>

        <!-- 积分变动记录 -->
        <div v-if="selectedMenu === 'points'" class="points-content">
          <a-card>
            <template #title>积分变动记录</template>
            <a-spin :spinning="pointsHistoryLoading">
              <a-table
                :columns="[
                  { title: '变动日期', dataIndex: 'changeDate', key: 'changeDate', width: 120 },
                  { title: '变动原因', dataIndex: 'changeReasonText', key: 'changeReasonText', width: 120 },
                  { title: '变动说明', dataIndex: 'changeDescription', key: 'changeDescription', width: 200 },
                  { title: '关联员工', dataIndex: 'withEmployeeName', key: 'withEmployeeName', width: 120 },
                  { title: '积分变动', dataIndex: 'points', key: 'points', width: 120 },
                ]"
                :data-source="pointsHistory"
                :loading="pointsHistoryLoading"
                :pagination="{
                  current: pointsHistoryPageNum,
                  pageSize: pointsHistoryPageSize,
                  total: pointsHistoryTotal,
                  showSizeChanger: false,
                  showTotal: (total) => `共 ${total} 条`,
                }"
                row-key="id"
                @change="(pag: any) => fetchPointsHistory(pag.current)"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'points'">
                    <span 
                      :style="{ 
                        color: formatPoints(record.points).color, 
                        fontWeight: 'bold' 
                      }"
                    >
                      {{ formatPoints(record.points).text }}
                    </span>
                  </template>
                  <template v-else-if="column.key === 'changeDescription'">
                    {{ record.changeDescription || '-' }}
                  </template>
                  <template v-else-if="column.key === 'withEmployeeName'">
                    {{ record.withEmployeeName || '-' }}
                  </template>
                </template>
              </a-table>
              <a-empty v-if="!pointsHistoryLoading && pointsHistory.length === 0" description="暂无积分变动记录" />
            </a-spin>
          </a-card>
        </div>

        <!-- 管理人变更 -->
        <div v-if="selectedMenu === 'managers'" class="manager-content">
          <a-card>
            <template #title>管理人列表</template>
            <template v-if="isSystemAdmin()" #extra>
              <a-space>
                <a-button type="primary" @click="openManagerModal('assign')">任命管理人</a-button>
                <a-button danger @click="openManagerModal('remove')">解除管理人</a-button>
              </a-space>
            </template>
            <div v-if="isCompanyAdmin()" style="margin-bottom: 16px; color: #8c8c8c; font-size: 12px">
              提示：如需变更管理人，请联系系统管理员
            </div>
            <a-table
              :columns="[
                { title: '用户名', dataIndex: 'username', width: 150 },
                { title: '昵称', dataIndex: 'nickname', width: 150 },
              ]"
              :data-source="managerList"
              :loading="managerLoading"
              :pagination="false"
            />
          </a-card>
        </div>

        <!-- 公司资料更新 -->
        <div v-if="selectedMenu === 'settings'" class="settings-content">
          <a-card>
            <template #title>公司资料</template>
            <a-form
              ref="companyFormRef"
              :model="companyFormState"
              :rules="companyFormRules"
              layout="vertical"
            >
              <!-- 系统管理员可以编辑所有字段 -->
              <template v-if="isSystemAdmin()">
                <a-form-item label="公司名称" name="name">
                  <a-input v-model:value="companyFormState.name" placeholder="请输入公司名称" />
                </a-form-item>
                <a-form-item label="联系人" name="contactPersonId">
                  <a-select
                    v-model:value="companyFormState.contactPersonId"
                    placeholder="请选择联系人（只能由本公司的管理人之一担任）"
                    :options="contactPersonOptions"
                  />
                </a-form-item>
                <a-form-item label="联系电话" name="phone">
                  <a-input v-model:value="companyFormState.phone" placeholder="请输入联系电话" />
                </a-form-item>
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="companyFormState.email" placeholder="请输入邮箱" />
                </a-form-item>
                <a-form-item label="公司行业" name="industries">
                  <IndustrySelector
                    v-model="companyFormState.industries"
                    placeholder="请选择公司行业（可跨类别多选）"
                    :max-tag-count="3"
                  />
                </a-form-item>
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="handleSaveCompany">保存</a-button>
                    <a-popconfirm
                      title="确认删除？"
                      description="确定要删除该公司吗？此操作不可恢复。"
                      ok-text="确定"
                      cancel-text="取消"
                      @confirm="handleDeleteCompany"
                    >
                      <a-button danger>删除公司</a-button>
                    </a-popconfirm>
                  </a-space>
                </a-form-item>
              </template>
              
              <!-- 公司管理员只能编辑联系电话和邮箱 -->
              <template v-else-if="isCompanyAdmin()">
                <a-form-item label="公司名称">
                  <a-input :value="companyInfo?.name || '-'" disabled />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px">
                    提示：如需修改公司名称，请联系系统管理员
                  </div>
                </a-form-item>
                <a-form-item label="联系人">
                  <a-input :value="companyInfo?.contactPersonName || '-'" disabled />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px">
                    提示：如需修改联系人，请联系系统管理员
                  </div>
                </a-form-item>
                <a-form-item label="联系电话" name="phone">
                  <a-input v-model:value="companyFormState.phone" placeholder="请输入联系电话" />
                </a-form-item>
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="companyFormState.email" placeholder="请输入邮箱" />
                </a-form-item>
                <a-form-item label="公司行业">
                  <div style="margin-bottom: 8px">
                    <a-tag v-for="(industry, index) in companyInfo?.industries" :key="index" color="blue" style="margin-right: 8px">
                      {{ industry }}
                    </a-tag>
                    <span v-if="!companyInfo?.industries || companyInfo.industries.length === 0">-</span>
                  </div>
                  <div style="color: #8c8c8c; font-size: 12px">
                    提示：如需修改公司行业，请联系系统管理员
                  </div>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="handleSaveCompany">保存</a-button>
                </a-form-item>
              </template>
            </a-form>
          </a-card>
        </div>
      </a-layout-content>
    </a-layout>

    <!-- 部门编辑/新增对话框 -->
    <a-modal
      v-model:open="departmentModalVisible"
      :title="isAddDepartmentMode ? '新增部门' : '编辑部门'"
      ok-text="保存"
      cancel-text="取消"
      @ok="handleSaveDepartment"
    >
      <a-form
        ref="departmentFormRef"
        :model="departmentFormState"
        :rules="departmentFormRules"
        layout="vertical"
      >
        <a-form-item label="部门名称" name="name">
          <a-input v-model:value="departmentFormState.name" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item v-if="!isAddDepartmentMode" label="主管">
          <a-select
            v-model:value="departmentFormState.leaderId"
            placeholder="请选择主管（本部门员工）"
            :options="employeeOptions"
            allow-clear
            show-search
            :filter-option="(input: string, option: any) =>
              option.label.toLowerCase().includes(input.toLowerCase())"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 管理人变更对话框 -->
    <a-modal
      v-model:open="managerModalVisible"
      :title="managerFormState.action === 'assign' ? '任命管理人' : '解除管理人'"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleSaveManager"
    >
      <a-form
        ref="managerFormRef"
        :model="managerFormState"
        :rules="managerFormRules"
        layout="vertical"
      >
        <a-form-item label="选择用户" name="userId">
          <a-select
            v-model:value="managerFormState.userId"
            :placeholder="
              managerFormState.action === 'assign'
                ? '请选择要任命为管理人的用户（只有company_admin角色）'
                : '请选择要解除的管理人'
            "
            :options="managerOptions"
            show-search
            :filter-option="(input: string, option: any) =>
              option.label.toLowerCase().includes(input.toLowerCase())"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 部门详情对话框 -->
    <a-modal
      v-model:open="departmentDetailModalVisible"
      title="部门详情"
      :footer="null"
      width="800"
    >
      <div v-if="currentDepartmentDetail">
        <a-descriptions :column="2" bordered style="margin-bottom: 24px">
          <a-descriptions-item label="部门名称">
            {{ currentDepartmentDetail.name || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="部门状态">
            <a-tag :color="(currentDepartmentDetail as any).isDelete ? 'red' : 'green'">
              {{ (currentDepartmentDetail as any).isDelete ? '已弃用' : '启用中' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="主管姓名">
            {{ getLeaderName }}
          </a-descriptions-item>
          <a-descriptions-item label="员工人数">
            {{ departmentEmployeeList.length }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider>部门员工列表</a-divider>
        <a-table
          :columns="[
            { title: '姓名', key: 'name', width: 150 },
            { title: '性别', dataIndex: 'gender', width: 80 },
            { title: '电话', dataIndex: 'phone', width: 150 },
            { title: '邮箱', dataIndex: 'email', width: 200 },
          ]"
          :data-source="departmentEmployeeList"
          :loading="departmentEmployeeLoading"
          :pagination="false"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <a-space>
                <span>{{ record.name || '-' }}</span>
                <a-tag
                  v-if="currentDepartmentDetail && currentDepartmentDetail.leaderId && String(record.id) === String(currentDepartmentDetail.leaderId)"
                  color="blue"
                >
                  主管
                </a-tag>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.home-content,
.employee-content,
.department-content,
.manager-content,
.settings-content {
  min-height: 500px;
}
</style>


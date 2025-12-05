<script setup lang="ts">
import { onMounted, reactive, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import * as employeeController from '@/api/employeeController'
import * as userController from '@/api/userController'
import * as departmentController from '@/api/departmentController'
import * as companyController from '@/api/companyController'
import { useRole } from '@/composables/useRole'
import { useUserStore } from '@/stores/userStore'
import type { FormInstance, UploadProps, UploadFile } from 'ant-design-vue'

const router = useRouter()

const { isSystemAdmin, isCompanyAdmin, isHR } = useRole()
const userStore = useUserStore()

// 获取当前公司ID
const getCurrentCompanyId = (): number | undefined => {
  return userStore.userInfo?.companyId
}

interface EmployeeTableData {
  key: string
  id: string | number // 支持字符串和数字，防止精度丢失
  name: string
  gender?: string
  phone?: string
  email?: string
  idCardNumber?: string
  companyId?: string | number
  companyName?: string
  departmentId?: string | number
  departmentName?: string
  photoUrl?: string
}

const employeeFormRef = ref<FormInstance>()
const tableLoading = ref(false)
const modalVisible = ref(false)

// 批量导入相关
const importModalVisible = ref(false)
const importResultVisible = ref(false)
const importResult = ref<API.EmployeeBatchImportResult | null>(null)
const importLoading = ref(false)
const fileList = ref<UploadFile[]>([])

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  departmentId: undefined as number | undefined,
  companyId: undefined as number | undefined,
  sortField: 'create_time',
  sortOrder: 'descend',
})

const employeeList = ref<EmployeeTableData[]>([])
const total = ref(0)
const departmentOptions = ref<{ label: string; value: number }[]>([])
const departmentLoading = ref(false)

const formState = reactive({
  name: '',
  gender: '男',
  phone: '',
  email: '',
  idCardNumber: '',
  companyId: undefined as number | undefined, // 系统管理员需要选择公司
  departmentId: undefined as number | undefined,
})

const companyOptions = ref<{ label: string; value: number }[]>([])
const companyLoading = ref(false)

const genderOptions = [
  { label: '男', value: '男' },
  { label: '女', value: '女' },
]

// 获取公司列表（系统管理员使用）
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
    } else {
      message.error('获取公司列表失败')
    }
  } catch (error) {
    console.error('Failed to fetch company list:', error)
    message.error('获取公司列表失败')
  } finally {
    companyLoading.value = false
  }
}

const columns = computed(() => {
  const baseColumns = [
    {
      title: '头像',
      dataIndex: 'photoUrl',
      width: 80,
      key: 'avatar',
    },
    {
      title: '姓名',
      dataIndex: 'name',
      width: 120,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 80,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 180,
    },
  ]

  // 系统管理员可以看到公司列
  if (isSystemAdmin()) {
    baseColumns.push({
      title: '所属公司',
      dataIndex: 'companyName',
      width: 150,
    } as any)
  }

  baseColumns.push(
    {
      title: '部门',
      dataIndex: 'departmentName',
      width: 120,
    } as any,
    {
      title: '操作',
      width: 100,
      key: 'operation',
      align: 'center' as const,
    } as any,
  )

  return baseColumns
})

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

const formRules = {
  companyId: [
    {
      validator: (_: any, value: number | undefined) => {
        // 管理员新增时公司可选，非管理员不需要此字段
        return Promise.resolve()
      },
      trigger: 'change',
    },
  ],
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  departmentId: [
    {
      validator: (_: any, value: number | undefined) => {
        // 部门为可选字段，所有角色都可以不选择部门
        return Promise.resolve()
      },
      trigger: 'change',
    },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur',
    },
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: 'blur',
    },
  ],
  idCardNumber: [
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
  ],
}

// 获取部门列表（根据公司ID）
const fetchDepartmentOptions = async (companyId?: number) => {
  // 系统管理员需要传入 companyId，非系统管理员使用当前公司ID
  const targetCompanyId = companyId || getCurrentCompanyId()
  
  if (!targetCompanyId) {
    // 系统管理员在未选择公司时不加载部门
    if (isSystemAdmin()) {
      departmentOptions.value = []
      return
    }
    message.error('无法获取当前公司信息')
    return
  }

  try {
    departmentLoading.value = true
    const response = await departmentController.listDepartmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      companyId: targetCompanyId,
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
    } else {
      message.error('获取部门列表失败')
    }
  } catch (error) {
    console.error('Failed to fetch department list:', error)
    message.error('获取部门列表失败')
  } finally {
    departmentLoading.value = false
  }
}

// 获取员工列表
const fetchEmployeeList = async () => {
  try {
    tableLoading.value = true

    // 构建查询参数
    const queryParams: API.EmployeeQueryRequest = {
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      name: searchParams.name || undefined,
      departmentId: searchParams.departmentId,
      sortField: searchParams.sortField,
      sortOrder: searchParams.sortOrder,
    }

    // 非系统管理员必须传入 companyId
    if (!isSystemAdmin()) {
      const companyId = getCurrentCompanyId()
      if (!companyId) {
        message.error('无法获取当前公司信息')
        tableLoading.value = false
        return
      }
      queryParams.companyId = companyId
    } else {
      // 系统管理员可以选择查看特定公司（可选）
      queryParams.companyId = searchParams.companyId
    }

    console.log('[fetchEmployeeList] Fetching employees with params:', queryParams)
    const result = await employeeController.listEmployeeVoByPage(queryParams)
    console.log('[fetchEmployeeList] API Response data:', result?.data?.data)

    if (result?.data?.code === 0 && result.data.data) {
      const pageData = result.data.data
      employeeList.value = (pageData.records || []).map((employee: any, index: number) => ({
        key: `${searchParams.pageNum}-${index}`,
        id: String(employee.id || '0'), // 转换为字符串，防止精度丢失
        name: employee.name || '',
        gender: employee.gender || '',
        phone: employee.phone || '',
        email: employee.email || '',
        idCardNumber: employee.idCardNumber || '',
        companyId: employee.companyId ? String(employee.companyId) : undefined,
        companyName: employee.companyName || '',
        departmentId: employee.departmentId ? String(employee.departmentId) : undefined,
        departmentName: employee.departmentName || '',
        photoUrl: employee.photoUrl || '',
      }))
      total.value = pageData.totalRow || 0
    }
  } catch (error) {
    console.error('Failed to fetch employee list:', error)
    message.error('获取员工列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 打开新增对话框
const openAddModal = async () => {
  formState.name = ''
  formState.gender = ''
  formState.phone = ''
  formState.email = ''
  formState.idCardNumber = ''
  formState.companyId = undefined
  formState.departmentId = undefined

  // 系统管理员需要加载公司列表
  if (isSystemAdmin()) {
    await fetchCompanyOptions()
    // 清空部门列表，等待选择公司后再加载
    departmentOptions.value = []
  } else {
    // 非系统管理员加载当前公司的部门列表
    await fetchDepartmentOptions()
  }

  modalVisible.value = true
}

// 查看员工详情（跳转到详情页）
const handleViewDetail = (record: EmployeeTableData) => {
  // 数据范围校验：非系统管理员只能查看本公司员工
  if (!isSystemAdmin()) {
    const currentCompanyId = getCurrentCompanyId()
    if (record.companyId !== currentCompanyId) {
      message.error('仅可查看本公司员工')
      return
    }
  }

  router.push(`/employees/${record.id}/detail`)
}

// 解雇员工
const handleFireEmployee = async (record: EmployeeTableData) => {
  // 数据范围校验：必须是本公司员工
  if (!isSystemAdmin()) {
    const currentCompanyId = getCurrentCompanyId()
    if (record.companyId !== currentCompanyId) {
      message.error('仅可解雇本公司员工')
      return
    }
  }

  try {
    const result = await employeeController.fireEmployee({ id: record.id as any })
    if (result?.data?.code === 0) {
      message.success('员工已解雇')
      await fetchEmployeeList()
    } else {
      message.error('解雇员工失败')
    }
  } catch (error) {
    console.error('Failed to fire employee:', error)
    message.error('解雇员工失败')
  }
}

// 冻结/恢复账户（仅系统管理员）
const handleToggleUserStatus = async (record: EmployeeTableData) => {
  if (!isSystemAdmin()) {
    message.error('仅系统管理员可执行此操作')
    return
  }

  try {
    // 需要获取员工对应的用户ID
    const employeeDetail = await employeeController.getEmployeeVoById({ id: record.id as any })
    if (employeeDetail?.data?.code === 0 && employeeDetail.data.data) {
      const employee = employeeDetail.data.data as any
      if (!employee.userId) {
        message.error('该员工未关联用户账户')
        return
      }

      const result = await userController.toggleUserStatus({ id: employee.userId })
      if (result?.data?.code === 0) {
        message.success('操作成功')
        await fetchEmployeeList()
      } else {
        message.error('操作失败')
      }
    }
  } catch (error) {
    console.error('Failed to toggle user status:', error)
    message.error('操作失败')
  }
}

// 身份证号输入时自动推断性别
const handleIdCardBlur = () => {
  if (formState.idCardNumber) {
    const inferredGender = inferGenderFromIdCard(formState.idCardNumber)
    if (inferredGender) {
      formState.gender = inferredGender
    }
  }
}

// 保存员工信息
const handleSave = async () => {
  try {
    await employeeFormRef.value?.validate()

    // 根据身份证号自动推断性别（如果还没有性别）
    if (formState.idCardNumber && !formState.gender) {
      const inferredGender = inferGenderFromIdCard(formState.idCardNumber)
      if (inferredGender) {
        formState.gender = inferredGender
      } else {
        message.error('无法从身份证号推断性别，请检查身份证号格式')
        return
      }
    }

    // 系统管理员可以选择公司（可选），非系统管理员使用当前公司ID
    let targetCompanyId: number | undefined
    if (isSystemAdmin()) {
      // 管理员可以选择公司，也可以不选择（为未加入公司的员工创建账户）
      targetCompanyId = formState.companyId
    } else {
      // 非管理员必须使用当前公司ID
      targetCompanyId = getCurrentCompanyId()
      if (!targetCompanyId) {
        message.error('无法获取当前公司信息')
        return
      }
    }

    // 新增员工
    const createPayload: API.EmployeeCreateRequest = {
      name: formState.name,
      gender: formState.gender, // 由身份证号推断
      phone: formState.phone || undefined,
      email: formState.email || undefined,
      idCardNumber: formState.idCardNumber || undefined,
      departmentId: formState.departmentId || undefined, // 管理员可以不选择部门
      companyId: targetCompanyId || undefined, // 管理员可以不选择公司
    }

    const result = await employeeController.employeeCreate(createPayload)

    if (result?.data?.code === 0) {
      message.success('员工创建成功')
      modalVisible.value = false
      await fetchEmployeeList()
    } else {
      message.error(result?.data?.message || '员工创建失败')
    }
  } catch (error) {
    console.error('Save error:', error)
  }
}

// 关闭对话框
const handleModalCancel = () => {
  modalVisible.value = false
}

// 搜索
const handleSearch = () => {
  searchParams.pageNum = 1
  fetchEmployeeList()
}

// 重置搜索
const handleReset = () => {
  searchParams.name = ''
  searchParams.departmentId = undefined
  searchParams.companyId = undefined
  searchParams.sortField = 'create_time'
  searchParams.sortOrder = 'descend'
  searchParams.pageNum = 1
  
  // 重置部门列表
  if (isSystemAdmin()) {
    departmentOptions.value = []
  } else {
    // 非系统管理员重新加载当前公司部门
    fetchDepartmentOptions()
  }
  
  fetchEmployeeList()
}

// 分页和排序改变
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  searchParams.pageNum = pagination.current
  searchParams.pageSize = pagination.pageSize

  // 处理排序
  if (sorter.field) {
    searchParams.sortField = sorter.field
    searchParams.sortOrder = sorter.order || 'descend'
  }

  fetchEmployeeList()
}

// 监听公司选择变化（新增员工表单用）
const handleCompanyChange = (companyId: number | undefined) => {
  if (companyId) {
    // 清空之前选择的部门
    formState.departmentId = undefined
    // 加载选中公司的部门列表
    fetchDepartmentOptions(companyId)
  } else {
    // 清空部门列表
    departmentOptions.value = []
    formState.departmentId = undefined
  }
}

// 监听搜索区公司选择变化（搜索用）
const handleSearchCompanyChange = (companyId: number | undefined) => {
  if (companyId) {
    // 清空之前选择的部门
    searchParams.departmentId = undefined
    // 加载选中公司的部门列表
    fetchDepartmentOptions(companyId)
  } else {
    // 清空部门列表
    departmentOptions.value = []
    searchParams.departmentId = undefined
    // 如果是系统管理员且公司被清空，重新加载所有公司的部门
    if (isSystemAdmin()) {
      departmentOptions.value = []
    }
  }
}

// 公司搜索过滤函数
const filterCompanyOption = (input: string, option: any) => {
  const label = option.label?.toString().toLowerCase() || ''
  return label.includes(input.toLowerCase())
}

// 批量导入员工
const handleBatchImport = () => {
  importModalVisible.value = true
  fileList.value = []
  importResult.value = null
  importResultVisible.value = false
}

// 上传文件前的验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const fileName = file.name.toLowerCase()
  const isExcel = fileName.endsWith('.xlsx') || fileName.endsWith('.xls')
  if (!isExcel) {
    message.error('只能上传 .xlsx 或 .xls 格式的Excel文件！')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB！')
    return false
  }
  return false // 阻止自动上传，手动控制
}

// 处理文件变化
const handleFileChange = (info: { file: UploadFile; fileList: UploadFile[] }) => {
  fileList.value = info.fileList
}

// 执行批量导入
const executeBatchImport = async () => {
  if (fileList.value.length === 0) {
    message.error('请先选择要导入的Excel文件')
    return
  }

  const file = fileList.value[0].originFileObj
  if (!file) {
    message.error('文件不存在')
    return
  }

  try {
    importLoading.value = true
    const formData = new FormData()
    formData.append('file', file)

    const result = await employeeController.batchImportEmployees(formData)
    
    if (result?.data?.code === 0 && result.data.data) {
      importResult.value = result.data.data
      importResultVisible.value = true
      
      const { totalRows, successCount, failCount } = result.data.data
      message.success(`导入完成！共导入 ${totalRows} 条，成功 ${successCount} 条，失败 ${failCount} 条`)
      
      // 刷新员工列表
      await fetchEmployeeList()
    } else {
      message.error(result?.data?.message || '批量导入失败')
    }
  } catch (error: any) {
    console.error('批量导入失败:', error)
    message.error(error?.response?.data?.message || '批量导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

// 关闭导入结果弹窗
const closeImportResult = () => {
  importResultVisible.value = false
  importModalVisible.value = false
  fileList.value = []
  importResult.value = null
}

onMounted(() => {
  fetchEmployeeList()
  
  // 系统管理员需要加载公司列表（用于搜索）
  if (isSystemAdmin()) {
    fetchCompanyOptions()
  } else {
    // 非系统管理员才预加载部门列表
    fetchDepartmentOptions()
  }
})
</script>

<template>
  <div class="employee-management">
    <a-card class="search-card">
      <template #title>员工管理</template>
      <a-row :gutter="[16, 16]" :wrap="false" class="search-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input
            v-model:value="searchParams.name"
            placeholder="请输入员工姓名"
            allow-clear
            class="filter-input"
          />
        </a-col>
        <a-col v-if="isSystemAdmin()" :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="searchParams.companyId"
            placeholder="请选择或搜索公司"
            allow-clear
            show-search
            :filter-option="filterCompanyOption"
            :options="companyOptions"
            :loading="companyLoading"
            class="filter-input"
            @change="handleSearchCompanyChange"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="searchParams.departmentId"
            placeholder="请选择部门"
            allow-clear
            :options="departmentOptions"
            :loading="departmentLoading"
            :disabled="isSystemAdmin() && !searchParams.companyId"
            class="filter-input"
          />
        </a-col>
        <a-col :xs="24" :sm="24" :md="6">
          <a-space class="filter-actions">
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="openAddModal">新增员工</a-button>
            <a-button type="default" @click="handleBatchImport">
              <template #icon><UploadOutlined /></template>
              批量导入
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data-source="employeeList"
        :loading="tableLoading"
        :pagination="{ current: searchParams.pageNum, pageSize: searchParams.pageSize, total }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar
              v-if="record.photoUrl"
              :src="record.photoUrl"
              :size="64"
              shape="square"
              style="width: 48px; height: 64px; border-radius: 4px; object-fit: cover"
            />
            <a-avatar
              v-else
              :size="64"
              shape="square"
              style="width: 48px; height: 64px; border-radius: 4px; background-color: #1890ff; line-height: 64px"
            >
              {{ record.name?.charAt(0) }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              查看
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 批量导入弹窗 -->
    <a-modal
      v-model:open="importModalVisible"
      title="批量导入员工"
      :width="800"
      :confirm-loading="importLoading"
      @ok="executeBatchImport"
      @cancel="closeImportResult"
    >
      <div style="margin-bottom: 16px">
        <p style="margin-bottom: 8px">
          <strong>导入说明：</strong>
        </p>
        <ul style="padding-left: 20px; margin-bottom: 16px">
          <li>请上传 .xlsx 或 .xls 格式的Excel文件</li>
          <li>Excel文件第一行为表头，必须包含：姓名、身份证号、部门名、手机号、邮箱</li>
          <li>从第二行开始为数据行</li>
          <li>部门名为空或不存在时，员工将不分配部门</li>
        </ul>
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :max-count="1"
          accept=".xlsx,.xls"
          @change="handleFileChange"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            选择Excel文件
          </a-button>
        </a-upload>
      </div>
    </a-modal>

    <!-- 导入结果弹窗 -->
    <a-modal
      v-model:open="importResultVisible"
      title="导入结果"
      :width="900"
      :footer="null"
      @cancel="closeImportResult"
    >
      <div v-if="importResult">
        <a-alert
          :message="`导入完成！共导入 ${importResult.totalRows} 条，成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
          :type="importResult.failCount === 0 ? 'success' : 'warning'"
          style="margin-bottom: 16px"
        />
        
        <a-tabs v-if="importResult.failCount > 0">
          <a-tab-pane key="fail" tab="失败记录">
            <a-table
              :columns="[
                { title: '行号', dataIndex: 'rowNumber', key: 'rowNumber', width: 80 },
                { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
                { title: '身份证号', dataIndex: 'idCardNumber', key: 'idCardNumber', width: 180 },
                { title: '失败原因', dataIndex: 'reason', key: 'reason' },
              ]"
              :data-source="importResult.failItems || []"
              :pagination="{ pageSize: 10 }"
              size="small"
            />
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>

    <!-- 新增员工表单 -->
    <a-modal
      v-model:open="modalVisible"
      title="新增员工"
      ok-text="保存"
      cancel-text="取消"
      @ok="handleSave"
      @cancel="handleModalCancel"
    >
      <a-form ref="employeeFormRef" :model="formState" :rules="formRules" layout="vertical">
        <!-- 系统管理员可以选择公司（可选） -->
        <a-form-item v-if="isSystemAdmin()" label="所属公司" name="companyId">
          <a-select
            v-model:value="formState.companyId"
            :options="companyOptions"
            placeholder="请选择所属公司（可选，可为未加入公司的员工创建账户）"
            :loading="companyLoading"
            allow-clear
            show-search
            :filter-option="filterCompanyOption"
            @change="handleCompanyChange"
          />
        </a-form-item>

        <a-form-item label="姓名" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入员工姓名" />
        </a-form-item>

        <a-form-item label="身份证号" name="idCardNumber">
          <a-input
            v-model:value="formState.idCardNumber"
            placeholder="请输入18位身份证号"
            maxlength="18"
            @blur="handleIdCardBlur"
          />
        </a-form-item>

        <a-form-item label="部门" name="departmentId">
          <a-select
            v-model:value="formState.departmentId"
            :options="departmentOptions"
            :placeholder="isSystemAdmin() ? '请选择部门（可选）' : '请选择部门'"
            :loading="departmentLoading"
            :disabled="isSystemAdmin() && !formState.companyId"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
        </a-form-item>

        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" placeholder="请输入邮箱地址" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.employee-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.filter-input {
  width: 100%;
}

.filter-actions {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
}

.search-row {
  flex-wrap: nowrap !important;
  align-items: center;
}

@media (max-width: 768px) {
  .search-row {
    flex-wrap: wrap !important;
  }
}
</style>


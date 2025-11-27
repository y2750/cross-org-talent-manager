<script setup lang="ts">
import { onMounted, reactive, ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import * as userController from '@/api/userController'
import * as companyController from '@/api/companyController'
import { useRole } from '@/composables/useRole'
import { useUserStore } from '@/stores/userStore'
import type { FormInstance } from 'ant-design-vue'

const { canManageUsers, canViewCompanyHRs, isCompanyAdmin } = useRole()
const userStore = useUserStore()

// 应用于公司管理员的默认筛选条件
const applyCompanyAdminFilters = () => {
  if (isCompanyAdmin()) {
    searchParams.userRole = 'hr'
    searchParams.companyId = userStore.userInfo?.companyId
  } else {
    searchParams.companyId = undefined
  }
}

interface UserTableData {
  key: string
  id: number
  username: string
  nickname: string
  userRole: string
  companyId?: number
  isDelete?: number
  createTime: string
}

const userFormRef = ref<FormInstance>()
const tableLoading = ref(false)
const modalVisible = ref(false)
const isEditMode = ref(false)

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  nickname: '',
  userRole: '',
  sortField: 'createTime',
  sortOrder: 'descend',
  companyId: undefined as number | undefined,
})

const userList = ref<UserTableData[]>([])
const total = ref(0)
const companyOptions = ref<{ label: string; value: number }[]>([])
const companyLoading = ref(false)

const formState = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  checkPassword: '',
  nickname: '',
  userRole: 'hr',
  companyId: undefined as number | undefined,
})

// 根据用户角色动态生成角色选项
const userRoleOptions = computed(() => {
  if (isCompanyAdmin()) {
    // 公司管理员只能添加HR
    return [{ label: 'HR', value: 'hr' }]
  } else {
    // 系统管理员可以添加所有角色
    return [
      { label: '系统管理员', value: 'admin' },
      { label: '公司管理员', value: 'company_admin' },
      { label: 'HR', value: 'hr' },
      { label: '员工', value: 'employee' },
    ]
  }
})

const userRoleCreationOptions = computed(() =>
  userRoleOptions.value.filter((option) => option.value !== 'employee'),
)

// 判断当前编辑的用户是否是员工
const isEditingEmployee = computed(() => isEditMode.value && formState.userRole === 'employee')

// 编辑时的角色选项：如果是员工，包含员工选项；否则不包含
const userRoleEditOptions = computed(() => {
  if (isEditingEmployee.value) {
    return userRoleOptions.value
  }
  return userRoleCreationOptions.value
})

const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: '昵称',
    dataIndex: 'nickname',
    width: 120,
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 120,
    customRender: ({ text }: { text: string }) => {
      const roleMap: Record<string, string> = {
        admin: '系统管理员',
        employee_admin: '公司管理员',
        company_admin: '公司管理员',
        hr: 'HR',
        employee: '员工',
      }
      return roleMap[text] || text
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
    sorter: true,
    sortDirections: ['ascend', 'descend'],
    defaultSortOrder: 'descend',
  },
  {
    title: '操作',
    width: 200,
    key: 'operation',
    align: 'center' as const,
  },
]

const needsCompanySelection = (role: string) =>
  ['hr', 'employee_admin', 'company_admin'].includes(role)

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
      validator: (_: any, value: string) => {
        if (isEditMode.value && !value) {
          return Promise.resolve()
        }
        if (!isEditMode.value && !value) {
          return Promise.reject(new Error('请输入密码'))
        }
        if (value && value.length < 6) {
          return Promise.reject(new Error('密码长度不少于 6 个字符'))
        }
        return Promise.resolve()
      },
    },
  ],
  checkPassword: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur',
      validator: (_: any, value: string) => {
        if (!isEditMode.value && !value) {
          return Promise.reject(new Error('请确认密码'))
        }
        if (value !== formState.password) {
          return Promise.reject(new Error('两次输入的密码不一致'))
        }
        return Promise.resolve()
      },
    },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  companyId: [
    {
      validator: (_: any, value: number | undefined) => {
        if (needsCompanySelection(formState.userRole) && !value) {
          return Promise.reject(new Error('请选择所属公司'))
        }
        return Promise.resolve()
      },
      trigger: 'change',
    },
  ],
}

const shouldShowCompanySelect = computed(() => needsCompanySelection(formState.userRole))

const fetchCompanyOptions = async () => {
  try {
    companyLoading.value = true
    const response = await companyController.listCompanyVoByPage({
      pageNum: 1,
      pageSize: 1000,
      sortField: 'createTime',
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

const ensureCompanyOptionsLoaded = async () => {
  if (companyOptions.value.length || companyLoading.value) {
    return
  }
  await fetchCompanyOptions()
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    tableLoading.value = true
    console.log('[fetchUserList] Fetching users with params:', searchParams)
    const result = await userController.listUserVoByPage({
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      username: searchParams.username || undefined,
      nickname: searchParams.nickname || undefined,
      userRole: searchParams.userRole || undefined,
      companyId: searchParams.companyId,
      sortField: searchParams.sortField,
      sortOrder: searchParams.sortOrder,
    })
    console.log('[fetchUserList] API Response data:', result?.data?.data)

    if (result?.data?.code === 0 && result.data.data) {
      const pageData = result.data.data
      // 根据用户角色过滤用户列表
      let filteredRecords = (pageData.records || []).filter(
        (user: any) => user.id !== userStore.userInfo?.id,
      )

      // 如果是公司管理员，只显示同公司的HR用户
      if (isCompanyAdmin()) {
        filteredRecords = filteredRecords.filter(
          (user: any) => user.userRole === 'hr' && user.companyId === userStore.userInfo?.companyId,
        )
      }
      userList.value = filteredRecords.map((user: any, index: number) => {
        const userData = {
          key: `${searchParams.pageNum}-${index}`,
          id: user.id || 0,
          username: user.username || '',
          nickname: user.nickname || '',
          userRole: user.userRole || 'employee',
          // 后端返回的 isDelete: 0=启用, 1=禁用
          isDelete: user.isDelete ?? 0,
          companyId: user.companyId,
          createTime: user.createTime || '',
        }
        console.log(
          `[fetchUserList] User ${user.username}: isDelete=${user.isDelete}, mapped to ${userData.isDelete}`,
        )
        return userData
      })
      console.log('[fetchUserList] Updated userList:', userList.value)
      // 由于前端过滤了当前用户，重新计算总数
      const originalTotal = pageData.totalRow || 0
      const isCurrentUserFiltered = (pageData.records || []).length > filteredRecords.length
      total.value = originalTotal - (isCurrentUserFiltered ? 1 : 0)
    }
  } catch (error) {
    console.error('Failed to fetch user list:', error)
    message.error('获取用户列表失败')
  } finally {
    tableLoading.value = false
  }
}

watch(
  () => userStore.userInfo?.companyId,
  () => {
    applyCompanyAdminFilters()
    fetchUserList()
  },
  { immediate: true },
)

watch(
  () => formState.userRole,
  async (role) => {
    if (needsCompanySelection(role)) {
      await ensureCompanyOptionsLoaded()
      if (isCompanyAdmin()) {
        formState.companyId = userStore.userInfo?.companyId
      }
    } else {
      formState.companyId = undefined
    }
  },
)

// 打开新增对话框
const openAddModal = async () => {
  isEditMode.value = false
  formState.id = undefined
  formState.username = ''
  formState.password = ''
  formState.checkPassword = ''
  formState.nickname = ''

  // 根据用户角色设置默认值
  if (isCompanyAdmin()) {
    // 公司管理员只能添加HR，并自动设置公司ID
    formState.userRole = 'hr'
    formState.companyId = userStore.userInfo?.companyId
  } else {
    // 系统管理员可以选择所有角色
    formState.userRole = 'hr'
    formState.companyId = undefined
  }

  if (needsCompanySelection(formState.userRole)) {
    await ensureCompanyOptionsLoaded()
  }

  modalVisible.value = true
}

// 打开编辑对话框
const openEditModal = async (record: UserTableData) => {
  isEditMode.value = true
  formState.id = record.id
  formState.username = record.username
  formState.password = ''
  formState.nickname = record.nickname
  formState.userRole = record.userRole
  formState.companyId = record.companyId
  if (needsCompanySelection(record.userRole)) {
    await ensureCompanyOptionsLoaded()
  }
  modalVisible.value = true
}

// 删除用户（禁用/启用）
const handleToggleUserStatus = async (userId: number) => {
  try {
    console.log('[handleToggleUserStatus] Starting toggle for userId:', userId)
    const result = await userController.toggleUserStatus({ id: userId })
    console.log('[handleToggleUserStatus] API Response:', result)

    if (result?.data?.code === 0) {
      message.success('操作成功')
      console.log('[handleToggleUserStatus] Success, fetching updated user list...')
      await fetchUserList()
    } else {
      message.error('操作失败')
    }
  } catch (error) {
    console.error('Failed to toggle user status:', error)
    message.error('操作出错')
  }
}

// 保存用户信息
const handleSave = async () => {
  try {
    await userFormRef.value?.validate()

    if (isEditMode.value) {
      // 编辑用户
      const updatePayload: API.UserUpdateRequest = {
        id: formState.id,
        nickname: formState.nickname,
        userRole: formState.userRole,
        companyId: needsCompanySelection(formState.userRole) ? formState.companyId : undefined,
      }
      const result = await userController.updateUser({
        ...updatePayload,
        username: formState.username,
      } as any)

      if (result?.data?.code === 0) {
        message.success('用户更新成功')
        modalVisible.value = false
        await fetchUserList()
      } else {
        message.error('用户更新失败')
      }
    } else {
      // 新增用户
      const registerPayload: API.UserRegisterRequest & { companyId?: number } = {
        username: formState.username,
        nickname: formState.nickname,
        userRole: formState.userRole,
        password: formState.password,
        checkPassword: formState.checkPassword,
      }
      if (formState.companyId !== undefined) {
        registerPayload.companyId = formState.companyId
      }
      const result = await userController.userRegister(registerPayload)

      if (result?.data?.code === 0) {
        message.success('用户创建成功')
        modalVisible.value = false
        await fetchUserList()
      } else {
        message.error('用户创建失败')
      }
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
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  searchParams.username = ''
  searchParams.nickname = ''
  searchParams.sortField = 'createTime'
  searchParams.sortOrder = 'descend'
  searchParams.pageNum = 1
  if (isCompanyAdmin()) {
    searchParams.userRole = 'hr'
    searchParams.companyId = userStore.userInfo?.companyId
  } else {
    searchParams.userRole = ''
    searchParams.companyId = undefined
  }
  fetchUserList()
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

  fetchUserList()
}

// 获取角色标签
const getRoleLabel = (role: string): string => {
  const roleMap: Record<string, string> = {
    admin: '系统管理员',
    employee_admin: '公司管理员',
    company_admin: '公司管理员',
    hr: 'HR',
    employee: '员工',
  }
  return roleMap[role] || role
}

// 判断是否可以操作用户
const canOperateUser = (record: UserTableData): boolean => {
  // 系统管理员可以操作所有用户
  if (userStore.userRole === 'admin') {
    return true
  }
  const companyAdminRoles = ['employee_admin', 'company_admin', 'companyAdmin']
  // 公司管理员只能操作本公司 HR
  if (
    companyAdminRoles.includes(userStore.userRole) &&
    record.userRole === 'hr' &&
    record.companyId === userStore.userInfo?.companyId
  ) {
    return true
  }
  return false
}

// 获取按钮文本
// 根据后端的 isDelete 字段判断按钮文案：isDelete=true 表示禁用（显示"启用"），isDelete=false 表示启用（显示"禁用"）
const getToggleButtonText = (record: any): string => {
  return record.isDelete ? '启用' : '禁用'
}

onMounted(() => {
  fetchUserList()
})
</script>

<template>
  <div class="user-management">
    <a-card class="search-card">
      <template #title>{{ isCompanyAdmin() ? 'HR管理' : '用户管理' }}</template>
      <a-row :gutter="[16, 16]" :wrap="false" class="search-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input
            v-model:value="searchParams.username"
            placeholder="请输入用户名"
            allow-clear
            class="filter-input"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-input
            v-model:value="searchParams.nickname"
            placeholder="请输入昵称"
            allow-clear
            class="filter-input"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="searchParams.userRole"
            placeholder="请选择角色"
            allow-clear
            :options="userRoleOptions"
            :disabled="isCompanyAdmin()"
            class="filter-input"
          />
        </a-col>
        <a-col :xs="24" :sm="24" :md="6">
          <a-space class="filter-actions" wrap>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="openAddModal">{{
              isCompanyAdmin() ? '新增HR' : '新增用户'
            }}</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="tableLoading"
        :pagination="{ current: searchParams.pageNum, pageSize: searchParams.pageSize, total }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userRole'">
            {{ getRoleLabel(record.userRole) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-space>
              <a-button type="link" size="small" @click="openEditModal(record)"> 编辑 </a-button>
              <a-popconfirm
                v-if="canOperateUser(record)"
                title="确认操作？"
                :description="`确定要${getToggleButtonText(record)}该用户吗？`"
                ok-text="确定"
                cancel-text="取消"
                @confirm="() => handleToggleUserStatus(record.id)"
              >
                <a-button type="link" :danger="!record.isDelete" size="small">
                  {{ getToggleButtonText(record) }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="
        isEditMode
          ? isCompanyAdmin()
            ? '编辑HR'
            : '编辑用户'
          : isCompanyAdmin()
            ? '新增HR'
            : '新增用户'
      "
      ok-text="保存"
      cancel-text="取消"
      @ok="handleSave"
      @cancel="handleModalCancel"
    >
      <a-form ref="userFormRef" :model="formState" :rules="formRules" layout="vertical">
        <a-form-item label="用户名" name="username">
          <a-input
            v-model:value="formState.username"
            placeholder="请输入用户名"
            :disabled="isEditMode"
          />
        </a-form-item>

        <a-form-item v-if="!isEditMode" label="密码" name="password">
          <a-input-password v-model:value="formState.password" placeholder="请输入密码" />
        </a-form-item>

        <a-form-item v-if="!isEditMode" label="确认密码" name="checkPassword">
          <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" />
        </a-form-item>

        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="formState.nickname" placeholder="请输入昵称" />
        </a-form-item>

        <a-form-item label="角色" name="userRole">
          <a-select
            v-model:value="formState.userRole"
            :options="isEditMode ? userRoleEditOptions : userRoleCreationOptions"
            placeholder="请选择角色"
            :disabled="isCompanyAdmin() || isEditingEmployee"
          />
        </a-form-item>

        <a-form-item v-if="shouldShowCompanySelect" label="所属公司" name="companyId">
          <a-select
            v-model:value="formState.companyId"
            :options="companyOptions"
            placeholder="请选择所属公司"
            :loading="companyLoading"
            allow-clear
            :disabled="isCompanyAdmin()"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

/* 将搜索区的输入和选择框宽度设置为当前的 2/5（约 40%），并为长文本添加省略号 */
.filter-input {
  width: 100%;
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
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

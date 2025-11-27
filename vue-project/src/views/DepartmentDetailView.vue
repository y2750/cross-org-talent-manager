<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import * as departmentController from '@/api/departmentController'
import * as employeeController from '@/api/employeeController'
import { useRole } from '@/composables/useRole'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const { isSystemAdmin } = useRole()
const userStore = useUserStore()

// 使用字符串类型，避免 JavaScript 数字精度丢失
const departmentId = ref<string>(String(route.params.id))
const loading = ref(false)

// 部门信息
const departmentInfo = ref<API.DepartmentVO | null>(null)

// 部门员工列表
const employeeList = ref<API.EmployeeVO[]>([])
const employeeLoading = ref(false)

// 批量移除相关
const selectedRemoveEmployeeIds = ref<string[]>([])

// 增加员工相关
const addEmployeeModalVisible = ref(false)
const addEmployeeLoading = ref(false)
const availableEmployeeList = ref<API.EmployeeVO[]>([])
const selectedEmployeeIds = ref<string[]>([])
const employeeSearchParams = reactive({
  name: '',
  departmentId: undefined as string | undefined,
  showNoDepartment: false,
})

// 部门选项（用于筛选）
const departmentOptions = ref<{ label: string; value: string }[]>([])

// 判断是否为主管（使用函数，确保响应式）
const isLeader = (employee: API.EmployeeVO): boolean => {
  if (!departmentInfo.value?.leaderId) {
    return false
  }
  
  const employeeIdStr = String(employee.id)
  const leaderIdStr = String(departmentInfo.value.leaderId)
  const isMatch = employeeIdStr === leaderIdStr
  
  return isMatch
}


// 获取部门详情
const fetchDepartmentDetail = async () => {
  try {
    loading.value = true
    const result = await departmentController.getDepartmentVoById({ id: departmentId.value as any })
    if (result?.data?.code === 0 && result.data.data) {
      departmentInfo.value = result.data.data
    } else {
      message.error('获取部门信息失败')
      router.back()
    }
  } catch (error) {
    console.error('Failed to fetch department detail:', error)
    message.error('获取部门信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 获取部门员工列表
const fetchEmployeeList = async () => {
  try {
    employeeLoading.value = true
    const result = await employeeController.listEmployeeVoByPage({
      pageNum: 1,
      pageSize: 1000,
      companyId: departmentInfo.value?.companyId as any,
      departmentId: departmentId.value as any,
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const employees = result.data.data.records || []
      // 确保只显示当前公司的员工
      const currentCompanyId = departmentInfo.value?.companyId
      const filtered = currentCompanyId
        ? employees.filter((emp: API.EmployeeVO) => String(emp.companyId) === String(currentCompanyId))
        : employees
      
      // 如果有主管，将主管放在第一位
      if (departmentInfo.value?.leaderId) {
        const leaderId = String(departmentInfo.value.leaderId)
        const leaderIndex = filtered.findIndex((emp: API.EmployeeVO) => {
          // 确保类型匹配，都转换为字符串比较
          return String(emp.id) === leaderId
        })
        if (leaderIndex > -1 && filtered[leaderIndex]) {
          const leader = filtered[leaderIndex]
          filtered.splice(leaderIndex, 1)
          filtered.unshift(leader)
        }
      }
      
      employeeList.value = filtered
      
      // 更新主管姓名显示
      if (departmentInfo.value) {
        if (departmentInfo.value.leaderId) {
          const leaderIdStr = String(departmentInfo.value.leaderId)
          const leader = filtered.find((emp: API.EmployeeVO) => String(emp.id) === leaderIdStr)
          // 只有在员工列表中找到对应的主管时，才设置主管姓名
          if (leader && leader.name) {
            departmentInfo.value.leaderName = leader.name
          } else {
            // 如果leaderId存在但找不到对应的员工，设置为undefined，模板会显示"-"
            departmentInfo.value.leaderName = undefined
          }
        } else {
          // 如果没有leaderId，确保leaderName为undefined
          departmentInfo.value.leaderName = undefined
        }
      }
    }
  } catch (error) {
    console.error('Failed to fetch employee list:', error)
    message.error('获取员工列表失败')
  } finally {
    employeeLoading.value = false
  }
}

// 获取部门列表（用于筛选）
const fetchDepartmentOptions = async () => {
  try {
    const result = await departmentController.listDepartmentVoByPage({
      pageNum: 1,
      pageSize: 1000,
      companyId: departmentInfo.value?.companyId as any,
    } as any)
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      departmentOptions.value = records
        .filter((dept: API.DepartmentVO) => dept.id && String(dept.id) !== departmentId.value)
        .map((dept: API.DepartmentVO) => ({
          label: dept.name || `部门${dept.id}`,
          value: String(dept.id),
        }))
    }
  } catch (error) {
    console.error('Failed to fetch department list:', error)
  }
}

// 移除员工部门（单个）
const handleRemoveEmployee = async (employee: API.EmployeeVO) => {
  await removeEmployeesFromDepartment([String(employee.id)])
}

// 批量移除员工部门
const handleBatchRemoveEmployees = async () => {
  if (selectedRemoveEmployeeIds.value.length === 0) {
    message.warning('请至少选择一个员工')
    return
  }
  
  await removeEmployeesFromDepartment(selectedRemoveEmployeeIds.value)
  selectedRemoveEmployeeIds.value = []
}

// 移除员工部门的通用方法
const removeEmployeesFromDepartment = async (employeeIds: string[]) => {
  try {
    // 使用批量移出部门接口
    // 直接发送字符串数组，后端会自动将字符串转换为 Long，避免精度丢失
    const employeeIdStrings = employeeIds.map(id => String(id))
    const result = await employeeController.batchRemoveFromDepartment(employeeIdStrings as any)
    
    if (result?.data?.code === 0) {
      message.success(`成功移出 ${result.data.data || employeeIds.length} 名员工`)
      await fetchEmployeeList()
    } else {
      message.error(result?.data?.message || '移出失败')
    }
  } catch (error: any) {
    console.error('Failed to remove employees:', error)
    message.error(error?.response?.data?.message || error?.message || '移出失败')
  }
}

// 打开增加员工弹窗
const openAddEmployeeModal = async () => {
  addEmployeeModalVisible.value = true
  selectedEmployeeIds.value = []
  employeeSearchParams.name = ''
  employeeSearchParams.departmentId = undefined
  employeeSearchParams.showNoDepartment = false
  await fetchDepartmentOptions()
  await fetchAvailableEmployees()
}

// 获取可添加的员工列表（除本部门外的员工）
const fetchAvailableEmployees = async () => {
  try {
    addEmployeeLoading.value = true
    const params: any = {
      pageNum: 1,
      pageSize: 1000,
      companyId: departmentInfo.value?.companyId as any,
    }
    
    // 如果选择了特定部门，则只获取该部门的员工
    if (employeeSearchParams.departmentId) {
      params.departmentId = employeeSearchParams.departmentId as any
    } else if (employeeSearchParams.showNoDepartment) {
      // 如果选择查看没有部门的员工，不传 departmentId，然后前端过滤
      // 实际上后端可能不支持这个，我们需要获取所有员工然后前端过滤
    }
    
    // 如果输入了姓名，添加姓名搜索
    if (employeeSearchParams.name) {
      params.name = employeeSearchParams.name
    }
    
    const result = await employeeController.listEmployeeVoByPage(params)
    if (result?.data?.code === 0 && result.data.data) {
      let employees = result.data.data.records || []
      
      // 确保只显示当前公司的员工
      const currentCompanyId = departmentInfo.value?.companyId
      if (currentCompanyId) {
        employees = employees.filter((emp: API.EmployeeVO) => String(emp.companyId) === String(currentCompanyId))
      }
      
      // 排除本部门的员工
      employees = employees.filter((emp: API.EmployeeVO) => String(emp.departmentId) !== departmentId.value)
      
      // 如果选择查看没有部门的员工，进一步过滤
      if (employeeSearchParams.showNoDepartment && !employeeSearchParams.departmentId) {
        employees = employees.filter((emp: API.EmployeeVO) => !emp.departmentId)
      }
      
      availableEmployeeList.value = employees
    }
  } catch (error) {
    console.error('Failed to fetch available employees:', error)
    message.error('获取员工列表失败')
  } finally {
    addEmployeeLoading.value = false
  }
}

// 搜索员工
const handleSearchEmployees = () => {
  fetchAvailableEmployees()
}

// 重置搜索
const handleResetSearch = () => {
  employeeSearchParams.name = ''
  employeeSearchParams.departmentId = undefined
  employeeSearchParams.showNoDepartment = false
  fetchAvailableEmployees()
}

// 添加员工到部门
const handleAddEmployees = async () => {
  if (selectedEmployeeIds.value.length === 0) {
    message.warning('请至少选择一个员工')
    return
  }
  
  try {
    addEmployeeLoading.value = true
    
    // 使用批量添加到部门接口
    // 直接发送字符串数组，后端会自动将字符串转换为 Long，避免精度丢失
    const employeeIdStrings = selectedEmployeeIds.value.map(id => String(id))
    const result = await employeeController.batchAddToDepartment({
      employeeIds: employeeIdStrings,
      departmentId: departmentId.value,
    } as any)
    
    if (result?.data?.code === 0) {
      message.success(`成功添加 ${result.data.data || selectedEmployeeIds.value.length} 名员工`)
      addEmployeeModalVisible.value = false
      selectedEmployeeIds.value = []
      await fetchEmployeeList()
    } else {
      message.error(result?.data?.message || '添加失败')
    }
  } catch (error: any) {
    console.error('Failed to add employees:', error)
    message.error(error?.response?.data?.message || error?.message || '添加失败')
  } finally {
    addEmployeeLoading.value = false
  }
}

// 返回列表
const handleBack = () => {
  // 如果是从公司详情页跳转过来的，返回公司详情页
  if (route.query.from === 'company') {
    router.push(`/companies/${route.query.companyId}`)
  } else {
    router.back()
  }
}

onMounted(async () => {
  await fetchDepartmentDetail()
  await fetchEmployeeList()
})
</script>

<template>
  <div class="department-detail">
    <a-card>
      <!-- 页面头部 -->
      <template #title>
        <a-space>
          <a-button type="text" @click="handleBack">
            <template #icon>←</template>
          </a-button>
          <span>部门详情</span>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <!-- 部门基本信息 -->
        <div style="margin-bottom: 24px">
          <a-descriptions bordered :column="2">
            <a-descriptions-item label="部门名称">{{ departmentInfo?.name || '-' }}</a-descriptions-item>
            <a-descriptions-item label="部门状态">
              <a-tag :color="(departmentInfo as any)?.isDelete ? 'red' : 'green'">
                {{ (departmentInfo as any)?.isDelete ? '已弃用' : '启用中' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="主管姓名">
              {{ departmentInfo?.leaderName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="员工人数">{{ employeeList.length }}</a-descriptions-item>
          </a-descriptions>
        </div>

        <a-divider />

        <!-- 部门员工列表 -->
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
            <h3>部门员工</h3>
            <a-space>
              <a-popconfirm
                v-if="selectedRemoveEmployeeIds.length > 0"
                title="确认批量移出？"
                description="确定要将选中的员工移出本部门吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleBatchRemoveEmployees"
              >
                <a-button danger>批量移出 ({{ selectedRemoveEmployeeIds.length }})</a-button>
              </a-popconfirm>
              <a-button type="primary" @click="openAddEmployeeModal">增加部门员工</a-button>
            </a-space>
          </div>

          <a-table
            :columns="[
              { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
              { title: '性别', dataIndex: 'gender', width: 80 },
              { title: '电话', dataIndex: 'phone', width: 150 },
              { title: '邮箱', dataIndex: 'email', width: 200 },
              {
                title: '操作',
                key: 'operation',
                width: 120,
                align: 'center',
              },
            ]"
            :data-source="employeeList"
            :loading="employeeLoading"
            :pagination="false"
            row-key="id"
            :row-selection="{
              selectedRowKeys: selectedRemoveEmployeeIds,
              onChange: (keys: (string | number)[]) => {
                selectedRemoveEmployeeIds = keys.map(k => String(k))
              },
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name' || column.dataIndex === 'name'">
                <a-space>
                  <span>{{ record.name || '-' }}</span>
                  <!-- 直接使用条件判断，确保响应式 -->
                  <a-tag
                    v-if="departmentInfo && departmentInfo.leaderId && String(record.id) === String(departmentInfo.leaderId)"
                    color="blue"
                    style="margin-left: 4px"
                  >
                    主管
                  </a-tag>
                </a-space>
              </template>
              <template v-else-if="column.key === 'operation'">
                <a-popconfirm
                  title="确认移出？"
                  description="确定要将该员工移出本部门吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleRemoveEmployee(record)"
                >
                  <a-button type="link" size="small" danger>移出</a-button>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
        </div>
      </a-spin>
    </a-card>

    <!-- 增加部门员工弹窗 -->
    <a-modal
      v-model:open="addEmployeeModalVisible"
      title="增加部门员工"
      ok-text="确定"
      cancel-text="取消"
      width="800px"
      :maskClosable="false"
      :confirm-loading="addEmployeeLoading"
      @ok="handleAddEmployees"
    >
      <!-- 搜索区域 -->
      <div style="margin-bottom: 16px">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-input
              v-model:value="employeeSearchParams.name"
              placeholder="按姓名搜索"
              allow-clear
              @pressEnter="handleSearchEmployees"
            />
          </a-col>
          <a-col :span="8">
            <a-select
              v-model:value="employeeSearchParams.departmentId"
              placeholder="选择部门"
              allow-clear
              style="width: 100%"
              :options="departmentOptions"
              @change="handleSearchEmployees"
            />
          </a-col>
          <a-col :span="8">
            <a-space>
              <a-checkbox v-model:checked="employeeSearchParams.showNoDepartment" @change="handleSearchEmployees">
                查看没有部门的员工
              </a-checkbox>
              <a-button type="primary" @click="handleSearchEmployees">查询</a-button>
              <a-button @click="handleResetSearch">重置</a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 员工列表 -->
      <a-table
        :columns="[
          { title: '姓名', dataIndex: 'name', width: 120 },
          { title: '性别', dataIndex: 'gender', width: 80 },
          { title: '电话', dataIndex: 'phone', width: 150 },
          { title: '邮箱', dataIndex: 'email', width: 200 },
          { title: '当前部门', dataIndex: 'departmentName', width: 150 },
        ]"
        :data-source="availableEmployeeList"
        :loading="addEmployeeLoading"
        :pagination="false"
        row-key="id"
        :row-selection="{
          selectedRowKeys: selectedEmployeeIds,
          onChange: (keys: (string | number)[]) => {
            selectedEmployeeIds = keys.map(k => String(k))
          },
        }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'departmentName'">
            {{ record.departmentName || '无部门' }}
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<style scoped>
.department-detail {
  padding: 20px;
}
</style>


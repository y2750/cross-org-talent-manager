<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import * as employeeController from '@/api/employeeController'
import * as companyController from '@/api/companyController'
import * as departmentController from '@/api/departmentController'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

// 当前员工信息
const myEmployeeInfo = ref<API.EmployeeVO | null>(null)
const employeeLoading = ref(false)

// 公司信息
const companyInfo = ref<API.CompanyVO | null>(null)
const companyLoading = ref(false)

// 部门信息
const departmentInfo = ref<API.DepartmentVO | null>(null)
const departmentLoading = ref(false)

// 部门同事列表（脱敏）
const colleagues = ref<API.EmployeeVO[]>([])
const colleaguesLoading = ref(false)

// 搜索和筛选
const searchName = ref('')
const selectedGender = ref<string | undefined>(undefined)

// 获取当前员工信息
const fetchMyEmployeeInfo = async () => {
  try {
    employeeLoading.value = true
    const result = await employeeController.getMyEmployeeVo()
    if (result?.data?.code === 0 && result.data.data) {
      myEmployeeInfo.value = result.data.data
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

// 获取公司信息
const fetchCompanyInfo = async () => {
  if (!myEmployeeInfo.value?.companyId) {
    return
  }
  try {
    companyLoading.value = true
    const result = await companyController.getCompanyVoById({
      id: myEmployeeInfo.value.companyId as any,
    })
    if (result?.data?.code === 0 && result.data.data) {
      companyInfo.value = result.data.data
    }
  } catch (error) {
    console.error('Failed to fetch company info:', error)
  } finally {
    companyLoading.value = false
  }
}

// 获取部门信息
const fetchDepartmentInfo = async () => {
  if (!myEmployeeInfo.value?.departmentId) {
    return
  }
  try {
    departmentLoading.value = true
    const result = await departmentController.getDepartmentVoById({
      id: myEmployeeInfo.value.departmentId as any,
    })
    if (result?.data?.code === 0 && result.data.data) {
      departmentInfo.value = result.data.data
      // 如果部门有 leaderId，从同事列表中查找主管并设置 leaderName
      if (departmentInfo.value.leaderId && colleagues.value.length > 0) {
        const leader = colleagues.value.find(
          (colleague) => colleague.id && String(colleague.id) === String(departmentInfo.value.leaderId)
        )
        if (leader && leader.name) {
          departmentInfo.value.leaderName = leader.name
        }
      }
    }
  } catch (error) {
    console.error('Failed to fetch department info:', error)
  } finally {
    departmentLoading.value = false
  }
}

// 获取部门同事列表（脱敏）
const fetchColleagues = async () => {
  try {
    colleaguesLoading.value = true
    const result = await employeeController.getDepartmentColleagues()
    if (result?.data?.code === 0 && result.data.data) {
      let colleagueList = result.data.data || []
      
      // 主管默认展示在第一位
      if (departmentInfo.value?.leaderId) {
        const leaderIndex = colleagueList.findIndex(
          (colleague) => colleague.id && String(colleague.id) === String(departmentInfo.value.leaderId)
        )
        if (leaderIndex > -1) {
          const leader = colleagueList[leaderIndex]
          colleagueList.splice(leaderIndex, 1)
          colleagueList.unshift(leader)
        }
      }
      
      colleagues.value = colleagueList
      
      // 更新部门主管姓名
      if (departmentInfo.value?.leaderId && colleagues.value.length > 0) {
        const leader = colleagues.value.find(
          (colleague) => colleague.id && String(colleague.id) === String(departmentInfo.value.leaderId)
        )
        if (leader && leader.name) {
          departmentInfo.value.leaderName = leader.name
        }
      }
    } else {
      message.error('获取部门同事列表失败')
    }
  } catch (error) {
    console.error('Failed to fetch colleagues:', error)
    message.error('获取部门同事列表失败')
  } finally {
    colleaguesLoading.value = false
  }
}

// 判断是否为主管
const isLeader = (employee: API.EmployeeVO): boolean => {
  if (!departmentInfo.value?.leaderId || !employee.id) {
    return false
  }
  return String(employee.id) === String(departmentInfo.value.leaderId)
}

// 脱敏处理联系方式
const maskContact = (contact: string | undefined, type: 'phone' | 'email'): string => {
  if (!contact) return '-'
  
  if (type === 'phone' && contact.length > 7) {
    return contact.substring(0, 3) + '****' + contact.substring(contact.length - 4)
  }
  
  if (type === 'email' && contact.includes('@')) {
    const atIndex = contact.indexOf('@')
    if (atIndex > 0) {
      return contact.substring(0, 1) + '***' + contact.substring(atIndex)
    }
  }
  
  return contact
}

// 过滤后的同事列表
const filteredColleagues = computed(() => {
  let filtered = [...colleagues.value]
  
  // 按姓名搜索
  if (searchName.value) {
    const searchLower = searchName.value.toLowerCase()
    filtered = filtered.filter((colleague) => 
      colleague.name?.toLowerCase().includes(searchLower)
    )
  }
  
  // 按性别筛选
  if (selectedGender.value) {
    filtered = filtered.filter((colleague) => colleague.gender === selectedGender.value)
  }
  
  return filtered
})

onMounted(async () => {
  await fetchMyEmployeeInfo()
  if (myEmployeeInfo.value) {
    await Promise.all([
      fetchCompanyInfo(),
      fetchDepartmentInfo(),
    ])
    // 先获取部门信息，再获取同事列表（因为需要部门信息来确定主管）
    await fetchColleagues()
  }
})
</script>

<template>
  <div class="my-company-view">
    <a-spin :spinning="employeeLoading">
      <a-card>
        <template #title>
          <h2>我的公司</h2>
        </template>

        <!-- 公司信息 -->
        <a-spin :spinning="companyLoading">
          <div style="margin-bottom: 24px">
            <h3>公司信息</h3>
            <a-descriptions bordered :column="2" v-if="companyInfo">
              <a-descriptions-item label="公司名称">{{ companyInfo.name || '-' }}</a-descriptions-item>
              <a-descriptions-item label="联系人姓名">{{ companyInfo.contactPersonName || '-' }}</a-descriptions-item>
              <a-descriptions-item label="联系电话">{{ maskContact(companyInfo.phone, 'phone') }}</a-descriptions-item>
              <a-descriptions-item label="联系邮箱">{{ maskContact(companyInfo.email, 'email') }}</a-descriptions-item>
            </a-descriptions>
            <a-empty v-else description="暂无公司信息" />
          </div>
        </a-spin>

        <a-divider />

        <!-- 部门信息 -->
        <a-spin :spinning="departmentLoading">
          <div style="margin-bottom: 24px">
            <h3>我的部门</h3>
            <a-descriptions bordered :column="2" v-if="departmentInfo">
              <a-descriptions-item label="部门名称">{{ departmentInfo.name || '-' }}</a-descriptions-item>
              <a-descriptions-item label="部门状态">
                <a-tag :color="departmentInfo.isDelete ? 'red' : 'green'">
                  {{ departmentInfo.isDelete ? '已弃用' : '启用中' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="主管姓名">
                {{ departmentInfo.leaderName || '-' }}
              </a-descriptions-item>
            </a-descriptions>
            <a-empty v-else description="暂无部门信息" />
          </div>
        </a-spin>

        <a-divider />

        <!-- 部门同事 -->
        <div>
          <h3>部门同事</h3>
          <!-- 搜索和筛选 -->
          <div style="margin-bottom: 16px">
            <a-space>
              <a-input
                v-model:value="searchName"
                placeholder="按姓名搜索"
                allow-clear
                style="width: 200px"
              />
              <a-select
                v-model:value="selectedGender"
                placeholder="选择性别"
                allow-clear
                style="width: 120px"
              >
                <a-select-option value="男">男</a-select-option>
                <a-select-option value="女">女</a-select-option>
              </a-select>
            </a-space>
          </div>
          
          <a-spin :spinning="colleaguesLoading">
            <a-table
              :columns="[
                { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
                { title: '性别', dataIndex: 'gender', width: 80 },
                { title: '电话', dataIndex: 'phone', width: 150 },
                { title: '邮箱', dataIndex: 'email', width: 200 },
              ]"
              :data-source="filteredColleagues"
              :loading="colleaguesLoading"
              :pagination="false"
              row-key="id"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'name' || column.dataIndex === 'name'">
                  <a-space>
                    <span>{{ record.name || '-' }}</span>
                    <a-tag
                      v-if="isLeader(record)"
                      color="blue"
                      style="margin-left: 4px"
                    >
                      主管
                    </a-tag>
                  </a-space>
                </template>
                <template v-else-if="column.dataIndex === 'phone'">
                  {{ record.phone || '-' }}
                </template>
                <template v-else-if="column.dataIndex === 'email'">
                  {{ record.email || '-' }}
                </template>
              </template>
            </a-table>
            <a-empty v-if="!colleaguesLoading && filteredColleagues.length === 0" description="暂无部门同事" />
          </a-spin>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<style scoped>
.my-company-view {
  padding: 0;
}

.my-company-view h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}
</style>


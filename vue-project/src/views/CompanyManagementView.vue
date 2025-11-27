<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import * as companyController from '@/api/companyController'
import IndustrySelector from '@/components/IndustrySelector.vue'

const router = useRouter()

interface CompanyTableData {
  key: string
  id: number
  name: string
  contactPersonName?: string
  phone?: string
  email?: string
  industryCategory?: string
  industries?: string[]
  employeeCount?: number
  createTime: string
}

const tableLoading = ref(false)

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  industries: [] as string[],
  sortField: 'createTime',
  sortOrder: 'descend',
})

const companyList = ref<CompanyTableData[]>([])
const total = ref(0)

const columns = [
  {
    title: '公司名称',
    dataIndex: 'name',
    width: 140,
    ellipsis: true,
  },
  {
    title: '联系人',
    dataIndex: 'contactPersonName',
    width: 100,
  },
  {
    title: '联系电话',
    dataIndex: 'phone',
    width: 120,
  },
  {
    title: '行业大类',
    dataIndex: 'industryCategory',
    width: 110,
  },
  {
    title: '行业子类',
    key: 'industries',
    width: 160,
    ellipsis: true,
  },
  {
    title: '员工人数',
    key: 'employeeCount',
    dataIndex: 'employeeCount',
    width: 100,
    sorter: true,
    align: 'center' as const,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 110,
    sorter: true,
    sortDirections: ['ascend', 'descend'],
    defaultSortOrder: 'descend',
  },
  {
    title: '操作',
    width: 80,
    key: 'operation',
    align: 'center' as const,
    fixed: 'right' as const,
  },
]

// 格式化日期为年月日
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取公司列表
const fetchCompanyList = async () => {
  try {
    tableLoading.value = true
    const result = await companyController.listCompanyVoByPage({
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      name: searchParams.name || undefined,
      industries: searchParams.industries.length > 0 ? searchParams.industries : undefined,
      sortField: searchParams.sortField,
      sortOrder: searchParams.sortOrder,
    })

    if (result?.data?.code === 0 && result.data.data) {
      const pageData = result.data.data
      // 获取每个公司的员工数量
      const companiesWithCount = await Promise.all(
        (pageData.records || []).map(async (company: API.CompanyVO) => {
          let employeeCount = 0
          try {
            // 使用专门的统计API获取该公司的在职员工数量
            const countResult = await import('@/api/employeeController').then((module) =>
              module.countEmployees({
                companyId: company.id as number,
                status: true, // 只统计在职员工
              })
            )
            if (countResult?.data?.code === 0 && countResult.data.data !== undefined) {
              employeeCount = Number(countResult.data.data) || 0
            }
          } catch (error) {
            console.error('Failed to fetch employee count:', error)
          }
          return {
            ...company,
            employeeCount,
          }
        })
      )

      companyList.value = companiesWithCount.map((company: any, index: number) => ({
        key: `${searchParams.pageNum}-${index}`,
        id: company.id || 0,
        name: company.name || '',
        contactPersonName: company.contactPersonName || '',
        phone: company.phone || '',
        email: company.email || '',
        industryCategory: company.industryCategory || '',
        industries: company.industries || [],
        employeeCount: company.employeeCount || 0,
        createTime: formatDate(company.createTime || ''),
      }))
      total.value = pageData.totalRow || 0
    }
  } catch (error) {
    console.error('Failed to fetch company list:', error)
    message.error('获取公司列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 查看公司详情
const viewCompanyDetail = (record: CompanyTableData) => {
  router.push(`/companies/${record.id}`)
}

// 搜索
const handleSearch = () => {
  searchParams.pageNum = 1
  fetchCompanyList()
}

// 重置搜索
const handleReset = () => {
  searchParams.name = ''
  searchParams.industries = []
  searchParams.sortField = 'createTime'
  searchParams.sortOrder = 'descend'
  searchParams.pageNum = 1
  fetchCompanyList()
}

// 行业变化
const handleIndustryChange = () => {
  searchParams.pageNum = 1
  fetchCompanyList()
}

// 分页和排序改变
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  searchParams.pageNum = pagination.current
  searchParams.pageSize = pagination.pageSize

  // 处理排序
  if (sorter.field) {
    // 员工数量排序在前端处理
    if (sorter.field === 'employeeCount') {
      const sortedList = [...companyList.value].sort((a, b) => {
        if (sorter.order === 'ascend') {
          return (a.employeeCount || 0) - (b.employeeCount || 0)
        } else {
          return (b.employeeCount || 0) - (a.employeeCount || 0)
        }
      })
      companyList.value = sortedList
    } else {
      // 其他字段由后端排序
      searchParams.sortField = sorter.field
      searchParams.sortOrder = sorter.order || 'descend'
      fetchCompanyList()
    }
  } else {
    fetchCompanyList()
  }
}

onMounted(() => {
  fetchCompanyList()
})
</script>

<template>
  <div class="company-management">
    <a-card class="search-card">
      <template #title>公司管理</template>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="12">
          <a-input
            v-model:value="searchParams.name"
            placeholder="请输入公司名称"
            allow-clear
            class="filter-input"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="12">
          <IndustrySelector
            v-model="searchParams.industries"
            placeholder="公司行业（可跨类别多选）"
            :max-tag-count="3"
            @change="handleIndustryChange"
          />
        </a-col>
      </a-row>
      <a-row style="margin-top: 16px">
        <a-col :span="24">
          <a-space class="filter-actions" wrap>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data-source="companyList"
        :loading="tableLoading"
        :pagination="{ current: searchParams.pageNum, pageSize: searchParams.pageSize, total }"
        :scroll="{ x: 1100 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'industries'">
            <a-tooltip v-if="record.industries && record.industries.length > 0">
              <template #title>{{ record.industries.join('、') }}</template>
              <span>{{ record.industries.slice(0, 2).join('、') }}
                <span v-if="record.industries.length > 2">...</span>
              </span>
            </a-tooltip>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'employeeCount'">
            <a-tag :color="record.employeeCount > 0 ? 'blue' : 'default'">
              {{ record.employeeCount }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" size="small" @click="viewCompanyDetail(record)"> 查看 </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.company-management {
  padding: 16px;
  max-width: 100%;
  overflow-x: hidden;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

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
  .company-management {
    padding: 12px;
  }
  
  .search-row {
    flex-wrap: wrap !important;
  }
}

/* 确保表格内容不超出 */
:deep(.ant-table-wrapper) {
  overflow-x: auto;
}

:deep(.ant-table) {
  font-size: 13px;
}

:deep(.ant-table-thead > tr > th) {
  padding: 12px 8px;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
}
</style>


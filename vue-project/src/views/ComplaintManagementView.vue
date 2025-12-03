<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import * as complaintController from '@/api/complaintController'
import * as companyController from '@/api/companyController'
import * as employeeController from '@/api/employeeController'

// 当前标签页
const activeTab = ref<'pending' | 'processed'>('pending')

// 投诉列表
const complaintList = ref<API.ComplaintVO[]>([])
const complaintLoading = ref(false)
const complaintPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 筛选条件
const filterComplainantId = ref<string | undefined>(undefined)
const filterCompanyId = ref<string | undefined>(undefined)
const filterStatus = ref<number | undefined>(undefined)

// 员工选项
const employeeOptions = ref<Array<{ label: string; value: string }>>([])
const employeeOptionsLoading = ref(false)

// 公司选项
const companyOptions = ref<Array<{ label: string; value: string }>>([])
const companyOptionsLoading = ref(false)

// 根据当前标签页计算筛选状态
const computedFilterStatus = computed(() => {
  if (activeTab.value === 'pending') {
    // 未处理：状态为0（待处理）或1（处理中）
    return filterStatus.value !== undefined ? filterStatus.value : undefined
  } else {
    // 已处理：状态为2（已处理）或3（已驳回）
    return filterStatus.value !== undefined ? filterStatus.value : undefined
  }
})

// 获取投诉列表
const fetchComplaints = async (page: number = 1) => {
  try {
    complaintLoading.value = true
    
    // 根据标签页设置状态筛选
    let statusFilter: number | undefined = computedFilterStatus.value
    if (statusFilter === undefined) {
      if (activeTab.value === 'pending') {
        // 未处理：查询状态为0（待处理）的投诉
        statusFilter = 0
      } else {
        // 已处理：查询状态为2（通过）或3（已驳回）的投诉，这里不传状态，查询所有后过滤
        statusFilter = undefined
      }
    }
    
    const requestParams: any = {
      pageNum: page,
      pageSize: complaintPagination.pageSize,
      sortField: 'create_time',
      sortOrder: 'descend',
    }
    
    // 添加筛选条件
    if (filterComplainantId.value) {
      requestParams.complainantId = String(filterComplainantId.value) // 转换为字符串
    }
    if (filterCompanyId.value) {
      requestParams.companyId = String(filterCompanyId.value) // 转换为字符串
    }
    
    // 根据标签页设置状态筛选
    if (statusFilter !== undefined) {
      requestParams.status = statusFilter
    } else {
      // 如果没有指定状态筛选，根据标签页设置默认状态
      // 注意：后端需要支持多状态查询，这里先查询所有，然后客户端过滤
      // 如果后端支持多状态查询，可以传递数组 [0, 1] 或 [2, 3]
    }
    
    const result = await complaintController.pageComplaint(requestParams)
    
    if (result?.data?.code === 0 && result.data.data) {
      let records = result.data.data.records || []
      
      // 根据标签页过滤
      if (activeTab.value === 'pending') {
        records = records.filter((item: API.ComplaintVO) => 
          item.status === 0
        )
      } else {
        records = records.filter((item: API.ComplaintVO) => 
          item.status === 2 || item.status === 3
        )
      }
      
      complaintList.value = records
      // 注意：由于做了客户端过滤，总数可能不准确
      // 理想情况下，后端应该支持多状态查询，返回准确的总数
      complaintPagination.total = records.length
      complaintPagination.current = page
    } else {
      message.error(result?.data?.message || '获取投诉列表失败')
    }
  } catch (error: any) {
    console.error('Failed to fetch complaints:', error)
    message.error(error?.response?.data?.message || '获取投诉列表失败')
  } finally {
    complaintLoading.value = false
  }
}

// 加载员工选项
const loadEmployeeOptions = async () => {
  try {
    employeeOptionsLoading.value = true
    // 这里应该调用获取所有员工的接口，但为了简化，我们可以从投诉列表中提取
    // 或者调用员工列表接口
    const result = await employeeController.pageEmployeeVOByPage({
      pageNum: 1,
      pageSize: 1000,
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      employeeOptions.value = records
        .filter((item: API.EmployeeVO) => item.id)
        .map((item: API.EmployeeVO) => ({
          label: item.name || `员工${item.id}`,
          value: String(item.id),
        }))
    }
  } catch (error) {
    console.error('Failed to load employee options:', error)
  } finally {
    employeeOptionsLoading.value = false
  }
}

// 加载公司选项
const loadCompanyOptions = async () => {
  try {
    companyOptionsLoading.value = true
    const result = await companyController.listCompanyVoByPage({
      pageNum: 1,
      pageSize: 1000,
    })
    
    if (result?.data?.code === 0 && result.data.data) {
      const records = result.data.data.records || []
      companyOptions.value = records
        .filter((item: API.CompanyVO) => item.id)
        .map((item: API.CompanyVO) => ({
          label: item.name || `公司${item.id}`,
          value: String(item.id), // 转换为字符串
        }))
    }
  } catch (error) {
    console.error('Failed to load company options:', error)
  } finally {
    companyOptionsLoading.value = false
  }
}

// 获取状态文本
const getStatusText = (status: number | undefined): string => {
  if (status === 0) return '待处理'
  if (status === 2) return '通过'
  if (status === 3) return '已驳回'
  return '未知'
}

// 获取状态颜色
const getStatusColor = (status: number | undefined): string => {
  if (status === 0) return 'orange'
  if (status === 2) return 'green'
  if (status === 3) return 'red'
  return 'default'
}

// 获取投诉类型文本
const getTypeText = (type: number | undefined): string => {
  if (type === 1) return '恶意评价'
  if (type === 2) return '虚假信息'
  if (type === 3) return '其他'
  return '未知'
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

// 筛选变化
const handleFilterChange = () => {
  complaintPagination.current = 1
  fetchComplaints(1)
}

// 标签页切换
const handleTabChange = (key: string) => {
  activeTab.value = key as 'pending' | 'processed'
  complaintPagination.current = 1
  filterStatus.value = undefined
  fetchComplaints(1)
}

// 查看详情
const complaintDetailVisible = ref(false)
const selectedComplaint = ref<API.ComplaintDetailVO | null>(null)
const detailLoading = ref(false)

const handleViewDetail = async (record: API.ComplaintVO) => {
  if (!record.id) {
    message.error('投诉ID不存在')
    return
  }
  try {
    detailLoading.value = true
    const result = await complaintController.getComplaintDetail({
      id: String(record.id), // 转换为字符串
    })
    if (result?.data?.code === 0) {
      selectedComplaint.value = result.data.data || null
      complaintDetailVisible.value = true
    } else {
      message.error(result?.data?.message || '获取投诉详情失败')
    }
  } catch (error: any) {
    console.error('Failed to load complaint detail:', error)
    message.error(error?.response?.data?.message || '获取投诉详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 处理投诉
const handleComplaintVisible = ref(false)
const handleComplaintForm = reactive({
  status: undefined as number | undefined,
  handleResult: '',
})
const handleComplaintSubmitting = ref(false)
const handleComplaintFormRef = ref()

const openHandleModal = (record: API.ComplaintVO) => {
  if (!record.id) {
    message.error('投诉ID不存在')
    return
  }
    if (record.status !== 0) {
      message.error('只能处理待处理的投诉')
    return
  }
  selectedComplaint.value = { ...record } as any
  handleComplaintForm.status = undefined
  handleComplaintForm.handleResult = ''
  handleComplaintVisible.value = true
}

const handleSubmitComplaint = async () => {
  if (!selectedComplaint.value?.id) {
    message.error('投诉ID不存在')
    return
  }
  
  if (!handleComplaintForm.status) {
    message.error('请选择处理结果')
    return
  }
  
  try {
    handleComplaintSubmitting.value = true
    const result = await complaintController.handleComplaint({
      id: String(selectedComplaint.value.id), // 转换为字符串
      status: handleComplaintForm.status,
      handleResult: handleComplaintForm.handleResult || undefined,
    })
    
    if (result?.data?.code === 0) {
      message.success('处理成功')
      handleComplaintVisible.value = false
      complaintDetailVisible.value = false
      await fetchComplaints(complaintPagination.current)
    } else {
      message.error(result?.data?.message || '处理失败')
    }
  } catch (error: any) {
    console.error('Failed to handle complaint:', error)
    message.error(error?.response?.data?.message || '处理失败')
  } finally {
    handleComplaintSubmitting.value = false
  }
}

onMounted(async () => {
  await loadEmployeeOptions()
  await loadCompanyOptions()
  await fetchComplaints(1)
})
</script>

<template>
  <div class="complaint-management-view">
    <a-spin :spinning="complaintLoading">
      <a-card>
        <template #title>
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="complaint-tabs">
            <a-tab-pane key="pending" tab="未处理" />
            <a-tab-pane key="processed" tab="已处理" />
          </a-tabs>
        </template>

        <!-- 筛选 -->
        <div style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap;">
          <span>员工：</span>
          <a-select
            v-model:value="filterComplainantId"
            placeholder="全部员工"
            allow-clear
            style="width: 150px"
            :loading="employeeOptionsLoading"
            @change="handleFilterChange"
          >
            <a-select-option
              v-for="employee in employeeOptions"
              :key="employee.value"
              :value="employee.value"
            >
              {{ employee.label }}
            </a-select-option>
          </a-select>
          
          <span>被投诉公司：</span>
          <a-select
            v-model:value="filterCompanyId"
            placeholder="全部公司"
            allow-clear
            style="width: 150px"
            :loading="companyOptionsLoading"
            @change="handleFilterChange"
          >
            <a-select-option
              v-for="company in companyOptions"
              :key="company.value"
              :value="company.value"
            >
              {{ company.label }}
            </a-select-option>
          </a-select>
          
          <span>状态：</span>
          <a-select
            v-model:value="filterStatus"
            placeholder="全部状态"
            allow-clear
            style="width: 150px"
            @change="handleFilterChange"
          >
            <a-select-option :value="0">待处理</a-select-option>
            <a-select-option :value="2">通过</a-select-option>
            <a-select-option :value="3">已驳回</a-select-option>
          </a-select>
        </div>

        <!-- 投诉列表 -->
        <a-table
          :columns="[
            { title: '投诉人', dataIndex: 'complainantName', key: 'complainantName', width: 120 },
            { title: '投诉类型', key: 'type', width: 120 },
            { title: '投诉标题', dataIndex: 'title', key: 'title', width: 200 },
            { title: '被投诉公司', dataIndex: 'companyName', key: 'companyName', width: 150 },
            { title: '投诉时间', key: 'createTime', width: 160 },
            { title: '状态', key: 'status', width: 100, align: 'center' },
            { title: '操作', key: 'operation', width: 150, align: 'center' },
          ]"
          :data-source="complaintList"
          :pagination="{
            current: complaintPagination.current,
            pageSize: complaintPagination.pageSize,
            total: complaintPagination.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }"
          row-key="id"
          size="middle"
          :bordered="true"
          @change="(pagination) => {
            if (pagination.current) {
              fetchComplaints(pagination.current)
            }
            if (pagination.pageSize) {
              complaintPagination.pageSize = pagination.pageSize
              fetchComplaints(1)
            }
          }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <a-tag>{{ getTypeText(record.type) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'createTime'">
              {{ formatDateTime(record.createTime) }}
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'operation'">
              <a-space>
                <a-button type="link" size="small" @click="handleViewDetail(record)">
                  查看详情
                </a-button>
                <a-button
                  v-if="activeTab === 'pending' && record.status === 0"
                  type="primary"
                  size="small"
                  @click="openHandleModal(record)"
                >
                  处理
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
        
        <a-empty
          v-if="!complaintLoading && complaintList.length === 0"
          description="暂无投诉记录"
          style="padding: 40px"
        />
      </a-card>
    </a-spin>

    <!-- 投诉详情弹窗 -->
    <a-modal
      v-model:open="complaintDetailVisible"
      title="投诉详情"
      width="900px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <div v-if="selectedComplaint">
          <a-descriptions bordered :column="1" style="margin-bottom: 24px">
            <a-descriptions-item label="投诉人">
              {{ selectedComplaint.complainantName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="投诉类型">
              <a-tag>{{ selectedComplaint.typeText || getTypeText(selectedComplaint.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="投诉标题">
              {{ selectedComplaint.title }}
            </a-descriptions-item>
            <a-descriptions-item label="投诉内容">
              {{ selectedComplaint.content }}
            </a-descriptions-item>
            <a-descriptions-item label="被投诉公司">
              {{ selectedComplaint.companyName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="证据图片" v-if="selectedComplaint.evidenceImages && selectedComplaint.evidenceImages.length > 0">
              <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                <a-image
                  v-for="(img, index) in selectedComplaint.evidenceImages"
                  :key="index"
                  :src="img"
                  :width="100"
                  :preview="true"
                />
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(selectedComplaint.status)">
                {{ selectedComplaint.statusText || getStatusText(selectedComplaint.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="处理结果" v-if="selectedComplaint.handleResult">
              {{ selectedComplaint.handleResult }}
            </a-descriptions-item>
            <a-descriptions-item label="处理时间" v-if="selectedComplaint.handleTime">
              {{ formatDateTime(selectedComplaint.handleTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="投诉时间">
              {{ formatDateTime(selectedComplaint.createTime) }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 被投诉的评价内容 -->
          <div v-if="selectedComplaint.evaluation" style="margin-top: 24px">
            <h3 style="margin-bottom: 16px">被投诉的评价内容</h3>
            <a-descriptions bordered :column="1">
              <a-descriptions-item label="评价人">
                {{ selectedComplaint.evaluation.evaluatorName || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="评价类型">
                <a-tag>{{ selectedComplaint.evaluation.evaluationTypeText || '-' }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="评价周期">
                {{ selectedComplaint.evaluation.evaluationPeriodText || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="评价日期">
                {{ selectedComplaint.evaluation.evaluationDate || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="评价内容">
                {{ selectedComplaint.evaluation.comment || '无' }}
              </a-descriptions-item>
              <a-descriptions-item label="维度评分" v-if="selectedComplaint.evaluation.dimensionScores && selectedComplaint.evaluation.dimensionScores.length > 0">
                <div v-for="ds in selectedComplaint.evaluation.dimensionScores" :key="ds.dimensionId" style="margin-bottom: 8px">
                  <span>{{ ds.dimensionName }}：</span>
                  <a-rate :value="ds.score" disabled :count="5" />
                </div>
              </a-descriptions-item>
              <a-descriptions-item label="评价标签" v-if="selectedComplaint.evaluation.tags && selectedComplaint.evaluation.tags.length > 0">
                <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                  <a-tag
                    v-for="tag in selectedComplaint.evaluation.tags"
                    :key="tag.tagId"
                    :color="tag.tagType === 1 ? 'green' : 'orange'"
                  >
                    {{ tag.tagName }}
                  </a-tag>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
      </a-spin>
    </a-modal>

    <!-- 处理投诉弹窗 -->
    <a-modal
      v-model:open="handleComplaintVisible"
      title="处理投诉"
      width="600px"
      @ok="handleSubmitComplaint"
      :confirm-loading="handleComplaintSubmitting"
    >
      <a-form :model="handleComplaintForm" ref="handleComplaintFormRef">
        <a-form-item label="处理结果" name="status" :rules="[{ required: true, message: '请选择处理结果' }]">
          <a-radio-group v-model:value="handleComplaintForm.status">
            <a-radio :value="2">通过</a-radio>
            <a-radio :value="3">驳回</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="处理说明" name="handleResult">
          <a-textarea
            v-model:value="handleComplaintForm.handleResult"
            placeholder="请输入处理说明（可选）"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.complaint-management-view {
  padding: 0;
}

.complaint-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
}
</style>


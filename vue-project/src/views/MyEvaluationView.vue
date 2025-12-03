<template>
  <div class="my-evaluation-container">
    <a-spin :spinning="loading">
      <a-card>
        <template #title>
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="evaluation-tabs">
            <a-tab-pane key="evaluation" tab="我的评价" />
            <a-tab-pane key="complaints" tab="我的投诉" />
          </a-tabs>
        </template>

        <!-- 我的评价标签页 -->
        <div v-if="activeTab === 'evaluation'">
          <a-row :gutter="24">
            <!-- 左侧：评价列表 -->
            <a-col :span="16">
              <a-card title="评价记录" style="margin-bottom: 24px">
            <!-- 排序和筛选 -->
            <div style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap;">
              <span>排序方式：</span>
              <a-select
                v-model:value="sortField"
                style="width: 150px"
                placeholder="选择排序字段"
                @change="handleSortChange"
              >
                <a-select-option value="">默认排序</a-select-option>
                <a-select-option value="create_time">评价时间</a-select-option>
              </a-select>
              <a-select
                v-model:value="sortOrder"
                style="width: 120px"
                placeholder="排序顺序"
                @change="handleSortChange"
                :disabled="!sortField"
              >
                <a-select-option value="ascend">升序</a-select-option>
                <a-select-option value="descend">降序</a-select-option>
              </a-select>
              <span style="margin-left: 16px;">筛选：</span>
              <a-select
                v-model:value="filterEvaluationType"
                style="width: 150px"
                placeholder="评价类型"
                allowClear
                @change="handleFilterChange"
              >
                <a-select-option :value="1 as number">领导评价</a-select-option>
                <a-select-option :value="2 as number">同事评价</a-select-option>
                <a-select-option :value="3 as number">HR评价</a-select-option>
                <a-select-option :value="4 as number">自评</a-select-option>
              </a-select>
              <a-select
                v-model:value="filterEvaluationPeriod"
                style="width: 150px"
                placeholder="评价周期"
                allowClear
                @change="handleFilterChange"
              >
                <a-select-option :value="1 as number">季度评价</a-select-option>
                <a-select-option :value="2 as number">年度评价</a-select-option>
                <a-select-option :value="3 as number">离职评价</a-select-option>
                <a-select-option :value="4 as number">临时评价</a-select-option>
              </a-select>
              <a-select
                v-model:value="filterCompanyId"
                style="width: 150px"
                placeholder="评价公司"
                allowClear
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
              <a-button @click="resetFilter">重置</a-button>
            </div>
            <a-table
              :columns="columns"
              :data-source="evaluationList"
              :pagination="pagination"
              @change="handleTableChange"
              row-key="id"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'evaluationType'">
                  <a-tag :color="getEvaluationTypeColor(record.evaluationType)">
                    {{ getEvaluationTypeText(record.evaluationType) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'evaluationPeriod'">
                  <a-tag>{{ record.evaluationPeriodText }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" @click="handleViewDetail(record)">查看详情</a-button>
                </template>
              </template>
            </a-table>
              </a-card>
            </a-col>

            <!-- 右侧：雷达图 -->
            <a-col :span="8">
              <a-card title="五维评价雷达图">
                <div style="margin-top: -20px;">
                  <div ref="radarChartRef" style="width: 100%; height: 400px"></div>
                  <!-- 综合平均评分 -->
                  <div v-if="overallAverageScore > 0" 
                       style="margin-top: -34px; padding: 16px; background: white; border-radius: 4px; text-align: center; color: #666; font-size: 12px;">
                    综合平均评分: <span style="font-size: 12px; font-weight: bold; color: #1890ff;">{{ overallAverageScore.toFixed(2) }}</span>
                  </div>
                </div>
                <!-- 标签统计 -->
                <div v-if="tagStatistics.length > 0" style="margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
                  <div style="font-size: 14px; font-weight: 500; margin-bottom: 12px; color: #262626;">标签统计</div>
                  <a-space wrap>
                    <a-tag 
                      v-for="tag in tagStatistics" 
                      :key="tag.tagId" 
                      :color="tag.tagType === 1 ? 'green' : 'orange'"
                    >
                      {{ tag.tagName }}×{{ tag.count }}
                    </a-tag>
                  </a-space>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>

        <!-- 我的投诉标签页 -->
        <div v-if="activeTab === 'complaints'">
          <a-spin :spinning="complaintLoading">
            <!-- 筛选 -->
            <div style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center;">
              <span>状态筛选：</span>
              <a-select
                v-model:value="complaintFilterStatus"
                placeholder="全部状态"
                allow-clear
                style="width: 150px"
                @change="handleComplaintFilterChange"
              >
                <a-select-option :value="0">待处理</a-select-option>
                <a-select-option :value="2">通过</a-select-option>
                <a-select-option :value="3">已驳回</a-select-option>
              </a-select>
            </div>

            <a-table
              :columns="[
                { title: '投诉类型', key: 'type', width: 120 },
                { title: '投诉标题', dataIndex: 'title', key: 'title', width: 200 },
                { title: '被投诉公司', dataIndex: 'companyName', key: 'companyName', width: 150 },
                { title: '投诉时间', key: 'createTime', width: 160 },
                { title: '状态', key: 'status', width: 100, align: 'center' },
                { title: '操作', key: 'operation', width: 100, align: 'center' },
              ]"
              :data-source="complaintList"
              :pagination="{
                current: complaintPagination.current,
                pageSize: complaintPagination.pageSize,
                total: complaintPagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => `共 ${total} 条`,
              }"
              row-key="id"
              size="middle"
              :bordered="true"
              @change="(pagination: any) => {
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
                  <a-tag>{{ getComplaintTypeText(record.type) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'createTime'">
                  {{ formatComplaintDateTime(record.createTime) }}
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="getComplaintStatusColor(record.status)">
                    {{ getComplaintStatusText(record.status) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'operation'">
                  <a-button type="link" size="small" @click="handleViewComplaintDetail(record)">
                    查看详情
                  </a-button>
                </template>
              </template>
            </a-table>
            
            <a-empty
              v-if="!complaintLoading && complaintList.length === 0"
              description="暂无投诉记录"
              style="padding: 40px"
            />
          </a-spin>
        </div>
      </a-card>

      <!-- 评价详情弹窗 -->
      <a-modal
        v-model:open="detailModalVisible"
        title="评价详情"
        width="700px"
      >
        <template #footer>
          <a-space>
            <a-button @click="handleComplaint(selectedEvaluation)">投诉</a-button>
            <a-button @click="detailModalVisible = false">关闭</a-button>
          </a-space>
        </template>
        <a-descriptions :column="1" bordered v-if="selectedEvaluation">
          <a-descriptions-item label="评价类型">
            <a-tag :color="getEvaluationTypeColor(selectedEvaluation.evaluationType || 0)">
              {{ getEvaluationTypeText(selectedEvaluation.evaluationType || 0) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="评价周期">
            {{ selectedEvaluation.evaluationPeriodText }}
          </a-descriptions-item>
          <a-descriptions-item label="评价日期">
            {{ selectedEvaluation.evaluationDate }}
          </a-descriptions-item>
          <a-descriptions-item label="维度评分" v-if="selectedEvaluation.dimensionScores">
            <div v-for="ds in selectedEvaluation.dimensionScores" :key="ds.dimensionId" style="margin-bottom: 8px">
              <span>{{ ds.dimensionName }}：</span>
              <a-rate :value="ds.score" disabled :count="5" />
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="评价标签" v-if="selectedEvaluation.tags">
            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
              <a-tag v-for="tag in selectedEvaluation.tags" :key="tag.tagId" :color="tag.tagType === 1 ? 'green' : 'orange'">
                {{ tag.tagName }}
              </a-tag>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="评价内容">
            {{ selectedEvaluation.comment || '无' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>

      <!-- 投诉弹窗 -->
      <a-modal
        v-model:open="complaintModalVisible"
        title="提交投诉"
        width="600px"
        @ok="handleSubmitComplaint"
        :confirm-loading="complaintSubmitting"
      >
        <a-form :model="complaintForm" :rules="complaintRules" ref="complaintFormRef">
          <a-form-item label="投诉类型" name="type">
            <a-select v-model:value="complaintForm.type" placeholder="请选择投诉类型">
              <a-select-option :value="1">恶意评价</a-select-option>
              <a-select-option :value="2">虚假信息</a-select-option>
              <a-select-option :value="3">其他</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="投诉标题" name="title">
            <a-input v-model:value="complaintForm.title" placeholder="请输入投诉标题" />
          </a-form-item>
          <a-form-item label="投诉内容" name="content">
            <a-textarea
              v-model:value="complaintForm.content"
              placeholder="请输入投诉内容"
              :rows="4"
            />
          </a-form-item>
          <a-form-item label="证据图片">
            <a-upload
              v-model:file-list="complaintForm.evidenceImages"
              list-type="picture-card"
              :before-upload="beforeUpload"
              @preview="handlePreview"
              @remove="handleRemove"
            >
              <div v-if="complaintForm.evidenceImages.length < 5">
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 图片预览 -->
      <a-modal
        v-model:open="previewVisible"
        :footer="null"
        centered
      >
        <img alt="preview" style="width: 100%" :src="previewImage" />
      </a-modal>

      <!-- 投诉详情弹窗 -->
      <a-modal
        v-model:open="complaintDetailVisible"
        title="投诉详情"
        width="800px"
        :footer="null"
      >
        <a-spin :spinning="complaintDetailLoading">
          <a-descriptions bordered :column="1" v-if="selectedComplaint">
            <a-descriptions-item label="投诉类型">
              <a-tag>{{ selectedComplaint.typeText || getComplaintTypeText(selectedComplaint.type) }}</a-tag>
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
              <a-tag :color="getComplaintStatusColor(selectedComplaint.status)">
                {{ selectedComplaint.statusText || getComplaintStatusText(selectedComplaint.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="处理结果" v-if="selectedComplaint.handleResult">
              {{ selectedComplaint.handleResult }}
            </a-descriptions-item>
            <a-descriptions-item label="处理时间" v-if="selectedComplaint.handleTime">
              {{ formatComplaintDateTime(selectedComplaint.handleTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="投诉时间">
              {{ formatComplaintDateTime(selectedComplaint.createTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-spin>
      </a-modal>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, nextTick, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import * as evaluationController from '@/api/evaluationController'
import * as employeeController from '@/api/employeeController'
import * as companyController from '@/api/companyController'
import * as complaintController from '@/api/complaintController'
import { useUserStore } from '@/stores/userStore'
import { onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { UploadProps, UploadFile } from 'ant-design-vue'
import { getBase64 } from '@/utils/image'
import type { FormInstance } from 'ant-design-vue'

const userStore = useUserStore()
const loading = ref(false)
const radarChartRef = ref<HTMLDivElement>()
let radarChart: echarts.ECharts | null = null
// 存储当前悬浮的指标索引（用于雷达图tooltip）

// 标签页
const activeTab = ref<'evaluation' | 'complaints'>('evaluation')

// 评价列表
const evaluationList = ref<API.EvaluationVO[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 排序和筛选
const sortField = ref<string>('')
const sortOrder = ref<string>('descend') // 默认降序，最新的在前
const filterEvaluationType = ref<number | undefined>(undefined) // 评价类型筛选
const filterEvaluationPeriod = ref<number | undefined>(undefined) // 评价周期筛选
const filterCompanyId = ref<string | undefined>(undefined) // 评价公司筛选（使用字符串格式，避免精度丢失）
const companyOptions = ref<Array<{ label: string; value: string }>>([])
const companyOptionsLoading = ref(false)

// 详情弹窗
const detailModalVisible = ref(false)
const selectedEvaluation = ref<API.EvaluationVO | null>(null)

// 投诉相关
const complaintModalVisible = ref(false)
const complaintFormRef = ref<FormInstance>()
const complaintSubmitting = ref(false)
const complaintForm = reactive({
  type: undefined as number | undefined,
  title: '',
  content: '',
  evidenceImages: [] as UploadFile[],
})
const complaintRules = {
  type: [{ required: true, message: '请选择投诉类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入投诉标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入投诉内容', trigger: 'blur' }],
}
const previewImage = ref('')
const previewVisible = ref(false)

// 我的投诉列表
const complaintList = ref<API.ComplaintVO[]>([])
const complaintLoading = ref(false)
const complaintPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
const complaintFilterStatus = ref<number | undefined>(undefined)
const complaintDetailVisible = ref(false)
const selectedComplaint = ref<API.ComplaintDetailVO | null>(null)
const complaintDetailLoading = ref(false)

// 表格列定义
const columns = [
  { title: '评价类型', key: 'evaluationType' },
  { title: '评价周期', key: 'evaluationPeriod' },
  { title: '评价日期', dataIndex: 'evaluationDate', key: 'evaluationDate' },
  { title: '操作', key: 'action', width: 100 },
]

// 获取评价类型文本
const getEvaluationTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: '领导评价',
    2: '同事评价',
    3: 'HR评价',
    4: '自评',
  }
  return map[type] || '未知'
}

// 获取评价类型颜色
const getEvaluationTypeColor = (type: number) => {
  const map: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple',
  }
  return map[type] || 'default'
}

// 加载评价列表
const loadEvaluationList = async () => {
  try {
    loading.value = true
    // 获取当前用户的员工信息
    const employeeResponse = await employeeController.getMyEmployeeVo()
    if (employeeResponse?.data?.code !== 0 || !employeeResponse?.data?.data?.id) {
      message.error('获取员工信息失败')
      return
    }
    const employeeId = employeeResponse.data.data.id

    const requestParams: API.EvaluationQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      employeeId: employeeId,
    }

    // 添加排序参数（默认按创建时间降序）
    if (sortField.value) {
      requestParams.sortField = sortField.value
      requestParams.sortOrder = sortOrder.value || 'descend'
    } else {
      // 如果没有选择排序字段，默认按创建时间降序
      requestParams.sortField = 'create_time'
      requestParams.sortOrder = 'descend'
    }

    // 添加筛选参数
    if (filterEvaluationType.value !== undefined && filterEvaluationType.value !== null) {
      requestParams.evaluationType = filterEvaluationType.value
    }
    if (filterEvaluationPeriod.value !== undefined && filterEvaluationPeriod.value !== null) {
      requestParams.evaluationPeriod = filterEvaluationPeriod.value
    }
    if (filterCompanyId.value !== undefined && filterCompanyId.value !== null && filterCompanyId.value !== '') {
      // @ts-ignore - companyId字段已添加到后端，但类型定义可能未更新
      // 已经是字符串格式，直接使用（与employeeId处理方式一致）
      requestParams.companyId = filterCompanyId.value
    }

    const response = await evaluationController.pageEvaluation(requestParams)
    if (response?.data?.code === 0) {
      const data = response.data.data
      evaluationList.value = data?.records || []
      pagination.total = Number(data?.totalRow) || 0
      // 更新雷达图
      await nextTick()
      updateRadarChart()
    }
  } catch (error) {
    console.error('Failed to load evaluations:', error)
    message.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 加载公司选项（只显示评价表中存在的公司）
const loadCompanyOptions = async () => {
  try {
    companyOptionsLoading.value = true
    // 先加载所有评价，获取所有companyId
    const employeeResponse = await employeeController.getMyEmployeeVo()
    if (employeeResponse?.data?.code !== 0 || !employeeResponse?.data?.data?.id) {
      companyOptions.value = []
      return
    }
    const employeeId = employeeResponse.data.data.id

    // 获取所有评价（不分页）以提取companyId
    const allEvaluationsResponse = await evaluationController.pageEvaluation({
      pageNum: 1,
      pageSize: 10000, // 获取所有评价
      employeeId: employeeId,
    })

    if (allEvaluationsResponse?.data?.code !== 0) {
      companyOptions.value = []
      return
    }

    const allEvaluations = allEvaluationsResponse.data.data?.records || []
    console.log('所有评价记录:', allEvaluations)
    
    // 提取所有唯一的companyId（使用字符串格式，避免精度丢失）
    const companyIds = new Set<string>()
    allEvaluations.forEach((evaluation: any) => {
      // @ts-ignore - companyId字段已添加到后端，但类型定义可能未更新
      let companyId = evaluation.companyId
      // 如果companyId不存在，尝试从其他字段获取
      if (companyId === undefined || companyId === null) {
        // 检查是否有其他可能的字段名
        companyId = (evaluation as any).company_id
      }
      console.log('评价记录companyId:', companyId, '类型:', typeof companyId, '完整记录:', evaluation)
      if (companyId !== undefined && companyId !== null && companyId !== '') {
        // 转换为字符串格式，避免精度丢失
        const companyIdStr = String(companyId)
        if (companyIdStr) {
          companyIds.add(companyIdStr)
        }
      }
    })

    console.log('提取的companyIds:', Array.from(companyIds))
    
    if (companyIds.size === 0) {
      console.warn('没有找到任何companyId')
      companyOptions.value = []
      return
    }

    // 查询所有公司
    const response = await companyController.listCompanyVoByPage({
      pageNum: 1,
      pageSize: 1000,
    })
    if (response?.data?.code === 0) {
      const records = response.data?.data?.records || []
      // 只保留评价表中存在的公司
      const companyIdArray = Array.from(companyIds)
      const filtered = records.filter((item: API.CompanyVO) => {
        if (!item.id) return false
        // 使用字符串比较，避免精度丢失
        return companyIdArray.includes(String(item.id))
      })
      
      companyOptions.value = filtered.map((item: API.CompanyVO) => ({
        label: item.name || `公司${item.id}`,
        value: item.id ? String(item.id) : '', // 使用字符串格式，避免精度丢失
      }))
      
      console.log('提取的companyIds:', Array.from(companyIds))
      console.log('查询到的公司记录数:', records.length)
      console.log('过滤后的公司数:', filtered.length)
      console.log('最终的公司选项:', companyOptions.value)
    } else {
      companyOptions.value = []
    }
  } catch (error) {
    console.error('Failed to load company options:', error)
    companyOptions.value = []
  } finally {
    companyOptionsLoading.value = false
  }
}

// 排序变化处理
const handleSortChange = () => {
  pagination.current = 1 // 重置到第一页
  loadEvaluationList()
}

// 筛选变化处理
const handleFilterChange = () => {
  pagination.current = 1 // 重置到第一页
  loadEvaluationList()
  loadDimensionScores() // 重新加载五维数据
  loadTagStatistics() // 重新加载标签统计
}

// 重置筛选
const resetFilter = () => {
  sortField.value = ''
  sortOrder.value = 'descend'
  filterEvaluationType.value = undefined
  filterEvaluationPeriod.value = undefined
  filterCompanyId.value = undefined
  pagination.current = 1
  loadEvaluationList()
  loadDimensionScores() // 重新加载五维数据
  loadTagStatistics() // 重新加载标签统计
}

// 从后端获取五维评价数据（考虑所有符合条件的评价，不分页）
const dimensionScoresFromBackend = ref<Array<{ dimensionId: number; dimensionName: string; averageScore: number }>>([])
const dimensionScoresLoading = ref(false)

// 标签统计
const tagStatistics = ref<API.EvaluationTagStatisticsVO[]>([])
const tagStatisticsLoading = ref(false)

// 加载五维评价数据
const loadDimensionScores = async () => {
  try {
    dimensionScoresLoading.value = true
    // 获取当前用户的员工信息
    const employeeResponse = await employeeController.getMyEmployeeVo()
    if (employeeResponse?.data?.code !== 0 || !employeeResponse?.data?.data?.id) {
      dimensionScoresFromBackend.value = []
      return
    }
    const employeeId = employeeResponse.data.data.id

    const requestParams: API.EvaluationQueryRequest = {
      pageNum: 1,
      pageSize: 1, // 不需要分页数据，只需要筛选条件
      employeeId: employeeId,
    }

    // 添加筛选参数（与评价列表的筛选条件一致）
    if (filterEvaluationType.value !== undefined && filterEvaluationType.value !== null) {
      requestParams.evaluationType = filterEvaluationType.value
    }
    if (filterEvaluationPeriod.value !== undefined && filterEvaluationPeriod.value !== null) {
      requestParams.evaluationPeriod = filterEvaluationPeriod.value
    }
    if (filterCompanyId.value !== undefined && filterCompanyId.value !== null && filterCompanyId.value !== '') {
      // @ts-ignore - companyId字段已添加到后端，但类型定义可能未更新
      requestParams.companyId = filterCompanyId.value
    }

    const response = await evaluationController.calculateDimensionScores(requestParams)
    if (response?.data?.code === 0) {
      const scores = response.data.data || []
      dimensionScoresFromBackend.value = scores.map((item: API.EvaluationDimensionScoreVO) => ({
        dimensionId: item.dimensionId || 0,
        dimensionName: item.dimensionName || `维度${item.dimensionId}`,
        averageScore: item.averageScore || 0,
      }))
      await nextTick()
      updateRadarChart()
    } else {
      dimensionScoresFromBackend.value = []
    }
  } catch (error) {
    console.error('Failed to load dimension scores:', error)
    dimensionScoresFromBackend.value = []
  } finally {
    dimensionScoresLoading.value = false
  }
}

// 加载标签统计
const loadTagStatistics = async () => {
  try {
    tagStatisticsLoading.value = true
    // 获取当前用户的员工信息
    const employeeResponse = await employeeController.getMyEmployeeVo()
    if (employeeResponse?.data?.code !== 0 || !employeeResponse?.data?.data?.id) {
      tagStatistics.value = []
      return
    }
    const employeeId = employeeResponse.data.data.id

    const requestParams: API.EvaluationQueryRequest = {
      pageNum: 1,
      pageSize: 1, // 不需要分页数据，只需要筛选条件
      employeeId: employeeId,
    }

    // 添加筛选参数（与评价列表的筛选条件一致）
    if (filterEvaluationType.value !== undefined && filterEvaluationType.value !== null) {
      requestParams.evaluationType = filterEvaluationType.value
    }
    if (filterEvaluationPeriod.value !== undefined && filterEvaluationPeriod.value !== null) {
      requestParams.evaluationPeriod = filterEvaluationPeriod.value
    }
    if (filterCompanyId.value !== undefined && filterCompanyId.value !== null && filterCompanyId.value !== '') {
      // @ts-ignore - companyId字段已添加到后端，但类型定义可能未更新
      requestParams.companyId = filterCompanyId.value
    }

    const response = await evaluationController.countTagStatistics(requestParams)
    if (response?.data?.code === 0) {
      tagStatistics.value = response.data.data || []
    } else {
      tagStatistics.value = []
    }
  } catch (error) {
    console.error('Failed to load tag statistics:', error)
    tagStatistics.value = []
  } finally {
    tagStatisticsLoading.value = false
  }
}

// 筛选后的维度评分（用于显示）
const filteredDimensionScores = computed(() => {
  return dimensionScoresFromBackend.value
})

// 综合平均评分
const overallAverageScore = computed(() => {
  const scores = filteredDimensionScores.value
  if (scores.length === 0) return 0
  const sum = scores.reduce((acc, ds) => acc + ds.averageScore, 0)
  return sum / scores.length
})

// 获取雷达图位置名称（用于调试）
const getPositionName = (index: number, total: number) => {
  const positions = ['顶部', '右上', '右下', '左下', '左上']
  const sectorAngle = 360 / total
  const angle = (index * sectorAngle) % 360
  if (angle < 45 || angle >= 315) return '顶部'
  if (angle >= 45 && angle < 135) return '右上'
  if (angle >= 135 && angle < 225) return '右下'
  if (angle >= 225 && angle < 315) return '左下'
  return positions[index % positions.length] || `位置${index}`
}

// 更新雷达图
const updateRadarChart = () => {
  if (!radarChartRef.value) return

  const dimensionScores = filteredDimensionScores.value

  // 如果没有数据，显示空图表
  if (dimensionScores.length === 0) {
    if (radarChart) {
      radarChart.dispose()
      radarChart = null
    }
    return
  }

  // 初始化或更新图表
  if (!radarChart && radarChartRef.value) {
    radarChart = echarts.init(radarChartRef.value)
  }

  if (!radarChart || !radarChartRef.value) {
    return
  }

  // 确保所有维度名称都有值，并记录索引映射
  // 注意：indicators和dimensionScores的顺序必须完全一致
  // ECharts雷达图的指标顺序是从顶部开始，按顺时针方向排列的
  // 但我们的dimensionScores数组是按dimensionId排序的，这会导致tooltip显示的维度与鼠标位置不对应
  // 解决方案：根据用户反馈，创建一个从雷达图位置到数组索引的映射
  // 根据反馈：悬浮在"工作业绩"时显示"专业技能"（偏移3），说明"工作业绩"在雷达图上的位置对应数组索引3
  // 根据反馈：悬浮在"责任心"时显示"团队协作"（偏移2），说明"责任心"在雷达图上的位置对应数组索引2
  // 我们需要创建一个映射：从雷达图位置（角度索引）到数组索引（dimensionId索引）
  const indicators = dimensionScores.map((item, index) => {
    const name = item.dimensionName && item.dimensionName.trim() !== '' 
      ? item.dimensionName.trim()
      : `维度${item.dimensionId}`
    return {
      name: name,
      max: 5,
      index: index, // 保存索引用于tooltip
    }
  })
  

  const option = {
    tooltip: {
      show: false, // 禁用tooltip
    },
    radar: {
      indicator: indicators.map(({ name, max }) => ({ name, max })), // 只传递name和max给ECharts
      center: ['50%', '50%'],
      radius: '60%', // 进一步减小半径，为标签留出更多空间，确保"创新能力"完整显示
      axisName: {
        color: '#333',
        fontSize: 12,
        // 在维度名称下方显示分数
        formatter: (params: any, indicator: any) => {
          // 获取维度名称和索引
          let name = ''
          let index = -1
          
          if (typeof params === 'string') {
            name = params
            // 从indicators数组中找到对应的索引
            index = indicators.findIndex((ind: any) => ind.name === params)
          } else if (params && params.name) {
            name = params.name
            index = indicators.findIndex((ind: any) => ind.name === params.name)
          } else if (indicator && indicator.name) {
            name = indicator.name
            index = indicators.findIndex((ind: any) => ind.name === indicator.name)
          } else {
            const idx = (params && params.index !== undefined) ? params.index : (indicator && indicator.index !== undefined ? indicator.index : 0)
            if (idx >= 0 && idx < indicators.length && indicators[idx]) {
              name = indicators[idx].name
              index = idx
            } else {
              name = `维度${idx}`
              index = idx
            }
          }
          
          // 获取对应的分数
          let score = ''
          const dimensionScore = index >= 0 && index < dimensionScores.length ? dimensionScores[index] : null
          if (dimensionScore && dimensionScore.averageScore !== undefined) {
            score = dimensionScore.averageScore.toFixed(2)
          }
          
          // 返回维度名称和分数，换行显示
          // 对于较长的名称（如"创新能力"），确保完整显示
          return '  ' + name + '\n' + '  ' + score + '分' // 前面加空格实现左移效果
        },
        // 进一步增加标签与雷达图的距离，避免被裁剪，确保"创新能力"等长文本完整显示
        distance: 35,
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(250, 250, 250, 0.3)', 'rgba(200, 200, 200, 0.3)'],
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(211, 253, 250, 0.8)',
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(211, 253, 250, 0.8)',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: dimensionScores.map((item) => item.averageScore),
            name: '综合评分',
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)',
            },
            lineStyle: {
              color: '#1890ff',
              width: 2,
            },
            itemStyle: {
              color: '#1890ff',
            },
          },
        ],
      },
    ],
  }

  radarChart.setOption(option)
}

// 查看详情
const handleViewDetail = async (record: API.EvaluationVO) => {
  try {
    const response = await evaluationController.getEvaluationDetail({
      id: record.id!,
    })
    if (response?.data?.code === 0) {
      selectedEvaluation.value = response.data.data || null
      detailModalVisible.value = true
    } else {
      message.error('获取评价详情失败')
    }
  } catch (error) {
    console.error('Failed to load evaluation detail:', error)
    message.error('获取评价详情失败')
  }
}

// 打开投诉弹窗
const handleComplaint = (evaluation: API.EvaluationVO | null) => {
  if (!evaluation || !evaluation.id) {
    message.error('评价信息不存在')
    return
  }
  // 检查评价是否在30天内
  const evaluationDate = evaluation.evaluationDate ? new Date(evaluation.evaluationDate) : new Date()
  const daysBetween = Math.floor((Date.now() - evaluationDate.getTime()) / (1000 * 60 * 60 * 24))
  if (daysBetween > 30) {
    message.error('只能对30天以内的评价进行投诉')
    return
  }
  complaintForm.type = undefined
  complaintForm.title = ''
  complaintForm.content = ''
  complaintForm.evidenceImages = []
  complaintModalVisible.value = true
}

// 上传前处理
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type?.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB')
    return false
  }
  return false // 阻止自动上传
}

// 预览图片
const handlePreview = async (file: UploadFile) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj as File)
  }
  previewImage.value = file.url || file.preview || ''
  previewVisible.value = true
}

// 移除图片
const handleRemove = (file: UploadFile) => {
  const index = complaintForm.evidenceImages.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    complaintForm.evidenceImages.splice(index, 1)
  }
}

// 提交投诉
const handleSubmitComplaint = async () => {
  if (!selectedEvaluation.value || !selectedEvaluation.value.id) {
    message.error('评价信息不存在')
    return
  }
  
  try {
    await complaintFormRef.value?.validate()
    complaintSubmitting.value = true

    // 先上传图片到腾讯云COS，获取图片URL
    const evidenceImageUrls: string[] = []
    const filesToUpload: File[] = []
    
    for (const file of complaintForm.evidenceImages) {
      if (file.url && !file.url.startsWith('data:')) {
        // 已经是URL（可能是之前上传过的）
        evidenceImageUrls.push(file.url)
      } else if (file.originFileObj) {
        // 需要上传的文件
        filesToUpload.push(file.originFileObj)
      }
    }

    // 如果有需要上传的文件，先上传获取URL
    if (filesToUpload.length > 0) {
      try {
        const uploadResult = await complaintController.uploadEvidenceImages(filesToUpload)
        if (uploadResult?.data?.code === 0 && uploadResult.data.data) {
          evidenceImageUrls.push(...uploadResult.data.data)
        } else {
          message.error(uploadResult?.data?.message || '图片上传失败')
          return
        }
      } catch (uploadError: any) {
        console.error('Failed to upload images:', uploadError)
        message.error(uploadError?.response?.data?.message || uploadError?.message || '图片上传失败')
        return
      }
    }

    // 提交投诉，传递图片URL数组
    const result = await complaintController.addComplaint({
      evaluationId: String(selectedEvaluation.value.id), // 转换为字符串
      type: complaintForm.type!,
      title: complaintForm.title,
      content: complaintForm.content,
      evidenceImages: evidenceImageUrls,
    })

    if (result?.data?.code === 0) {
      message.success('投诉提交成功')
      complaintModalVisible.value = false
      detailModalVisible.value = false
      // 重置表单
      complaintForm.type = undefined
      complaintForm.title = ''
      complaintForm.content = ''
      complaintForm.evidenceImages = []
      // 刷新投诉列表（如果在投诉标签页）
      if (activeTab.value === 'complaints') {
        fetchComplaints(1)
      }
    } else {
      message.error(result?.data?.message || '投诉提交失败')
    }
  } catch (error: any) {
    console.error('Failed to submit complaint:', error)
    if (error?.errorFields) {
      // 表单验证错误
      return
    }
    message.error(error?.response?.data?.message || '投诉提交失败')
  } finally {
    complaintSubmitting.value = false
  }
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadEvaluationList()
}

// 标签页切换
const handleTabChange = (key: string) => {
  activeTab.value = key as 'evaluation' | 'complaints'
  if (key === 'complaints') {
    fetchComplaints(1)
  }
}

// 获取投诉列表
const fetchComplaints = async (page: number = 1) => {
  try {
    complaintLoading.value = true
    const result = await complaintController.pageComplaint({
      pageNum: page,
      pageSize: complaintPagination.pageSize,
      status: complaintFilterStatus.value,
      sortField: 'create_time',
      sortOrder: 'descend', // 按时间降序排列
    } as any)
    
    if (result?.data?.code === 0 && result.data.data) {
      complaintList.value = result.data.data.records || []
      complaintPagination.total = Number(result.data.data.totalRow) || 0
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

// 投诉筛选变化
const handleComplaintFilterChange = () => {
  complaintPagination.current = 1
  fetchComplaints(1)
}

// 获取投诉状态文本
const getComplaintStatusText = (status: number | undefined): string => {
  if (status === 0) return '待处理'
  if (status === 2) return '通过'
  if (status === 3) return '已驳回'
  return '未知'
}

// 获取投诉状态颜色
const getComplaintStatusColor = (status: number | undefined): string => {
  if (status === 0) return 'orange'
  if (status === 2) return 'green'
  if (status === 3) return 'red'
  return 'default'
}

// 获取投诉类型文本
const getComplaintTypeText = (type: number | undefined): string => {
  if (type === 1) return '恶意评价'
  if (type === 2) return '虚假信息'
  if (type === 3) return '其他'
  return '未知'
}

// 格式化投诉日期时间
const formatComplaintDateTime = (dateTime: string | undefined): string => {
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

// 查看投诉详情
const handleViewComplaintDetail = async (record: API.ComplaintVO) => {
  if (!record.id) {
    message.error('投诉ID不存在')
    return
  }
  try {
    complaintDetailLoading.value = true
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
    complaintDetailLoading.value = false
  }
}

// 监听筛选条件变化，重新加载五维数据和标签统计
watch(
  () => [filterEvaluationType.value, filterEvaluationPeriod.value, filterCompanyId.value],
  () => {
    loadDimensionScores()
    loadTagStatistics()
  },
)

onMounted(async () => {
  if (activeTab.value === 'evaluation') {
    await loadEvaluationList()
    // 加载公司选项（在评价列表加载后）
    await loadCompanyOptions()
    // 加载五维评价数据（基于所有符合条件的评价，不分页）
    await loadDimensionScores()
    // 加载标签统计（基于所有符合条件的评价，不分页）
    await loadTagStatistics()
  }
})

// 组件卸载时销毁图表
onUnmounted(() => {
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
})
</script>

<style scoped>
.my-evaluation-container {
  padding: 0;
}

.evaluation-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
}
</style>


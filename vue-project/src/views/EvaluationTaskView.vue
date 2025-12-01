<template>
  <div class="evaluation-task-container">
    <a-page-header title="评价任务" @back="() => $router.push('/home')">
      <template #extra>
        <a-button type="primary" @click="refreshPendingCount">刷新</a-button>
      </template>
    </a-page-header>

    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <a-tab-pane key="pending">
        <template #tab>
          <span>待评价</span>
        </template>
        <!-- 待评价排序和筛选 -->
        <div style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap;">
          <span>排序方式：</span>
          <a-select
            v-model:value="pendingSortField"
            style="width: 150px"
            placeholder="选择排序字段"
            @change="handlePendingSortChange"
          >
            <a-select-option value="">默认排序</a-select-option>
            <a-select-option value="deadline">截止时间</a-select-option>
          </a-select>
          <a-select
            v-model:value="pendingSortOrder"
            style="width: 120px"
            placeholder="排序顺序"
            @change="handlePendingSortChange"
            :disabled="!pendingSortField"
          >
            <a-select-option value="ascend">升序</a-select-option>
            <a-select-option value="descend">降序</a-select-option>
          </a-select>
          <span style="margin-left: 16px;">筛选：</span>
          <a-select
            v-model:value="pendingEvaluationPeriod"
            style="width: 150px"
            placeholder="评价周期"
            allowClear
            @change="handlePendingFilterChange"
          >
            <a-select-option :value="1">季度评价</a-select-option>
            <a-select-option :value="2">年度评价</a-select-option>
            <a-select-option :value="3">离职评价</a-select-option>
            <a-select-option :value="4">临时评价</a-select-option>
          </a-select>
          <a-button @click="resetPendingFilter">重置</a-button>
        </div>
        <a-table
          :columns="pendingColumns"
          :data-source="pendingList"
          :loading="pendingLoading"
          :pagination="pendingPagination"
          @change="handlePendingTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-button type="link" @click="handleEvaluate(record)">评价</a-button>
            </template>
            <template v-else-if="column.key === 'evaluationType'">
              <a-tag :color="getEvaluationTypeColor(record.evaluationType)">
                {{ record.evaluationTypeText || getEvaluationTypeText(record.evaluationType) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'deadline'">
              {{ record.deadline ? new Date(record.deadline).toLocaleString('zh-CN') : '-' }}
            </template>
            <template v-else-if="column.key === 'employeeName'">
              <span>{{ record.employeeName }}</span>
            </template>
            <template v-else-if="column.key === 'departmentName'">
              {{ record.departmentName || '-' }}
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="completed" tab="已完成">
        <!-- 已完成排序和筛选 -->
        <div style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap;">
          <span>排序方式：</span>
          <a-select
            v-model:value="completedSortField"
            style="width: 150px"
            placeholder="选择排序字段"
            @change="handleCompletedSortChange"
          >
            <a-select-option value="">默认排序</a-select-option>
            <a-select-option value="create_time">评价时间</a-select-option>
          </a-select>
          <a-select
            v-model:value="completedSortOrder"
            style="width: 120px"
            placeholder="排序顺序"
            @change="handleCompletedSortChange"
            :disabled="!completedSortField"
          >
            <a-select-option value="ascend">升序</a-select-option>
            <a-select-option value="descend">降序</a-select-option>
          </a-select>
          <span style="margin-left: 16px;">筛选：</span>
          <a-select
            v-model:value="completedEvaluationType"
            style="width: 150px"
            placeholder="评价类型"
            allowClear
            @change="handleCompletedFilterChange"
          >
            <a-select-option :value="1">领导评价</a-select-option>
            <a-select-option :value="2">同事评价</a-select-option>
            <a-select-option :value="3">HR评价</a-select-option>
            <a-select-option :value="4">自评</a-select-option>
          </a-select>
          <a-select
            v-model:value="completedEvaluationPeriod"
            style="width: 150px"
            placeholder="评价周期"
            allowClear
            @change="handleCompletedFilterChange"
          >
            <a-select-option :value="1">季度评价</a-select-option>
            <a-select-option :value="2">年度评价</a-select-option>
            <a-select-option :value="3">离职评价</a-select-option>
            <a-select-option :value="4">临时评价</a-select-option>
          </a-select>
          <a-select
            v-model:value="completedCompanyId"
            style="width: 150px"
            placeholder="评价公司"
            allowClear
            :loading="companyOptionsLoading"
            @change="handleCompletedFilterChange"
          >
            <a-select-option
              v-for="company in companyOptions"
              :key="company.value"
              :value="company.value"
            >
              {{ company.label }}
            </a-select-option>
          </a-select>
          <a-button @click="resetCompletedFilter">重置</a-button>
        </div>
        <a-table
          :columns="completedColumns"
          :data-source="completedList"
          :loading="completedLoading"
          :pagination="completedPagination"
          @change="handleCompletedTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'evaluationType'">
              <a-tag :color="getEvaluationTypeColor(record.evaluationType)">
                {{ getEvaluationTypeText(record.evaluationType) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" @click="handleViewDetail(record)">查看详情</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <!-- 评价弹窗 -->
    <a-modal
      v-model:open="evaluateModalVisible"
      title="评价"
      width="800px"
      :confirm-loading="evaluateSubmitting"
      @ok="handleSubmitEvaluation"
      @cancel="handleCancelEvaluation"
    >
      <a-form :model="evaluateForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="被评价人">
          <a-input v-model:value="evaluateForm.employeeName" disabled />
        </a-form-item>

        <a-form-item label="评价类型">
          <a-input v-model:value="evaluateForm.evaluationTypeText" disabled />
        </a-form-item>

        <a-form-item label="评价维度">
          <div v-for="dimension in dimensions" :key="dimension.id" style="margin-bottom: 16px">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ dimension.name }}</span>
              <a-rate v-model:value="evaluateForm.dimensionScores[dimension.id]" :count="5" />
            </div>
          </div>
        </a-form-item>

        <a-form-item label="评价标签">
          <a-checkbox-group 
            v-model:value="evaluateForm.tagIds" 
            style="display: block; width: 100%;"
          >
            <div v-for="group in groupedTags" :key="group.sortOrder" style="display: flex; margin-bottom: 8px; width: 100%;">
              <div style="flex: 1; padding-right: 8px;">
                <a-checkbox 
                  v-if="group.positiveTag" 
                  :value="group.positiveTag.id"
                >
                  {{ group.positiveTag.name }}
                </a-checkbox>
              </div>
              <div style="flex: 1; padding-left: 8px;">
                <a-checkbox 
                  v-if="group.neutralTag" 
                  :value="group.neutralTag.id"
                >
                  {{ group.neutralTag.name }}
                </a-checkbox>
              </div>
            </div>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="评价内容">
          <a-textarea
            v-model:value="evaluateForm.comment"
            :rows="4"
            placeholder="请输入评价内容"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 评价详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="评价详情"
      width="900px"
      :footer="null"
      @cancel="handleCloseDetailModal"
    >
      <a-spin :spinning="detailLoading">
        <div v-if="evaluationDetail" class="evaluation-detail">
          <!-- 基本信息 -->
          <a-descriptions title="基本信息" :column="2" bordered>
            <a-descriptions-item label="评价人">
              {{ evaluationDetail.evaluatorName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="被评价人">
              {{ evaluationDetail.employeeName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="评价类型">
              <a-tag :color="getEvaluationTypeColor(evaluationDetail.evaluationType || 0)">
                {{ evaluationDetail.evaluationTypeText || '-' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="评价周期">
              {{ evaluationDetail.evaluationPeriodText || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="评价年份" v-if="evaluationDetail.periodYear">
              {{ evaluationDetail.periodYear }}
            </a-descriptions-item>
            <a-descriptions-item label="评价季度" v-if="evaluationDetail.periodQuarter">
              第{{ evaluationDetail.periodQuarter }}季度
            </a-descriptions-item>
            <a-descriptions-item label="评价日期" v-if="evaluationDetail.evaluationDate">
              {{ evaluationDetail.evaluationDate }}
            </a-descriptions-item>
            <a-descriptions-item label="评价时间" v-if="evaluationDetail.createTime">
              {{ new Date(evaluationDetail.createTime).toLocaleString('zh-CN') }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 评价维度雷达图 -->
          <div v-if="evaluationDetail.dimensionScores && evaluationDetail.dimensionScores.length > 0" 
               style="margin-top: 24px;">
            <h3 style="margin-bottom: 16px;">评价维度评分</h3>
            <div ref="radarChartRef" style="width: 100%; height: 400px;"></div>
            <!-- 综合平均评分 -->
            <div v-if="overallAverageScore > 0" 
                 style="margin-top: -34px; padding: 16px; background: white; border-radius: 4px; text-align: center; color: #666; font-size: 12px;">
              综合平均评分: <span style="font-size: 12px; font-weight: bold; color: #1890ff;">{{ overallAverageScore.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 评价标签 -->
          <div v-if="evaluationDetail.tags && evaluationDetail.tags.length > 0" style="margin-top: 24px;">
            <h3 style="margin-bottom: 16px;">评价标签</h3>
            <div>
              <a-tag 
                v-for="tag in evaluationDetail.tags" 
                :key="tag.tagId"
                :color="tag.tagType === 1 ? 'green' : 'orange'"
                style="margin-bottom: 8px;"
              >
                {{ tag.tagName }}
              </a-tag>
            </div>
          </div>

          <!-- 评价内容 -->
          <div v-if="evaluationDetail.comment" style="margin-top: 24px;">
            <h3 style="margin-bottom: 16px;">评价内容</h3>
            <a-typography-paragraph :copyable="{ text: evaluationDetail.comment }">
              {{ evaluationDetail.comment }}
            </a-typography-paragraph>
          </div>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, reactive, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import * as evaluationController from '@/api/evaluationController'
import * as evaluationTaskController from '@/api/evaluationTaskController'
import * as evaluationTagController from '@/api/evaluationTagController'
import * as employeeController from '@/api/employeeController'
import * as companyController from '@/api/companyController'
import { useUserStore } from '@/stores/userStore'
import { useRole } from '@/composables/useRole'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()
const { isHR, isEmployee, isSystemAdmin } = useRole()

const activeTab = ref('pending')
const pendingCount = ref(0)
const pendingLoading = ref(false)
const completedLoading = ref(false)

// 待评价列表
const pendingList = ref<any[]>([])
const pendingPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 已完成列表
const completedList = ref<any[]>([])
const completedPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 待评价排序和筛选
const pendingSortField = ref<string>('')
const pendingSortOrder = ref<string>('ascend') // 默认升序，早截止的在前
const pendingEvaluationPeriod = ref<number | undefined>(undefined) // 评价周期筛选

// 已完成排序和筛选
const completedSortField = ref<string>('')
const completedSortOrder = ref<string>('descend') // 默认降序，最新的在前
const completedEvaluationType = ref<number | undefined>(undefined) // 评价类型筛选
const completedEvaluationPeriod = ref<number | undefined>(undefined) // 评价周期筛选
const completedCompanyId = ref<string | undefined>(undefined) // 评价公司筛选（使用字符串格式，避免精度丢失）
const companyOptions = ref<Array<{ label: string; value: string }>>([])
const companyOptionsLoading = ref(false)

// 评价弹窗
const evaluateModalVisible = ref(false)
const evaluateSubmitting = ref(false)
const evaluateForm = reactive({
  id: undefined as number | undefined,
  employeeId: undefined as number | undefined,
  employeeName: '',
  evaluationType: undefined as number | undefined,
  evaluationTypeText: '',
  evaluationPeriod: undefined as number | undefined,
  periodYear: undefined as number | undefined,
  periodQuarter: undefined as number | undefined,
  dimensionScores: {} as Record<number, number>,
  tagIds: [] as number[],
  comment: '',
})

// 维度列表
const dimensions = ref<Array<{ id: number; name: string }>>([])
// 标签列表
const tags = ref<Array<{ id: number; name: string; type: number; sortOrder: number }>>([])

// 表格列定义
const pendingColumns = [
  { title: '被评价人', key: 'employeeName' },
  { title: '部门', key: 'departmentName' },
  { title: '评价类型', key: 'evaluationType' },
  { title: '评价周期', dataIndex: 'evaluationPeriodText', key: 'evaluationPeriodText' },
  { title: '年份', dataIndex: 'periodYear', key: 'periodYear' },
  { title: '季度', dataIndex: 'periodQuarter', key: 'periodQuarter' },
  { title: '截止时间', key: 'deadline' },
  { title: '操作', key: 'action', width: 100 },
]

const completedColumns = [
  { title: '被评价人', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '评价类型', key: 'evaluationType' },
  { title: '评价周期', dataIndex: 'evaluationPeriodText', key: 'evaluationPeriodText' },
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

// 加载待评价列表
const loadPendingList = async () => {
  try {
    pendingLoading.value = true
    
    // 查询待评价任务列表
    const requestParams: API.EvaluationTaskQueryRequest = {
      pageNum: pendingPagination.current,
      pageSize: pendingPagination.pageSize,
      status: 0, // 待评价
    }
    
    // 添加排序参数
    if (pendingSortField.value) {
      requestParams.sortField = pendingSortField.value
      requestParams.sortOrder = pendingSortOrder.value || 'ascend'
    }
    
    // 添加筛选参数
    if (pendingEvaluationPeriod.value !== undefined && pendingEvaluationPeriod.value !== null) {
      requestParams.evaluationPeriod = pendingEvaluationPeriod.value
    }
    
    const response = await evaluationTaskController.pageEvaluationTasks(requestParams)
    if (response?.data?.code === 0) {
      const data = response.data.data
      pendingList.value = data?.records || []
      pendingPagination.total = data?.totalRow || 0
      pendingCount.value = pendingPagination.total
    }
  } catch (error) {
    console.error('Failed to load pending evaluations:', error)
    message.error('加载待评价列表失败')
  } finally {
    pendingLoading.value = false
  }
}

// 加载已完成列表
const loadCompletedList = async () => {
  try {
    completedLoading.value = true
    
    // HR和Employee都可以作为评价人
    // HR直接使用userId作为evaluatorId
    // Employee也需要使用userId，因为evaluatorId存储的是userId
    const evaluatorId = userStore.userInfo?.id
    
    const requestParams: API.EvaluationQueryRequest = {
      pageNum: completedPagination.current,
      pageSize: completedPagination.pageSize,
      evaluatorId: evaluatorId,
    }
    
    // 添加排序参数
    if (completedSortField.value) {
      requestParams.sortField = completedSortField.value
      requestParams.sortOrder = completedSortOrder.value || 'descend'
    }
    
    // 添加筛选参数
    if (completedEvaluationType.value !== undefined && completedEvaluationType.value !== null) {
      requestParams.evaluationType = completedEvaluationType.value
    }
    if (completedEvaluationPeriod.value !== undefined && completedEvaluationPeriod.value !== null) {
      requestParams.evaluationPeriod = completedEvaluationPeriod.value
    }
    if (completedCompanyId.value !== undefined && completedCompanyId.value !== null && completedCompanyId.value !== '') {
      // @ts-ignore - companyId字段已添加到后端，但类型定义可能未更新
      // 已经是字符串格式，直接使用（与employeeId处理方式一致）
      requestParams.companyId = completedCompanyId.value
    }
    
    const response = await evaluationController.pageEvaluation(requestParams)
    if (response?.data?.code === 0) {
      const data = response.data.data
      completedList.value = data?.records || []
      completedPagination.total = data?.totalRow || 0
    }
  } catch (error) {
    console.error('Failed to load completed evaluations:', error)
    message.error('加载已完成列表失败')
  } finally {
    completedLoading.value = false
  }
}

// 加载维度和标签
const loadDimensionsAndTags = async () => {
  // 维度暂时使用模拟数据（后续可以从后端获取）
  dimensions.value = [
    { id: 1, name: '工作业绩' },
    { id: 2, name: '责任心' },
    { id: 3, name: '专业技能' },
    { id: 4, name: '团队协作' },
    { id: 5, name: '创新能力' },
  ]
  
  // 从后端获取标签列表
  try {
    const response = await evaluationTagController.listActiveTags()
    if (response?.data?.code === 0 && response.data.data) {
      tags.value = response.data.data.map((tag: API.EvaluationTagVO) => ({
        id: tag.id,
        name: tag.name || '',
        type: tag.type || 1,
        sortOrder: tag.sortOrder || 0,
      }))
    } else {
      // 如果获取失败，使用默认标签
      console.warn('获取标签列表失败，使用默认标签')
      tags.value = [
        { id: 1, name: '团队协作', type: 1, sortOrder: 1 },
        { id: 2, name: '创新能力', type: 1, sortOrder: 2 },
        { id: 3, name: '责任心强', type: 1, sortOrder: 3 },
        { id: 4, name: '专业技能突出', type: 1, sortOrder: 4 },
        { id: 5, name: '沟通能力强', type: 1, sortOrder: 5 },
        { id: 9, name: '需提升沟通技巧', type: 2, sortOrder: 5 },
        { id: 10, name: '需加强团队协作', type: 2, sortOrder: 1 },
      ]
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
    // 如果获取失败，使用默认标签
    tags.value = [
      { id: 1, name: '团队协作', type: 1, sortOrder: 1 },
      { id: 2, name: '创新能力', type: 1, sortOrder: 2 },
      { id: 3, name: '责任心强', type: 1, sortOrder: 3 },
      { id: 4, name: '专业技能突出', type: 1, sortOrder: 4 },
      { id: 5, name: '沟通能力强', type: 1, sortOrder: 5 },
      { id: 9, name: '需提升沟通技巧', type: 2, sortOrder: 5 },
      { id: 10, name: '需加强团队协作', type: 2, sortOrder: 1 },
    ]
  }
}

// 按 sortOrder 分组的标签
const groupedTags = computed(() => {
  const groups: Map<number, { sortOrder: number; positiveTag?: { id: number; name: string; type: number; sortOrder: number }; neutralTag?: { id: number; name: string; type: number; sortOrder: number } }> = new Map()
  
  tags.value.forEach(tag => {
    const sortOrder = tag.sortOrder || 0
    if (!groups.has(sortOrder)) {
      groups.set(sortOrder, { sortOrder })
    }
    const group = groups.get(sortOrder)!
    if (tag.type === 1) {
      group.positiveTag = tag
    } else if (tag.type === 2) {
      group.neutralTag = tag
    }
  })
  
  // 按 sortOrder 排序
  return Array.from(groups.values()).sort((a, b) => a.sortOrder - b.sortOrder)
})

// 防止循环更新的标志
const isUpdatingTagIds = ref(false)

// 监听标签选择变化，实现相同 sortOrder 的标签互斥
watch(
  () => evaluateForm.tagIds,
  (newTagIds, oldTagIds) => {
    // 如果正在更新中，忽略此次变化（避免循环触发）
    if (isUpdatingTagIds.value) {
      return
    }
    
    // 如果没有旧值（初始化），不需要处理
    if (!oldTagIds) {
      return
    }
    
    // 找出新选中的标签（在 newTagIds 中但不在 oldTagIds 中）
    const newlySelected = newTagIds.filter(id => !oldTagIds.includes(id))
    
    // 如果有新选中的标签，需要处理互斥逻辑
    if (newlySelected.length > 0) {
      isUpdatingTagIds.value = true
      
      const finalTagIds: number[] = [...newTagIds]
      
      // 对于每个新选中的标签，取消同 sortOrder 的其他标签
      newlySelected.forEach(tagId => {
        const tag = tags.value.find(t => t.id === tagId)
        if (tag) {
          const sortOrder = tag.sortOrder || 0
          // 找出同 sortOrder 的其他标签ID（包括正面和中性标签）
          const sameSortOrderTagIds = tags.value
            .filter(t => t.sortOrder === sortOrder && t.id !== tagId)
            .map(t => t.id)
          
          // 从选中列表中移除同 sortOrder 的其他标签
          sameSortOrderTagIds.forEach(id => {
            const index = finalTagIds.indexOf(id)
            if (index > -1) {
              finalTagIds.splice(index, 1)
            }
          })
        }
      })
      
      // 更新选中的标签ID列表
      nextTick(() => {
        evaluateForm.tagIds = finalTagIds
        isUpdatingTagIds.value = false
      })
    }
  },
  { deep: true }
)

// 处理评价
const handleEvaluate = (record: API.EvaluationTaskVO) => {
  evaluateForm.id = undefined
  evaluateForm.employeeId = record.employeeId
  evaluateForm.employeeName = record.employeeName || ''
  evaluateForm.evaluationType = record.evaluationType
  evaluateForm.evaluationTypeText = record.evaluationTypeText || getEvaluationTypeText(record.evaluationType || 0)
  evaluateForm.evaluationPeriod = record.evaluationPeriod || 4
  evaluateForm.periodYear = record.periodYear || new Date().getFullYear()
  evaluateForm.periodQuarter = record.periodQuarter
  evaluateForm.dimensionScores = {}
  evaluateForm.tagIds = []
  evaluateForm.comment = ''
  evaluateModalVisible.value = true
}

// 提交评价
const handleSubmitEvaluation = async () => {
  try {
    evaluateSubmitting.value = true

    // 构建维度评分
    const dimensionScores = Object.entries(evaluateForm.dimensionScores)
      .filter(([_, score]) => score > 0)
      .map(([dimensionId, score]) => ({
        dimensionId: Number(dimensionId),
        score: score,
      }))

    if (dimensionScores.length === 0) {
      message.warning('请至少为一个维度打分')
      return
    }

    const requestData: API.EvaluationAddRequest = {
      employeeId: evaluateForm.employeeId,
      comment: evaluateForm.comment,
      evaluationType: evaluateForm.evaluationType,
      evaluationPeriod: evaluateForm.evaluationPeriod,
      periodYear: evaluateForm.periodYear,
      periodQuarter: evaluateForm.periodQuarter,
      dimensionScores: dimensionScores,
      tagIds: evaluateForm.tagIds,
    }

    const response = await evaluationController.addEvaluation(requestData)
    if (response?.data?.code === 0) {
      message.success('评价提交成功')
      evaluateModalVisible.value = false
      await loadPendingList()
      await loadCompletedList()
      await refreshPendingCount()
    } else {
      message.error(response?.data?.message || '评价提交失败')
    }
  } catch (error) {
    console.error('Failed to submit evaluation:', error)
    message.error('评价提交失败')
  } finally {
    evaluateSubmitting.value = false
  }
}

// 取消评价
const handleCancelEvaluation = () => {
  evaluateModalVisible.value = false
}

// 评价详情弹窗
const detailModalVisible = ref(false)
const detailLoading = ref(false)
const evaluationDetail = ref<API.EvaluationDetailVO | null>(null)
const radarChartRef = ref<HTMLDivElement | null>(null)
let radarChart: echarts.ECharts | null = null

// 综合平均评分
const overallAverageScore = computed(() => {
  if (!evaluationDetail.value?.dimensionScores || evaluationDetail.value.dimensionScores.length === 0) {
    return 0
  }
  const scores = evaluationDetail.value.dimensionScores
  const sum = scores.reduce((acc, ds) => acc + (ds.score || 0), 0)
  return sum / scores.length
})

// 查看详情
const handleViewDetail = async (record: any) => {
  try {
    detailLoading.value = true
    detailModalVisible.value = true
    
    // 调用详情接口
    const response = await evaluationController.getEvaluationDetail({
      id: record.id,
    })
    
    if (response?.data?.code === 0) {
      evaluationDetail.value = response.data.data || null
    } else {
      message.error(response?.data?.message || '获取评价详情失败')
      detailModalVisible.value = false
    }
  } catch (error) {
    console.error('Failed to load evaluation detail:', error)
    message.error('获取评价详情失败')
    detailModalVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 刷新待评价数量
const refreshPendingCount = async () => {
  try {
    const response = await evaluationTaskController.getPendingTaskCount()
    if (response?.data?.code === 0) {
      pendingCount.value = response.data.data || 0
    }
  } catch (error) {
    console.error('Failed to refresh pending count:', error)
  }
}

// 标签页切换
const handleTabChange = (key: string) => {
  if (key === 'pending') {
    loadPendingList()
  } else if (key === 'completed') {
    loadCompletedList()
  }
}

// 表格分页变化
const handlePendingTableChange = (pagination: any) => {
  pendingPagination.current = pagination.current
  pendingPagination.pageSize = pagination.pageSize
  loadPendingList()
}

const handleCompletedTableChange = (pagination: any) => {
  completedPagination.current = pagination.current
  completedPagination.pageSize = pagination.pageSize
  loadCompletedList()
}

// 待评价排序变化处理
const handlePendingSortChange = () => {
  pendingPagination.current = 1 // 重置到第一页
  loadPendingList()
}

// 待评价筛选变化处理
const handlePendingFilterChange = () => {
  pendingPagination.current = 1 // 重置到第一页
  loadPendingList()
}

// 重置待评价排序和筛选
const resetPendingFilter = () => {
  pendingSortField.value = ''
  pendingSortOrder.value = 'ascend'
  pendingEvaluationPeriod.value = undefined
  pendingPagination.current = 1
  loadPendingList()
}

// 已完成排序变化处理
const handleCompletedSortChange = () => {
  completedPagination.current = 1 // 重置到第一页
  loadCompletedList()
}

// 已完成筛选变化处理
const handleCompletedFilterChange = () => {
  completedPagination.current = 1 // 重置到第一页
  loadCompletedList()
  // 如果详情弹窗打开，需要重新加载详情以更新雷达图
  if (detailModalVisible.value && evaluationDetail.value?.id) {
    handleViewDetail({ id: evaluationDetail.value.id })
  }
}

// 重置已完成排序和筛选
const resetCompletedFilter = () => {
  completedSortField.value = ''
  completedSortOrder.value = 'descend'
  completedEvaluationType.value = undefined
  completedEvaluationPeriod.value = undefined
  completedCompanyId.value = undefined
  completedPagination.current = 1
  loadCompletedList()
}

// 加载公司选项（只显示评价表中存在的公司）
const loadCompanyOptions = async () => {
  try {
    companyOptionsLoading.value = true
    // 先加载所有已完成的评价，获取所有companyId
    // HR和Employee都可以作为评价人
    const evaluatorId = userStore.userInfo?.id
    
    // 获取所有已完成的评价（不分页）以提取companyId
    const allEvaluationsResponse = await evaluationController.pageEvaluation({
      pageNum: 1,
      pageSize: 10000, // 获取所有评价
      evaluatorId: evaluatorId,
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

// 绘制雷达图
const renderRadarChart = () => {
  if (!radarChartRef.value || !evaluationDetail.value?.dimensionScores) {
    return
  }

  // 销毁旧图表
  if (radarChart) {
    radarChart.dispose()
  }

  // 创建新图表
  radarChart = echarts.init(radarChartRef.value)

  const dimensionScores = evaluationDetail.value.dimensionScores || []
  
  // 准备数据 - 按dimensionId排序，确保顺序一致
  const sortedDimensionScores = [...dimensionScores].sort((a, b) => (a.dimensionId || 0) - (b.dimensionId || 0))
  
  // 准备indicators，保存索引信息
  const indicators = sortedDimensionScores.map((ds, index) => {
    const name = ds.dimensionName && ds.dimensionName.trim() !== '' 
      ? ds.dimensionName.trim()
      : `维度${ds.dimensionId}`
    return {
      name: name,
      max: 5,
      index: index, // 保存索引用于formatter
    }
  })

  const values = sortedDimensionScores.map((ds) => ds.score || 0)

  const option: echarts.EChartsOption = {
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
          const dimensionScore = index >= 0 && index < sortedDimensionScores.length ? sortedDimensionScores[index] : null
          if (dimensionScore && dimensionScore.score !== undefined) {
            score = dimensionScore.score.toFixed(2)
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
            value: values,
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

  // 响应式调整
  window.addEventListener('resize', () => {
    radarChart?.resize()
  })
}

// 关闭详情弹窗
const handleCloseDetailModal = () => {
  evaluationDetail.value = null
  // 销毁图表
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
}

// 监听评价详情变化，绘制雷达图
watch(
  () => [evaluationDetail.value, detailModalVisible.value],
  async () => {
    if (detailModalVisible.value && evaluationDetail.value?.dimensionScores) {
      await nextTick()
      renderRadarChart()
    } else if (!detailModalVisible.value) {
      // 弹窗关闭时清理图表
      if (radarChart) {
        radarChart.dispose()
        radarChart = null
      }
    }
  },
  { deep: true }
)

onMounted(async () => {
  // 系统管理员不能访问评价任务页面
  if (isSystemAdmin()) {
    message.warning('系统管理员不能访问评价任务页面')
    router.push('/home')
    return
  }
  await loadDimensionsAndTags()
  await refreshPendingCount()
  await loadPendingList()
  await loadCompletedList()
  await loadCompanyOptions()
})

onBeforeUnmount(() => {
  // 组件卸载时清理图表
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
})
</script>

<style scoped>
.evaluation-task-container {
  padding: 24px;
}
</style>


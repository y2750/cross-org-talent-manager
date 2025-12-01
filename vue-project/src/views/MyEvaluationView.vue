<template>
  <div class="my-evaluation-container">
    <a-page-header title="我的评价" @back="() => $router.push('/home')" />

    <a-spin :spinning="loading">
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

      <!-- 评价详情弹窗 -->
      <a-modal
        v-model:open="detailModalVisible"
        title="评价详情"
        width="700px"
        :footer="null"
      >
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
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, nextTick, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import * as evaluationController from '@/api/evaluationController'
import * as employeeController from '@/api/employeeController'
import * as companyController from '@/api/companyController'
import { useUserStore } from '@/stores/userStore'
import { onUnmounted } from 'vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const loading = ref(false)
const radarChartRef = ref<HTMLDivElement>()
let radarChart: echarts.ECharts | null = null
// 存储当前悬浮的指标索引（用于雷达图tooltip）

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

    // 添加排序参数
    if (sortField.value) {
      requestParams.sortField = sortField.value
      requestParams.sortOrder = sortOrder.value || 'descend'
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

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadEvaluationList()
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
  await loadEvaluationList()
  // 加载公司选项（在评价列表加载后）
  await loadCompanyOptions()
  // 加载五维评价数据（基于所有符合条件的评价，不分页）
  await loadDimensionScores()
  // 加载标签统计（基于所有符合条件的评价，不分页）
  await loadTagStatistics()
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
  padding: 24px;
}
</style>


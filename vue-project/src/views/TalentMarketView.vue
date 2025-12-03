<script setup lang="ts">
import { onMounted, onActivated, reactive, ref, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  StarOutlined,
  StarFilled,
  EyeOutlined,
  TeamOutlined,
  SettingOutlined,
  BarChartOutlined,
  SwapOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import * as talentMarketController from '@/api/talentMarketController'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const talentList = ref<any[]>([])
const total = ref(0)
const tagOptions = ref<any[]>([])
const unlockPrices = ref<any[]>([])
const activeTab = ref('search')
const selectedTalents = ref<string[]>([]) // 用于对比的人才ID

// 搜索参数
const searchParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  occupation: '',
  occupations: [] as string[],
  minAverageScore: undefined as number | undefined,
  maxAverageScore: undefined as number | undefined,
  includeTagIds: [] as string[],
  excludeTagIds: [] as string[],
  evaluationKeyword: '',
  excludeReasonKeywords: [] as string[],
  gender: undefined as string | undefined,
  status: undefined as string | undefined, // 'working'=在职, 'left'=离职, undefined=不限
  excludeOwnCompany: false,
  excludeMajorIncident: false,
  minAttendanceRate: undefined as number | undefined,
  sortField: 'averageScore',
  sortOrder: 'desc',
})

// 高级搜索展开状态
const advancedSearchVisible = ref(false)

// 是否已经确认过积分扣除（用于避免返回后重复弹出确认）
const hasConfirmedCost = ref(false)

// 保存搜索条件到 sessionStorage
const saveSearchParams = () => {
  try {
    const paramsToSave = {
      ...searchParams,
      // 将数组和对象转换为 JSON 字符串
      occupations: searchParams.occupations,
      includeTagIds: searchParams.includeTagIds,
      excludeTagIds: searchParams.excludeTagIds,
      excludeReasonKeywords: searchParams.excludeReasonKeywords,
      pageNum: searchParams.pageNum,
    }
    sessionStorage.setItem('talentMarket_searchParams', JSON.stringify(paramsToSave))
    sessionStorage.setItem('talentMarket_advancedSearchVisible', String(advancedSearchVisible.value))
    sessionStorage.setItem('talentMarket_hasConfirmedCost', String(hasConfirmedCost.value))
  } catch (error) {
    console.error('Failed to save search params:', error)
  }
}

// 从 sessionStorage 恢复搜索条件
const restoreSearchParams = () => {
  try {
    const savedParams = sessionStorage.getItem('talentMarket_searchParams')
    const savedAdvancedVisible = sessionStorage.getItem('talentMarket_advancedSearchVisible')
    
    if (savedParams) {
      const params = JSON.parse(savedParams)
      // 恢复搜索参数
      Object.assign(searchParams, {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 12,
        keyword: params.keyword || '',
        occupation: params.occupation || '',
        occupations: params.occupations || [],
        minAverageScore: params.minAverageScore,
        maxAverageScore: params.maxAverageScore,
        includeTagIds: params.includeTagIds || [],
        excludeTagIds: params.excludeTagIds || [],
        evaluationKeyword: params.evaluationKeyword || '',
        excludeReasonKeywords: params.excludeReasonKeywords || [],
        gender: params.gender,
        status: params.status,
        excludeOwnCompany: params.excludeOwnCompany || false,
        excludeMajorIncident: params.excludeMajorIncident || false,
        minAttendanceRate: params.minAttendanceRate,
        sortField: params.sortField || 'averageScore',
        sortOrder: params.sortOrder || 'desc',
      })
    }
    
    if (savedAdvancedVisible !== null) {
      advancedSearchVisible.value = savedAdvancedVisible === 'true'
    }
    
    // 恢复积分确认标记
    const savedHasConfirmedCost = sessionStorage.getItem('talentMarket_hasConfirmedCost')
    if (savedHasConfirmedCost !== null) {
      hasConfirmedCost.value = savedHasConfirmedCost === 'true'
    }
  } catch (error) {
    console.error('Failed to restore search params:', error)
  }
}

// 收藏列表
const bookmarkedTalents = ref<any[]>([])
const bookmarkLoading = ref(false)
const bookmarkTotal = ref(0)
const bookmarkParams = reactive({
  pageNum: 1,
  pageSize: 12,
})

// 推荐列表
const recommendedTalents = ref<any[]>([])
const recommendLoading = ref(false)
const recommendTotal = ref(0)
const recommendParams = reactive({
  pageNum: 1,
  pageSize: 12,
})

// 浏览历史
const viewHistory = ref<any[]>([])
const viewHistoryLoading = ref(false)
const viewHistoryTotal = ref(0)
const viewHistoryParams = reactive({
  pageNum: 1,
  pageSize: 10,
})

// 浏览统计
const viewStatistics = ref<any>(null)
const statisticsLoading = ref(false)

// 企业偏好
const preference = ref<any>(null)
const preferenceModalVisible = ref(false)
const preferenceForm = reactive({
  preferredOccupations: [] as string[],
  preferredTagIds: [] as string[],
  excludedTagIds: [] as string[],
  minScore: undefined as number | undefined,
  excludeMajorIncident: true,
  minAttendanceRate: undefined as number | undefined,
})

// 获取标签列表
const fetchTags = async () => {
  try {
    const res = await talentMarketController.getAllTags()
    if (res?.data?.code === 0 && res.data.data) {
      tagOptions.value = res.data.data.map((tag: any) => ({
        label: tag.name,
        value: String(tag.id),
        type: tag.type,
      }))
    }
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

// 获取解锁价格配置
const fetchUnlockPrices = async () => {
  try {
    const res = await talentMarketController.getUnlockPriceConfigs()
    if (res?.data?.code === 0 && res.data.data) {
      unlockPrices.value = res.data.data
    }
  } catch (error) {
    console.error('获取价格配置失败:', error)
  }
}

// 高级搜索积分预估
const advancedSearchCost = ref<number>(0)

// 构建搜索参数
const buildSearchParams = (skipPointDeduction: boolean = false) => {
  const params: any = {
    pageNum: searchParams.pageNum,
    pageSize: searchParams.pageSize,
    sortField: searchParams.sortField,
    sortOrder: searchParams.sortOrder,
  }

  if (searchParams.keyword) params.keyword = searchParams.keyword
  if (searchParams.occupation) params.occupation = searchParams.occupation
  if (searchParams.occupations.length > 0) params.occupations = searchParams.occupations
  if (searchParams.minAverageScore !== undefined) params.minAverageScore = searchParams.minAverageScore
  if (searchParams.maxAverageScore !== undefined) params.maxAverageScore = searchParams.maxAverageScore
  if (searchParams.includeTagIds.length > 0) params.includeTagIds = searchParams.includeTagIds.map(id => parseInt(id))
  if (searchParams.excludeTagIds.length > 0) params.excludeTagIds = searchParams.excludeTagIds.map(id => parseInt(id))
  if (searchParams.evaluationKeyword) params.evaluationKeyword = searchParams.evaluationKeyword
  if (searchParams.excludeReasonKeywords.length > 0) params.excludeReasonKeywords = searchParams.excludeReasonKeywords
  if (searchParams.gender) params.gender = searchParams.gender
  // 在职状态筛选
  if (searchParams.status === 'working') {
    params.onlyWorking = true
  } else if (searchParams.status === 'left') {
    params.onlyLeft = true
  }
  if (searchParams.excludeOwnCompany) params.excludeOwnCompany = true
  if (searchParams.excludeMajorIncident) params.excludeMajorIncident = true
  if (searchParams.minAttendanceRate !== undefined) params.minAttendanceRate = searchParams.minAttendanceRate
  
  // 如果需要跳过积分扣除（恢复搜索条件时不重复扣除）
  if (skipPointDeduction) {
    params.skipPointDeduction = true
  }

  return params
}

// 检查是否使用了高级搜索功能
const hasAdvancedSearchFeatures = () => {
  return (
    searchParams.includeTagIds.length > 0 ||
    searchParams.excludeTagIds.length > 0 ||
    !!searchParams.evaluationKeyword ||
    searchParams.excludeReasonKeywords.length > 0 ||
    searchParams.excludeMajorIncident ||
    searchParams.minAttendanceRate !== undefined
  )
}

// 执行实际搜索
const doSearch = async (skipPointDeduction: boolean = false) => {
  try {
    loading.value = true
    const params = buildSearchParams(skipPointDeduction)

    const res = await talentMarketController.searchTalents(params)
    if (res?.data?.code === 0 && res.data.data) {
      talentList.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    }
    
    // 搜索成功后保存搜索条件
    saveSearchParams()
  } catch (error: any) {
    console.error('搜索失败:', error)
    const errorMsg = error?.response?.data?.message || '搜索失败'
    message.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 用户主动点击搜索按钮
const handleManualSearch = () => {
  // 用户主动搜索时，重置积分确认标记（因为可能是新的搜索条件）
  // 这样会触发重新确认积分扣除
  if (searchParams.pageNum === 1) {
    hasConfirmedCost.value = false
  }
  searchTalents()
}

// 搜索人才
const searchTalents = async () => {
  // 如果是第一页且使用了高级搜索功能，且未确认过积分扣除，先获取积分预估并确认
  if (searchParams.pageNum === 1 && hasAdvancedSearchFeatures() && !hasConfirmedCost.value) {
    try {
      const params = buildSearchParams()
      const costRes = await talentMarketController.getAdvancedSearchCostPreview(params)
      const cost = costRes?.data?.data || 0
      
      if (cost > 0) {
        Modal.confirm({
          title: '高级搜索积分消耗',
          content: `此次高级搜索将消耗 ${cost} 积分，确定要继续吗？`,
          okText: '确定搜索',
          cancelText: '取消',
          onOk: () => {
            // 确认后设置标记并保存
            hasConfirmedCost.value = true
            saveSearchParams()
            // 确认后正常扣除积分
            doSearch(false)
          },
        })
        return
      }
    } catch (error) {
      console.error('获取积分预估失败:', error)
      // 如果预估失败，仍然允许搜索
    }
  }
  
  // 如果已经确认过积分扣除（恢复搜索条件），跳过积分扣除
  const shouldSkipDeduction = hasConfirmedCost.value
  doSearch(shouldSkipDeduction)
}

// 重置搜索
const resetSearch = () => {
  searchParams.pageNum = 1
  searchParams.keyword = ''
  searchParams.occupation = ''
  searchParams.occupations = []
  searchParams.minAverageScore = undefined
  searchParams.maxAverageScore = undefined
  searchParams.includeTagIds = []
  searchParams.excludeTagIds = []
  searchParams.evaluationKeyword = ''
  searchParams.excludeReasonKeywords = []
  searchParams.gender = undefined
  searchParams.status = undefined
  searchParams.excludeOwnCompany = false
  searchParams.excludeMajorIncident = false
  searchParams.minAttendanceRate = undefined
  advancedSearchVisible.value = false
  
  // 重置积分确认标记
  hasConfirmedCost.value = false
  
  // 清除保存的搜索条件
  sessionStorage.removeItem('talentMarket_searchParams')
  sessionStorage.removeItem('talentMarket_advancedSearchVisible')
  sessionStorage.removeItem('talentMarket_hasConfirmedCost')
  
  searchTalents()
}

// 分页改变
const handlePageChange = (page: number, pageSize: number) => {
  searchParams.pageNum = page
  searchParams.pageSize = pageSize
  // 分页改变时也保存搜索条件
  saveSearchParams()
  searchTalents()
}

// 查看人才详情
const viewTalentDetail = (employeeId: string) => {
  // 保存搜索条件
  saveSearchParams()
  
  // 记录浏览
  talentMarketController.recordView({
    employeeId,
    viewSource: 'search',
    searchKeyword: searchParams.keyword || undefined,
  })
  router.push(`/talent-market/detail/${employeeId}`)
}

// 收藏/取消收藏
const toggleBookmark = async (talent: any) => {
  try {
    const employeeId = String(talent.id)
    if (talent.bookmarked) {
      await talentMarketController.unbookmarkTalent({ employeeId })
      talent.bookmarked = false
      message.success('已取消收藏')
    } else {
      await talentMarketController.bookmarkTalent({ employeeId })
      talent.bookmarked = true
      message.success('收藏成功')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

// 选择人才进行对比
const toggleCompareSelect = (talent: any) => {
  const id = String(talent.id)
  const index = selectedTalents.value.indexOf(id)
  if (index > -1) {
    selectedTalents.value.splice(index, 1)
  } else {
    if (selectedTalents.value.length >= 5) {
      message.warning('最多选择5个人才进行对比')
      return
    }
    selectedTalents.value.push(id)
  }
}

// 开始对比
const startCompare = () => {
  if (selectedTalents.value.length < 2) {
    message.warning('请至少选择2个人才进行对比')
    return
  }
  router.push({
    path: '/talent-market/compare',
    query: { ids: selectedTalents.value.join(',') },
  })
}

// 获取收藏列表
const fetchBookmarks = async () => {
  try {
    bookmarkLoading.value = true
    const res = await talentMarketController.getBookmarkedTalents({
      pageNum: bookmarkParams.pageNum,
      pageSize: bookmarkParams.pageSize,
    })
    if (res?.data?.code === 0 && res.data.data) {
      bookmarkedTalents.value = res.data.data.records || []
      bookmarkTotal.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    bookmarkLoading.value = false
  }
}

// 获取推荐列表
const fetchRecommended = async () => {
  try {
    recommendLoading.value = true
    const res = await talentMarketController.getRecommendedTalents({
      pageNum: recommendParams.pageNum,
      pageSize: recommendParams.pageSize,
    })
    if (res?.data?.code === 0 && res.data.data) {
      recommendedTalents.value = res.data.data.records || []
      recommendTotal.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取推荐列表失败:', error)
  } finally {
    recommendLoading.value = false
  }
}

// 获取浏览历史
const fetchViewHistory = async () => {
  try {
    viewHistoryLoading.value = true
    const res = await talentMarketController.getViewHistory({
      pageNum: viewHistoryParams.pageNum,
      pageSize: viewHistoryParams.pageSize,
    })
    if (res?.data?.code === 0 && res.data.data) {
      viewHistory.value = res.data.data.records || []
      viewHistoryTotal.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取浏览历史失败:', error)
  } finally {
    viewHistoryLoading.value = false
  }
}

// 获取浏览统计
const fetchViewStatistics = async () => {
  try {
    statisticsLoading.value = true
    const res = await talentMarketController.getViewStatistics()
    if (res?.data?.code === 0 && res.data.data) {
      viewStatistics.value = res.data.data
    }
  } catch (error) {
    console.error('获取浏览统计失败:', error)
  } finally {
    statisticsLoading.value = false
  }
}

// 清除浏览记录
const clearHistory = () => {
  Modal.confirm({
    title: '确认清除',
    content: '确定要清除所有浏览记录吗？',
    onOk: async () => {
      try {
        await talentMarketController.clearViewHistory()
        message.success('已清除浏览记录')
        fetchViewHistory()
        fetchViewStatistics()
      } catch (error) {
        message.error('清除失败')
      }
    },
  })
}

// 获取企业偏好
const fetchPreference = async () => {
  try {
    const res = await talentMarketController.getPreference()
    if (res?.data?.code === 0 && res.data.data) {
      preference.value = res.data.data
      // 填充表单
      preferenceForm.preferredOccupations = res.data.data.preferredOccupations || []
      preferenceForm.preferredTagIds = (res.data.data.preferredTags || []).map((t: any) => String(t.id))
      preferenceForm.excludedTagIds = (res.data.data.excludedTags || []).map((t: any) => String(t.id))
      preferenceForm.minScore = res.data.data.minScore
      preferenceForm.excludeMajorIncident = res.data.data.excludeMajorIncident ?? true
      preferenceForm.minAttendanceRate = res.data.data.minAttendanceRate
    }
  } catch (error) {
    console.error('获取偏好失败:', error)
  }
}

// 保存企业偏好
const savePreference = async () => {
  try {
    await talentMarketController.savePreference({
      preferredOccupations: preferenceForm.preferredOccupations,
      preferredTagIds: preferenceForm.preferredTagIds.map(id => parseInt(id)),
      excludedTagIds: preferenceForm.excludedTagIds.map(id => parseInt(id)),
      minScore: preferenceForm.minScore,
      excludeMajorIncident: preferenceForm.excludeMajorIncident,
      minAttendanceRate: preferenceForm.minAttendanceRate,
    })
    message.success('偏好设置已保存')
    preferenceModalVisible.value = false
    fetchPreference()
    fetchRecommended()
  } catch (error) {
    message.error('保存失败')
  }
}

// Tab 切换
const handleTabChange = (key: string) => {
  activeTab.value = key
  if (key === 'bookmarks') {
    fetchBookmarks()
  } else if (key === 'recommend') {
    fetchRecommended()
    fetchPreference()
  } else if (key === 'history') {
    fetchViewHistory()
    fetchViewStatistics()
  }
}

// 正面标签
const positiveTagOptions = computed(() => tagOptions.value.filter((t) => t.type === 1))

// 中性标签
const neutralTagOptions = computed(() => tagOptions.value.filter((t) => t.type === 2))

// 格式化评分
const formatScore = (score: number | undefined) => {
  if (score === undefined || score === null) return '-'
  return score.toFixed(1)
}

// 初始化页面数据
const initPage = async () => {
  // 恢复搜索条件
  restoreSearchParams()
  
  await fetchTags()
  await fetchUnlockPrices()
  
  // 如果有保存的搜索条件，使用保存的条件进行搜索
  // 否则使用默认条件
  searchTalents()
}

onMounted(() => {
  initPage()
})

// 当页面被激活时（keep-alive 场景），恢复搜索条件并执行搜索
onActivated(() => {
  const hadSavedParams = !!sessionStorage.getItem('talentMarket_searchParams')
  restoreSearchParams()
  // 如果有保存的搜索条件，恢复后需要重新搜索
  if (hadSavedParams) {
    searchTalents()
  }
})
</script>

<template>
  <div class="talent-market">
    <a-page-header title="人才市场" sub-title="发现优秀人才，助力企业发展">
      <template #extra>
        <a-space>
          <a-button
            v-if="selectedTalents.length >= 2"
            type="primary"
            @click="startCompare"
          >
            <SwapOutlined />
            对比已选 ({{ selectedTalents.length }})
          </a-button>
          <a-button @click="preferenceModalVisible = true">
            <SettingOutlined />
            招聘偏好
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" class="market-tabs">
      <!-- 搜索人才 -->
      <a-tab-pane key="search">
        <template #tab>
          <span><SearchOutlined /> 搜索人才</span>
        </template>

        <a-card class="search-card">
          <a-form layout="inline" class="search-form">
            <a-form-item label="姓名">
              <a-input
                v-model:value="searchParams.keyword"
                placeholder="请输入姓名"
                allow-clear
                style="width: 150px"
                @press-enter="searchTalents"
              />
            </a-form-item>
            <a-form-item label="职位">
              <a-input
                v-model:value="searchParams.occupation"
                placeholder="请输入职位"
                allow-clear
                style="width: 150px"
                @press-enter="searchTalents"
              />
            </a-form-item>
            <a-form-item label="最低评分">
              <a-input-number
                v-model:value="searchParams.minAverageScore"
                :min="0"
                :max="5"
                :step="0.5"
                placeholder="0-5"
                style="width: 100px"
              />
            </a-form-item>
            <a-form-item label="性别">
              <a-select
                v-model:value="searchParams.gender"
                placeholder="不限"
                allow-clear
                style="width: 100px"
              >
                <a-select-option value="男">男</a-select-option>
                <a-select-option value="女">女</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="在职状态">
              <a-select
                v-model:value="searchParams.status"
                placeholder="不限"
                allow-clear
                style="width: 100px"
              >
                <a-select-option value="working">在职</a-select-option>
                <a-select-option value="left">离职</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-checkbox v-model:checked="searchParams.excludeOwnCompany">
                排除本公司员工
              </a-checkbox>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleManualSearch">
                  <SearchOutlined /> 搜索
                </a-button>
                <a-button @click="resetSearch">
                  <ReloadOutlined /> 重置
                </a-button>
                <a-button type="link" @click="advancedSearchVisible = !advancedSearchVisible">
                  {{ advancedSearchVisible ? '收起' : '高级搜索' }}
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>

          <!-- 高级搜索 -->
          <div v-if="advancedSearchVisible" class="advanced-search">
            <a-divider />
            <a-row :gutter="[16, 16]">
              <a-col :span="8">
                <a-form-item label="包含标签">
                  <a-select
                    v-model:value="searchParams.includeTagIds"
                    mode="multiple"
                    placeholder="选择必须包含的标签"
                    :options="tagOptions"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="排除标签">
                  <a-select
                    v-model:value="searchParams.excludeTagIds"
                    mode="multiple"
                    placeholder="选择要排除的标签"
                    :options="tagOptions"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="评价关键词">
                  <a-input
                    v-model:value="searchParams.evaluationKeyword"
                    placeholder="搜索评价内容"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="最低出勤率">
                  <a-input-number
                    v-model:value="searchParams.minAttendanceRate"
                    :min="0"
                    :max="100"
                    placeholder="0-100"
                    style="width: 100%"
                    addon-after="%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="在职状态">
                  <a-radio-group v-model:value="searchParams.onlyWorking">
                    <a-radio :value="false">全部</a-radio>
                    <a-radio :value="true">仅在职</a-radio>
                  </a-radio-group>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item>
                  <a-checkbox v-model:checked="searchParams.excludeMajorIncident">
                    排除有重大违纪记录
                  </a-checkbox>
                </a-form-item>
              </a-col>
            </a-row>
          </div>
        </a-card>

        <!-- 人才列表 -->
        <a-spin :spinning="loading">
          <div class="talent-grid">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="talent in talentList"
                :key="talent.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card
                  hoverable
                  class="talent-card"
                  :class="{ 'selected': selectedTalents.includes(String(talent.id)) }"
                >
                  <template #cover>
                    <div class="talent-cover" @click="viewTalentDetail(String(talent.id))">
                      <a-avatar
                        :size="80"
                        :src="talent.photoUrl"
                        class="talent-avatar"
                      >
                        {{ talent.name?.charAt(0) }}
                      </a-avatar>
                      <div class="talent-score">
                        <a-rate :value="talent.averageScore || 0" disabled allow-half :count="5" />
                        <span class="score-text">{{ formatScore(talent.averageScore) }}</span>
                      </div>
                    </div>
                  </template>

                  <a-card-meta>
                    <template #title>
                      <div class="talent-name" @click="viewTalentDetail(String(talent.id))">
                        {{ talent.name }}
                        <a-tag v-if="talent.status" color="green" size="small">在职</a-tag>
                        <a-tag v-else color="orange" size="small">离职</a-tag>
                      </div>
                    </template>
                    <template #description>
                      <div class="talent-info">
                        <div class="occupation-list">
                          <strong>职位：</strong>
                          <template v-if="talent.occupationHistory?.length">
                            <a-tag
                              v-for="(occ, index) in talent.occupationHistory.slice(0, 3)"
                              :key="index"
                              size="small"
                              color="cyan"
                            >
                              {{ occ }}
                            </a-tag>
                            <a-tag
                              v-if="talent.occupationHistory.length > 3"
                              size="small"
                            >
                              +{{ talent.occupationHistory.length - 3 }}
                            </a-tag>
                          </template>
                          <span v-else>-</span>
                        </div>
                        <p><strong>公司：</strong>{{ talent.currentCompanyName || '-' }}</p>
                        <p><strong>评价数：</strong>{{ talent.evaluationCount || 0 }}</p>
                      </div>
                      <div class="talent-tags" v-if="talent.positiveTags?.length">
                        <a-tag
                          v-for="tag in talent.positiveTags.slice(0, 3)"
                          :key="tag.tagId"
                          color="blue"
                          size="small"
                        >
                          {{ tag.tagName }}
                        </a-tag>
                      </div>
                    </template>
                  </a-card-meta>

                  <template #actions>
                    <a-tooltip title="查看详情">
                      <EyeOutlined @click="viewTalentDetail(String(talent.id))" />
                    </a-tooltip>
                    <a-tooltip :title="talent.bookmarked ? '取消收藏' : '收藏'">
                      <StarFilled
                        v-if="talent.bookmarked"
                        style="color: #faad14"
                        @click="toggleBookmark(talent)"
                      />
                      <StarOutlined v-else @click="toggleBookmark(talent)" />
                    </a-tooltip>
                    <a-tooltip :title="selectedTalents.includes(String(talent.id)) ? '取消选择' : '加入对比'">
                      <SwapOutlined
                        :style="{ color: selectedTalents.includes(String(talent.id)) ? '#1890ff' : '' }"
                        @click="toggleCompareSelect(talent)"
                      />
                    </a-tooltip>
                  </template>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <div class="pagination-wrapper" v-if="total > 0">
            <a-pagination
              v-model:current="searchParams.pageNum"
              v-model:pageSize="searchParams.pageSize"
              :total="total"
              show-size-changer
              show-quick-jumper
              :show-total="(t: number) => `共 ${t} 条`"
              @change="handlePageChange"
            />
          </div>

          <a-empty v-if="!loading && talentList.length === 0" description="暂无人才数据" />
        </a-spin>
      </a-tab-pane>

      <!-- 我的收藏 -->
      <a-tab-pane key="bookmarks">
        <template #tab>
          <span><StarOutlined /> 我的收藏</span>
        </template>

        <a-spin :spinning="bookmarkLoading">
          <div class="talent-grid">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="talent in bookmarkedTalents"
                :key="talent.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card hoverable class="talent-card">
                  <template #cover>
                    <div class="talent-cover" @click="viewTalentDetail(String(talent.id))">
                      <a-avatar :size="80" :src="talent.photoUrl" class="talent-avatar">
                        {{ talent.name?.charAt(0) }}
                      </a-avatar>
                      <div class="talent-score">
                        <a-rate :value="talent.averageScore || 0" disabled allow-half />
                        <span class="score-text">{{ formatScore(talent.averageScore) }}</span>
                      </div>
                    </div>
                  </template>
                  <a-card-meta>
                    <template #title>
                      <div class="talent-name" @click="viewTalentDetail(String(talent.id))">
                        {{ talent.name }}
                      </div>
                    </template>
                    <template #description>
                      <div class="talent-info">
                        <p><strong>职位：</strong>{{ talent.latestOccupation || '-' }}</p>
                        <p><strong>评价数：</strong>{{ talent.evaluationCount || 0 }}</p>
                      </div>
                    </template>
                  </a-card-meta>
                  <template #actions>
                    <EyeOutlined @click="viewTalentDetail(String(talent.id))" />
                    <StarFilled style="color: #faad14" @click="toggleBookmark(talent)" />
                  </template>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <div class="pagination-wrapper" v-if="bookmarkTotal > 0">
            <a-pagination
              v-model:current="bookmarkParams.pageNum"
              :total="bookmarkTotal"
              @change="fetchBookmarks"
            />
          </div>

          <a-empty v-if="!bookmarkLoading && bookmarkedTalents.length === 0" description="暂无收藏" />
        </a-spin>
      </a-tab-pane>

      <!-- 智能推荐 -->
      <a-tab-pane key="recommend">
        <template #tab>
          <span><TeamOutlined /> 智能推荐</span>
        </template>

        <a-alert
          v-if="!preference?.preferredOccupations?.length && !preference?.preferredTags?.length"
          message="设置招聘偏好可获得更精准的人才推荐"
          type="info"
          show-icon
          class="preference-tip"
        >
          <template #action>
            <a-button type="link" @click="preferenceModalVisible = true">立即设置</a-button>
          </template>
        </a-alert>

        <a-spin :spinning="recommendLoading">
          <div class="talent-grid">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="talent in recommendedTalents"
                :key="talent.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card hoverable class="talent-card recommend-card">
                  <template #cover>
                    <div class="talent-cover" @click="viewTalentDetail(String(talent.id))">
                      <a-avatar :size="80" :src="talent.photoUrl" class="talent-avatar">
                        {{ talent.name?.charAt(0) }}
                      </a-avatar>
                      <div class="match-badge">
                        匹配度 {{ talent.matchPercentage || 0 }}%
                      </div>
                    </div>
                  </template>
                  <a-card-meta>
                    <template #title>
                      <div class="talent-name" @click="viewTalentDetail(String(talent.id))">
                        {{ talent.name }}
                      </div>
                    </template>
                    <template #description>
                      <div class="talent-info">
                        <p><strong>职位：</strong>{{ talent.latestOccupation || '-' }}</p>
                        <p><strong>评分：</strong>{{ formatScore(talent.averageScore) }}</p>
                      </div>
                      <div class="recommend-reasons" v-if="talent.recommendReasons?.length">
                        <a-tag v-for="(reason, idx) in talent.recommendReasons.slice(0, 2)" :key="idx" color="green" size="small">
                          {{ reason }}
                        </a-tag>
                      </div>
                    </template>
                  </a-card-meta>
                  <template #actions>
                    <EyeOutlined @click="viewTalentDetail(String(talent.id))" />
                    <StarOutlined @click="toggleBookmark(talent)" />
                  </template>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <div class="pagination-wrapper" v-if="recommendTotal > 0">
            <a-pagination
              v-model:current="recommendParams.pageNum"
              :total="recommendTotal"
              @change="fetchRecommended"
            />
          </div>

          <a-empty v-if="!recommendLoading && recommendedTalents.length === 0" description="暂无推荐" />
        </a-spin>
      </a-tab-pane>

      <!-- 浏览记录 -->
      <a-tab-pane key="history">
        <template #tab>
          <span><BarChartOutlined /> 浏览记录</span>
        </template>

        <a-row :gutter="[16, 16]">
          <!-- 统计卡片 -->
          <a-col :span="24">
            <a-card title="浏览统计" :loading="statisticsLoading">
              <a-row :gutter="16" v-if="viewStatistics">
                <a-col :span="6">
                  <a-statistic title="总浏览" :value="viewStatistics.totalViews || 0" />
                </a-col>
                <a-col :span="6">
                  <a-statistic title="今日浏览" :value="viewStatistics.todayViews || 0" />
                </a-col>
                <a-col :span="6">
                  <a-statistic title="本周浏览" :value="viewStatistics.weekViews || 0" />
                </a-col>
                <a-col :span="6">
                  <a-statistic title="浏览人才数" :value="viewStatistics.uniqueTalentCount || 0" />
                </a-col>
              </a-row>
            </a-card>
          </a-col>

          <!-- 浏览历史列表 -->
          <a-col :span="24">
            <a-card title="浏览历史">
              <template #extra>
                <a-button danger @click="clearHistory">清除记录</a-button>
              </template>

              <a-table
                :data-source="viewHistory"
                :loading="viewHistoryLoading"
                :pagination="{
                  current: viewHistoryParams.pageNum,
                  pageSize: viewHistoryParams.pageSize,
                  total: viewHistoryTotal,
                  onChange: (page: number) => { viewHistoryParams.pageNum = page; fetchViewHistory(); }
                }"
                row-key="id"
              >
                <a-table-column title="人才" key="employee">
                  <template #default="{ record }">
                    <a-space>
                      <a-avatar :src="record.employeePhotoUrl" :size="40">
                        {{ record.employeeName?.charAt(0) }}
                      </a-avatar>
                      <div>
                        <div>{{ record.employeeName }}</div>
                        <div style="color: #999; font-size: 12px">{{ record.latestOccupation }}</div>
                      </div>
                    </a-space>
                  </template>
                </a-table-column>
                <a-table-column title="浏览时间" data-index="viewTime" key="viewTime">
                  <template #default="{ record }">
                    {{ new Date(record.viewTime).toLocaleString() }}
                  </template>
                </a-table-column>
                <a-table-column title="来源" data-index="viewSource" key="viewSource">
                  <template #default="{ record }">
                    <a-tag v-if="record.viewSource === 'search'" color="blue">搜索</a-tag>
                    <a-tag v-else-if="record.viewSource === 'recommend'" color="green">推荐</a-tag>
                    <a-tag v-else-if="record.viewSource === 'bookmark'" color="orange">收藏</a-tag>
                    <a-tag v-else>其他</a-tag>
                  </template>
                </a-table-column>
                <a-table-column title="操作" key="action">
                  <template #default="{ record }">
                    <a-button type="link" @click="viewTalentDetail(String(record.employeeId))">
                      查看
                    </a-button>
                  </template>
                </a-table-column>
              </a-table>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>

    <!-- 招聘偏好设置弹窗 -->
    <a-modal
      v-model:visible="preferenceModalVisible"
      title="招聘偏好设置"
      width="600px"
      @ok="savePreference"
    >
      <a-form layout="vertical">
        <a-form-item label="偏好职位">
          <a-select
            v-model:value="preferenceForm.preferredOccupations"
            mode="tags"
            placeholder="输入并回车添加偏好职位"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="偏好标签（优先推荐拥有这些标签的人才）">
          <a-select
            v-model:value="preferenceForm.preferredTagIds"
            mode="multiple"
            placeholder="选择偏好标签"
            :options="positiveTagOptions"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="排除标签（不推荐拥有这些标签的人才）">
          <a-select
            v-model:value="preferenceForm.excludedTagIds"
            mode="multiple"
            placeholder="选择要排除的标签"
            :options="neutralTagOptions"
            style="width: 100%"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最低评分要求">
              <a-input-number
                v-model:value="preferenceForm.minScore"
                :min="0"
                :max="5"
                :step="0.5"
                placeholder="0-5"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最低出勤率要求">
              <a-input-number
                v-model:value="preferenceForm.minAttendanceRate"
                :min="0"
                :max="100"
                placeholder="0-100"
                style="width: 100%"
                addon-after="%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item>
          <a-checkbox v-model:checked="preferenceForm.excludeMajorIncident">
            排除有重大违纪记录的人才
          </a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.talent-market {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.market-tabs {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.advanced-search {
  margin-top: 16px;
}

.talent-grid {
  margin-top: 20px;
}

.talent-card {
  height: 100%;
  transition: all 0.3s;
}

.talent-card.selected {
  border-color: #1890ff;
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.3);
}

.talent-cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

.talent-avatar {
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.talent-score {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.talent-score :deep(.ant-rate) {
  font-size: 14px;
}

.talent-score :deep(.ant-rate-star-full .anticon) {
  color: #ffd700;
}

.score-text {
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}

.talent-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.talent-name:hover {
  color: #1890ff;
}

.talent-info {
  font-size: 13px;
  line-height: 1.8;
}

.talent-info p {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.occupation-list {
  margin-bottom: 4px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.occupation-list strong {
  flex-shrink: 0;
}

.talent-tags {
  margin-top: 8px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.recommend-card .match-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.recommend-reasons {
  margin-top: 8px;
}

.preference-tip {
  margin-bottom: 16px;
}
</style>


<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  StarOutlined,
  StarFilled,
  CheckCircleOutlined,
  CloseCircleOutlined,
  TrophyOutlined,
} from '@ant-design/icons-vue'
import * as talentMarketController from '@/api/talentMarketController'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const compareData = ref<any>(null)

// 解析URL中的人才ID
const talentIds = computed(() => {
  const ids = route.query.ids as string
  return ids ? ids.split(',') : []
})

// 获取对比数据
const fetchCompareData = async () => {
  if (talentIds.value.length < 2) {
    message.error('请至少选择2个人才进行对比')
    router.back()
    return
  }

  try {
    loading.value = true
    const res = await talentMarketController.compareTalents({
      employeeIds: talentIds.value,
    })
    if (res?.data?.code === 0 && res.data.data) {
      compareData.value = res.data.data
    } else {
      message.error('获取对比数据失败')
    }
  } catch (error) {
    console.error('对比失败:', error)
    message.error('获取对比数据失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/talent-market')
}

// 查看详情
const viewDetail = (employeeId: string) => {
  router.push(`/talent-market/detail/${employeeId}`)
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 获取最高分人才
const getBestTalent = (field: string) => {
  if (!compareData.value?.talents?.length) return null
  let best = compareData.value.talents[0]
  for (const talent of compareData.value.talents) {
    const current = getFieldValue(talent, field)
    const bestValue = getFieldValue(best, field)
    if (current > bestValue) {
      best = talent
    }
  }
  return best
}

// 获取字段值
const getFieldValue = (talent: any, field: string) => {
  switch (field) {
    case 'averageScore':
      return talent.averageScore || 0
    case 'evaluationCount':
      return talent.evaluationCount || 0
    case 'profileCount':
      return talent.profiles?.length || 0
    case 'positiveTagCount':
      return talent.positiveTags?.length || 0
    default:
      return 0
  }
}

// 判断是否是最佳
const isBest = (talent: any, field: string) => {
  const best = getBestTalent(field)
  return best && String(best.id) === String(talent.id)
}

// 对比项配置
const compareItems = [
  { key: 'averageScore', label: '综合评分', type: 'score' },
  { key: 'evaluationCount', label: '评价数量', type: 'number' },
  { key: 'latestOccupation', label: '最新职位', type: 'text' },
  { key: 'status', label: '在职状态', type: 'status' },
  { key: 'gender', label: '性别', type: 'text' },
  { key: 'positiveTags', label: '正面标签', type: 'tags' },
  { key: 'neutralTags', label: '待改进标签', type: 'tags' },
]

// 维度对比项
const dimensionItems = computed(() => {
  if (!compareData.value?.dimensions) return []
  return compareData.value.dimensions.map((dim: string) => ({
    key: dim,
    label: dim,
  }))
})

onMounted(() => {
  fetchCompareData()
})
</script>

<template>
  <div class="talent-compare">
    <a-page-header title="人才对比" @back="goBack">
      <template #extra>
        <a-button @click="goBack">返回人才市场</a-button>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <div class="compare-content" v-if="compareData?.talents?.length">
        <!-- 人才头部信息 -->
        <a-card class="compare-header-card">
          <a-row :gutter="0" class="compare-header">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">人才信息</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="talent-header" @click="viewDetail(String(talent.id))">
                <a-avatar :size="80" :src="talent.photoUrl" class="talent-avatar">
                  {{ talent.name?.charAt(0) }}
                </a-avatar>
                <h3 class="talent-name">{{ talent.name }}</h3>
                <a-tag :color="talent.status ? 'green' : 'orange'" size="small">
                  {{ talent.status ? '在职' : '离职' }}
                </a-tag>
                <div class="talent-company">{{ talent.currentCompanyName || '-' }}</div>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 基本信息对比 -->
        <a-card title="基本信息对比" class="compare-section-card">
          <a-row
            v-for="item in compareItems"
            :key="item.key"
            :gutter="0"
            class="compare-row"
          >
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">{{ item.label }}</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div
                class="compare-value"
                :class="{ 'is-best': item.type === 'score' && isBest(talent, item.key) }"
              >
                <!-- 评分类型 -->
                <template v-if="item.type === 'score'">
                  <div class="score-display">
                    <TrophyOutlined v-if="isBest(talent, item.key)" class="best-icon" />
                    <span class="score-value">{{ talent[item.key]?.toFixed(1) || '-' }}</span>
                    <a-rate :value="talent[item.key] || 0" disabled allow-half style="font-size: 14px" />
                  </div>
                </template>

                <!-- 数量类型 -->
                <template v-else-if="item.type === 'number'">
                  <span :class="{ 'highlight': isBest(talent, item.key) }">
                    {{ talent[item.key] || 0 }}
                  </span>
                </template>

                <!-- 状态类型 -->
                <template v-else-if="item.type === 'status'">
                  <a-tag :color="talent[item.key] ? 'green' : 'orange'">
                    {{ talent[item.key] ? '在职' : '离职' }}
                  </a-tag>
                </template>

                <!-- 标签类型 -->
                <template v-else-if="item.type === 'tags'">
                  <div class="tag-list">
                    <a-tag
                      v-for="tag in (talent[item.key] || []).slice(0, 5)"
                      :key="tag.tagId"
                      :color="item.key === 'positiveTags' ? 'blue' : 'orange'"
                      size="small"
                    >
                      {{ tag.tagName }}
                    </a-tag>
                    <span v-if="!talent[item.key]?.length" class="no-data">暂无</span>
                  </div>
                </template>

                <!-- 文本类型 -->
                <template v-else>
                  {{ talent[item.key] || '-' }}
                </template>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 维度评分对比 -->
        <a-card title="维度评分对比" class="compare-section-card">
          <a-row
            v-for="dim in dimensionItems"
            :key="dim.key"
            :gutter="0"
            class="compare-row"
          >
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">{{ dim.label }}</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="compare-value dimension-value">
                <a-progress
                  :percent="((talent.dimensionScores?.[dim.key] || 0) / 5) * 100"
                  :stroke-color="isBest(talent, `dim_${dim.key}`) ? '#52c41a' : '#1890ff'"
                  :show-info="false"
                  style="width: 100px; margin-right: 8px"
                />
                <span class="dim-score">{{ talent.dimensionScores?.[dim.key]?.toFixed(1) || '-' }}</span>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 工作经历对比 -->
        <a-card title="工作经历对比" class="compare-section-card">
          <a-row :gutter="0" class="compare-row">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">工作经历</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="profile-list">
                <a-timeline>
                  <a-timeline-item
                    v-for="profile in (talent.profiles || []).slice(0, 3)"
                    :key="profile.profileId"
                    :color="profile.hasMajorIncident ? 'red' : 'blue'"
                  >
                    <div class="profile-item">
                      <strong>{{ profile.companyName }}</strong>
                      <div class="profile-occupation">{{ profile.occupation }}</div>
                      <div class="profile-date">
                        {{ profile.startDate }} ~ {{ profile.endDate || '至今' }}
                      </div>
                      <div class="profile-info" v-if="profile.attendanceRate">
                        出勤率：{{ profile.attendanceRate }}%
                      </div>
                    </div>
                  </a-timeline-item>
                </a-timeline>
                <div v-if="!talent.profiles?.length" class="no-data">暂无工作经历</div>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 评价统计对比 -->
        <a-card title="评价统计对比" class="compare-section-card">
          <a-row :gutter="0" class="compare-row">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">HR评价</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="compare-value">
                {{ talent.hrEvaluationCount || 0 }} 条
              </div>
            </a-col>
          </a-row>

          <a-row :gutter="0" class="compare-row">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">领导评价</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="compare-value">
                {{ talent.leaderEvaluationCount || 0 }} 条
              </div>
            </a-col>
          </a-row>

          <a-row :gutter="0" class="compare-row">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">同事评价</div>
            </a-col>
            <a-col
              v-for="talent in compareData.talents"
              :key="talent.id"
              :span="Math.floor(20 / compareData.talents.length)"
              class="compare-value-col"
            >
              <div class="compare-value">
                {{ talent.colleagueEvaluationCount || 0 }} 条
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 操作按钮 -->
        <div class="compare-actions">
          <a-space>
            <a-button
              v-for="talent in compareData.talents"
              :key="talent.id"
              type="primary"
              @click="viewDetail(String(talent.id))"
            >
              查看 {{ talent.name }} 详情
            </a-button>
          </a-space>
        </div>
      </div>

      <a-empty v-if="!loading && !compareData?.talents?.length" description="暂无对比数据" />
    </a-spin>
  </div>
</template>

<style scoped>
.talent-compare {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.compare-content {
  margin-top: 16px;
}

.compare-header-card {
  margin-bottom: 16px;
}

.compare-section-card {
  margin-bottom: 16px;
}

.compare-header {
  display: flex;
}

.compare-label-col {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-right: 1px solid #f0f0f0;
  min-height: 60px;
}

.compare-label {
  font-weight: 600;
  color: #333;
  text-align: center;
}

.compare-value-col {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-right: 1px solid #f0f0f0;
}

.compare-value-col:last-child {
  border-right: none;
}

.talent-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.talent-header:hover {
  opacity: 0.8;
}

.talent-avatar {
  border: 3px solid #1890ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.talent-name {
  margin: 0;
  font-size: 18px;
}

.talent-company {
  color: #999;
  font-size: 12px;
}

.compare-row {
  border-bottom: 1px solid #f0f0f0;
}

.compare-row:last-child {
  border-bottom: none;
}

.compare-value {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  text-align: center;
}

.compare-value.is-best {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border-radius: 4px;
  padding: 8px;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.best-icon {
  color: #52c41a;
  font-size: 16px;
}

.score-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.highlight {
  font-weight: bold;
  color: #52c41a;
  font-size: 18px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.no-data {
  color: #999;
  font-size: 12px;
}

.dimension-value {
  display: flex;
  align-items: center;
}

.dim-score {
  font-weight: bold;
  color: #1890ff;
}

.profile-list {
  width: 100%;
  padding: 8px;
}

.profile-item {
  font-size: 12px;
}

.profile-occupation {
  color: #1890ff;
}

.profile-date {
  color: #999;
}

.profile-info {
  color: #666;
}

.compare-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>


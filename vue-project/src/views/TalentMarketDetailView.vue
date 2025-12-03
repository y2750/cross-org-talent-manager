<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal, Input } from 'ant-design-vue'
import * as echarts from 'echarts'
import {
  ArrowLeftOutlined,
  StarOutlined,
  StarFilled,
  LockOutlined,
  UnlockOutlined,
  UserOutlined,
  PhoneOutlined,
  MailOutlined,
  TeamOutlined,
  TrophyOutlined,
  SafetyCertificateOutlined,
  ExclamationCircleOutlined,
  EyeInvisibleOutlined,
  FileProtectOutlined,
} from '@ant-design/icons-vue'
import * as talentMarketController from '@/api/talentMarketController'
import API from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const talent = ref<any>(null)
const similarTalents = ref<any[]>([])
const unlockPrices = ref<any[]>([])

// 评价类型筛选
const evaluationTypeFilter = ref<number | undefined>(undefined)

// 页面停留时间统计
const viewLogId = ref<string | null>(null)
const viewStartTime = ref<number>(Date.now())

// 申请查看联系方式
const showContactRequestModal = ref(false)
const contactRequestReason = ref<string>('')

// 获取人才详情
const fetchTalentDetail = async () => {
  const employeeId = route.params.employeeId as string
  if (!employeeId) {
    message.error('参数错误')
    router.back()
    return
  }

  try {
    loading.value = true
    const res = await talentMarketController.getTalentDetail({ employeeId })
    if (res?.data?.code === 0 && res.data.data) {
      talent.value = res.data.data
    } else {
      message.error('获取人才详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    message.error('获取人才详情失败')
  } finally {
    loading.value = false
  }
}

// 获取解锁价格
const fetchUnlockPrices = async () => {
  try {
    const res = await talentMarketController.getUnlockPriceConfigs()
    if (res?.data?.code === 0 && res.data.data) {
      unlockPrices.value = res.data.data
    }
  } catch (error) {
    console.error('获取价格失败:', error)
  }
}

// 获取相似人才
const fetchSimilarTalents = async () => {
  const employeeId = route.params.employeeId as string
  try {
    const res = await talentMarketController.getSimilarTalents({ employeeId, limit: 4 })
    if (res?.data?.code === 0 && res.data.data) {
      similarTalents.value = res.data.data
    }
  } catch (error) {
    console.error('获取相似人才失败:', error)
  }
}

// 记录浏览
const recordView = async () => {
  const employeeId = route.params.employeeId as string
  try {
    const res = await talentMarketController.recordView({
      employeeId,
      viewSource: 'search',
    })
    if (res?.data?.code === 0 && res.data.data) {
      viewLogId.value = String(res.data.data)
    }
  } catch (error) {
    console.error('记录浏览失败:', error)
  }
}

// 更新浏览时长
const updateViewDuration = async () => {
  if (!viewLogId.value) return
  const duration = Math.floor((Date.now() - viewStartTime.value) / 1000)
  try {
    await talentMarketController.updateViewDuration({
      viewLogId: viewLogId.value,
      viewDuration: duration,
    })
  } catch (error) {
    console.error('更新浏览时长失败:', error)
  }
}

// 收藏/取消收藏
const toggleBookmark = async () => {
  if (!talent.value) return
  const employeeId = String(talent.value.id)
  try {
    if (talent.value.bookmarked) {
      await talentMarketController.unbookmarkTalent({ employeeId })
      talent.value.bookmarked = false
      message.success('已取消收藏')
    } else {
      await talentMarketController.bookmarkTalent({ employeeId })
      talent.value.bookmarked = true
      message.success('收藏成功')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

// 解锁评价
const unlockEvaluation = async (evaluation: any) => {
  Modal.confirm({
    title: '确认解锁',
    content: `解锁此评价需要消耗 ${evaluation.unlockCost} 积分，确定要解锁吗？`,
    onOk: async () => {
      try {
        await talentMarketController.unlockEvaluation({
          evaluationId: String(evaluation.id),
        })
        message.success('解锁成功')
        fetchTalentDetail() // 刷新详情
      } catch (error) {
        message.error('解锁失败，请检查积分是否充足')
      }
    },
  })
}

// 批量解锁所有锁定的评价
const unlockAll = async () => {
  const lockedEvaluations = talent.value?.evaluations?.filter((e: any) => !e.unlocked) || []
  if (lockedEvaluations.length === 0) {
    message.info('没有需要解锁的评价')
    return
  }

  const totalCost = lockedEvaluations.reduce((sum: number, e: any) => sum + (e.unlockCost || 0), 0)

  Modal.confirm({
    title: '批量解锁',
    content: `共 ${lockedEvaluations.length} 条评价需要解锁，预计消耗 ${totalCost} 积分，确定要全部解锁吗？`,
    onOk: async () => {
      try {
        await talentMarketController.batchUnlockEvaluations({
          evaluationIds: lockedEvaluations.map((e: any) => String(e.id)),
        })
        message.success('批量解锁成功')
        fetchTalentDetail()
      } catch (error) {
        message.error('解锁失败，请检查积分是否充足')
      }
    },
  })
}

// 查看相似人才
const viewSimilarTalent = (employeeId: string) => {
  router.push(`/talent-market/detail/${employeeId}`)
}

// 申请查看联系方式
const requestContactAccess = () => {
  if (!talent.value) return
  contactRequestReason.value = ''
  showContactRequestModal.value = true
}

const submitContactRequest = async () => {
  if (!talent.value) return

  if (!contactRequestReason.value || contactRequestReason.value.trim().length === 0) {
    message.error('请输入申请理由')
    return
  }

  try {
    // 一次性申请查看电话和邮箱（request_type = 5）
    await API.contactAccessRequestController.createContactAccessRequest({
      employeeId: String(talent.value.id),
      requestType: 5, // 查看电话和邮箱
      requestReason: contactRequestReason.value.trim(),
    })
    message.success('申请已提交，请等待员工审批')
    showContactRequestModal.value = false
    contactRequestReason.value = ''
    // 刷新人才详情，更新授权状态
    await fetchTalentDetail()
  } catch (error: any) {
    // 响应拦截器已经自动显示了后端返回的错误信息
    // 错误信息会通过响应拦截器显示后端返回的具体错误信息（如"已有未处理的联系方式申请请求，请等待处理完成后再申请"）
    // 这里不需要再次显示，避免重复提示
    console.error('申请联系方式失败:', error)
  }
}

// 申请查看档案
const requestProfileAccess = async (profile: any) => {
  if (!talent.value || !profile) return
  Modal.confirm({
    title: '申请查看档案',
    content: `确定要申请查看该员工在 ${profile.companyName} 的工作档案吗？申请后需等待审批。`,
    onOk: async () => {
      try {
        await API.profileAccessRequestController.createProfileAccessRequest({
          employeeId: String(talent.value.id),
          employeeProfileId: String(profile.profileId),
          requestReason: '招聘需要',
        })
        message.success('申请已提交，请等待审批')
      } catch (error) {
        message.error('申请提交失败')
      }
    },
  })
}

// 返回
const goBack = () => {
  router.push('/talent-market')
}

// 获取评价类型名称
const getEvaluationTypeName = (type: number) => {
  const types: Record<number, string> = {
    1: '领导评价',
    2: '同事评价',
    3: 'HR评价',
    4: '自评',
  }
  return types[type] || '未知'
}

// 获取评价类型颜色
const getEvaluationTypeColor = (type: number) => {
  const colors: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'purple',
    4: 'orange',
  }
  return colors[type] || 'default'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 雷达图
const radarChartRef = ref<HTMLDivElement | null>(null)
let radarChart: echarts.ECharts | null = null

// 初始化雷达图
const initRadarChart = () => {
  if (!radarChartRef.value || !talent.value?.dimensionScores) {
    return
  }

  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }

  const dimensionScores = talent.value.dimensionScores
  const indicators = Object.keys(dimensionScores).map((name) => ({
    name,
    max: 5,
  }))

  const values = Object.values(dimensionScores).map((score) => Number(score) || 0)

  const option: echarts.EChartsOption = {
    tooltip: {
      show: false,
    },
    radar: {
      indicator: indicators,
      center: ['50%', '50%'],
      radius: '60%',
      axisName: {
        color: '#333',
        fontSize: 12,
        formatter: (params: any) => {
          const name = typeof params === 'string' ? params : params?.name || ''
          const score = dimensionScores[name] || 0
          return '  ' + name + '\n  ' + Number(score).toFixed(2) + '分'
        },
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
}

// 计算雷达图数据
const radarData = computed(() => {
  if (!talent.value?.dimensionScores) return []
  return Object.entries(talent.value.dimensionScores).map(([name, score]) => ({
    name,
    score: score as number,
  }))
})

// 监听talent变化，更新雷达图
watch(
  () => talent.value?.dimensionScores,
  () => {
    nextTick(() => {
      initRadarChart()
    })
  },
  { deep: true }
)

// 过滤后的评价列表
const filteredEvaluations = computed(() => {
  if (!talent.value?.evaluations) return []
  if (evaluationTypeFilter.value === undefined) {
    return talent.value.evaluations
  }
  return talent.value.evaluations.filter((e: any) => e.evaluationType === evaluationTypeFilter.value)
})

onMounted(() => {
  viewStartTime.value = Date.now()
  fetchTalentDetail()
  fetchUnlockPrices()
  fetchSimilarTalents()
  recordView()
  nextTick(() => {
    initRadarChart()
  })
})

onUnmounted(() => {
  updateViewDuration()
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
})
</script>

<template>
  <div class="talent-detail">
    <a-page-header @back="goBack">
      <template #title>
        <span>人才详情</span>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="toggleBookmark">
            <StarFilled v-if="talent?.bookmarked" style="color: #faad14" />
            <StarOutlined v-else />
            {{ talent?.bookmarked ? '已收藏' : '收藏' }}
          </a-button>
          <a-button
            v-if="talent?.lockedEvaluationCount > 0"
            type="primary"
            @click="unlockAll"
          >
            <UnlockOutlined />
            解锁全部 ({{ talent?.lockedEvaluationCount }})
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <div class="detail-content" v-if="talent">
        <a-row :gutter="[24, 24]">
          <!-- 左侧：基本信息 -->
          <a-col :xs="24" :lg="8">
            <!-- 个人信息卡片 -->
            <a-card class="profile-card">
              <div class="profile-header">
                <a-avatar :size="100" :src="talent.photoUrl" class="profile-avatar">
                  {{ talent.name?.charAt(0) }}
                </a-avatar>
                <h2 class="profile-name">{{ talent.name }}</h2>
                <a-tag :color="talent.status ? 'green' : 'orange'" size="large">
                  {{ talent.status ? '在职' : '离职' }}
                </a-tag>
              </div>

              <a-divider />

              <a-descriptions :column="1" class="profile-info">
                <a-descriptions-item label="性别">
                  <UserOutlined /> {{ talent.gender || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="电话">
                  <PhoneOutlined />
                  <span :class="{ 'masked-info': !talent.isOwnEmployee && !talent.contactAuthorized }">
                    {{ talent.phone || '-' }}
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="邮箱">
                  <MailOutlined />
                  <span :class="{ 'masked-info': !talent.isOwnEmployee && !talent.contactAuthorized }">
                    {{ talent.email || '-' }}
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="身份证号">
                  <SafetyCertificateOutlined />
                  <span :class="{ 'masked-info': !talent.isOwnEmployee && !talent.contactAuthorized }">
                    {{ talent.idCardNumber || '-' }}
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="当前公司">
                  <TeamOutlined /> {{ talent.currentCompanyName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="最新职位">
                  {{ talent.latestOccupation || '-' }}
                </a-descriptions-item>
              </a-descriptions>

              <!-- 申请查看联系方式按钮 -->
              <a-button
                v-if="talent.canRequestContact && !talent.isOwnEmployee"
                type="primary"
                block
                style="margin-top: 16px"
                @click="requestContactAccess"
              >
                <EyeInvisibleOutlined /> 申请查看联系方式
              </a-button>

              <a-divider />

              <!-- 标签统计 -->
              <div class="tag-section">
                <h4>正面评价标签</h4>
                <div class="tag-list">
                  <a-tag
                    v-for="tag in talent.positiveTags"
                    :key="tag.tagId"
                    color="blue"
                  >
                    {{ tag.tagName }} ({{ tag.count }})
                  </a-tag>
                  <span v-if="!talent.positiveTags?.length" class="no-data">暂无</span>
                </div>

                <h4 style="margin-top: 16px">待改进标签</h4>
                <div class="tag-list">
                  <a-tag
                    v-for="tag in talent.neutralTags"
                    :key="tag.tagId"
                    color="orange"
                  >
                    {{ tag.tagName }} ({{ tag.count }})
                  </a-tag>
                  <span v-if="!talent.neutralTags?.length" class="no-data">暂无</span>
                </div>
              </div>
            </a-card>

            <!-- 评分概览 -->
            <a-card title="评分概览" class="score-card" style="margin-top: 16px">
              <div class="score-overview">
                <div class="main-score">
                  <span class="score-value">{{ talent.averageScore?.toFixed(1) || '-' }}</span>
                  <span class="score-label">综合评分</span>
                </div>
                <a-rate :value="talent.averageScore || 0" disabled allow-half />
              </div>

              <a-divider />

              <!-- 五维雷达图 -->
              <div v-if="talent.dimensionScores && Object.keys(talent.dimensionScores).length > 0" class="radar-chart-container">
                <div ref="radarChartRef" style="width: 100%; height: 300px"></div>
              </div>
              <a-empty v-else description="暂无评分数据" :image="false" />

              <a-divider />

              <a-row :gutter="16" class="stats-row">
                <a-col :span="8">
                  <a-statistic title="评价数" :value="talent.evaluationCount || 0" />
                </a-col>
                <a-col :span="8">
                  <a-statistic title="工作经历" :value="talent.profileCount || 0" />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="企业积分"
                    :value="talent.companyPoints || 0"
                    :precision="0"
                  />
                </a-col>
              </a-row>
            </a-card>
          </a-col>

          <!-- 右侧：详细信息 -->
          <a-col :xs="24" :lg="16">
            <!-- 工作经历 -->
            <a-card title="工作经历" class="section-card">
              <a-timeline>
                <a-timeline-item
                  v-for="profile in talent.profiles"
                  :key="profile.profileId"
                  :color="profile.hasMajorIncident ? 'red' : (profile.canViewDetail ? 'blue' : 'gray')"
                >
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <h4>{{ profile.companyName }}</h4>
                      <a-tag v-if="profile.canViewDetail">{{ profile.occupation }}</a-tag>
                      <a-tag v-else color="default">
                        <LockOutlined /> **
                      </a-tag>
                      <a-tag v-if="profile.visibility === 0" color="red" size="small">
                        <LockOutlined /> 保密
                      </a-tag>
                      <a-tag v-if="profile.visibility === 1 && !profile.authorized" color="orange" size="small">
                        <FileProtectOutlined /> 需申请
                      </a-tag>
                    </div>
                    <p class="timeline-date">
                      {{ profile.startDate }} ~ {{ profile.endDate || '至今' }}
                    </p>
                    
                    <!-- 可查看详情时显示完整信息 -->
                    <template v-if="profile.canViewDetail">
                      <div class="timeline-details">
                        <a-space wrap>
                          <span v-if="profile.attendanceRate">
                            <SafetyCertificateOutlined /> 出勤率：{{ profile.attendanceRate }}%
                          </span>
                          <a-tag v-if="profile.hasMajorIncident" color="red">
                            <ExclamationCircleOutlined /> 有重大违纪
                          </a-tag>
                          <span v-if="profile.reasonForLeaving">
                            离职原因：{{ profile.reasonForLeaving }}
                          </span>
                        </a-space>
                      </div>
                      <p v-if="profile.performanceSummary" class="timeline-summary">
                        {{ profile.performanceSummary }}
                      </p>
                    </template>
                    
                    <!-- 不可查看时显示保密提示和申请按钮 -->
                    <template v-else>
                      <div class="profile-locked">
                        <a-alert
                          :message="profile.visibility === 0 ? '此档案为完全保密' : '此档案需要申请查看'"
                          :type="profile.visibility === 0 ? 'error' : 'warning'"
                          show-icon
                          style="margin-top: 8px"
                        />
                        <a-button
                          v-if="profile.needRequest"
                          type="primary"
                          size="small"
                          style="margin-top: 8px"
                          @click="requestProfileAccess(profile)"
                        >
                          <FileProtectOutlined /> 申请查看档案
                        </a-button>
                      </div>
                    </template>
                  </div>
                </a-timeline-item>
              </a-timeline>

              <a-empty v-if="!talent.profiles?.length" description="暂无工作经历" />
            </a-card>

            <!-- 评价列表 -->
            <a-card title="评价记录" class="section-card" style="margin-top: 16px">
              <template #extra>
                <a-space>
                  <span>免费可见：{{ talent.freeEvaluationCount }}</span>
                  <span>已解锁：{{ talent.unlockedEvaluationCount }}</span>
                  <span>待解锁：{{ talent.lockedEvaluationCount }}</span>
                </a-space>
              </template>

              <!-- 评价类型筛选 -->
              <div style="margin-bottom: 16px">
                <a-space>
                  <span>评价类型：</span>
                  <a-radio-group v-model:value="evaluationTypeFilter" button-style="solid" size="small">
                    <a-radio-button :value="undefined">全部</a-radio-button>
                    <a-radio-button :value="1">领导评价</a-radio-button>
                    <a-radio-button :value="2">同事评价</a-radio-button>
                    <a-radio-button :value="3">HR评价</a-radio-button>
                    <a-radio-button :value="4">自评</a-radio-button>
                  </a-radio-group>
                </a-space>
              </div>

              <div class="evaluation-list">
                <a-card
                  v-for="evaluation in filteredEvaluations"
                  :key="evaluation.id"
                  size="small"
                  class="evaluation-card"
                  :class="{ locked: !evaluation.unlocked }"
                >
                  <template #title>
                    <a-space>
                      <a-tag :color="getEvaluationTypeColor(evaluation.evaluationType)">
                        {{ evaluation.evaluationTypeName }}
                      </a-tag>
                      <span>{{ evaluation.companyName }}</span>
                      <span class="eval-date">{{ formatDate(evaluation.evaluationDate) }}</span>
                    </a-space>
                  </template>

                  <template #extra v-if="!evaluation.unlocked">
                    <a-button type="primary" size="small" @click="unlockEvaluation(evaluation)">
                      <LockOutlined /> 解锁 ({{ evaluation.unlockCost }}积分)
                    </a-button>
                  </template>

                  <div v-if="evaluation.unlocked" class="evaluation-content">
                    <p class="eval-comment">{{ evaluation.comment }}</p>

                    <div class="eval-scores" v-if="evaluation.dimensionScores">
                      <a-space wrap>
                        <span v-for="(score, name) in evaluation.dimensionScores" :key="name">
                          {{ name }}：<a-rate :value="score" disabled :count="5" style="font-size: 12px" />
                        </span>
                      </a-space>
                    </div>

                    <div class="eval-tags" v-if="evaluation.tags?.length">
                      <a-tag
                        v-for="tag in evaluation.tags"
                        :key="tag.id"
                        :color="tag.type === 1 ? 'blue' : 'orange'"
                        size="small"
                      >
                        {{ tag.name }}
                      </a-tag>
                    </div>

                    <div class="eval-avg" v-if="evaluation.averageScore">
                      <TrophyOutlined /> 平均分：{{ evaluation.averageScore.toFixed(1) }}
                    </div>
                  </div>

                  <div v-else class="locked-content">
                    <LockOutlined style="font-size: 24px; color: #999" />
                    <p>解锁查看完整评价内容</p>
                  </div>
                </a-card>
              </div>

              <a-empty v-if="!filteredEvaluations.length" description="暂无评价记录" />
            </a-card>

            <!-- 相似人才推荐 -->
            <a-card title="相似人才推荐" class="section-card" style="margin-top: 16px">
              <a-row :gutter="[16, 16]">
                <a-col
                  v-for="similar in similarTalents"
                  :key="similar.id"
                  :xs="12"
                  :sm="12"
                  :md="6"
                >
                  <a-card
                    hoverable
                    size="small"
                    @click="viewSimilarTalent(String(similar.id))"
                  >
                    <a-card-meta>
                      <template #avatar>
                        <a-avatar :src="similar.photoUrl" :size="48">
                          {{ similar.name?.charAt(0) }}
                        </a-avatar>
                      </template>
                      <template #title>{{ similar.name }}</template>
                      <template #description>
                        <div>{{ similar.latestOccupation }}</div>
                        <div>评分：{{ similar.averageScore?.toFixed(1) || '-' }}</div>
                      </template>
                    </a-card-meta>
                  </a-card>
                </a-col>
              </a-row>

              <a-empty v-if="!similarTalents.length" description="暂无相似人才" />
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>
  </div>

  <!-- 申请查看联系方式 Modal -->
  <a-modal
    v-model:open="showContactRequestModal"
    title="申请查看联系方式"
    ok-text="提交"
    cancel-text="取消"
    @ok="submitContactRequest"
  >
    <p style="margin-bottom: 12px">确定要申请查看该员工的电话和邮箱吗？申请后需等待员工本人审批。授权有效期为7天。</p>
    <Input.TextArea
      v-model:value="contactRequestReason"
      placeholder="请输入申请理由"
      :rows="4"
      :maxlength="500"
      show-count
    />
  </a-modal>
</template>

<style scoped>
.talent-detail {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.detail-content {
  margin-top: 16px;
}

.profile-card {
  text-align: center;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.profile-avatar {
  border: 4px solid #1890ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.profile-name {
  margin: 0;
  font-size: 24px;
}

.profile-info :deep(.ant-descriptions-item-label) {
  color: #666;
}

.tag-section h4 {
  margin: 0 0 8px;
  color: #666;
  font-size: 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.no-data {
  color: #999;
  font-size: 12px;
}

.score-card .score-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.main-score {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #1890ff;
  line-height: 1;
}

.score-label {
  color: #666;
  font-size: 14px;
}

.dimension-scores {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dimension-name {
  width: 80px;
  color: #666;
  font-size: 13px;
}

.dimension-item :deep(.ant-progress) {
  flex: 1;
}

.dimension-value {
  width: 30px;
  text-align: right;
  font-weight: bold;
  color: #1890ff;
}

.stats-row {
  text-align: center;
}

.section-card {
  height: fit-content;
}

.timeline-content {
  padding-bottom: 8px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.timeline-header h4 {
  margin: 0;
}

.timeline-date {
  color: #999;
  font-size: 12px;
  margin: 4px 0;
}

.timeline-details {
  margin: 8px 0;
  font-size: 13px;
  color: #666;
}

.timeline-summary {
  margin: 8px 0 0;
  color: #333;
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 4px;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.evaluation-card {
  transition: all 0.3s;
}

.evaluation-card.locked {
  background: #fafafa;
}

.evaluation-card .eval-date {
  color: #999;
  font-size: 12px;
}

.evaluation-content {
  padding: 8px 0;
}

.eval-comment {
  margin: 0 0 12px;
  line-height: 1.6;
}

.eval-scores {
  margin-bottom: 12px;
  font-size: 13px;
  color: #666;
}

.eval-tags {
  margin-bottom: 12px;
}

.eval-avg {
  font-weight: bold;
  color: #1890ff;
}

.locked-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  color: #999;
}

.locked-content p {
  margin: 8px 0 0;
}

.masked-info {
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.profile-locked {
  margin-top: 4px;
}
</style>


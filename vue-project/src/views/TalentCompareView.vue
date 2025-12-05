<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  StarOutlined,
  StarFilled,
  CheckCircleOutlined,
  CloseCircleOutlined,
  TrophyOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue'
import * as talentMarketController from '@/api/talentMarketController'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const aiAnalyzing = ref(false)
const compareData = ref<any>(null)
const aiAnalysisResult = ref<any>(null)
const aiAnalysisTaskId = ref<string | null>(null) // AI分析任务ID
const pollingTimer = ref<any>(null) // 轮询定时器
const showHistoryModal = ref(false)
const historyList = ref<Array<{ 
  id: string
  employeeIds: string[]
  timestamp: number
  talentNames: string[]
  compareData?: any // 完整的对比数据（包括AI分析结果）
}>>([])

// 解析URL中的人才ID
const talentIds = computed(() => {
  const ids = route.query.ids as string
  return ids ? ids.split(',') : []
})

// 生成缓存key（基于人才ID列表）
const getCacheKey = (ids: string[]) => {
  return `talent_compare_${ids.sort().join('_')}`
}

// 从缓存加载对比数据
const loadFromCache = () => {
  if (talentIds.value.length < 2) {
    return null
  }
  
  try {
    const cacheKey = getCacheKey(talentIds.value)
    const cachedData = sessionStorage.getItem(cacheKey)
    
    if (cachedData) {
      const parsed = JSON.parse(cachedData)
      // 检查缓存是否过期（1小时内有效）
      const now = Date.now()
      if (parsed.timestamp && (now - parsed.timestamp < 3600000)) {
        console.log('从缓存加载对比数据')
        return parsed.data
      } else {
        console.log('缓存已过期，清除缓存')
        sessionStorage.removeItem(cacheKey)
      }
    }
  } catch (error) {
    console.error('加载缓存失败:', error)
  }
  
  return null
}

// 保存对比数据到缓存
const saveToCache = (data: any) => {
  if (talentIds.value.length < 2 || !data) {
    return
  }
  
  const cacheKey = getCacheKey(talentIds.value)
  try {
    const cacheData = {
      timestamp: Date.now(),
      data: data,
    }
    sessionStorage.setItem(cacheKey, JSON.stringify(cacheData))
    console.log('对比数据已保存到缓存')
    
    // 同时保存到历史记录
    saveToHistory(data)
  } catch (error) {
    console.error('保存缓存失败:', error)
    // 如果存储空间不足，尝试清除旧缓存
    try {
      clearOldCache()
      sessionStorage.setItem(cacheKey, JSON.stringify({ timestamp: Date.now(), data: data }))
      // 仍然尝试保存历史记录
      saveToHistory(data)
    } catch (e) {
      console.error('清除旧缓存后仍然保存失败:', e)
    }
  }
}

// 保存到历史记录
const saveToHistory = (data: any) => {
  if (talentIds.value.length < 2 || !data) {
    return
  }
  
  try {
    const historyKey = 'talent_compare_history'
    const historyStr = localStorage.getItem(historyKey)
    let history: Array<{ 
      id: string
      employeeIds: string[]
      timestamp: number
      talentNames: string[]
      compareData?: any // 保存完整的对比数据（包括AI分析结果）
    }> = []
    
    if (historyStr) {
      try {
        history = JSON.parse(historyStr)
      } catch (e) {
        console.error('解析历史记录失败:', e)
        history = []
      }
    }
    
    // 生成历史记录ID（基于人才ID）
    const historyId = talentIds.value.sort().join('_')
    
    // 获取人才姓名列表
    const talentNames = (data.items || []).map((item: any) => item.name || '未知').filter(Boolean)
    
    // 检查是否已存在相同的历史记录，如果存在则更新时间
    const existingIndex = history.findIndex(item => item.id === historyId)
    const historyItem = {
      id: historyId,
      employeeIds: [...talentIds.value],
      timestamp: Date.now(),
      talentNames: talentNames,
      compareData: data, // 保存完整的对比数据
    }
    
    if (existingIndex >= 0) {
      // 如果已存在，更新数据（包括最新的AI分析结果）
      history[existingIndex] = historyItem
    } else {
      history.unshift(historyItem)
      // 只保留最近50条记录
      if (history.length > 50) {
        // 删除最旧的记录，但要注意数据大小限制
        history = history.slice(0, 50)
      }
    }
    
    localStorage.setItem(historyKey, JSON.stringify(history))
    console.log('历史记录已保存（包含完整对比数据）')
  } catch (error) {
    console.error('保存历史记录失败:', error)
    // 如果存储空间不足，尝试只保存基本信息
    try {
      const historyKey = 'talent_compare_history'
      const historyStr = localStorage.getItem(historyKey)
      let history: Array<{ id: string; employeeIds: string[]; timestamp: number; talentNames: string[] }> = []
      
      if (historyStr) {
        try {
          history = JSON.parse(historyStr)
        } catch (e) {
          history = []
        }
      }
      
      const historyId = talentIds.value.sort().join('_')
      const talentNames = (data.items || []).map((item: any) => item.name || '未知').filter(Boolean)
      
      const existingIndex = history.findIndex(item => item.id === historyId)
      const historyItem = {
        id: historyId,
        employeeIds: [...talentIds.value],
        timestamp: Date.now(),
        talentNames: talentNames,
      }
      
      if (existingIndex >= 0) {
        history[existingIndex] = historyItem
      } else {
        history.unshift(historyItem)
        if (history.length > 50) {
          history = history.slice(0, 50)
        }
      }
      
      localStorage.setItem(historyKey, JSON.stringify(history))
      console.log('历史记录已保存（仅基本信息，完整数据因存储空间不足未保存）')
    } catch (e) {
      console.error('保存基本信息历史记录也失败:', e)
    }
  }
}

// 清除过期的缓存（保留最近的10条）
const clearOldCache = () => {
  try {
    const cacheKeys = Object.keys(sessionStorage).filter(key => key.startsWith('talent_compare_'))
    const cacheEntries = cacheKeys
      .map(key => {
        try {
          const cached = sessionStorage.getItem(key)
          if (cached) {
            const parsed = JSON.parse(cached)
            return { key, timestamp: parsed.timestamp || 0 }
          }
        } catch (e) {
          return null
        }
        return null
      })
      .filter(entry => entry !== null)
      .sort((a, b) => (b?.timestamp || 0) - (a?.timestamp || 0))
    
    // 保留最近的10条，删除其他的
    if (cacheEntries.length > 10) {
      cacheEntries.slice(10).forEach(entry => {
        if (entry) {
          sessionStorage.removeItem(entry.key)
        }
      })
    }
  } catch (error) {
    console.error('清除旧缓存失败:', error)
  }
}

// 解析AI分析结果
const parseAiAnalysis = (aiResultStr: string | null | undefined) => {
  if (!aiResultStr || aiResultStr.trim() === '') {
    return null
  }

  // 检查是否是错误信息
  if (aiResultStr.includes('AI服务暂不可用') || aiResultStr.includes('AI分析服务异常')) {
    return { error: aiResultStr }
  }

  let jsonStr = aiResultStr.trim()
  
  console.log('开始解析AI分析结果，原始字符串长度:', jsonStr.length)
  console.log('原始字符串前200字符:', jsonStr.substring(0, 200))
  
  // 如果包含markdown代码块，提取JSON部分
  const jsonMatch = jsonStr.match(/```(?:json)?\s*([\s\S]*?)\s*```/)
  if (jsonMatch && jsonMatch[1]) {
    jsonStr = jsonMatch[1].trim()
    console.log('✓ 从markdown代码块中提取JSON成功，长度:', jsonStr.length)
  } else {
    console.log('未找到markdown代码块，使用原始字符串')
  }
  
  // 尝试找到JSON对象的开始和结束位置（处理可能的额外文本）
  let jsonStart = jsonStr.indexOf('{')
  let jsonEnd = jsonStr.lastIndexOf('}')
  
  // 如果没找到，可能是字符串被转义了，尝试找转义的JSON
  if (jsonStart === -1) {
    jsonStart = jsonStr.indexOf('\\{')
    if (jsonStart !== -1) {
      // 找到转义的JSON，需要先解码
      try {
        jsonStr = JSON.parse('"' + jsonStr + '"') // 尝试作为JSON字符串解析
        jsonStart = jsonStr.indexOf('{')
        jsonEnd = jsonStr.lastIndexOf('}')
      } catch (e) {
        console.warn('尝试解码转义JSON失败:', e)
      }
    }
  }
  
  if (jsonStart !== -1 && jsonEnd !== -1 && jsonEnd > jsonStart) {
    jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1)
    console.log('✓ 提取JSON对象，长度:', jsonStr.length)
    console.log('JSON对象前200字符:', jsonStr.substring(0, 200))
  }
  
  // 修复常见的JSON格式问题
  let fixedJson = jsonStr
    .replace(/\/\*[\s\S]*?\*\//g, '') // 移除块注释
    .replace(/\/\/.*$/gm, '') // 移除行注释
    .replace(/,\s*}/g, '}') // 移除尾随逗号
    .replace(/,\s*]/g, ']') // 移除数组尾随逗号
  
  // Unicode引号字符映射
  const unicodeQuotes = [
    ['\u201C', '"'], // 中文左双引号 "
    ['\u201D', '"'], // 中文右双引号 "
    ['\u201E', '"'], // 德语双引号 „
    ['\u201F', '"'], // 德语双引号 ‟
    ['\u2018', "'"], // 中文左单引号 '
    ['\u2019', "'"], // 中文右单引号 '
    ['\u201A', "'"], // 单引号 ‚
    ['\u201B', "'"], // 单引号 ‛
    ['\u2039', '<'], // 左单书名号 ‹
    ['\u203A', '>'], // 右单书名号 ›
    ['\u00AB', '"'], // 左双书名号 «
    ['\u00BB', '"'], // 右双书名号 »
  ]
  
  // 关键修复：使用状态机智能处理Unicode引号
  // 在JSON结构部分（键名、字符串边界），Unicode引号替换为英文引号
  // 在字符串值内部，Unicode引号转义为 \"
  let result = ''
  let inString = false
  let escapeNext = false
  
  for (let i = 0; i < fixedJson.length; i++) {
    const char = fixedJson[i]
    const prevChar = i > 0 ? fixedJson[i - 1] : ''
    
    if (escapeNext) {
      result += char
      escapeNext = false
      continue
    }
    
    if (char === '\\') {
      result += char
      escapeNext = true
      continue
    }
    
    if (char === '"' && prevChar !== '\\') {
      inString = !inString
      result += char
      continue
    }
    
    // 检查是否是Unicode引号
    let isUnicodeQuote = false
    let unicodeQuote = ''
    for (const [unicode, replacement] of unicodeQuotes) {
      if (char === unicode) {
        isUnicodeQuote = true
        unicodeQuote = unicode as string
        break
      }
    }
    
    if (isUnicodeQuote) {
      if (inString) {
        // 在字符串值内部，Unicode引号转义为 \"
        result += '\\"'
      } else {
        // 在JSON结构部分，Unicode引号替换为英文引号
        result += '"'
      }
    } else {
      result += char
    }
  }
  
  fixedJson = result.trim()
  
  // 尝试解析JSON
  try {
    const parsed = JSON.parse(fixedJson)
    console.log('✓ AI分析结果解析成功!')
    console.log('解析结果结构:', parsed ? Object.keys(parsed) : 'null')
    
    // 验证解析结果是否有comparison字段
    if (parsed && typeof parsed === 'object') {
      if (parsed.comparison) {
        console.log('✓ 找到comparison字段，解析完成')
        return parsed
      } else {
        console.warn('解析的JSON没有comparison字段，结构:', Object.keys(parsed))
        // 如果没有comparison字段，但对象本身可能就是comparison的内容
        if (Object.keys(parsed).length > 0) {
          console.log('尝试将整个对象作为comparison')
          return { comparison: parsed }
        }
      }
    }
    
    return parsed
  } catch (error: any) {
    console.error('❌ 解析AI分析结果失败:', error.message)
    console.error('尝试解析的JSON字符串长度:', fixedJson.length)
    console.error('JSON字符串前500字符:', fixedJson.substring(0, 500))
    console.error('JSON字符串后500字符:', fixedJson.substring(Math.max(0, fixedJson.length - 500)))
    
    // 如果还是失败，尝试更激进的修复（处理其他边缘情况）
    try {
      console.log('尝试激进修复...')
      
      // 尝试修复其他可能的JSON格式问题
      let aggressiveFix = fixedJson
        // 修复可能的BOM字符
        .replace(/^\uFEFF/, '') // 移除BOM
        // 移除JSON结构部分（键名、值边界之外）的控制字符
        .replace(/([^"\\])([\u0000-\u001F])/g, '$1') // 移除未转义的控制字符
      
      const parsed = JSON.parse(aggressiveFix)
      console.log('✓ 激进修复后解析成功')
      if (parsed && typeof parsed === 'object' && parsed.comparison) {
        return parsed
      } else if (parsed && typeof parsed === 'object') {
        return { comparison: parsed }
      }
      return parsed
    } catch (fixError: any) {
      console.error('❌ 所有修复尝试都失败:', fixError.message)
      const errorPos = fixError.message.match(/position (\d+)/)?.[1] || 'unknown'
      console.error('错误位置:', errorPos)
      if (errorPos !== 'unknown') {
        const pos = parseInt(errorPos)
        console.error('错误位置前后100字符:', fixedJson.substring(Math.max(0, pos - 100), Math.min(fixedJson.length, pos + 100)))
      }
      
      // 返回友好的错误信息
      return { 
        error: `JSON解析失败: ${fixError.message}`, 
        raw: aiResultStr,
        suggestion: 'AI返回的JSON格式可能存在问题，请联系管理员检查AI服务配置'
      }
    }
  }
}

// 获取对比数据
const fetchCompareData = async (forceRefresh = false) => {
  if (talentIds.value.length < 2) {
    message.error('请至少选择2个人才进行对比')
    router.back()
    return
  }

  // 先检查缓存，如果不强制刷新且有缓存，直接使用缓存
  // 如果URL中有forceRefresh参数，也要跳过缓存
  const shouldUseCache = !forceRefresh && route.query.forceRefresh !== 'true'
  if (shouldUseCache) {
    const cachedData = loadFromCache()
    if (cachedData) {
      console.log('使用缓存数据，跳过API请求')
      compareData.value = cachedData
      loading.value = false
      aiAnalyzing.value = false
      
      // 解析AI分析结果
      if (compareData.value.aiAnalysisResult) {
        aiAnalysisResult.value = parseAiAnalysis(compareData.value.aiAnalysisResult)
      }
      return
    }
  }

  // 检查是否存在相同的对比记录（如果URL中有forceRefresh参数，跳过检查直接重新分析）
  const shouldCheckHistory = !forceRefresh && route.query.forceRefresh !== 'true'
  
  if (shouldCheckHistory) {
    try {
      const checkRes = await talentMarketController.checkExistingCompare({
        employeeIds: talentIds.value.map(id => Number(id)) as any
      })
      
      if (checkRes?.data?.code === 0 && checkRes.data.data) {
        const existingRecord = checkRes.data.data
        // 显示确认对话框
        const confirmed = await new Promise<boolean>((resolve) => {
          Modal.confirm({
            title: '发现历史对比记录',
            content: `检测到您之前进行过相同的人才对比（${existingRecord.employeeNames?.join(' vs ') || '未知'}），创建时间：${existingRecord.createTime ? new Date(existingRecord.createTime).toLocaleString('zh-CN') : '未知'}。是否重新对比？`,
            okText: '重新对比',
            cancelText: '查看历史记录',
            onOk: () => {
              resolve(true)
            },
            onCancel: async () => {
              // 用户选择查看历史记录，从数据库加载完整数据
              try {
                loading.value = true
                const historyRes = await talentMarketController.getCompareHistoryById({
                  recordId: String(existingRecord.id)
                })
                
                if (historyRes?.data?.code === 0 && historyRes.data.data) {
                  const historyRecord = historyRes.data.data
                  
                  // 解析对比结果
                  if (historyRecord.compareResult) {
                    try {
                      compareData.value = typeof historyRecord.compareResult === 'string' 
                        ? JSON.parse(historyRecord.compareResult) 
                        : historyRecord.compareResult
                    } catch (e) {
                      console.error('解析历史对比结果失败:', e)
                      message.error('历史记录数据格式错误')
                      resolve(false)
                      return
                    }
                  }
                  
                  // 解析AI分析结果
                  if (historyRecord.aiAnalysisResult) {
                    aiAnalysisResult.value = parseAiAnalysis(historyRecord.aiAnalysisResult)
                  } else if (compareData.value?.aiAnalysisResult) {
                    aiAnalysisResult.value = parseAiAnalysis(compareData.value.aiAnalysisResult)
                  } else {
                    aiAnalysisResult.value = null
                  }
                  
                  loading.value = false
                  aiAnalyzing.value = false
                  
                  // 更新URL参数，标记为查看历史记录
                  router.replace({
                    path: '/talent-market/compare',
                    query: {
                      ids: talentIds.value.join(','),
                      fromHistory: 'true',
                      recordId: String(existingRecord.id),
                    },
                  })
                  
                  message.success('已加载历史对比记录')
                } else {
                  message.error(historyRes?.data?.message || '加载历史记录失败')
                }
              } catch (error: any) {
                console.error('加载历史记录失败:', error)
                message.error(error?.response?.data?.message || '加载历史记录失败')
              } finally {
                loading.value = false
              }
              
              resolve(false)
            }
          })
        })
        
        if (!confirmed) {
          return // 用户选择查看历史记录，不进行新对比
        }
        // 用户选择重新对比，继续执行
      }
    } catch (error) {
      console.warn('检查历史对比记录失败:', error)
      // 检查失败不影响对比功能，继续执行
    }
  } else {
    // 如果forceRefresh为true，跳过历史记录检查，直接进行新对比
    console.log('跳过历史记录检查，直接进行新对比')
  }

  try {
    loading.value = true
    aiAnalyzing.value = true // 初始显示AI分析加载状态
    aiAnalysisResult.value = null
    
    const res = await talentMarketController.compareTalents({
      employeeIds: talentIds.value,
    })
    
    if (res?.data?.code === 0 && res.data.data) {
      compareData.value = res.data.data
      
      // 先显示基础数据，隐藏loading（立即显示分数等基础信息）
      loading.value = false
      
      // 检查是否有AI分析任务ID（异步AI分析）
      if (compareData.value.aiAnalysisTaskId) {
        // 如果有任务ID，说明AI分析正在异步进行，开始轮询
        aiAnalysisTaskId.value = compareData.value.aiAnalysisTaskId
        aiAnalyzing.value = true
        aiAnalysisResult.value = null
        console.log('AI分析任务已提交，任务ID=', aiAnalysisTaskId.value, '开始轮询AI结果')
        if (aiAnalysisTaskId.value) {
          startPollingAiResult(aiAnalysisTaskId.value)
        }
      } else if (compareData.value.aiAnalysisResult && compareData.value.aiAnalysisResult.trim()) {
        // 如果AI结果已返回（同步返回的情况），直接解析并显示
        try {
          aiAnalysisResult.value = parseAiAnalysis(compareData.value.aiAnalysisResult)
          aiAnalyzing.value = false
          console.log('AI分析结果已同步返回并显示')
        } catch (error) {
          console.error('解析AI分析结果失败:', error)
          aiAnalyzing.value = false
          aiAnalysisResult.value = { error: 'AI分析结果解析失败' }
        }
      } else {
        // AI分析结果为空，可能是AI分析失败或未配置
        aiAnalyzing.value = false
        aiAnalysisResult.value = null
        console.log('AI分析结果为空')
      }
      
      // 保存到缓存
      saveToCache(compareData.value)
    } else {
      message.error('获取对比数据失败')
      loading.value = false
      aiAnalyzing.value = false
    }
  } catch (error: any) {
    console.error('对比失败:', error)
    loading.value = false
    aiAnalyzing.value = false
    
    // 如果是超时错误，request拦截器已经显示了友好的错误提示，这里不需要再显示
    // 其他错误已经在拦截器中处理，这里只做日志记录
    if (error.code === 'ECONNABORTED') {
      console.log('请求超时，AI分析可能需要更长时间')
    } else {
      console.log('对比请求失败:', error.message || error)
    }
  }
}


// 刷新数据（强制刷新，不使用缓存）
const handleRefresh = () => {
  // 清除缓存
  if (talentIds.value.length >= 2) {
    const cacheKey = getCacheKey(talentIds.value)
    sessionStorage.removeItem(cacheKey)
  }
  // 重新获取数据
  fetchCompareData(true)
}

// 返回
const goBack = () => {
  router.push('/talent-market')
}

// 查看详情
const viewDetail = (employeeId: string) => {
  // 保存当前对比页面的URL，以便从详情页返回
  const currentUrl = route.fullPath
  sessionStorage.setItem('talent_compare_return_url', currentUrl)
  router.push(`/talent-market/detail/${employeeId}`)
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 获取最高分人才
const getBestTalent = (field: string) => {
  if (!compareData.value?.items?.length) return null
  let best = compareData.value.items[0]
  for (const talent of compareData.value.items) {
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
      return talent.profileCount || 0
    case 'positiveTagCount':
      return talent.topPositiveTags?.length || 0
    default:
      return 0
  }
}

// 判断是否是最佳
const isBest = (talent: any, field: string) => {
  const best = getBestTalent(field)
  return best && String(best.employeeId) === String(talent.employeeId)
}

// 对比项配置
const compareItems = [
  { key: 'averageScore', label: '综合评分', type: 'score' },
  { key: 'evaluationCount', label: '评价数量', type: 'number' },
  { key: 'latestOccupation', label: '最新职位', type: 'text' },
  { key: 'status', label: '在职状态', type: 'status' },
  { key: 'gender', label: '性别', type: 'text' },
  { key: 'topPositiveTags', label: '正面标签', type: 'tags' },
  { key: 'topNeutralTags', label: '待改进标签', type: 'tags' },
]

// 维度对比项
const dimensionItems = computed(() => {
  if (!compareData.value?.dimensionNames) return []
  return compareData.value.dimensionNames.map((dim: string) => ({
    key: dim,
    label: dim,
  }))
})

// 获取人才列表（兼容items和talents两种数据结构）
const talents = computed(() => {
  if (compareData.value?.items) {
    return compareData.value.items
  }
  if (compareData.value?.talents) {
    return compareData.value.talents
  }
  return []
})

// 根据推荐分数获取颜色
const getScoreColor = (score: number | undefined) => {
  if (!score) return '#d9d9d9'
  if (score >= 80) return '#52c41a' // 绿色 - 优秀
  if (score >= 60) return '#1890ff' // 蓝色 - 良好
  if (score >= 40) return '#faad14' // 橙色 - 一般
  return '#ff4d4f' // 红色 - 较差
}

// 加载历史记录（从数据库）
const loadHistory = async () => {
  try {
    const res = await talentMarketController.getCompareHistory({
      pageNum: 1,
      pageSize: 50
    })
    
    if (res?.data?.code === 0 && res.data.data?.records) {
      // 转换数据库记录格式为前端使用的格式
      historyList.value = res.data.data.records.map((record: any) => ({
        id: String(record.id),
        employeeIds: record.employeeIds || [],
        timestamp: record.createTime ? new Date(record.createTime).getTime() : Date.now(),
        talentNames: record.employeeNames || [],
        compareData: record.compareResult, // 完整的对比数据
        aiAnalysisResult: record.aiAnalysisResult // AI分析结果
      }))
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    historyList.value = []
  }
}

// 查看历史记录
const viewHistory = (historyItem: { id: string; employeeIds: string[]; compareData?: any; aiAnalysisResult?: string }) => {
  showHistoryModal.value = false
  
  // 跳转到对比页面，通过记录ID从数据库加载历史记录
  router.push({
    path: '/talent-market/compare',
    query: { 
      ids: historyItem.employeeIds.join(','),
      fromHistory: 'true',
      recordId: historyItem.id,
    },
  })
}

// 删除历史记录
const deleteHistory = (historyId: string) => {
  try {
    historyList.value = historyList.value.filter(item => item.id !== historyId)
    localStorage.setItem('talent_compare_history', JSON.stringify(historyList.value))
    message.success('删除成功')
  } catch (error) {
    console.error('删除历史记录失败:', error)
    message.error('删除失败')
  }
}

// 格式化时间
const formatHistoryTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 开始轮询AI分析结果
const startPollingAiResult = (taskId: string) => {
  // 清除之前的轮询定时器
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
  }
  
  let pollCount = 0
  const maxPollCount = 120 // 最多轮询120次（约2分钟，每次1秒）
  
  pollingTimer.value = setInterval(async () => {
    pollCount++
    
    if (pollCount > maxPollCount) {
      // 超过最大轮询次数，停止轮询
      clearInterval(pollingTimer.value!)
      pollingTimer.value = null
      aiAnalyzing.value = false
      aiAnalysisResult.value = { error: 'AI分析超时，请稍后重试' }
      console.error('AI分析轮询超时')
      return
    }
    
    try {
      const res = await talentMarketController.getAiAnalysisResult({ taskId })
      
      if (res?.data?.code === 0 && res.data.data) {
        const { status, result, error } = res.data.data
        
        if (status === 'completed' && result) {
          // AI分析完成，解析并显示结果
          clearInterval(pollingTimer.value!)
          pollingTimer.value = null
          aiAnalyzing.value = false
          aiAnalysisResult.value = parseAiAnalysis(result)
          console.log('AI分析结果已获取并显示')
          
          // 更新缓存中的AI结果
          if (compareData.value) {
            compareData.value.aiAnalysisResult = result
            saveToCache(compareData.value)
          }
        } else if (status === 'failed') {
          // AI分析失败
          clearInterval(pollingTimer.value!)
          pollingTimer.value = null
          aiAnalyzing.value = false
          aiAnalysisResult.value = { error: error || 'AI分析失败' }
          console.error('AI分析失败:', error)
        } else if (status === 'processing') {
          // AI分析仍在进行中，继续轮询
          console.log(`AI分析进行中... (${pollCount}/${maxPollCount})`)
        } else if (status === 'not_found') {
          // 任务不存在，停止轮询
          clearInterval(pollingTimer.value!)
          pollingTimer.value = null
          aiAnalyzing.value = false
          aiAnalysisResult.value = { error: 'AI分析任务不存在' }
          console.error('AI分析任务不存在')
        }
      }
    } catch (error: any) {
      console.error('轮询AI分析结果失败:', error)
      // 轮询失败不停止，继续尝试
    }
  }, 1000) // 每秒轮询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

// 从数据库加载历史记录数据
const loadFromHistory = async () => {
  const recordId = route.query.recordId as string
  if (!recordId) {
    return false
  }

  try {
    console.log('从数据库加载历史记录，记录ID:', recordId)
    loading.value = true
    
    const res = await talentMarketController.getCompareHistoryById({ recordId })
    
    if (res?.data?.code === 0 && res.data.data) {
      const historyRecord = res.data.data
      
      // 检查员工ID是否匹配
      const currentIds = talentIds.value.sort().join(',')
      const historyIds = (historyRecord.employeeIds || []).map((id: any) => String(id)).sort().join(',')
      
      if (currentIds === historyIds) {
        console.log('从数据库加载历史记录成功，不发起新请求')
        
        // 解析对比结果
        if (historyRecord.compareResult) {
          try {
            compareData.value = typeof historyRecord.compareResult === 'string' 
              ? JSON.parse(historyRecord.compareResult) 
              : historyRecord.compareResult
          } catch (e) {
            console.error('解析历史对比结果失败:', e)
            message.error('历史记录数据格式错误')
            return false
          }
        }
        
        // 解析AI分析结果
        if (historyRecord.aiAnalysisResult) {
          aiAnalysisResult.value = parseAiAnalysis(historyRecord.aiAnalysisResult)
        } else if (compareData.value?.aiAnalysisResult) {
          aiAnalysisResult.value = parseAiAnalysis(compareData.value.aiAnalysisResult)
        } else {
          aiAnalysisResult.value = null
        }
        
        loading.value = false
        aiAnalyzing.value = false
        
        return true
      } else {
        console.warn('员工ID不匹配，无法使用历史记录')
        message.warning('历史记录与当前对比人员不匹配')
        return false
      }
    } else {
      console.error('获取历史记录失败:', res?.data?.message)
      message.error(res?.data?.message || '获取历史记录失败')
      return false
    }
  } catch (error: any) {
    console.error('从数据库加载历史记录失败:', error)
    message.error(error?.response?.data?.message || '加载历史记录失败')
    return false
  } finally {
    if (!compareData.value) {
      loading.value = false
    }
  }
}

onMounted(async () => {
  // 如果URL中有 fromHistory 参数，说明是查看历史记录，应该从数据库加载历史数据
  if (route.query.fromHistory === 'true') {
    const loaded = await loadFromHistory()
    if (loaded) {
      // 成功从数据库加载历史记录，不发起新请求
      return
    }
    // 如果加载失败，继续正常流程
  }
  
  // 检查URL中是否有forceRefresh参数，如果有则强制刷新，跳过历史记录检查
  const forceRefresh = route.query.forceRefresh === 'true'
  
  // 否则，正常进行对比分析
  fetchCompareData(forceRefresh)
})

// 组件卸载时清理轮询
onBeforeUnmount(() => {
  stopPolling()
})
</script>

<template>
  <div class="talent-compare">
    <a-page-header title="深度人才对比分析" @back="goBack">
      <template #extra>
        <a-space>
          <a-button @click="showHistoryModal = true">历史记录</a-button>
          <a-button @click="handleRefresh">刷新</a-button>
          <a-button @click="goBack">返回人才市场</a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-spin :spinning="loading" tip="正在加载对比数据...">
      <div class="compare-content" v-if="!loading && talents.length">

        <!-- 人才头部信息 -->
        <a-card class="compare-header-card">
          <a-row :gutter="0" class="compare-header">
            <a-col :span="4" class="compare-label-col">
              <div class="compare-label">人才信息</div>
            </a-col>
            <a-col
              v-for="talent in talents"
              :key="talent.employeeId || talent.id"
              :span="Math.floor(20 / talents.length)"
              class="compare-value-col"
            >
              <div class="talent-header" @click="viewDetail(String(talent.employeeId || talent.id))">
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
              v-for="talent in talents"
              :key="talent.employeeId || talent.id"
              :span="Math.floor(20 / talents.length)"
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
                    <a-rate :value="Number(talent[item.key] || 0)" disabled allow-half :count="5" style="font-size: 14px" />
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
                      :color="item.key === 'topPositiveTags' ? 'blue' : 'orange'"
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
              v-for="talent in talents"
              :key="talent.employeeId || talent.id"
              :span="Math.floor(20 / talents.length)"
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

        <!-- 深度智能分析 -->
        <a-card title="深度智能分析" class="compare-section-card">
          <!-- AI分析加载状态 -->
          <template v-if="aiAnalyzing">
            <a-alert
              message="正在进行深度分析，请稍后......"
              type="info"
              show-icon
              style="margin: 20px 0"
            >
              <template #icon>
                <LoadingOutlined spin />
              </template>
            </a-alert>
          </template>
          
          <!-- AI分析结果 -->
          <template v-else>
          <template v-if="aiAnalysisResult">
            <!-- 如果解析成功，显示结构化结果 -->
            <template v-if="aiAnalysisResult.comparison">
              <div class="ai-analysis-content">
                <!-- 整体对比总结 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.summary">
                  <h4>
                    <TrophyOutlined style="margin-right: 8px; color: #1890ff" />
                    整体对比总结
                  </h4>
                  <p class="summary-text">{{ aiAnalysisResult.comparison.summary }}</p>
                </div>

                <!-- 优势分析 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.strengths">
                  <h4>
                    <CheckCircleOutlined style="margin-right: 8px; color: #52c41a" />
                    优势分析
                  </h4>
                  <div
                    v-for="(strengths, candidateName) in aiAnalysisResult.comparison.strengths"
                    :key="candidateName"
                    class="candidate-analysis-item"
                  >
                    <h5>{{ candidateName }}</h5>
                    <div class="tag-group">
                      <a-tag
                        v-for="(strength, i) in strengths"
                        :key="i"
                        color="green"
                        class="analysis-tag"
                      >
                        {{ strength }}
                      </a-tag>
                    </div>
                  </div>
                </div>

                <!-- 劣势分析 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.weaknesses">
                  <h4>
                    <CloseCircleOutlined style="margin-right: 8px; color: #ff4d4f" />
                    劣势分析
                  </h4>
                  <div
                    v-for="(weaknesses, candidateName) in aiAnalysisResult.comparison.weaknesses"
                    :key="candidateName"
                    class="candidate-analysis-item"
                  >
                    <h5>{{ candidateName }}</h5>
                    <div class="tag-group">
                      <a-tag
                        v-for="(weakness, i) in weaknesses"
                        :key="i"
                        color="orange"
                        class="analysis-tag"
                      >
                        {{ weakness }}
                      </a-tag>
                    </div>
                  </div>
                </div>

                <!-- 关键差异点 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.keyDifferences?.length">
                  <h4>
                    <StarOutlined style="margin-right: 8px; color: #faad14" />
                    关键差异点
                  </h4>
                  <div
                    v-for="(diff, index) in aiAnalysisResult.comparison.keyDifferences"
                    :key="index"
                    class="key-difference-item"
                  >
                    <div class="difference-aspect">
                      <strong>{{ diff.aspect }}</strong>
                    </div>
                    <p class="difference-text">{{ diff.comparison }}</p>
                  </div>
                </div>

                <!-- 推荐建议 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.recommendations?.length">
                  <h4>
                    <TrophyOutlined style="margin-right: 8px; color: #722ed1" />
                    招聘建议
                  </h4>
                  <div
                    v-for="(rec, index) in aiAnalysisResult.comparison.recommendations"
                    :key="index"
                    class="recommendation-item"
                  >
                    <div class="recommendation-header">
                      <h5>{{ rec.candidateName }}</h5>
                      <div class="recommendation-score">
                        <span class="score-label">推荐指数：</span>
                        <a-progress
                          :percent="rec.score || 0"
                          :stroke-color="getScoreColor(rec.score)"
                          :format="(percent: number) => `${rec.score}分`"
                          :show-info="true"
                          style="width: 200px"
                        />
                      </div>
                    </div>
                    <div class="recommendation-content">
                      <div class="content-row" v-if="rec.reason">
                        <strong>推荐理由：</strong>
                        <p>{{ rec.reason }}</p>
                      </div>
                      <div class="content-row" v-if="rec.suitability">
                        <strong>适合岗位：</strong>
                        <p>{{ rec.suitability }}</p>
                      </div>
                      <div class="content-row" v-if="rec.riskPoints?.length">
                        <strong>风险提示：</strong>
                        <div class="risk-tags">
                          <a-tag
                            v-for="(risk, i) in rec.riskPoints"
                            :key="i"
                            color="red"
                            class="risk-tag"
                          >
                            {{ risk }}
                          </a-tag>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 最终建议 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.finalSuggestion">
                  <h4>
                    <StarFilled style="margin-right: 8px; color: #1890ff" />
                    最终建议
                  </h4>
                  <div class="final-suggestion-box">
                    <p>{{ aiAnalysisResult.comparison.finalSuggestion }}</p>
                  </div>
                </div>

                <!-- 兼容旧格式：候选人分析 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.candidate_analysis?.length">
                  <h4>候选人分析</h4>
                  <div
                    v-for="(analysis, index) in aiAnalysisResult.comparison.candidate_analysis"
                    :key="index"
                    class="candidate-analysis-item"
                  >
                    <h5>{{ analysis.candidate_name }}</h5>
                    <div class="analysis-row" v-if="analysis.strengths?.length">
                      <strong>优势：</strong>
                      <div class="tag-group">
                        <a-tag
                          v-for="(strength, i) in analysis.strengths"
                          :key="i"
                          color="green"
                          class="analysis-tag"
                        >
                          {{ strength }}
                        </a-tag>
                      </div>
                    </div>
                    <div class="analysis-row" v-if="analysis.weaknesses?.length">
                      <strong>劣势：</strong>
                      <div class="tag-group">
                        <a-tag
                          v-for="(weakness, i) in analysis.weaknesses"
                          :key="i"
                          color="orange"
                          class="analysis-tag"
                        >
                          {{ weakness }}
                        </a-tag>
                      </div>
                    </div>
                    <div class="analysis-row" v-if="analysis.fit_to_preference">
                      <strong>匹配度分析：</strong>
                      <p>{{ analysis.fit_to_preference }}</p>
                    </div>
                  </div>
                </div>

                <!-- 兼容旧格式：推荐建议 -->
                <div class="ai-section" v-if="aiAnalysisResult.comparison.recommendation">
                  <h4>招聘建议</h4>
                  <div
                    v-if="aiAnalysisResult.comparison.recommendation.overall_recommendation"
                  >
                    <p><strong>综合建议：</strong>{{ aiAnalysisResult.comparison.recommendation.overall_recommendation }}</p>
                  </div>
                  <div
                    v-if="aiAnalysisResult.comparison.recommendation.considerations?.length"
                  >
                    <strong>额外考虑因素：</strong>
                    <ul>
                      <li
                        v-for="(consideration, index) in aiAnalysisResult.comparison.recommendation.considerations"
                        :key="index"
                      >
                        {{ consideration }}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </template>

            <!-- 如果是错误信息 -->
            <template v-else-if="aiAnalysisResult.error">
              <a-alert
                :message="aiAnalysisResult.error"
                type="warning"
                show-icon
              />
            </template>

            <!-- 如果是原始文本 -->
            <template v-else-if="aiAnalysisResult.raw">
              <a-alert
                message="深度智能分析结果解析失败，请检查浏览器控制台查看详细错误信息"
                type="error"
                show-icon
                style="margin-bottom: 16px"
              />
              <pre class="ai-raw-text">{{ aiAnalysisResult.raw }}</pre>
            </template>
          </template>

          <!-- 没有AI分析结果 -->
          <template v-else>
            <a-empty description="暂无深度智能分析结果" :image="false" />
          </template>
          </template>
        </a-card>

        <!-- 操作按钮 -->
        <div class="compare-actions">
          <a-space>
            <a-button
              v-for="talent in talents"
              :key="talent.employeeId || talent.id"
              type="primary"
              @click="viewDetail(String(talent.employeeId || talent.id))"
            >
              查看 {{ talent.name }} 详情
            </a-button>
          </a-space>
        </div>
      </div>

      <a-empty v-if="!loading && !talents.length" description="暂无对比数据" />
    </a-spin>

    <!-- 历史记录弹窗 -->
    <a-modal
      v-model:open="showHistoryModal"
      title="对比历史记录"
      width="800px"
      :footer="null"
      @open="loadHistory"
    >
      <a-list
        :data-source="historyList"
        :pagination="{
          pageSize: 10,
          showSizeChanger: false,
          showTotal: (total: number) => `共 ${total} 条记录`,
        }"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-space>
                  <span>{{ item.talentNames.join(' vs ') }}</span>
                  <a-tag color="blue">{{ item.employeeIds.length }}人</a-tag>
                </a-space>
              </template>
              <template #description>
                <span style="color: #999">{{ formatHistoryTime(item.timestamp) }}</span>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" @click="viewHistory(item)">查看</a-button>
              <a-popconfirm
                title="确定要删除这条记录吗？"
                @confirm="deleteHistory(item.id)"
              >
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </template>
          </a-list-item>
        </template>
        <template #emptyText>
          <a-empty description="暂无历史记录" :image="false" />
        </template>
      </a-list>
    </a-modal>
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

.compare-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* AI分析结果样式 */
.ai-analysis-content {
  padding: 16px 0;
}

.ai-section {
  margin-bottom: 24px;
}

.ai-section:last-child {
  margin-bottom: 0;
}

.ai-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e6f7ff;
}

.ai-section h5 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  margin-top: 16px;
}

.ai-section p {
  line-height: 1.8;
  color: #666;
  margin: 8px 0;
}

.candidate-analysis-item {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.candidate-analysis-item:last-child {
  margin-bottom: 0;
}

.analysis-row {
  margin-bottom: 12px;
  line-height: 1.8;
}

.analysis-row strong {
  color: #333;
  margin-right: 8px;
}

.ai-section ul {
  margin: 8px 0;
  padding-left: 24px;
}

.ai-section li {
  line-height: 1.8;
  color: #666;
  margin-bottom: 4px;
}

.ai-raw-text {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* 新增样式 */
.summary-text {
  background: #e6f7ff;
  padding: 16px;
  border-radius: 4px;
  border-left: 4px solid #1890ff;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.analysis-tag {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 4px;
}

.key-difference-item {
  background: #fffbe6;
  padding: 16px;
  border-radius: 4px;
  border-left: 4px solid #faad14;
  margin-bottom: 16px;
}

.key-difference-item:last-child {
  margin-bottom: 0;
}

.difference-aspect {
  font-size: 15px;
  color: #333;
  margin-bottom: 8px;
}

.difference-text {
  line-height: 1.8;
  color: #666;
  margin: 0;
}

.recommendation-item {
  background: #f9f0ff;
  padding: 20px;
  border-radius: 4px;
  border-left: 4px solid #722ed1;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.recommendation-item:last-child {
  margin-bottom: 0;
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8d4ff;
}

.recommendation-header h5 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #722ed1;
}

.recommendation-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.recommendation-content {
  line-height: 1.8;
}

.content-row {
  margin-bottom: 16px;
}

.content-row:last-child {
  margin-bottom: 0;
}

.content-row strong {
  color: #333;
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
}

.content-row p {
  color: #666;
  margin: 0;
  line-height: 1.8;
}

.risk-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.risk-tag {
  font-size: 12px;
  padding: 4px 10px;
}

.final-suggestion-box {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  padding: 20px;
  border-radius: 4px;
  border: 2px solid #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.final-suggestion-box p {
  margin: 0;
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}
</style>

/**
 * 行业分类常量
 * 包含大类和对应的子类列表
 */

export interface IndustrySubCategory {
  label: string
  value: string
}

export interface IndustryCategory {
  label: string
  value: string
  children: IndustrySubCategory[]
}

/**
 * 行业分类数据
 */
export const INDUSTRY_CATEGORIES: IndustryCategory[] = [
  {
    label: '制造业',
    value: '制造业',
    children: [
      { label: '电子/电气/通信设备', value: '电子/电气/通信设备' },
      { label: '汽车制造', value: '汽车制造' },
      { label: '机械制造', value: '机械制造' },
      { label: '纺织服装', value: '纺织服装' },
      { label: '食品饮料', value: '食品饮料' },
      { label: '化工/材料', value: '化工/材料' },
      { label: '金属制品', value: '金属制品' },
      { label: '家具/家居', value: '家具/家居' },
      { label: '玩具/礼品', value: '玩具/礼品' },
    ],
  },
  {
    label: '信息技术/互联网',
    value: '信息技术/互联网',
    children: [
      { label: '软件开发', value: '软件开发' },
      { label: '互联网平台', value: '互联网平台' },
      { label: '人工智能', value: '人工智能' },
      { label: '大数据/云计算', value: '大数据/云计算' },
      { label: '网络安全', value: '网络安全' },
      { label: '电子商务', value: '电子商务' },
      { label: '游戏/娱乐', value: '游戏/娱乐' },
      { label: '移动应用开发', value: '移动应用开发' },
    ],
  },
  {
    label: '金融/保险',
    value: '金融/保险',
    children: [
      { label: '银行/证券/基金', value: '银行/证券/基金' },
      { label: '保险', value: '保险' },
      { label: '金融科技', value: '金融科技' },
      { label: '投资管理', value: '投资管理' },
      { label: '融资租赁', value: '融资租赁' },
    ],
  },
  {
    label: '房地产/建筑',
    value: '房地产/建筑',
    children: [
      { label: '房地产开发', value: '房地产开发' },
      { label: '物业管理', value: '物业管理' },
      { label: '建筑工程', value: '建筑工程' },
      { label: '室内设计', value: '室内设计' },
      { label: '装饰装修', value: '装饰装修' },
    ],
  },
  {
    label: '专业服务',
    value: '专业服务',
    children: [
      { label: '人力资源', value: '人力资源' },
      { label: '咨询/管理', value: '咨询/管理' },
      { label: '会计/审计', value: '会计/审计' },
      { label: '法律服务', value: '法律服务' },
      { label: '广告/传媒', value: '广告/传媒' },
      { label: '市场营销', value: '市场营销' },
    ],
  },
  {
    label: '消费零售',
    value: '消费零售',
    children: [
      { label: '快消品', value: '快消品' },
      { label: '零售/批发', value: '零售/批发' },
      { label: '电子商务', value: '电子商务' },
      { label: '餐饮/酒店', value: '餐饮/酒店' },
      { label: '旅游/度假', value: '旅游/度假' },
    ],
  },
  {
    label: '医疗健康',
    value: '医疗健康',
    children: [
      { label: '医药/生物', value: '医药/生物' },
      { label: '医疗器械', value: '医疗器械' },
      { label: '医疗服务', value: '医疗服务' },
      { label: '医美/健康', value: '医美/健康' },
      { label: '医疗科技', value: '医疗科技' },
    ],
  },
  {
    label: '能源/环保',
    value: '能源/环保',
    children: [
      { label: '石油/化工', value: '石油/化工' },
      { label: '新能源', value: '新能源' },
      { label: '环保/节能', value: '环保/节能' },
      { label: '电力/能源', value: '电力/能源' },
      { label: '水利/环境', value: '水利/环境' },
    ],
  },
  {
    label: '教育/文化',
    value: '教育/文化',
    children: [
      { label: 'K12教育', value: 'K12教育' },
      { label: '职业教育', value: '职业教育' },
      { label: '高等教育', value: '高等教育' },
      { label: '文化传媒', value: '文化传媒' },
      { label: '影视/娱乐', value: '影视/娱乐' },
    ],
  },
  {
    label: '其他',
    value: '其他',
    children: [
      { label: '农林牧渔', value: '农林牧渔' },
      { label: '交通运输', value: '交通运输' },
      { label: '物流/仓储', value: '物流/仓储' },
      { label: '公共事业', value: '公共事业' },
      { label: '政府/非营利组织', value: '政府/非营利组织' },
    ],
  },
]

/**
 * 根据大类获取子类列表
 */
export function getIndustrySubCategories(category: string): IndustrySubCategory[] {
  const found = INDUSTRY_CATEGORIES.find((item) => item.value === category)
  return found ? found.children : []
}

/**
 * 获取所有大类
 */
export function getIndustryCategories(): { label: string; value: string }[] {
  return INDUSTRY_CATEGORIES.map((item) => ({
    label: item.label,
    value: item.value,
  }))
}


<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { INDUSTRY_CATEGORIES, type IndustryCategory } from '@/constants/industry'

interface Props {
  modelValue?: string[]
  placeholder?: string
  maxTagCount?: number
  category?: string // 行业大类，如果提供则只显示该大类的子类
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  placeholder: '公司行业',
  maxTagCount: 3,
  category: undefined,
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  change: [value: string[]]
}>()

const dropdownVisible = ref(false)
const selectedIndustries = ref<string[]>([...props.modelValue])
const expandedCategories = ref<Set<string>>(new Set()) // 记录展开的行业大类

// 监听外部值变化
watch(
  () => props.modelValue,
  (newValue) => {
    selectedIndustries.value = [...newValue]
  }
)

// 根据传入的 category 过滤显示的行业分类
const displayedCategories = computed(() => {
  if (props.category) {
    // 如果指定了行业大类，只显示该大类的子类
    const category = INDUSTRY_CATEGORIES.find((cat) => cat.value === props.category)
    return category ? [category] : []
  }
  // 如果没有指定，显示所有大类
  return INDUSTRY_CATEGORIES
})

// 切换选中状态
const toggleIndustry = (industry: string) => {
  const index = selectedIndustries.value.indexOf(industry)
  if (index > -1) {
    selectedIndustries.value.splice(index, 1)
  } else {
    selectedIndustries.value.push(industry)
  }
  emit('update:modelValue', selectedIndustries.value)
  emit('change', selectedIndustries.value)
}

// 判断是否选中
const isSelected = (industry: string) => {
  return selectedIndustries.value.includes(industry)
}

// 切换大类展开/收起
const toggleCategory = (categoryValue: string) => {
  if (expandedCategories.value.has(categoryValue)) {
    expandedCategories.value.delete(categoryValue)
  } else {
    expandedCategories.value.add(categoryValue)
  }
}

// 判断大类是否展开
const isCategoryExpanded = (categoryValue: string) => {
  return expandedCategories.value.has(categoryValue)
}

// 清空选择
const clearSelection = () => {
  selectedIndustries.value = []
  emit('update:modelValue', selectedIndustries.value)
  emit('change', selectedIndustries.value)
}

// 显示文本
const displayText = computed(() => {
  if (selectedIndustries.value.length === 0) {
    return props.placeholder
  }
  if (selectedIndustries.value.length <= props.maxTagCount) {
    return selectedIndustries.value.join('、')
  }
  return `${selectedIndustries.value.slice(0, props.maxTagCount).join('、')} +${selectedIndustries.value.length - props.maxTagCount}`
})

// 点击外部关闭下拉框
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.industry-selector')) {
    dropdownVisible.value = false
  }
}

// 挂载和卸载事件监听
const toggleDropdown = () => {
  if (props.disabled) {
    return
  }
  dropdownVisible.value = !dropdownVisible.value
  // 如果指定了行业大类，自动展开
  if (dropdownVisible.value && props.category) {
    expandedCategories.value.add(props.category)
  }
}

// 监听下拉框状态
watch(dropdownVisible, (visible) => {
  if (visible) {
    document.addEventListener('click', handleClickOutside)
  } else {
    document.removeEventListener('click', handleClickOutside)
  }
})

// 监听 category 变化，自动展开
watch(
  () => props.category,
  (newCategory) => {
    if (newCategory && dropdownVisible.value) {
      expandedCategories.value.add(newCategory)
    }
  }
)
</script>

<template>
  <div class="industry-selector" :class="{ disabled: disabled }">
    <div class="current-select" @click="toggleDropdown" :class="{ disabled: disabled }">
      <span :class="{ 'placeholder-text': selectedIndustries.length === 0, 'selected-text': selectedIndustries.length > 0 }">
        {{ displayText }}
      </span>
      <span v-if="selectedIndustries.length > 0" class="clear-btn" @click.stop="clearSelection">
        <a-icon type="close-circle" />
      </span>
    </div>

    <div v-show="dropdownVisible" class="filter-select-dropdown">
      <ul>
        <li v-for="category in displayedCategories" :key="category.value" class="category-item">
          <div class="category-header" @click="toggleCategory(category.value)">
            <span class="label">{{ category.label }}</span>
            <span class="expand-icon" :class="{ expanded: isCategoryExpanded(category.value) }">
              ▼
            </span>
          </div>
          <div
            class="select-list"
            :class="{ expanded: isCategoryExpanded(category.value) }"
            v-show="isCategoryExpanded(category.value)"
          >
            <a
              v-for="subCategory in category.children"
              :key="subCategory.value"
              href="javascript:;"
              :class="{ selected: isSelected(subCategory.value) }"
              @click="toggleIndustry(subCategory.value)"
            >
              {{ subCategory.label }}
              <span v-if="isSelected(subCategory.value)" class="check-icon">✓</span>
            </a>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.industry-selector {
  position: relative;
  width: 100%;
}

.current-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 11px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 32px;
}

.current-select:hover:not(.disabled) {
  border-color: #4096ff;
}

.current-select.disabled {
  cursor: not-allowed;
  background-color: #f5f5f5;
  color: #bfbfbf;
}

.industry-selector.disabled .placeholder-text {
  color: #bfbfbf;
}

.placeholder-text {
  color: #bfbfbf;
  font-size: 14px;
}

.selected-text {
  color: #000000d9;
  font-size: 14px;
}

.clear-btn {
  color: #bfbfbf;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.3s;
  margin-left: 8px;
  flex-shrink: 0;
}

.clear-btn:hover {
  color: #ff4d4f;
}

.filter-select-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 1050;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
    0 9px 28px 8px rgba(0, 0, 0, 0.05);
  max-height: 400px;
  overflow-y: auto;
}

.filter-select-dropdown ul {
  list-style: none;
  margin: 0;
  padding: 8px 0;
}

.category-item {
  padding: 0;
  border-bottom: 1px solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  user-select: none;
}

.category-header:hover {
  background-color: #fafafa;
}

.label {
  font-weight: 600;
  font-size: 14px;
  color: #000000d9;
}

.expand-icon {
  font-size: 12px;
  color: #8c8c8c;
  transition: transform 0.3s;
  transform: rotate(-90deg);
}

.expand-icon.expanded {
  transform: rotate(0deg);
}

.select-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 12px 16px;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease-out, padding 0.3s ease-out;
}

.select-list.expanded {
  max-height: 500px;
  padding: 0 16px 12px 16px;
}

.select-list a {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  font-size: 13px;
  color: #00000073;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
}

.select-list a:hover {
  background-color: #f5f5f5;
  color: #1890ff;
}

.select-list a.selected {
  background-color: #e6f4ff;
  color: #1890ff;
  font-weight: 500;
}

.check-icon {
  margin-left: 4px;
  font-size: 12px;
  font-weight: bold;
}

/* 滚动条样式 */
.filter-select-dropdown::-webkit-scrollbar {
  width: 6px;
}

.filter-select-dropdown::-webkit-scrollbar-thumb {
  background-color: #d9d9d9;
  border-radius: 3px;
}

.filter-select-dropdown::-webkit-scrollbar-thumb:hover {
  background-color: #bfbfbf;
}
</style>


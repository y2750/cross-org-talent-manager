<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { INDUSTRY_CATEGORIES } from '@/constants/industry'

interface Props {
  modelValue?: string[]
  placeholder?: string
  maxTagCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  placeholder: '公司行业',
  maxTagCount: 3,
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  change: [value: string[]]
}>()

const dropdownVisible = ref(false)
const selectedIndustries = ref<string[]>([...props.modelValue])

// 监听外部值变化
watch(
  () => props.modelValue,
  (newValue) => {
    selectedIndustries.value = [...newValue]
  }
)

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
  dropdownVisible.value = !dropdownVisible.value
}

// 监听下拉框状态
watch(dropdownVisible, (visible) => {
  if (visible) {
    document.addEventListener('click', handleClickOutside)
  } else {
    document.removeEventListener('click', handleClickOutside)
  }
})
</script>

<template>
  <div class="industry-selector">
    <div class="current-select" @click="toggleDropdown">
      <span :class="{ 'placeholder-text': selectedIndustries.length === 0, 'selected-text': selectedIndustries.length > 0 }">
        {{ displayText }}
      </span>
      <span v-if="selectedIndustries.length > 0" class="clear-btn" @click.stop="clearSelection">
        <a-icon type="close-circle" />
      </span>
    </div>

    <div v-show="dropdownVisible" class="filter-select-dropdown">
      <ul>
        <li v-for="category in INDUSTRY_CATEGORIES" :key="category.value" class="category-item">
          <span class="label">{{ category.label }}</span>
          <div class="select-list">
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

.current-select:hover {
  border-color: #4096ff;
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
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

.label {
  display: block;
  font-weight: 600;
  font-size: 14px;
  color: #000000d9;
  margin-bottom: 8px;
}

.select-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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


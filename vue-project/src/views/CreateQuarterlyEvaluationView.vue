<template>
  <div class="create-quarterly-evaluation-container">
    <a-page-header title="创建季度评价任务" @back="() => $router.push('/home')" />

    <a-card style="margin-top: 16px">
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }" @finish="handleSubmit">
        <a-form-item label="选择部门" name="departmentId" :rules="[{ required: true, message: '请选择部门' }]">
          <a-select v-model:value="form.departmentId" placeholder="请选择部门" :loading="departmentLoading">
            <a-select-option v-for="dept in departmentList" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="评价年份" name="periodYear" :rules="[{ required: true, message: '请选择评价年份' }]">
          <a-select v-model:value="form.periodYear" placeholder="请选择评价年份">
            <a-select-option v-for="year in yearOptions" :key="year" :value="year">
              {{ year }}年
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="评价季度" name="periodQuarter" :rules="[{ required: true, message: '请选择评价季度' }]">
          <a-select v-model:value="form.periodQuarter" placeholder="请选择评价季度">
            <a-select-option :value="1">第一季度（1-3月）</a-select-option>
            <a-select-option :value="2">第二季度（4-6月）</a-select-option>
            <a-select-option :value="3">第三季度（7-9月）</a-select-option>
            <a-select-option :value="4">第四季度（10-12月）</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-button type="primary" html-type="submit" :loading="submitting">创建评价任务</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import * as evaluationTaskController from '@/api/evaluationTaskController'
import * as departmentController from '@/api/departmentController'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

const form = reactive({
  departmentId: undefined as number | undefined,
  periodYear: new Date().getFullYear(),
  periodQuarter: undefined as number | undefined,
})

const submitting = ref(false)
const departmentLoading = ref(false)
const departmentList = ref<API.DepartmentVO[]>([])

// 生成年份选项（当前年份及前后2年）
const yearOptions = ref<number[]>([])
const currentYear = new Date().getFullYear()
for (let i = currentYear - 2; i <= currentYear + 2; i++) {
  yearOptions.value.push(i)
}

// 加载部门列表（只加载当前用户作为主管的部门）
const loadDepartments = async () => {
  try {
    departmentLoading.value = true
    // 获取当前用户的员工信息
    const employeeResponse = await departmentController.listDepartment()
    if (employeeResponse?.data?.code === 0) {
      const allDepartments = employeeResponse.data.data || []
      // 过滤出当前用户作为主管的部门
      // 这里需要根据实际情况判断，暂时显示所有部门
      departmentList.value = allDepartments
    }
  } catch (error) {
    console.error('Failed to load departments:', error)
    message.error('加载部门列表失败')
  } finally {
    departmentLoading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!form.departmentId || !form.periodYear || !form.periodQuarter) {
    message.warning('请填写完整信息')
    return
  }

  try {
    submitting.value = true
    const response = await evaluationTaskController.createQuarterlyEvaluationTasks({
      departmentId: form.departmentId,
      periodYear: form.periodYear,
      periodQuarter: form.periodQuarter,
    })

    if (response?.data?.code === 0) {
      const count = response.data.data || 0
      message.success(`成功创建 ${count} 个季度评价任务`)
      handleReset()
    } else {
      message.error(response?.data?.message || '创建评价任务失败')
    }
  } catch (error) {
    console.error('Failed to create quarterly evaluation tasks:', error)
    message.error('创建评价任务失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  form.departmentId = undefined
  form.periodYear = new Date().getFullYear()
  form.periodQuarter = undefined
}

onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.create-quarterly-evaluation-container {
  padding: 24px;
}
</style>



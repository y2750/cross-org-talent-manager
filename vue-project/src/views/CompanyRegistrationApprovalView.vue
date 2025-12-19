<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import * as companyRegistrationController from '@/api/companyRegistrationController'

const loading = ref(false)
const tableData = ref<API.CompanyRegistrationRequestVO[]>([])

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  companyName: '',
  status: 0 as number | undefined,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const columns = [
  { title: '企业名称', dataIndex: 'companyName', key: 'companyName', width: 160 },
  { title: '行业大类', dataIndex: 'industryCategory', key: 'industryCategory', width: 120 },
  { title: '管理人姓名', dataIndex: 'adminName', key: 'adminName', width: 120 },
  { title: '管理人电话', dataIndex: 'adminPhone', key: 'adminPhone', width: 140 },
  { title: '企业邮箱', dataIndex: 'companyEmail', key: 'companyEmail', width: 180 },
  { title: '状态', key: 'status', width: 100 },
  { title: '申请时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'operation', width: 100, fixed: 'right' as const },
]

const statusTextMap: Record<number, string> = {
  0: '待处理',
  1: '已通过',
  2: '已拒绝',
}

const statusColorMap: Record<number, string> = {
  0: 'orange',
  1: 'green',
  2: 'red',
}

const formatDateTime = (val?: string) => {
  if (!val) return '-'
  try {
    const d = new Date(val)
    return d.toLocaleString('zh-CN')
  } catch {
    return val
  }
}

const fetchData = async () => {
  try {
    loading.value = true
    const res = await companyRegistrationController.listCompanyRegistrationPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      companyName: searchParams.companyName || undefined,
      status: searchParams.status,
      sortField: searchParams.sortField,
      sortOrder: searchParams.sortOrder,
    })
    if (res?.data?.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error(res?.data?.message || '获取企业注册申请列表失败')
    }
  } catch (e) {
    console.error(e)
    message.error('获取企业注册申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchParams.companyName = ''
  searchParams.status = 0
  pagination.current = 1
  fetchData()
}

const handleTableChange = (p: any) => {
  if (p.current) {
    pagination.current = p.current
  }
  if (p.pageSize) {
    pagination.pageSize = p.pageSize
  }
  fetchData()
}

const detailModalVisible = ref(false)
const currentRecord = ref<API.CompanyRegistrationRequestVO | null>(null)

const openDetail = (record: API.CompanyRegistrationRequestVO) => {
  currentRecord.value = record
  rejectReason.value = ''
  rejectReasonVisible.value = false
  detailModalVisible.value = true
}

const approveLoading = ref(false)
const rejectReasonVisible = ref(false)
const rejectReason = ref('')

const handleApprove = async () => {
  if (!currentRecord.value) {
    message.error('记录不存在')
    return
  }
  try {
    approveLoading.value = true
    const res = await companyRegistrationController.approveCompanyRegistration({
      id: currentRecord.value.id,
      approved: true,
      rejectReason: undefined,
    })
    if (res?.data?.code === 0) {
      message.success('审批通过')
      detailModalVisible.value = false
      fetchData()
    } else {
      message.error(res?.data?.message || '操作失败')
    }
  } catch (e) {
    console.error(e)
    message.error('操作失败，请稍后重试')
  } finally {
    approveLoading.value = false
  }
}

const handleReject = () => {
  if (!currentRecord.value) {
    message.error('记录不存在')
    return
  }
  rejectReason.value = ''
  rejectReasonVisible.value = true
}

const submitReject = async () => {
  if (!rejectReason.value || !rejectReason.value.trim()) {
    message.error('请填写拒绝原因')
    return
  }
  if (!currentRecord.value) {
    message.error('记录不存在')
    return
  }
  try {
    approveLoading.value = true
    const res = await companyRegistrationController.approveCompanyRegistration({
      id: currentRecord.value.id,
      approved: false,
      rejectReason: rejectReason.value.trim(),
    })
    if (res?.data?.code === 0) {
      message.success('已拒绝申请')
      rejectReasonVisible.value = false
      detailModalVisible.value = false
      fetchData()
    } else {
      message.error(res?.data?.message || '操作失败')
    }
  } catch (e) {
    console.error(e)
    message.error('操作失败，请稍后重试')
  } finally {
    approveLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="company-registration-approval">
    <a-card class="search-card">
      <template #title>企业注册申请管理</template>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-input
            v-model:value="searchParams.companyName"
            placeholder="请输入企业名称搜索"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :md="12">
          <a-select
            v-model:value="searchParams.status"
            style="width: 180px"
            placeholder="申请状态"
          >
            <a-select-option :value="0">待处理</a-select-option>
            <a-select-option :value="1">已通过</a-select-option>
            <a-select-option :value="2">已拒绝</a-select-option>
          </a-select>
        </a-col>
      </a-row>
      <a-row style="margin-top: 16px">
        <a-col :span="24">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        row-key="id"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
        }"
        @change="handleTableChange"
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColorMap[record.status ?? 0]">
              {{ statusTextMap[record.status ?? 0] || '未知' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="detailModalVisible"
      title="企业注册申请详情"
      width="800px"
      :footer="null"
    >
      <div v-if="currentRecord">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="企业名称" :span="2">
            {{ currentRecord.companyName }}
          </a-descriptions-item>
          <a-descriptions-item label="企业地址" :span="2">
            {{ currentRecord.address }}
          </a-descriptions-item>
          <a-descriptions-item label="企业邮箱">
            {{ currentRecord.companyEmail }}
          </a-descriptions-item>
          <a-descriptions-item label="行业大类">
            {{ currentRecord.industryCategory }}
          </a-descriptions-item>
          <a-descriptions-item label="行业子类" :span="2">
            <span v-if="currentRecord.industries && currentRecord.industries.length">
              {{ currentRecord.industries.join('、') }}
            </span>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="管理人姓名">
            {{ currentRecord.adminName }}
          </a-descriptions-item>
          <a-descriptions-item label="管理人电话">
            {{ currentRecord.adminPhone }}
          </a-descriptions-item>
          <a-descriptions-item label="管理人邮箱">
            {{ currentRecord.adminEmail }}
          </a-descriptions-item>
          <a-descriptions-item label="身份证号">
            {{ currentRecord.adminIdNumber }}
          </a-descriptions-item>
          <a-descriptions-item label="账号名">
            {{ currentRecord.adminUsername || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="statusColorMap[currentRecord.status ?? 0]">
              {{ statusTextMap[currentRecord.status ?? 0] || '未知' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="申请时间">
            {{ formatDateTime(currentRecord.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="拒绝原因" v-if="currentRecord.status === 2" :span="2">
            {{ currentRecord.rejectReason || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="证明材料" :span="2">
            <div v-if="currentRecord.proofImages && currentRecord.proofImages.length" style="display: flex; flex-wrap: wrap; gap: 8px;">
              <a-image
                v-for="(img, index) in currentRecord.proofImages"
                :key="index"
                :src="img"
                :width="100"
              />
            </div>
            <span v-else>-</span>
          </a-descriptions-item>
        </a-descriptions>
        
        <!-- 审批操作按钮（仅待处理状态显示） -->
        <div v-if="currentRecord.status === 0" style="margin-top: 24px; padding-top: 24px; border-top: 1px solid #f0f0f0;">
          <div v-if="!rejectReasonVisible">
            <div style="text-align: right;">
              <a-space>
                <a-button @click="handleReject">拒绝</a-button>
                <a-button type="primary" :loading="approveLoading" @click="handleApprove">同意</a-button>
              </a-space>
            </div>
          </div>
          <div v-else>
            <a-form layout="vertical">
              <a-form-item label="拒绝原因" required>
                <a-textarea
                  v-model:value="rejectReason"
                  placeholder="请输入拒绝原因"
                  :rows="4"
                />
              </a-form-item>
              <a-form-item>
                <div style="text-align: right;">
                  <a-space>
                    <a-button @click="rejectReasonVisible = false">取消</a-button>
                    <a-button type="primary" danger :loading="approveLoading" @click="submitReject">确认拒绝</a-button>
                  </a-space>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.company-registration-approval {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}
</style>




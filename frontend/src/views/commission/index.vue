<template>
  <div class="commission-page">
    <PageHeader title="提成结算" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '人员管理'}]">
      <template #extra>
        <el-button type="success" @click="openCreateDialog">生成结算</el-button>
      </template>
    </PageHeader>

    <DrawerFilter
      :has-active-filters="hasActiveFilters"
      :active-count="activeFilterCount"
      @reset="resetQuery"
      @confirm="fetchData"
    >
      <template #filters>
        <el-form-item label="员工">
          <el-select v-model="query.employeeId" placeholder="选择员工" clearable style="width:160px">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期">
          <el-date-picker
            v-model="query.periodRange"
            type="daterange"
            range-separator="至"
            start-placeholder="起始"
            end-placeholder="截止"
            value-format="YYYY-MM-DD"
            style="width:260px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已支付" :value="2" />
          </el-select>
        </el-form-item>
      </template>
      <template #actions>
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </template>
    </DrawerFilter>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="employeeName" label="员工" min-width="100" />
      <el-table-column label="结算周期" min-width="200">
        <template #default="{ row }">{{ row.periodStart }} ~ {{ row.periodEnd }}</template>
      </el-table-column>
      <el-table-column prop="orderCount" label="订单数" min-width="80" />
      <el-table-column label="提成总额" min-width="120">
        <template #default="{ row }">¥{{ (row.totalCommission || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button v-if="row.status === 0" size="small" text type="primary" @click="handleConfirm(row.id)">确认</el-button>
          <el-button v-if="row.status === 1" size="small" text type="primary" @click="handlePay(row.id)">标记已付</el-button>
          <el-button v-if="row.status === 0" size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty><EmptyState description="暂无结算数据" /></template>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 生成结算弹窗 -->
    <el-dialog v-model="createDialog" title="生成提成结算" width="450px" @close="resetCreateForm">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="结算周期">
          <el-date-picker
            v-model="createForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="起始日期"
            end-placeholder="截止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">生成结算</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="结算详情" width="700px">
      <div v-if="detail" class="detail-section">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="员工">{{ detail.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="周期">{{ detail.periodStart }} ~ {{ detail.periodEnd }}</el-descriptions-item>
          <el-descriptions-item label="订单数">{{ detail.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="提成总额">¥{{ (detail.totalCommission || 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 16px">订单明细</h4>
        <el-table :data="detail.items || []" size="small" stripe>
          <el-table-column prop="orderNo" label="订单号" min-width="200" />
          <el-table-column prop="serviceNames" label="服务项目" min-width="150" />
          <el-table-column label="订单金额" min-width="100">
            <template #default="{ row }">¥{{ (row.orderAmount || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="提成" min-width="80">
            <template #default="{ row }">¥{{ (row.commissionAmount || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="orderTime" label="时间" min-width="170" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commissionSettlementApi } from '../../api/commission'
import { getEmployeeList } from '../../api/employee'
import PageHeader from '../../components/PageHeader.vue'
import EmptyState from '../../components/EmptyState.vue'
import DrawerFilter from '../../components/DrawerFilter.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const employees = ref([])

const query = reactive({
  page: 1,
  size: 10,
  employeeId: null,
  periodRange: null,
  status: null
})

const hasActiveFilters = computed(() => {
  return !!(query.employeeId || query.periodRange || query.status != null)
})

const activeFilterCount = computed(() => {
  let n = 0
  if (query.employeeId) n++
  if (query.periodRange) n++
  if (query.status != null) n++
  return n
})

function resetQuery() {
  query.page = 1
  query.employeeId = null
  query.periodRange = null
  query.status = null
  fetchData()
}

const createDialog = ref(false)
const creating = ref(false)
const createForm = reactive({ dateRange: null })

const detailDialog = ref(false)
const detail = ref(null)

function statusText(status) {
  const map = { 0: '草稿', 1: '已确认', 2: '已支付' }
  return map[status] || '未知'
}

function statusTag(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.employeeId) params.employeeId = query.employeeId
    if (query.status !== null && query.status !== '') params.status = query.status
    if (query.periodRange && query.periodRange.length === 2) {
      params.periodStart = query.periodRange[0]
      params.periodEnd = query.periodRange[1]
    }
    const res = await commissionSettlementApi.page(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch { /* handled by interceptor */ }
  loading.value = false
}

async function loadEmployees() {
  try {
    employees.value = (await getEmployeeList({ pageSize: 200, status: 1 })).list || []
  } catch { /* empty */ }
}

function openCreateDialog() {
  createForm.dateRange = null
  createDialog.value = true
}

function resetCreateForm() {
  createForm.dateRange = null
}

async function handleCreate() {
  if (!createForm.dateRange || createForm.dateRange.length !== 2) {
    ElMessage.warning('请选择结算周期')
    return
  }
  creating.value = true
  try {
    await commissionSettlementApi.create({
      periodStart: createForm.dateRange[0],
      periodEnd: createForm.dateRange[1]
    })
    ElMessage.success('结算生成成功')
    createDialog.value = false
    fetchData()
  } catch { ElMessage.error('生成结算失败，请重试') }
  creating.value = false
}

async function handleConfirm(id) {
  try {
    await ElMessageBox.confirm('确认该结算单？确认后不可修改。', '提示', { type: 'warning' })
    await commissionSettlementApi.confirm(id)
    ElMessage.success('已确认')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('确认失败，请重试')
  }
}

async function handlePay(id) {
  try {
    await ElMessageBox.confirm('标记该结算单为已支付？', '提示', { type: 'warning' })
    await commissionSettlementApi.pay(id)
    ElMessage.success('已标记为已支付')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('确认失败，请重试')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该结算单？', '提示', { type: 'error' })
    await commissionSettlementApi.remove(id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('确认失败，请重试')
  }
}

async function openDetail(id) {
  try {
    detail.value = await commissionSettlementApi.getById(id)
    detailDialog.value = true
  } catch { ElMessage.error('生成结算失败，请重试') }
}

onMounted(() => {
  loadEmployees()
  fetchData()
})
</script>

<style scoped>
.commission-page {
  overflow-x: hidden;
  padding: 0;
}


.detail-section h4 {
  margin: 16px 0 8px;
  font-size: 14px;
  color: var(--text-primary);
}
</style>

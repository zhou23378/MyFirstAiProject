<template>
  <div class="orders-page">
    <PageHeader title="消费订单" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '店务管理'}]" />

    <!-- 筛选栏 -->
    <DrawerFilter
      :has-active-filters="hasActiveFilters"
      :active-count="activeFilterCount"
      @reset="resetQuery"
      @confirm="fetchData"
    >
      <template #toolbar>
        <el-input
          v-model="query.memberName"
          placeholder="搜索会员姓名…"
          clearable
          @keyup.enter="fetchData"
          @clear="fetchData"
        />
      </template>

      <template #filters>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option :value="1" label="正常" />
            <el-option :value="2" label="已退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="query.payMethod" placeholder="全部" clearable>
            <el-option :value="1" label="现金" />
            <el-option :value="2" label="余额" />
            <el-option :value="3" label="微信" />
            <el-option :value="4" label="支付宝" />
            <el-option :value="5" label="银行卡" />
            <el-option :value="6" label="储值卡" />
            <el-option :value="7" label="团购券" />
            <el-option :value="8" label="混合" />
          </el-select>
        </el-form-item>
      </template>

      <template #actions>
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </template>
    </DrawerFilter>

    <!-- 数据列表 -->
    <template v-if="loading">
      <SkeletonTable :rows="5" :cols="8" />
    </template>
    <ResponsiveDataList
      v-else
      :data="tableData"
      :columns="columns"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      primary-field="memberName"
      empty-text="暂无消费订单"
      actions-width="140"
      @page-change="onPageChange"
    >
      <template #items="{ row }">
        <span class="items-text">{{ row.items || '-' }}</span>
      </template>
      <template #totalAmount="{ row }">
        ¥{{ row.totalAmount?.toFixed(2) }}
      </template>
      <template #couponDiscount="{ row }">
        <span v-if="row.couponDiscount > 0" class="discount-text">-¥{{ row.couponDiscount?.toFixed(2) }}</span>
        <span v-else>-</span>
      </template>
      <template #payMethod="{ row }">
        {{ payMethodMap[row.payMethod] || '未知' }}
      </template>
      <template #status="{ row }">
        <el-tag :type="row.status === 2 ? 'danger' : 'success'" size="small">
          {{ row.status === 2 ? '已退款' : '正常' }}
        </el-tag>
      </template>
      <template #actions="{ row }">
        <div class="action-cell">
          <el-button type="primary" size="small" plain @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            type="danger"
            size="small"
            plain
            @click="handleRefund(row)"
          >退款</el-button>
        </div>
      </template>
    </ResponsiveDataList>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="订单详情"
      :size="drawerWidth"
      :close-on-click-modal="true"
      @closed="detailItems = []"
    >
      <template v-if="detailOrder">
        <div class="drawer-section">
          <el-tag :type="detailOrder.status === 2 ? 'danger' : 'success'" size="large">
            {{ detailOrder.status === 2 ? '已退款' : '正常' }}
          </el-tag>
        </div>

        <div class="drawer-section info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value">#{{ detailOrder.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">会员</span>
            <span class="info-value">{{ detailOrder.memberName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单金额</span>
            <span class="info-value">¥{{ detailOrder.totalAmount?.toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付方式</span>
            <span class="info-value">{{ payMethodMap[detailOrder.payMethod] || '未知' }}</span>
          </div>
          <div class="info-item" v-if="detailOrder.payAmount > 0">
            <span class="info-label">支付金额</span>
            <span class="info-value">¥{{ detailOrder.payAmount?.toFixed(2) }}</span>
          </div>
          <div class="info-item" v-if="detailOrder.balanceUsed > 0">
            <span class="info-label">使用余额</span>
            <span class="info-value">¥{{ detailOrder.balanceUsed?.toFixed(2) }}</span>
          </div>
          <div class="info-item" v-if="detailOrder.couponDiscount > 0">
            <span class="info-label">优惠券优惠</span>
            <span class="info-value discount-text">-¥{{ detailOrder.couponDiscount?.toFixed(2) }}</span>
          </div>
          <div class="info-item" v-if="detailOrder.pointsEarned != null">
            <span class="info-label">获得积分</span>
            <span class="info-value">{{ detailOrder.pointsEarned }}</span>
          </div>
          <div class="info-item" v-if="detailOrder.employeeName">
            <span class="info-label">技师</span>
            <span class="info-value">{{ detailOrder.employeeName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">时间</span>
            <span class="info-value">{{ detailOrder.createTime }}</span>
          </div>
          <div class="info-item full-width" v-if="detailOrder.payRemark">
            <span class="info-label">备注</span>
            <span class="info-value">{{ detailOrder.payRemark }}</span>
          </div>
        </div>

        <!-- 服务项目明细 -->
        <div class="drawer-section" v-if="detailItems.length > 0">
          <h4 class="section-title">服务项目</h4>
          <el-table :data="detailItems" size="small" border>
            <el-table-column prop="itemName" label="项目名称" />
            <el-table-column prop="itemPrice" label="单价" width="100">
              <template #default="{ row }">¥{{ row.itemPrice?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="70" />
          </el-table>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { consumptionApi } from '@/api/consumption'
import { payMethodMap } from '@/constants/payMethod'
import PageHeader from '@/components/PageHeader.vue'
import DrawerFilter from '@/components/DrawerFilter.vue'
import ResponsiveDataList from '@/components/ResponsiveDataList.vue'
import SkeletonTable from '@/components/SkeletonTable.vue'

const columns = [
  { prop: 'id', label: '订单号', width: '80', hideOnCard: true },
  { prop: 'memberName', label: '会员', width: '100', cardPrimary: true },
  { prop: 'items', label: '服务项目', minWidth: '160', hideOnCard: true },
  { prop: 'totalAmount', label: '金额', width: '100' },
  { prop: 'couponDiscount', label: '优惠', width: '80', hideOnCard: true },
  { prop: 'payMethod', label: '支付方式', width: '90' },
  { prop: 'employeeName', label: '技师', width: '80' },
  { prop: 'status', label: '状态', width: '80' },
  { prop: 'createTime', label: '时间', minWidth: '160' },
]

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  dateRange: null,
  memberName: '',
  status: null,
  payMethod: null
})

const detailVisible = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])

const drawerWidth = computed(() => {
  return typeof window !== 'undefined' && window.innerWidth < 768 ? '100%' : '480px'
})

const hasActiveFilters = computed(() => {
  return !!(query.dateRange || query.status != null || query.payMethod != null)
})

const activeFilterCount = computed(() => {
  let n = 0
  if (query.dateRange) n++
  if (query.status != null) n++
  if (query.payMethod != null) n++
  return n
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      pageSize: query.pageSize,
      status: query.status,
      payMethod: query.payMethod,
      memberName: query.memberName || undefined
    }
    if (query.dateRange) {
      params.startDate = query.dateRange[0]
      params.endDate = query.dateRange[1]
    }
    const res = await consumptionApi.page(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.page = 1
  query.status = null
  query.payMethod = null
  query.dateRange = null
  query.memberName = ''
  fetchData()
}

function onPageChange(page) {
  query.page = page
  fetchData()
}

async function openDetail(row) {
  detailOrder.value = row
  detailVisible.value = true
  try {
    const items = await consumptionApi.getOrderItems(row.id)
    detailItems.value = items || []
  } catch {
    detailItems.value = []
  }
}

function handleRefund(row) {
  ElMessageBox.confirm('确定对该订单进行退款吗？将退还余额和积分。', '退款确认', { type: 'warning' })
    .then(async () => {
      await consumptionApi.refund(row.id)
      ElMessage.success('退款成功')
      fetchData()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.orders-page {
  max-width: 1200px;
  margin: 0 auto;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.items-text {
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 200px;
}

.discount-text {
  color: var(--danger-color);
}

.drawer-section {
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.info-value {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  font-weight: 500;
}

.section-title {
  margin: 0 0 8px;
  font-size: var(--font-size-md);
  color: var(--text-primary);
}

@media (max-width: 767px) {
  .orders-page {
    padding: 0;
  }

  .action-cell {
    flex-wrap: wrap;
    gap: 4px;
  }

  .action-cell .el-button {
    font-size: var(--font-size-mobile-sm);
    padding: 4px 8px;
  }
}
</style>

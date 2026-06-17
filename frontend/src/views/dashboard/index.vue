<template>
  <div class="dashboard-page">
    <PageHeader title="仪表盘" />

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="(s, i) in statsCards" :key="i">
        <el-card shadow="never" class="stat-card" @click="$router.push(s.path)">
          <div class="stat-icon" :style="{ background: s.color }">
            <el-icon :size="28"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="quick-section-card">
      <template #header>快捷入口</template>
      <el-row :gutter="16">
        <el-col :xs="6" :sm="3" v-for="(q, i) in quickActions" :key="i">
          <div class="quick-card" @click="$router.push(q.path)">
            <div class="quick-icon" :style="{ background: q.color }">
              <el-icon :size="22"><component :is="q.icon" /></el-icon>
            </div>
            <span class="quick-label">{{ q.label }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 营收图表 + 快捷操作 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="16" :xs="24">
        <el-card shadow="never">
          <template #header>近30天营收趋势</template>
          <BarChart v-if="revenueData.length > 0" :option="revenueChartOption" style="height: 300px" />
          <EmptyState v-else description="暂无营收数据" />
        </el-card>
      </el-col>
      <el-col :span="8" :xs="24">
        <el-card shadow="never">
          <template #header>支付方式分布</template>
          <BarChart v-if="payMethods.length > 0" :option="pieChartOption" style="height: 300px" />
          <EmptyState v-else description="暂无支付数据" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近订单 -->
    <el-card shadow="never" class="orders-card">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>最近订单</span>
          <el-button text type="primary" @click="$router.push('/orders')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <template v-if="ordersLoading">
        <SkeletonTable :rows="5" :cols="6" />
      </template>
      <template v-else>
        <el-table :data="recentOrders" stripe>
          <el-table-column prop="id" label="订单号" min-width="80" />
          <el-table-column prop="memberName" label="会员" min-width="90" />
          <el-table-column prop="items" label="服务项目" min-width="140" show-overflow-tooltip />
          <el-table-column prop="employeeName" label="技师" min-width="80" />
          <el-table-column label="金额" min-width="90">
            <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="支付方式" min-width="80">
            <template #default="{ row }">
              {{ payMethodMap[row.payMethod] || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 2 ? 'danger' : 'success'" size="small">
                {{ row.status === 2 ? '已退款' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="160" />
          <template #empty>
            <EmptyState description="暂无订单记录" />
          </template>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, defineAsyncComponent } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { getDashboardStats, getRecentOrders, getQuickActions, getStatCards } from '@/api/dashboard'
import { defaultQuickActions, routeMetaMap } from '@/config/dashboardQuickActions'
import { defaultStatCards, statMetaMap } from '@/config/dashboardStatCards'
import SkeletonTable from '@/components/SkeletonTable.vue'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { payColorMap, payMethodMap } from '@/constants/payMethod'
const BarChart = defineAsyncComponent(() => import('@/components/BarChart.vue'))

const statsCards = ref(defaultStatCards.map(item => ({
  ...item,
  value: '0',
})))

const keyValueMap = {
  memberCount: (stats, card) => { card.value = String(stats.memberCount || 0) },
  monthRevenue: (stats, card) => { card.value = '¥' + (stats.monthRevenue || 0).toLocaleString() },
  todayOrder: (stats, card) => { card.value = String(stats.todayOrder || 0) },
  todayMember: (stats, card) => { card.value = String(stats.todayMember || 0) },
  todayRevenue: (stats, card) => { card.value = '¥' + (stats.todayRevenue || 0).toLocaleString() },
  employeeCount: (stats, card) => { card.value = String(stats.employeeCount || 0) },
  serviceCount: (stats, card) => { card.value = String(stats.serviceCount || 0) },
}

const quickActions = ref(defaultQuickActions.map(item => ({ ...item })))

const revenueData = ref([])
const revenueChartOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>营收: ¥${p[0].value}` },
  grid: { left: 60, right: 20, top: 10, bottom: 30 },
  xAxis: { type: 'category', data: revenueData.value.map(d => d.date) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    type: 'bar', data: revenueData.value.map(d => d.value),
    itemStyle: { borderRadius: [4, 4, 0, 0], color: 'var(--primary-color)' },
  }],
}))

const payMethods = ref([])
const pieChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['40%', '50%'],
    data: payMethods.value.map(p => ({
      name: p.name, value: p.percentage,
      itemStyle: { color: payColorMap[p.payMethod] || '#666' },
    })),
    label: { formatter: '{b}\n{c}%' },
  }],
}))

const recentOrders = ref([])
const ordersLoading = ref(false)

async function loadData() {
  try {
    const stats = await getDashboardStats()
    if (stats) {
      statsCards.value.forEach(card => {
        const setter = keyValueMap[card.key]
        if (setter) setter(stats, card)
      })
      if (stats.revenueTrend) {
        revenueData.value = stats.revenueTrend
      }
      if (stats.payMethods) {
        payMethods.value = stats.payMethods
      }
    }
  } catch { /* 加载失败不影响页面展示 */ }

  ordersLoading.value = true
  try {
    recentOrders.value = await getRecentOrders()
  } catch { /* 加载失败不影响页面展示 */ }
  finally { ordersLoading.value = false }
}

async function loadQuickActions() {
  try {
    const configs = await getQuickActions()
    if (configs && configs.length > 0) {
      const map = Object.fromEntries(configs.map(c => [c.slot, c]))
      quickActions.value.forEach((item, i) => {
        if (map[i]) {
          item.label = map[i].label
          item.path = map[i].path
          const meta = routeMetaMap[map[i].path]
          if (meta) {
            item.icon = meta.icon
            item.color = meta.color
          }
        }
      })
    }
  } catch { /* 加载失败使用默认值 */ }
}

async function loadStatCards() {
  try {
    const configs = await getStatCards()
    if (configs && configs.length > 0) {
      const map = Object.fromEntries(configs.map(c => [c.slot, c]))
      statsCards.value.forEach((item, i) => {
        if (map[i]) {
          item.key = map[i].statKey
          item.label = map[i].label
          item.path = map[i].path
          const meta = statMetaMap[map[i].statKey]
          if (meta) {
            item.icon = meta.icon
            item.color = meta.color
          }
        }
      })
    }
  } catch { /* 加载失败使用默认值 */ }
}

let pollTimer = null

onMounted(() => {
  loadData()
  loadQuickActions()
  loadStatCards()
  pollTimer = setInterval(loadData, 30000)
})

onBeforeUnmount(() => {
  clearInterval(pollTimer)
})
</script>

<style scoped>
.dashboard-page { overflow-x: hidden; max-width: 1400px; margin: 0 auto; }

.stats-row { margin-bottom: 16px; }

.stat-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 56px; height: 56px;
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  color: var(--text-light);
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); font-variant-numeric: tabular-nums; }
.stat-label { font-size: 13px; color: var(--text-muted); }

.quick-section-card { margin-bottom: 16px; }
.quick-section-card :deep(.el-card__body) { padding-top: 8px; }

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: var(--radius-md);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-icon {
  width: 44px; height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.quick-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.content-row { margin-bottom: 16px; }

.orders-card { margin-bottom: 20px; }

@media (max-width: 767px) {
  .dashboard-page { padding: 0; }
  .stat-value { font-size: 20px; }
  .stat-card :deep(.el-card__body) { padding: 12px; gap: 10px; }
  .quick-card { padding: 12px 4px; }
  .quick-icon { width: 36px; height: 36px; }
  .quick-label { font-size: 11px; }
}
</style>

<template>
  <div class="report-page">
    <PageHeader title="营业报表" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '商品与财务'}]" />
    <!-- ========== 统计摘要卡片 ========== -->
    <div class="summary-grid">
      <div class="summary-card" v-for="(s, i) in summaryCards" :key="i" :style="{ animationDelay: `${i * 0.08}s` }">
        <div class="summary-icon">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-label">{{ s.label }}</span>
          <span class="summary-value">{{ s.value }}</span>
        </div>
      </div>
    </div>

    <!-- ========== 营收趋势（日+月） ========== -->
    <el-card class="report-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>营收趋势</span>
          <div class="header-controls">
            <el-segmented v-model="trendMode" :options="trendModes" size="small" />
          </div>
        </div>
      </template>
      <template v-if="trendMode === 'daily'">
        <div class="trend-toolbar">
          <el-date-picker v-model="dailyRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadDaily" size="small" />
          <el-radio-group v-model="dailyView" size="small">
            <el-radio-button value="chart">图表</el-radio-button>
            <el-radio-button value="table">表格</el-radio-button>
          </el-radio-group>
        </div>
        <template v-if="loadingDaily">
          <SkeletonTable :rows="4" :cols="4" />
        </template>
        <template v-else-if="dailyData.length === 0">
          <EmptyState description="暂无日报数据" />
        </template>
        <template v-else>
          <BarChart v-if="dailyView === 'chart'" :option="dailyChartOption" />
          <el-table v-else :data="dailyData" stripe>
            <el-table-column prop="period" label="日期" />
            <el-table-column prop="orderCount" label="订单数" />
            <el-table-column label="营收"><template #default="{ row }">¥{{ row.revenue }}</template></el-table-column>
            <el-table-column prop="points" label="赠送积分" />
          </el-table>
        </template>
      </template>
      <template v-else>
        <div class="trend-toolbar">
          <el-select v-model="year" @change="loadMonthly" style="width:120px" size="small">
            <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </div>
        <template v-if="loadingMonthly">
          <SkeletonTable :rows="4" :cols="4" />
        </template>
        <template v-else-if="monthlyData.length === 0">
          <EmptyState description="暂无月报数据" />
        </template>
        <template v-else>
          <BarChart v-if="monthlyData.length > 0" :option="monthlyChartOption" />
        </template>
      </template>
    </el-card>

    <!-- ========== 服务排行 + 收银日报 双栏 ========== -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="report-card" shadow="never">
          <template #header>
            <div class="card-header"><span>服务项目排行</span></div>
          </template>
          <template v-if="loadingRank">
            <SkeletonTable :rows="4" :cols="3" />
          </template>
          <template v-else-if="serviceRank.length === 0">
            <EmptyState description="暂无排行数据" />
          </template>
          <template v-else>
            <div class="rank-list">
              <div class="rank-item" v-for="(item, idx) in serviceRank" :key="idx">
                <span class="rank-badge" :class="idx < 3 ? `rank-${idx + 1}` : ''">{{ idx + 1 }}</span>
                <div class="rank-info">
                  <span class="rank-name">{{ item.itemName }}</span>
                  <div class="rank-bar-wrap">
                    <div class="rank-bar" :style="{ width: serviceMax > 0 ? (item.count / serviceMax * 100) + '%' : '0%', animationDelay: `${idx * 0.1}s` }" />
                  </div>
                </div>
                <div class="rank-meta">
                  <span class="rank-count">{{ item.count }}次</span>
                  <span class="rank-revenue">¥{{ item.revenue }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="report-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>收银日报</span>
              <el-date-picker v-model="cashierDate" type="date" placeholder="选择日期" @change="loadCashierDaily" size="small" />
            </div>
          </template>
          <template v-if="loadingCashier">
            <SkeletonTable :rows="4" :cols="3" />
          </template>
          <template v-else-if="cashierDailyData.length === 0">
            <EmptyState description="暂无收银数据" />
          </template>
          <template v-else>
            <div class="cashier-list">
              <div class="cashier-item" v-for="(item, idx) in cashierDailyData" :key="idx">
                <div class="cashier-dot" :style="{ background: payColorMap[payMethodCode(item.payMethod)] || 'var(--text-muted)' }" />
                <span class="cashier-name">{{ item.payMethod }}</span>
                <div class="cashier-bar-wrap">
                  <div class="cashier-bar" :style="{ width: cashierMax > 0 ? (item.totalAmount / cashierMax * 100) + '%' : '0%', background: payColorMap[payMethodCode(item.payMethod)] || 'var(--text-muted)' }" />
                </div>
                <span class="cashier-amount">¥{{ item.totalAmount?.toFixed(2) }}</span>
                <span class="cashier-orders">{{ item.orderCount }}单</span>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- ========== 会员趋势 + 优惠券使用率 ========== -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="report-card" shadow="never">
          <template #header>
            <div class="card-header"><span>会员增长趋势（近12月）</span></div>
          </template>
          <template v-if="loadingMemberTrend">
            <SkeletonTable :rows="4" :cols="2" />
          </template>
          <template v-else-if="memberTrendData.length === 0">
            <EmptyState description="暂无新增数据" />
          </template>
          <template v-else>
            <BarChart :option="memberTrendChartOption" />
          </template>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="report-card" shadow="never">
          <template #header>
            <div class="card-header"><span>优惠券使用率</span></div>
          </template>
          <template v-if="loadingCouponUsage">
            <SkeletonTable :rows="4" :cols="4" />
          </template>
          <template v-else-if="couponUsageData.length === 0">
            <EmptyState description="暂无优惠券数据" />
          </template>
          <template v-else>
            <div class="coupon-usage-list">
              <div class="coupon-usage-item" v-for="(c, idx) in couponUsageData" :key="idx">
                <span class="coupon-usage-name">{{ c.templateName }}</span>
                <div class="coupon-usage-bar-wrap">
                  <div class="coupon-usage-bar" :style="{ width: c.usageRate + '%', animationDelay: `${idx * 0.08}s` }" />
                </div>
                <span class="coupon-usage-pct">{{ c.usageRate }}%</span>
                <span class="coupon-usage-nums">{{ c.usedQty }}/{{ c.issuedQty }}</span>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- ========== 员工业绩 ========== -->
    <el-card class="report-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>员工业绩</span>
          <el-date-picker v-model="perfRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadEmployeePerformance" size="small" />
        </div>
      </template>
      <template v-if="loadingPerf">
        <SkeletonTable :rows="4" :cols="4" />
      </template>
      <template v-else-if="employeePerfData.length === 0">
        <EmptyState description="暂无业绩数据" />
      </template>
      <template v-else>
        <div class="perf-list">
          <div class="perf-item" v-for="(emp, idx) in employeePerfData" :key="idx">
            <div class="perf-header">
              <span class="perf-name">{{ emp.name }}</span>
              <span class="perf-revenue">¥{{ emp.totalRevenue?.toFixed(2) }}</span>
            </div>
            <div class="perf-stats">
              <div class="perf-stat">
                <span class="perf-stat-label">订单</span>
                <span class="perf-stat-val">{{ emp.orderCount }}</span>
              </div>
              <div class="perf-stat">
                <span class="perf-stat-label">服务次数</span>
                <span class="perf-stat-val">{{ emp.serviceCount }}</span>
              </div>
              <div class="perf-stat">
                <span class="perf-stat-label">提成</span>
                <span class="perf-stat-val commission">¥{{ (emp.totalCommission || 0).toFixed(2) }}</span>
              </div>
            </div>
            <div class="perf-bar-wrap">
              <div class="perf-bar" :style="{ width: perfMax > 0 ? (emp.totalRevenue / perfMax * 100) + '%' : '0%', animationDelay: `${idx * 0.1}s` }" />
            </div>
          </div>
        </div>
      </template>
    </el-card>

    <!-- ========== 会员消费排行 ========== -->
    <el-card class="report-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>会员消费排行</span>
          <el-select v-model="memberSpendingLimit" @change="loadMemberSpending" style="width:100px" size="small">
            <el-option :value="10" label="Top 10" />
            <el-option :value="20" label="Top 20" />
          </el-select>
        </div>
      </template>
      <template v-if="loadingMemberSpending">
        <SkeletonTable :rows="5" :cols="4" />
      </template>
      <template v-else-if="memberSpendingData.length === 0">
        <EmptyState description="暂无消费数据" />
      </template>
      <template v-else>
        <div class="member-rank-list">
          <div class="member-rank-item" v-for="(m, idx) in memberSpendingData" :key="idx">
            <span class="member-rank-badge" :class="idx < 3 ? `top-${idx + 1}` : ''">{{ idx + 1 }}</span>
            <div class="member-rank-info">
              <span class="member-rank-name">{{ m.name }}</span>
              <span class="member-rank-phone">{{ m.phone }}</span>
            </div>
            <div class="member-rank-stats">
              <span class="member-rank-orders">{{ m.orderCount }}次</span>
              <span class="member-rank-spent">¥{{ m.totalSpent?.toFixed(2) }}</span>
            </div>
            <div class="member-rank-bar" :style="{ width: memberMax > 0 ? (m.totalSpent / memberMax * 60) + '%' : '0%' }" />
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineAsyncComponent } from "vue";
import {
  getDailyReport,
  getMonthlyReport,
  getServiceRank,
  getCashierDaily,
  getEmployeePerformance,
  getMemberSpending,
  getMemberTrend,
  getCouponUsage,
} from "@/api/report";
import SkeletonTable from "@/components/SkeletonTable.vue";
import EmptyState from "@/components/EmptyState.vue";
import PageHeader from "@/components/PageHeader.vue";
import { Coin, ShoppingCart, Star, UserFilled } from "@element-plus/icons-vue";
import { payColorMap, payMethodMap } from "@/constants/payMethod";
const BarChart = defineAsyncComponent(() => import("@/components/BarChart.vue"));

const loadingDaily = ref(false);
const loadingMonthly = ref(false);
const loadingRank = ref(false);
const loadingCashier = ref(false);
const loadingPerf = ref(false);
const loadingMemberSpending = ref(false);

const dailyRange = ref(null);
const dailyData = ref([]);
const dailyView = ref("chart");
const trendMode = ref("daily");
const trendModes = [
  { label: "日报", value: "daily" },
  { label: "月报", value: "monthly" },
];
const year = ref(new Date().getFullYear());
const years = ref([]);
const monthlyData = ref([]);
const serviceRank = ref([]);
const cashierDate = ref(null);
const cashierDailyData = ref([]);
const perfRange = ref(null);
const employeePerfData = ref([]);
const memberSpendingLimit = ref(10);
const memberSpendingData = ref([]);

const loadingMemberTrend = ref(false);
const memberTrendData = ref([]);
const loadingCouponUsage = ref(false);
const couponUsageData = ref([]);

const memberTrendChartOption = computed(() => ({
  tooltip: { trigger: "axis", formatter: p => `${p[0].name}<br/>新增: ${p[0].value}人` },
  grid: { left: 50, right: 20, top: 10, bottom: 30 },
  xAxis: { type: "category", data: memberTrendData.value.map(d => d.period) },
  yAxis: { type: "value", minInterval: 1 },
  series: [{
    type: "bar", data: memberTrendData.value.map(d => d.newMembers),
    itemStyle: { borderRadius: [4, 4, 0, 0], color: "var(--primary-color)" },
  }],
}));

function payMethodCode(name) {
  const entry = Object.entries(payMethodMap).find(([, v]) => v === name);
  return entry ? Number(entry[0]) : 8;
}

const serviceMax = computed(() => Math.max(...serviceRank.value.map(d => d.count || 0), 1));
const cashierMax = computed(() => Math.max(...cashierDailyData.value.map(d => d.totalAmount || 0), 1));
const perfMax = computed(() => Math.max(...employeePerfData.value.map(d => d.totalRevenue || 0), 1));
const memberMax = computed(() => Math.max(...memberSpendingData.value.map(d => d.totalSpent || 0), 1));

const summaryCards = computed(() => {
  const totalOrders = dailyData.value.reduce((s, d) => s + (d.orderCount || 0), 0);
  const totalRevenue = dailyData.value.reduce((s, d) => s + (d.revenue || 0), 0);
  const totalPoints = dailyData.value.reduce((s, d) => s + (d.points || 0), 0);
  const avgRevenue = dailyData.value.length > 0 ? (totalRevenue / dailyData.value.length).toFixed(0) : "0";
  return [
    { label: "总订单数", value: totalOrders, icon: ShoppingCart },
    { label: "总营收", value: `¥${totalRevenue}`, icon: Coin },
    { label: "总积分", value: totalPoints, icon: Star },
    { label: "日均营收", value: `¥${avgRevenue}`, icon: UserFilled },
  ];
});

const dailyChartOption = computed(() => ({
  tooltip: { trigger: "axis", formatter: p => `${p[0].name}<br/>营收: ¥${p[0].value}<br/>订单: ${dailyData.value[p[0].dataIndex]?.orderCount || 0}单` },
  grid: { left: 55, right: 20, top: 10, bottom: 30 },
  xAxis: { type: "category", data: dailyData.value.map(d => d.period) },
  yAxis: { type: "value", minInterval: 1 },
  series: [{
    type: "bar", data: dailyData.value.map(d => d.revenue),
    itemStyle: { borderRadius: [4, 4, 0, 0], color: "var(--primary-color)" },
  }],
}));

const monthlyChartOption = computed(() => ({
  tooltip: { trigger: "axis", formatter: p => `${p[0].name}月<br/>营收: ¥${p[0].value}<br/>订单: ${monthlyData.value[p[0].dataIndex]?.orderCount || 0}单` },
  grid: { left: 55, right: 20, top: 10, bottom: 30 },
  xAxis: { type: "category", data: monthlyData.value.map(d => (d.period || "").split("-").pop() + "月") },
  yAxis: { type: "value", minInterval: 1 },
  series: [{
    type: "bar", data: monthlyData.value.map(d => d.revenue),
    itemStyle: { borderRadius: [4, 4, 0, 0], color: "var(--success-color)" },
  }],
}));

const loadDaily = async () => {
  loadingDaily.value = true;
  try {
    const start = dailyRange.value?.[0]?.toISOString().split("T")[0];
    const end = dailyRange.value?.[1]?.toISOString().split("T")[0];
    dailyData.value = await getDailyReport(start, end);
  } finally { loadingDaily.value = false; }
};

const loadMonthly = async () => {
  loadingMonthly.value = true;
  try {
    monthlyData.value = await getMonthlyReport(year.value);
  } finally { loadingMonthly.value = false; }
};

const loadServiceRank = async () => {
  loadingRank.value = true;
  try { serviceRank.value = await getServiceRank(); } finally { loadingRank.value = false; }
};

const loadCashierDaily = async () => {
  loadingCashier.value = true;
  try {
    const date = cashierDate.value ? cashierDate.value.toISOString().split("T")[0] : null;
    cashierDailyData.value = await getCashierDaily(date);
  } finally { loadingCashier.value = false; }
};

const loadEmployeePerformance = async () => {
  loadingPerf.value = true;
  try {
    const start = perfRange.value?.[0]?.toISOString().split("T")[0];
    const end = perfRange.value?.[1]?.toISOString().split("T")[0];
    employeePerfData.value = await getEmployeePerformance(start, end);
  } finally { loadingPerf.value = false; }
};

const loadMemberSpending = async () => {
  loadingMemberSpending.value = true;
  try {
    memberSpendingData.value = await getMemberSpending(null, null, memberSpendingLimit.value);
  } finally { loadingMemberSpending.value = false; }
};

const loadMemberTrend = async () => {
  loadingMemberTrend.value = true;
  try { memberTrendData.value = await getMemberTrend() || []; } finally { loadingMemberTrend.value = false; }
};

const loadCouponUsage = async () => {
  loadingCouponUsage.value = true;
  try { couponUsageData.value = await getCouponUsage() || []; } finally { loadingCouponUsage.value = false; }
};

onMounted(() => {
  const y = new Date().getFullYear();
  years.value = [y - 2, y - 1, y, y + 1];
  loadDaily();
  loadMonthly();
  loadServiceRank();
  loadCashierDaily();
  loadEmployeePerformance();
  loadMemberSpending();
  loadMemberTrend();
  loadCouponUsage();
});
</script>

<style scoped>
.report-page {
  overflow-x: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

/* ========== 摘要卡片 ========== */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-card);
  transition: all var(--transition-normal);
  animation: fadeInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.summary-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
}

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  flex-shrink: 0;
}

.summary-icon { background: var(--primary-color); }

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-label {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

/* ========== 卡片通用 ========== */
.report-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* ========== 服务排行 ========== */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.06);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.rank-badge.rank-1 { background: var(--rank-gold); color: var(--text-light); }
.rank-badge.rank-2 { background: var(--rank-silver); color: var(--text-light); }
.rank-badge.rank-3 { background: var(--rank-bronze); color: var(--text-light); }

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.rank-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.rank-bar-wrap {
  height: 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  overflow: hidden;
}

.rank-bar {
  height: 100%;
  background: var(--primary-gradient);
  border-radius: 3px;
  transition: width var(--transition-slow);
  animation: barGrow 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.rank-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.rank-count {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.rank-revenue {
  font-size: 12px;
  color: var(--primary-color);
  font-weight: 500;
}

/* ========== 收银日报 ========== */
.cashier-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cashier-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cashier-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cashier-name {
  width: 48px;
  font-size: 13px;
  color: var(--text-primary);
  flex-shrink: 0;
}

.cashier-bar-wrap {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  overflow: hidden;
}

.cashier-bar {
  height: 100%;
  border-radius: 4px;
  transition: width var(--transition-slow);
  animation: barGrow 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.cashier-amount {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  flex-shrink: 0;
  min-width: 72px;
  text-align: right;
}

.cashier-orders {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}

/* ========== 员工业绩 ========== */
.perf-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.perf-item {
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.015);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.perf-item:hover {
  background: rgba(108, 92, 231, 0.04);
}

.perf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.perf-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.perf-revenue {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color);
}

.perf-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 10px;
}

.perf-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.perf-stat-label {
  font-size: 11px;
  color: var(--text-muted);
}

.perf-stat-val {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.perf-stat-val.commission {
  color: var(--success-color);
}

.perf-bar-wrap {
  height: 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  overflow: hidden;
}

.perf-bar {
  height: 100%;
  background: var(--primary-gradient);
  border-radius: 3px;
  transition: width var(--transition-slow);
  animation: barGrow 0.7s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ========== 优惠券使用率 ========== */
.coupon-usage-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-usage-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coupon-usage-name {
  width: 80px;
  font-size: 13px;
  color: var(--text-primary);
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-usage-bar-wrap {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  overflow: hidden;
}

.coupon-usage-bar {
  height: 100%;
  background: var(--primary-gradient);
  border-radius: 4px;
  transition: width var(--transition-slow);
  animation: barGrow 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.coupon-usage-pct {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  min-width: 42px;
  text-align: right;
}

.coupon-usage-nums {
  font-size: 11px;
  color: var(--text-muted);
  min-width: 48px;
  text-align: right;
}

/* ========== 会员消费排行 ========== */
.member-rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  background: rgba(0, 0, 0, 0.015);
  overflow: hidden;
}

.member-rank-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: rgba(108, 92, 231, 0.05);
  border-radius: var(--radius-sm);
  transition: width var(--transition-slow);
  z-index: 0;
}

.member-rank-badge {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.06);
  color: var(--text-secondary);
  flex-shrink: 0;
  z-index: 1;
}

.member-rank-badge.top-1 { background: var(--rank-gold); color: var(--text-light); }
.member-rank-badge.top-2 { background: var(--rank-silver); color: var(--text-light); }
.member-rank-badge.top-3 { background: var(--rank-bronze); color: var(--text-light); }

.member-rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  z-index: 1;
}

.member-rank-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.member-rank-phone {
  font-size: 12px;
  color: var(--text-muted);
}

.member-rank-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1;
}

.member-rank-orders {
  font-size: 12px;
  color: var(--text-secondary);
}

.member-rank-spent {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

/* ========== 动画 ========== */
@keyframes barGrow {
  from { width: 0 !important; height: 0 !important; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .report-page {
    padding: 0;
  }

  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .summary-card {
    padding: 14px;
    gap: 10px;
  }

  .summary-value {
    font-size: 18px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .trend-toolbar {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .bar-chart {
    height: 160px;
  }

  .report-page .el-row {
    flex-direction: column;
    gap: 12px;
  }

  .report-page .el-col {
    max-width: 100% !important;
    width: 100% !important;
  }

  .perf-stats {
    gap: 16px;
  }
}
</style>

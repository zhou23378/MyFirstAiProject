<template>
  <div class="consumption-page">
    <PageHeader title="消费收银" />

    <!-- 移动端步骤条 -->
    <el-steps v-if="isMobile" :active="currentStep" align-center class="checkout-steps">
      <el-step title="选会员" />
      <el-step title="选服务" />
      <el-step title="结算" />
    </el-steps>

    <!-- 移动端步骤摘要条 -->
    <div v-if="isMobile && currentStep > 0" class="step-summary" @click="summaryExpanded = !summaryExpanded">
      <div class="summary-bar">
        <span class="summary-member">{{ selectedMember?.name || '未选会员' }}</span>
        <span class="summary-sep">|</span>
        <span class="summary-services">{{ selectedItems.length }}项服务</span>
        <span class="summary-sep">|</span>
        <span class="summary-total">¥{{ discountedTotal }}</span>
        <el-icon class="summary-arrow" :class="{ expanded: summaryExpanded }"><ArrowDown /></el-icon>
      </div>
      <div v-show="summaryExpanded" class="summary-detail">
        <div v-for="(item, i) in selectedItems" :key="i" class="summary-item">
          <span>{{ item.name }}</span>
          <span>¥{{ item.price?.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 三栏布局（桌面并排 / 移动端步骤切换） -->
    <el-row :gutter="20" :class="{ 'step-mode': isMobile }">
      <!-- 左侧：选择会员 -->
      <el-col :span="8" :class="stepPanelClass(0)">
        <el-card shadow="never">
          <template #header>选择会员</template>
          <el-input
            v-model="memberSearch"
            placeholder="搜索会员姓名/手机号"
            clearable
            @input="searchMembers"
          />
          <template v-if="memberLoading">
            <SkeletonTable :rows="5" :cols="3" />
          </template>
          <template v-else>
            <el-table
              :data="members"
              stripe
              highlight-current-row
              height="400"
              @row-click="onMemberSelect"
            >
              <el-table-column prop="name" label="姓名" min-width="80" />
              <el-table-column prop="phone" label="手机号" min-width="120" />
              <el-table-column prop="balance" label="余额" min-width="100">
                <template #default="{ row }">¥{{ row.balance?.toFixed(2) }}</template>
              </el-table-column>
              <template #empty>
                <EmptyState description="暂无会员" />
              </template>
            </el-table>
          </template>
        </el-card>
      </el-col>

      <!-- 中间：选择服务 -->
      <el-col :span="10" :class="stepPanelClass(1)">
        <el-card shadow="never">
          <template #header>选择服务项目</template>
          <el-input
            v-model="serviceSearch"
            placeholder="搜索服务名称..."
            clearable
            class="service-search"
          />
          <el-collapse v-model="activeCategory">
            <el-collapse-item
              v-for="cat in filteredCategories"
              :key="cat.id"
              :name="cat.id"
              :title="`${cat.name} (${cat.count})`"
            >
              <div
                v-for="item in cat.items"
                :key="item.id"
                class="service-item"
                @click="addItem(item)"
              >
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-duration">{{ item.duration }}分钟</span>
                </div>
                <span class="item-price">¥{{ item.price?.toFixed(2) }}</span>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>

      <!-- 右侧：结算 -->
      <el-col :span="6" :class="stepPanelClass(2)">
        <el-card shadow="never">
          <template #header>结算</template>
          <div v-if="!selectedMember" class="empty-tip">请先选择会员</div>
          <div v-else>
            <div class="selected-member">
              <el-tag type="success">{{ selectedMember.name }}</el-tag>
              <el-tag v-if="memberLevelName" type="warning" size="small">
                {{ memberLevelName }}
              </el-tag>
              <span class="member-balance">余额: ¥{{ selectedMember.balance?.toFixed(2) }}</span>
            </div>

            <el-divider />
            <div v-for="(item, index) in selectedItems" :key="index" class="selected-item">
              <span>{{ item.name }}</span>
              <span>¥{{ item.price?.toFixed(2) }}</span>
              <el-button type="danger" link size="small" @click="removeItem(index)">×</el-button>
            </div>
            <el-divider />
            <div class="total-line">
              <span>小计</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div v-if="discountRate < 1" class="total-line discount-line">
              <span>等级折扣 ({{ memberLevelName }})</span>
              <span>-{{ ((1 - discountRate) * 100).toFixed(0) }}%</span>
            </div>
            <div class="total">
              <span>实收金额</span>
              <span class="total-price">¥{{ discountedTotal }}</span>
            </div>
            <el-select v-model="payMethod" class="pay-select" placeholder="支付方式">
              <el-option :value="1" label="现金" />
              <el-option :value="2" label="余额" />
              <el-option :value="3" label="微信" />
              <el-option :value="4" label="支付宝" />
              <el-option :value="5" label="银行卡" />
              <el-option :value="6" label="储值卡" />
              <el-option :value="7" label="团购券" />
              <el-option :value="8" label="混合" />
            </el-select>

            <!-- 混合支付明细 -->
            <div v-if="payMethod === 8" class="mixed-pay-section">
              <div class="mixed-pay-header">
                <span>支付明细</span>
                <el-button size="small" text type="primary" @click="addPaymentRow">+ 添加</el-button>
              </div>
              <div v-for="(p, idx) in payments" :key="idx" class="payment-row">
                <el-select v-model="p.payMethod" size="small" style="width:90px">
                  <el-option :value="1" label="现金" />
                  <el-option :value="2" label="余额" />
                  <el-option :value="3" label="微信" />
                  <el-option :value="4" label="支付宝" />
                  <el-option :value="5" label="银行卡" />
                  <el-option :value="6" label="储值卡" />
                  <el-option :value="7" label="团购券" />
                </el-select>
                <el-input-number
                  v-model="p.amount"
                  size="small"
                  :min="0"
                  :precision="2"
                  :max="99999"
                  style="width:110px"
                  placeholder="金额"
                />
                <el-button size="small" text type="danger" @click="payments.splice(idx, 1)">×</el-button>
              </div>
              <div class="mixed-pay-summary">
                <span>已分配：¥{{ paymentTotal.toFixed(2) }}</span>
                <span :class="paymentDiff < 0 ? 'diff-under' : paymentDiff > 0 ? 'diff-over' : 'diff-ok'">
                  {{ paymentDiff < 0 ? `还差 ¥${Math.abs(paymentDiff).toFixed(2)}` : paymentDiff > 0 ? `超出 ¥${paymentDiff.toFixed(2)}` : '✓ 已匹配' }}
                </span>
              </div>
            </div>
            <el-input v-model="payRemark" class="pay-remark" placeholder="支付备注（选填）" size="small" />
            <el-select
              v-model="employeeId"
              class="pay-select"
              placeholder="服务技师（选填）"
              clearable
              @focus="loadEmployees"
            >
              <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
            <div class="btn-group">
              <el-button
                type="primary"
                class="pay-btn"
                :loading="paying"
                :disabled="selectedItems.length === 0"
                @click="handlePay"
              >
                确认收款 ¥{{ discountedTotal }}
              </el-button>
              <el-button
                class="suspend-btn"
                :disabled="selectedItems.length === 0"
                :loading="suspending"
                @click="handleSuspend"
              >
                挂单
              </el-button>
            </div>
            <el-button
              class="resume-btn"
              text
              type="primary"
              @click="openResumeDialog"
            >
              取单结算
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 取单对话框 -->
    <el-dialog v-model="resumeDialogVisible" title="挂起订单" width="500px">
      <el-table :data="suspendedOrders" @row-click="onResumeOrder" highlight-current-row>
        <el-table-column prop="id" label="单号" min-width="70" />
        <el-table-column prop="memberId" label="会员ID" min-width="80" />
        <el-table-column label="金额" min-width="100">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="itemCount" label="项目数" min-width="70" />
        <el-table-column label="挂单时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.suspendTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无挂起订单" />
        </template>
      </el-table>
    </el-dialog>

    <!-- 移动端步骤导航 -->
    <div v-if="isMobile" class="step-nav">
      <el-button
        v-if="currentStep > 0"
        class="step-nav-btn"
        @click="currentStep--"
      >
        <el-icon><ArrowLeft /></el-icon> 上一步
      </el-button>
      <div v-else class="step-nav-spacer" />
      <el-button
        type="primary"
        class="step-nav-btn"
        :disabled="!canProceed"
        @click="onNext"
      >
        {{ currentStep === 2 ? (paying ? '收款中…' : '确认收款 ¥' + discountedTotal) : '下一步' }}
        <el-icon v-if="currentStep < 2"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { memberApi } from "../../api/member";
import { memberLevelApi } from "../../api/memberLevel";
import { serviceApi } from "../../api/service";
import { consumptionApi } from "../../api/consumption";
import { getEmployeeList } from "../../api/employee";
import { ElMessage } from "element-plus";
import { ArrowLeft, ArrowRight, ArrowDown } from "@element-plus/icons-vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import EmptyState from "@/components/EmptyState.vue";
import PageHeader from "@/components/PageHeader.vue";

const members = ref([]);
const memberLoading = ref(false);
const categories = ref([]);
const allItems = ref([]);
const serviceSearch = ref("");

const filteredCategories = computed(() => {
  const kw = serviceSearch.value.trim().toLowerCase();
  return categories.value
    .map(cat => {
      const items = allItems.value.filter(
        item => item.categoryId === cat.id && item.status === 1
          && (!kw || item.name.toLowerCase().includes(kw))
      );
      return { ...cat, items, count: items.length };
    })
    .filter(cat => cat.items.length > 0);
});
const selectedMember = ref(null);
const selectedItems = ref([]);
const paying = ref(false);
const payMethod = ref(1);
const payRemark = ref("");
const employeeId = ref(null);
const payments = ref([]);
const suspending = ref(false);
const resumeDialogVisible = ref(false);
const suspendedOrders = ref([]);

const paymentTotal = computed(() => {
  return payments.value.reduce((s, p) => s + (p.amount || 0), 0);
});
const paymentDiff = computed(() => {
  return parseFloat(discountedTotal.value) - paymentTotal.value;
});
const employees = ref([]);
const memberSearch = ref("");
const activeCategory = ref([]);
const levelMap = ref({});

// 移动端步骤
const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < 768);
const currentStep = ref(0);
const summaryExpanded = ref(false);

function onResize() {
  windowWidth.value = window.innerWidth;
}

onMounted(() => window.addEventListener("resize", onResize));
onUnmounted(() => window.removeEventListener("resize", onResize));

function stepPanelClass(step) {
  if (!isMobile.value) return {};
  return {
    'step-panel': true,
    'step-panel--active': currentStep.value === step,
  };
}

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0: return !!selectedMember.value;
    case 1: return selectedItems.value.length > 0;
    case 2: return selectedItems.value.length > 0;
    default: return false;
  }
});

function onMemberSelect(row) {
  selectedMember.value = row;
  if (isMobile.value && currentStep.value === 0) {
    currentStep.value = 1;
  }
}

function onNext() {
  if (currentStep.value === 2) {
    handlePay();
    return;
  }
  if (canProceed.value) {
    currentStep.value++;
  }
}

const memberLevelName = computed(() => {
  if (!selectedMember.value) return "";
  const lv = levelMap.value[selectedMember.value.level];
  return lv ? lv.name : "";
});

const discountRate = computed(() => {
  if (!selectedMember.value) return 1;
  const lv = levelMap.value[selectedMember.value.level];
  return lv ? lv.discountRate : 1;
});

const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price, 0);
});

const discountedTotal = computed(() => {
  return (totalAmount.value * discountRate.value).toFixed(2);
});

async function searchMembers() {
  memberLoading.value = true;
  try {
    const res = await memberApi.page({
      name: memberSearch.value,
      page: 1,
      pageSize: 50,
    });
    members.value = res.list || [];
  } catch { ElMessage.error("搜索会员失败"); } finally { memberLoading.value = false; }
}

function getItemsByCategory(categoryId) {
  return allItems.value.filter(
    (item) => item.categoryId === categoryId && item.status === 1
  );
}

function addItem(item) {
  selectedItems.value.push({ ...item });
}

function removeItem(index) {
  selectedItems.value.splice(index, 1);
}

async function handlePay() {
  if (payMethod.value === 8) {
    const diff = parseFloat(paymentDiff.value.toFixed(2));
    if (Math.abs(diff) > 0.01) {
      ElMessage.warning("混合支付金额未匹配实收金额");
      return;
    }
  }
  paying.value = true;
  const dto = {
    memberId: selectedMember.value.id,
    payMethod: payMethod.value,
    payRemark: payRemark.value || undefined,
    employeeId: employeeId.value || undefined,
    items: selectedItems.value.map((item) => ({
      itemId: item.id,
      itemName: item.name,
      itemPrice: item.price,
      quantity: 1,
    })),
  };
  if (payMethod.value === 8) {
    dto.payments = payments.value.map((p) => ({
      payMethod: p.payMethod,
      amount: p.amount,
    }));
    const balancePay = payments.value.find((p) => p.payMethod === 2);
    dto.balanceUsed = balancePay ? balancePay.amount : 0;
    dto.payAmount = paymentTotal.value - (balancePay ? balancePay.amount : 0);
  } else if (payMethod.value === 2) {
    dto.balanceUsed = parseFloat(discountedTotal.value);
  }
  try {
    await consumptionApi.create(dto);
    ElMessage.success("收款成功");
    selectedItems.value = [];
    payments.value = [];
    currentStep.value = 0;
    const updated = await memberApi.getById(selectedMember.value.id);
    selectedMember.value = updated;
  } catch (error) {
    ElMessage.error(error.message || "收款失败，请重试");
  } finally {
    paying.value = false;
  }
}

function addPaymentRow() {
  payments.value.push({ payMethod: 1, amount: 0 });
}

async function loadEmployees() {
  if (employees.value.length > 0) return;
  try {
    const empRes = await getEmployeeList({ pageSize: 200, status: 1 });
    employees.value = empRes.list || [];
  } catch { ElMessage.error("加载员工列表失败"); }
}

async function handleSuspend() {
  if (!selectedMember.value) {
    ElMessage.warning("请先选择会员")
    return
  }
  suspending.value = true
  try {
    await consumptionApi.suspend({
      memberId: selectedMember.value.id,
      employeeId: employeeId.value || undefined,
      items: selectedItems.value.map(item => ({
        itemId: item.id,
        itemName: item.name,
        itemPrice: item.price,
        quantity: 1
      }))
    })
    ElMessage.success("挂单成功")
    selectedItems.value = []
    payments.value = []
  } catch { ElMessage.error("挂单失败") } finally {
    suspending.value = false
  }
}

async function openResumeDialog() {
  resumeDialogVisible.value = true
  try {
    suspendedOrders.value = await consumptionApi.listSuspended()
  } catch { ElMessage.error("加载挂单列表失败") }
}

async function onResumeOrder(row) {
  if (!selectedMember.value) {
    ElMessage.warning("请先选择与挂单对应的会员")
    return
  }
  try {
    const itemsResp = await consumptionApi.getOrderItems(row.id)
    const items = Array.isArray(itemsResp) ? itemsResp : []
    selectedItems.value = items.map(it => ({
      id: it.itemId,
      name: it.itemName,
      price: it.itemPrice,
      categoryId: null
    }))
    resumeDialogVisible.value = false
    ElMessage.success(`已加载挂单 #${row.id}，请确认收款`)
  } catch { ElMessage.error("加载挂单明细失败") }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadLevels() {
  try {
    const levels = await memberLevelApi.list();
    const map = {};
    levels.forEach((lv) => {
      map[lv.id] = { name: lv.name, discountRate: lv.discountRate };
    });
    levelMap.value = map;
  } catch (e) {
    ElMessage.error("加载等级列表失败");
  }
}

onMounted(async () => {
  loadLevels();
  try {
    categories.value = await serviceApi.getCategories();
    allItems.value = await serviceApi.getItems();
  } catch { ElMessage.error("加载服务数据失败"); }
  searchMembers();
});
</script>

<style scoped>
.consumption-page {
  max-width: 1400px;
  margin: 0 auto;
  overflow-x: hidden;
}

.service-search {
  margin-bottom: 12px;
}

/* 服务项目列表项 */
.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.service-item:hover {
  background: var(--bg-primary-ghost);
  transform: translateX(4px);
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.item-duration {
  font-size: 12px;
  color: var(--text-muted);
}

.item-price {
  color: var(--primary-color);
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
}

.selected-member {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-balance {
  font-size: 12px;
  color: var(--text-muted);
}

.selected-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 14px;
  color: var(--text-primary);
}

.total-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  padding: 2px 0;
}

.discount-line {
  color: var(--success-color);
}

.total {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-subtle);
}

.total-price {
  color: var(--primary-color);
}

.pay-select {
  width: 100%;
  margin: 12px 0;
}

.btn-group {
  display: flex;
  gap: 8px;
}

.pay-btn {
  flex: 1;
}

.suspend-btn {
  flex-shrink: 0;
}

.resume-btn {
  width: 100%;
  margin-top: 8px;
}

.pay-remark {
  margin-bottom: 12px;
}

.mixed-pay-section {
  margin: 8px 0;
  padding: 12px;
  background: var(--bg-overlay-subtle);
  border-radius: var(--radius-sm);
}

.mixed-pay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.payment-row {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: 6px;
}

.mixed-pay-summary {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-muted);
}

.diff-ok { color: var(--success-color); }
.diff-under { color: var(--danger-color); }
.diff-over { color: var(--warning-color); }

/* ========== 移动端步骤条 ========== */
.checkout-steps {
  margin-bottom: 12px;
  padding: 0 8px;
}

/* ========== 移动端步骤摘要条 ========== */
.step-summary {
  margin: 0 8px 12px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  user-select: none;
}

.summary-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 13px;
}

.summary-member {
  font-weight: 600;
  color: var(--text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-sep {
  color: var(--text-muted);
}

.summary-services {
  color: var(--text-secondary);
}

.summary-total {
  font-weight: 700;
  color: var(--primary-color);
  margin-left: auto;
}

.summary-arrow {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform 0.2s;
}

.summary-arrow.expanded {
  transform: rotate(180deg);
}

.summary-detail {
  padding: 0 14px 10px;
  border-top: 1px solid var(--border-light);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== 移动端步骤面板切换 ========== */
.step-mode .el-col {
  display: none;
}

.step-mode .step-panel--active {
  display: block;
  max-width: 100% !important;
  width: 100% !important;
}

/* ========== 移动端步骤导航 ========== */
.step-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-bottom: calc(var(--safe-area-bottom) + 8px);
}

.step-nav-spacer {
  flex: 1;
}

.step-nav-btn {
  min-height: 48px;
  min-width: 120px;
  font-size: var(--font-size-mobile-base);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .consumption-page {
    padding: 0;
  }

  .step-mode .el-row {
    flex-direction: column;
    gap: 0;
  }

  .service-item {
    padding: 12px;
    min-height: var(--touch-target-min);
  }

  .item-name {
    font-size: var(--font-size-mobile-base);
  }

  .item-price {
    font-size: var(--font-size-mobile-base);
  }

  .pay-btn {
    min-height: 48px;
    font-size: var(--font-size-mobile-base);
  }

  .selected-item {
    padding: 8px 0;
  }

  .mixed-pay-section {
    padding: 10px;
  }

  .payment-row {
    flex-wrap: wrap;
  }
}
</style>

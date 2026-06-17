<template>
  <div class="member-detail-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回会员列表
      </el-button>
      <span class="page-title">会员详情</span>
    </div>

    <!-- 会员基本信息卡片 -->
    <el-card class="info-card" shadow="never">
      <div class="member-info">
        <div class="avatar-section">
          <el-avatar :size="80" class="member-avatar">
            {{ member.name?.charAt(0) || "?" }}
          </el-avatar>
          <div class="member-name-level">
            <span class="member-name">{{ member.name }}</span>
            <el-tag :type="getLevelTagType(member.level)" size="small">
              {{ getLevelName(member.level) }}
            </el-tag>
          </div>
        </div>
        <el-divider direction="vertical" />
        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-label">手机号</span>
            <span class="stat-value">{{ member.phone || "-" }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">性别</span>
            <span class="stat-value">
              <el-tag
                :type="
                  member.gender === 1
                    ? 'primary'
                    : member.gender === 2
                    ? 'danger'
                    : 'info'
                "
                size="small"
              >
                {{
                  member.gender === 1
                    ? "男"
                    : member.gender === 2
                    ? "女"
                    : "未知"
                }}
              </el-tag>
            </span>
          </div>
          <div class="stat-item">
            <span class="stat-label">积分</span>
            <span class="stat-value highlight">{{ member.points ?? 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">余额</span>
            <span class="stat-value highlight balance"
              >¥{{ (member.balance ?? 0).toFixed(2) }}</span
            >
          </div>
          <div class="stat-item">
            <span class="stat-label">注册时间</span>
            <span class="stat-value">{{ member.createTime || "-" }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 消费记录 & 次卡 -->
    <el-card class="order-card" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="消费记录" name="orders" />
        <el-tab-pane label="充值记录" name="recharges" />
        <el-tab-pane label="次卡管理" name="cards" />
      </el-tabs>

      <template v-if="activeTab === 'orders'">
      <el-table
        :data="orderList"
        v-loading="orderLoading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单号" min-width="100" />
        <el-table-column prop="totalAmount" label="金额" min-width="120">
          <template #default="{ row }">
            <span class="balance-text"
              >¥{{ (row.totalAmount ?? 0).toFixed(2) }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" min-width="120">
          <template #default="{ row }">
            <el-tag :type="payMethodTag(row.payMethod)" size="small">
              {{ payMethodText(row.payMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balanceUsed" label="使用余额" min-width="120">
          <template #default="{ row }">
            ¥{{ (row.balanceUsed ?? 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="获得积分" min-width="100" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'danger' : 'success'" size="small">
              {{ row.status === 2 ? '已退款' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="消费时间" min-width="180" />
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showOrderDetail(row)">
              查看明细
            </el-button>
            <el-button
              v-if="row.status !== 2"
              type="danger"
              link
              @click="handleRefund(row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model="orderQuery.page"
          :page-size="orderQuery.pageSize"
          :total="orderTotal"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="
            (p) => {
              orderQuery.page = p;
              fetchOrders();
            }
          "
          @size-change="
            (s) => {
              orderQuery.pageSize = s;
              orderQuery.page = 1;
              fetchOrders();
            }
          "
        />
        </div>
      </template>

      <!-- 充值记录 -->
      <template v-if="activeTab === 'recharges'">
        <el-table :data="rechargeList" v-loading="rechargeLoading" stripe>
          <el-table-column prop="amount" label="充值金额" min-width="140">
            <template #default="{ row }">
              <span class="balance-text">¥{{ (row.amount ?? 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="payMethod" label="支付方式" min-width="120">
            <template #default="{ row }">
              <el-tag :type="payMethodTag(row.payMethod)" size="small">
                {{ payMethodText(row.payMethod) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
          <el-table-column prop="createTime" label="充值时间" min-width="180" />
          <template #empty>
            <EmptyState description="暂无充值记录" />
          </template>
        </el-table>
      </template>

      <!-- 次卡管理 -->
      <template v-if="activeTab === 'cards'">
        <div class="action-bar">
          <el-button type="primary" size="small" @click="openCardDialog()">购买次卡</el-button>
        </div>
        <el-table :data="cardList" v-loading="cardLoading" stripe>
          <el-table-column prop="id" label="编号" min-width="70" />
          <el-table-column prop="name" label="次卡名称" min-width="120" />
          <el-table-column label="次数" min-width="120">
            <template #default="{ row }">
              <span :class="{ 'text-danger': row.usedCount >= row.totalCount }">
                {{ row.usedCount }} / {{ row.totalCount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="购买金额" min-width="100">
            <template #default="{ row }">¥{{ (row.price ?? 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="80">
            <template #default="{ row }">
              <el-tag :type="cardStatusTag(row.status)" size="small">{{ cardStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expireDate" label="有效期" min-width="120" />
          <el-table-column label="操作" min-width="120">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" type="primary" link size="small" @click="handleDeduct(row)">扣次</el-button>
              <el-button type="danger" link size="small" @click="handleCardDelete(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState description="暂无次卡" />
          </template>
        </el-table>
      </template>
    </el-card>

    <!-- 订单明细弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="订单明细"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{
          currentOrder?.id
        }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="balance-text"
            >¥{{ (currentOrder?.totalAmount ?? 0).toFixed(2) }}</span
          >
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{
          payMethodText(currentOrder?.payMethod)
        }}</el-descriptions-item>
        <el-descriptions-item label="获得积分">{{
          currentOrder?.pointsEarned ?? 0
        }}</el-descriptions-item>
        <el-descriptions-item label="使用余额"
          >¥{{
            (currentOrder?.balanceUsed ?? 0).toFixed(2)
          }}</el-descriptions-item
        >
        <el-descriptions-item label="消费时间">{{
          currentOrder?.createTime
        }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>服务项目</el-divider>

      <el-table :data="orderItems" stripe style="width: 100%">
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="itemPrice" label="单价" min-width="120">
          <template #default="{ row }">
            ¥{{ (row.itemPrice ?? 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" min-width="80" />
        <el-table-column label="小计" min-width="120">
          <template #default="{ row }">
            <span class="balance-text"
              >¥{{
                ((row.itemPrice ?? 0) * (row.quantity ?? 1)).toFixed(2)
              }}</span
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 购买次卡弹窗 -->
    <el-dialog v-model="cardDialogVisible" title="购买次卡" width="420px">
      <el-form :model="cardForm" label-width="80px">
        <el-form-item label="服务项目">
          <el-select v-model="cardForm.serviceItemId" filterable placeholder="选择服务项目" style="width:100%">
            <el-option v-for="item in allServiceItems" :key="item.id" :label="`${item.name} ¥${item.price?.toFixed(2)}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="次卡名称">
          <el-input v-model="cardForm.name" placeholder="如：洗剪吹10次卡" />
        </el-form-item>
        <el-form-item label="总次数">
          <el-input-number v-model="cardForm.totalCount" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="购买金额">
          <el-input-number v-model="cardForm.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="cardForm.expireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cardDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cardSubmitting" @click="handleCardPurchase">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { memberApi } from "../../api/member";
import { memberLevelApi } from "../../api/memberLevel";
import { consumptionApi, serviceCardApi } from "../../api/consumption";
import { serviceApi } from "../../api/service";
import { ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { payMethodMap } from "@/constants/payMethod";

const route = useRoute();
const router = useRouter();
const memberId = route.params.id;

const member = ref({});
const orderList = ref([]);
const orderTotal = ref(0);
const orderLoading = ref(false);
const detailVisible = ref(false);
const currentOrder = ref(null);
const orderItems = ref([]);

const orderQuery = reactive({
  page: 1,
  pageSize: 10,
});

const activeTab = ref("orders");

// 充值记录
const rechargeList = ref([]);
const rechargeLoading = ref(false);

async function fetchRecharges() {
  rechargeLoading.value = true;
  try {
    rechargeList.value = await memberApi.rechargeRecords(memberId);
  } finally {
    rechargeLoading.value = false;
  }
}

const cardList = ref([]);
const cardLoading = ref(false);
const cardDialogVisible = ref(false);
const cardSubmitting = ref(false);
const allServiceItems = ref([]);
const cardForm = reactive({
  serviceItemId: null,
  name: "",
  totalCount: 10,
  price: 0,
  expireDate: "",
});

function cardStatusText(s) {
  return { 1: "有效", 2: "用完", 3: "过期", 4: "退款" }[s] || "未知";
}
function cardStatusTag(s) {
  return { 1: "success", 2: "info", 3: "warning", 4: "danger" }[s] || "info";
}

async function fetchCards() {
  cardLoading.value = true;
  try {
    const res = await serviceCardApi.page({ memberId, pageSize: 100 });
    cardList.value = res.list || [];
  } finally {
    cardLoading.value = false;
  }
}

function openCardDialog() {
  cardForm.serviceItemId = null;
  cardForm.name = "";
  cardForm.totalCount = 10;
  cardForm.price = 0;
  cardForm.expireDate = "";
  cardDialogVisible.value = true;
}

async function handleCardPurchase() {
  if (!cardForm.serviceItemId) {
    ElMessage.warning("请选择服务项目");
    return;
  }
  if (!cardForm.name) {
    ElMessage.warning("请输入次卡名称");
    return;
  }
  cardSubmitting.value = true;
  try {
    await serviceCardApi.purchase({
      memberId: Number(memberId),
      ...cardForm,
    });
    ElMessage.success("购买成功");
    cardDialogVisible.value = false;
    fetchCards();
    fetchMember();
  } catch {
  } finally {
    cardSubmitting.value = false;
  }
}

async function handleDeduct(row) {
  try {
    await ElMessageBox.confirm(`确认对「${row.name}」扣次？(${row.usedCount}/${row.totalCount})`, "扣次确认", { type: "warning" });
  } catch {
    return;
  }
  try {
    await serviceCardApi.deduct(row.id);
    ElMessage.success("扣次成功");
    fetchCards();
  } catch { ElMessage.error('退款失败') }
}

async function handleCardDelete(row) {
  try {
    await ElMessageBox.confirm(`删除次卡「${row.name}」？`, "确认", { type: "warning" });
  } catch {
    return;
  }
  try {
    await serviceCardApi.remove(row.id);
    ElMessage.success("删除成功");
    fetchCards();
  } catch { ElMessage.error('购卡失败') }
}

// 等级名称映射
const levelMap = ref({});

function getLevelName(levelId) {
  return levelMap.value[levelId] || `等级${levelId}`;
}

function getLevelTagType(levelId) {
  const map = {
    1: "info",
    2: "primary",
    3: "warning",
    4: "danger",
  };
  return map[levelId] || "info";
}

async function loadLevels() {
  try {
    const levels = await memberLevelApi.list();
    const map = {};
    levels.forEach((lv) => {
      map[lv.id] = lv.name;
    });
    levelMap.value = map;
  } catch (e) {
    ElMessage.error("加载等级列表失败");
  }
}

function goBack() {
  router.push("/member");
}

function payMethodText(method) {
  return payMethodMap[method] || "未知";
}

function payMethodTag(method) {
  const map = { 1: "", 2: "success", 3: "primary", 4: "info", 5: "", 6: "", 7: "danger", 8: "warning" };
  return map[method] || "info";
}

async function fetchMember() {
  try {
    const res = await memberApi.getById(memberId);
    member.value = res;
  } catch (e) {
    // 会员不存在则返回列表
    router.push("/member");
  }
}

async function fetchOrders() {
  orderLoading.value = true;
  try {
    const res = await consumptionApi.pageByMember(memberId, orderQuery);
    orderList.value = res.list || [];
    orderTotal.value = res.total || 0;
  } finally {
    orderLoading.value = false;
  }
}

async function showOrderDetail(row) {
  currentOrder.value = row;
  detailVisible.value = true;
  try {
    const res = await consumptionApi.getOrderItems(row.id);
    orderItems.value = res || [];
  } catch {
    orderItems.value = [];
  }
}

async function handleRefund(row) {
  try {
    await ElMessageBox.confirm(
      `确定退款订单 #${row.id}？将退还余额 ¥${(row.balanceUsed ?? 0).toFixed(2)} 并扣回 ${row.pointsEarned ?? 0} 积分。`,
      "退款确认",
      { type: "warning" }
    );
  } catch {
    return;
  }
  try {
    await consumptionApi.refund(row.id);
    ElMessage.success("退款成功");
    fetchOrders();
    fetchMember();
  } catch (e) {
    // error handled by interceptor
  }
}

watch(activeTab, (tab) => {
  if (tab === 'recharges') fetchRecharges();
});

onMounted(async () => {
  loadLevels();
  fetchMember();
  fetchOrders();
  try {
    allServiceItems.value = await serviceApi.getItems();
  } catch { ElMessage.error('操作失败') }
  try {
    fetchCards();
  } catch { ElMessage.error('操作失败') }
});
</script>

<style scoped>
.member-detail-page {
  overflow-x: hidden;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 会员信息卡片 */
.info-card {
  margin-bottom: 20px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.member-avatar {
  background: var(--primary-color);
  font-size: 28px;
  color: var(--text-light);
}

.member-name-level {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.member-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stats-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px 40px;
  flex: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

.stat-value.highlight {
  font-size: 18px;
  font-weight: 700;
}

.stat-value.balance {
  color: var(--primary-color);
}

/* 消费记录 */
.order-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-text {
  font-weight: 600;
  color: var(--primary-color);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.action-bar {
  margin-bottom: 12px;
}

.text-danger {
  color: var(--danger-color);
  font-weight: 600;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .member-detail-page {
    padding: 0;
  }

  .member-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-section {
    gap: 12px 24px;
  }

  .el-divider--vertical {
    display: none;
  }
}
</style>



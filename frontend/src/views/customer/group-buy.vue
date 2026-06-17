<template>
  <div class="group-buy-page">
    <div class="page-header">
      <el-icon class="back-icon" @click="$router.back()"><ArrowLeft /></el-icon>
      <span class="page-title">拼团活动</span>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="热门活动" name="templates" />
      <el-tab-pane label="进行中的团" name="orders" />
      <el-tab-pane label="我的拼团" name="mine" />
    </el-tabs>

    <!-- 热门活动 -->
    <div v-if="activeTab === 'templates'" v-loading="templatesLoading" class="content-section">
      <div v-if="templates.length === 0" class="empty-state">
        <el-empty description="暂无拼团活动" />
      </div>
      <div v-for="t in templates" :key="t.id" class="activity-card" @click="openCreateDialog(t)">
        <div class="card-left">
          <h4>{{ t.name }}</h4>
          <p class="card-desc">{{ t.description || '超值拼团，邀请好友一起享优惠' }}</p>
          <p class="card-meta">{{ t.groupSize }}人团 · {{ t.expireHours }}小时有效</p>
        </div>
        <div class="card-right">
          <span class="group-price">¥{{ t.groupPrice }}</span>
          <span class="original-price">¥{{ t.originalPrice }}</span>
          <el-button type="primary" size="small">去开团</el-button>
        </div>
      </div>
    </div>

    <!-- 进行中的团 -->
    <div v-if="activeTab === 'orders'" v-loading="ordersLoading" class="content-section">
      <div v-if="orders.length === 0" class="empty-state">
        <el-empty description="暂无进行中的团" />
      </div>
      <div v-for="o in orders" :key="o.id" class="order-card" @click="openJoinDialog(o)">
        <div class="order-top">
          <span class="order-leader">{{ o.leaderName }}发起的团</span>
          <span class="order-price">¥{{ o.groupPrice }}</span>
        </div>
        <div class="order-mid">
          <span>{{ o.currentSize }}/{{ o.groupSize }} 人</span>
          <el-progress :percentage="Math.round(o.currentSize / o.groupSize * 100)" :stroke-width="8" style="flex:1;margin:0 12px" />
          <span class="order-deadline">剩余 {{ formatRemain(o.expireTime) }}</span>
        </div>
        <el-button type="primary" size="small" style="margin-top:8px">去参团</el-button>
      </div>
    </div>

    <!-- 我的拼团 -->
    <div v-if="activeTab === 'mine'" v-loading="myOrdersLoading" class="content-section">
      <div v-if="myOrders.length === 0" class="empty-state">
        <el-empty description="暂无参与的拼团" />
      </div>
      <div v-for="o in myOrders" :key="o.id" class="order-card">
        <div class="order-top">
          <span class="order-no">{{ o.orderNo }}</span>
          <el-tag :type="statusTag(o.status)" size="small">{{ statusText(o.status) }}</el-tag>
        </div>
        <div class="order-mid">
          <span>{{ o.currentSize }}/{{ o.groupSize }} 人</span>
          <el-progress :percentage="Math.round(o.currentSize / o.groupSize * 100)" :stroke-width="8" style="flex:1;margin:0 12px" />
        </div>
        <div class="order-bottom">
          <span>¥{{ o.groupPrice }}</span>
          <span class="order-time">{{ o.createTime }}</span>
        </div>
      </div>
    </div>

    <!-- 开团确认弹窗 -->
    <el-dialog v-model="createDialogVisible" title="确认开团" width="90%">
      <div v-if="selectedTemplate" class="create-dialog">
        <div class="create-info">
          <h4>{{ selectedTemplate.name }}</h4>
          <p>拼团价：¥{{ selectedTemplate.groupPrice }} / 原价：¥{{ selectedTemplate.originalPrice }}</p>
          <p>{{ selectedTemplate.groupSize }}人成团 · {{ selectedTemplate.expireHours }}小时内有效</p>
          <p v-if="selectedTemplate.description">{{ selectedTemplate.description }}</p>
        </div>
        <el-button type="primary" size="large" :loading="creating" @click="doCreate" style="width:100%">
          立即开团 ¥{{ selectedTemplate.groupPrice }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 参团确认弹窗 -->
    <el-dialog v-model="joinDialogVisible" title="确认参团" width="90%">
      <div v-if="selectedOrder" class="join-dialog">
        <h4>{{ selectedOrder.leaderName }}发起的团</h4>
        <p>拼团价：¥{{ selectedOrder.groupPrice }}</p>
        <p>进度：{{ selectedOrder.currentSize }}/{{ selectedOrder.groupSize }} 人</p>
        <p>截止：{{ selectedOrder.expireTime }}</p>
        <el-button type="primary" size="large" :loading="joining" @click="doJoin" style="width:100%">
          立即参团 ¥{{ selectedOrder.groupPrice }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { customerApi } from '../../api/customer'

const activeTab = ref('templates')

const templates = ref([])
const templatesLoading = ref(false)
const orders = ref([])
const ordersLoading = ref(false)
const myOrders = ref([])
const myOrdersLoading = ref(false)

const createDialogVisible = ref(false)
const selectedTemplate = ref(null)
const creating = ref(false)

const joinDialogVisible = ref(false)
const selectedOrder = ref(null)
const joining = ref(false)

const statusMap = { 1: '拼团中', 2: '已成团', 3: '已核销', 4: '已过期', 5: '已取消' }
const statusTagMap = { 1: 'warning', 2: 'success', 3: '', 4: 'info', 5: 'danger' }
function statusText(s) { return statusMap[s] || '未知' }
function statusTag(s) { return statusTagMap[s] || 'info' }

function formatRemain(expireTime) {
  if (!expireTime) return ''
  const diff = new Date(expireTime).getTime() - Date.now()
  if (diff <= 0) return '已截止'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  return h > 0 ? `${h}小时${m}分` : `${m}分钟`
}

async function fetchTemplates() {
  templatesLoading.value = true
  try { templates.value = await customerApi.getGroupBuyTemplates() } catch { ElMessage.error('加载活动失败') } finally { templatesLoading.value = false }
}

async function fetchOrders() {
  ordersLoading.value = true
  try { orders.value = await customerApi.getGroupBuyOrders() } catch { ElMessage.error('加载团单失败') } finally { ordersLoading.value = false }
}

async function fetchMyOrders() {
  myOrdersLoading.value = true
  try {
    const res = await customerApi.getMyGroupBuyOrders(1, 50)
    myOrders.value = res.list || []
  } catch { ElMessage.error('加载我的拼团失败') } finally { myOrdersLoading.value = false }
}

function openCreateDialog(template) {
  selectedTemplate.value = template
  createDialogVisible.value = true
}

async function doCreate() {
  creating.value = true
  try {
    await customerApi.createGroupBuy({ templateId: selectedTemplate.value.id })
    ElMessage.success('开团成功')
    createDialogVisible.value = false
    activeTab.value = 'mine'
    fetchMyOrders()
  } catch { ElMessage.error('开团失败') } finally { creating.value = false }
}

function openJoinDialog(order) {
  selectedOrder.value = order
  joinDialogVisible.value = true
}

async function doJoin() {
  joining.value = true
  try {
    await customerApi.joinGroupBuy(selectedOrder.value.id)
    ElMessage.success('参团成功')
    joinDialogVisible.value = false
    activeTab.value = 'mine'
    fetchMyOrders()
    fetchOrders()
  } catch { ElMessage.error('参团失败') } finally { joining.value = false }
}

onMounted(() => {
  fetchTemplates()
  fetchOrders()
  fetchMyOrders()
})
</script>

<style scoped>
.group-buy-page {
  overflow-x: hidden;
  padding: 0 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-card);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-icon {
  font-size: 20px;
  margin-right: 12px;
  color: var(--text-primary);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.content-section {
  padding: 12px 16px;
}

.empty-state {
  padding: 40px 0;
}

.activity-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-sm);
}

.card-left h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: var(--text-primary);
}

.card-desc {
  margin: 0 0 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.card-meta {
  margin: 0;
  font-size: 11px;
  color: var(--text-secondary);
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.group-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--danger-color);
}

.original-price {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.order-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-sm);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-leader {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.order-no {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--danger-color);
}

.order-mid {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.order-deadline {
  font-size: 12px;
  color: var(--warning-color);
  white-space: nowrap;
}

.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 13px;
}

.order-time {
  font-size: 11px;
  color: var(--text-secondary);
}

.create-info, .join-dialog {
  text-align: center;
}

.create-info h4, .join-dialog h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.create-info p, .join-dialog p {
  margin: 0 0 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>

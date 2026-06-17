<template>
  <div class="customer-profile">
    <div class="profile-header">
      <div class="avatar-circle">
        <el-icon :size="32"><UserFilled /></el-icon>
      </div>
      <span class="profile-name">{{ authStore.name || '未设置' }}</span>
      <span class="profile-phone">{{ authStore.phone || '' }}</span>
    </div>

    <div class="asset-cards">
      <div class="asset-card" @click="$router.push('/h5/points-mall')">
        <span class="asset-value">{{ profile?.points ?? '--' }}</span>
        <span class="asset-label">积分</span>
      </div>
      <div class="asset-card" @click="$router.push('/h5/recharge')">
        <span class="asset-value">&yen;{{ displayBalance }}</span>
        <span class="asset-label">余额</span>
      </div>
      <div class="asset-card">
        <span class="asset-value">{{ coupons.length }}</span>
        <span class="asset-label">优惠券</span>
      </div>
      <div class="asset-card" @click="$router.push('/h5/service-cards')">
        <span class="asset-value">{{ cardCount }}</span>
        <span class="asset-label">次卡</span>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item" @click="$router.push('/h5/booking')">
        <el-icon class="menu-icon"><Calendar /></el-icon>
        <span class="menu-label">我的预约</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="scrollTo('order-section')">
        <el-icon class="menu-icon"><Document /></el-icon>
        <span class="menu-label">消费记录</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="scrollTo('coupon-section')">
        <el-icon class="menu-icon"><Ticket /></el-icon>
        <span class="menu-label">我的优惠券</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/h5/service-cards')">
        <el-icon class="menu-icon"><Present /></el-icon>
        <span class="menu-label">我的次卡</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item" @click="$router.push('/h5/recharge')">
        <el-icon class="menu-icon"><Wallet /></el-icon>
        <span class="menu-label">余额充值</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/h5/progress')">
        <el-icon class="menu-icon"><Clock /></el-icon>
        <span class="menu-label">服务进度</span>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-item menu-item--logout" @click="handleLogout">
        <el-icon class="menu-icon"><SwitchButton /></el-icon>
        <span class="menu-label">退出登录</span>
      </div>
    </div>

    <!-- 消费记录 -->
    <div class="order-section">
      <div class="section-header">
        <span class="section-title">消费记录</span>
        <div class="filter-row">
          <el-date-picker v-model="filterStartDate" type="date" placeholder="开始日期" size="small" style="width:130px" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
          <span class="filter-sep">—</span>
          <el-date-picker v-model="filterEndDate" type="date" placeholder="结束日期" size="small" style="width:130px" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </div>
        <div class="sort-row">
          <el-radio-group v-model="sortBy" size="small" @change="loadOrders">
            <el-radio-button value="date">按时间</el-radio-button>
            <el-radio-button value="amount">按金额</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-if="orders.length === 0" class="empty-state">
        <el-empty description="暂无消费记录" :image-size="80" />
      </div>

      <div v-for="o in orders" :key="o.id" class="order-card" @click="toggleDetail(o)">
        <div class="order-top">
          <span class="order-amount">&yen;{{ Number(o.totalAmount).toFixed(2) }}</span>
          <span class="order-date">{{ formatDate(o.createTime) }}</span>
        </div>
        <div class="order-meta">
          <el-tag :type="o.payMethod === 2 ? 'warning' : o.payMethod === 3 ? 'success' : ''" size="small">
            {{ payMethodLabel(o.payMethod) }}
          </el-tag>
          <span class="order-status">{{ o.status === 1 ? '已完成' : '已退款' }}</span>
        </div>

        <!-- 展开详情 -->
        <div v-if="expandedId === o.id" class="order-detail" @click.stop>
          <div v-if="o.items && o.items.length" class="detail-items">
            <div v-for="item in o.items" :key="item.id" class="detail-item">
              <span>{{ item.itemName }}</span>
              <span>&yen;{{ Number(item.itemPrice).toFixed(2) }} x{{ item.quantity }}</span>
            </div>
          </div>

          <!-- 评价区域 -->
          <div class="rating-section">
            <template v-if="o.rated">
              <div class="rated-display">
                <span class="rated-stars">{{ '★'.repeat(o.ratingValue) }}{{ '☆'.repeat(5 - o.ratingValue) }}</span>
                <span v-if="o.ratingTags" class="rated-tags">{{ o.ratingTags }}</span>
              </div>
            </template>
            <template v-else>
              <div class="rating-prompt">对本次服务评价</div>
              <div class="star-row">
                <span v-for="s in 5" :key="s" class="star" :class="{ active: ratingForm[o.id]?.rating >= s }" @click="setRating(o.id, s)">{{ ratingForm[o.id]?.rating >= s ? '★' : '☆' }}</span>
              </div>
              <div class="tag-row">
                <el-tag v-for="t in quickTags" :key="t" size="small" :type="ratingForm[o.id]?.tags?.includes(t) ? 'primary' : ''" @click="toggleTag(o.id, t)" class="rate-tag">{{ t }}</el-tag>
              </div>
              <el-input v-model="ratingForm[o.id].comment" placeholder="说点什么吧（选填）" size="small" maxlength="200" />
              <el-button type="primary" size="small" @click="submitRate(o.id)" :loading="ratingSubmitting" style="margin-top:8px;width:100%">提交评价</el-button>
            </template>
          </div>
        </div>
      </div>

      <el-pagination v-if="orderTotal > 0" layout="prev, next" :total="orderTotal" :page-size="orderPageSize" :current-page="orderPage" @current-change="loadOrders" small style="justify-content:center;margin-top:12px" />
    </div>

    <!-- 优惠券 -->
    <div class="coupon-section">
      <div class="section-header">
        <span class="section-title">我的优惠券</span>
      </div>
      <div v-if="coupons.length === 0" class="empty-state">
        <el-empty description="暂无优惠券" :image-size="80" />
      </div>
      <div v-for="c in coupons" :key="c.id" class="coupon-card" :class="{ expired: c.status === 3 }">
        <div class="coupon-left">
          <span class="coupon-value">&yen;{{ Number(c.discountValue).toFixed(0) }}</span>
          <span class="coupon-condition" v-if="c.conditionAmount > 0">满{{ Number(c.conditionAmount).toFixed(0) }}可用</span>
        </div>
        <div class="coupon-right">
          <span class="coupon-name">{{ c.name }}</span>
          <span class="coupon-expire">{{ c.status === 3 ? '已过期' : '有效期至 ' + (c.expireTime || '').split(' ')[0] }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/store/customerAuth'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  UserFilled, Calendar, Document, Ticket, Present,
  ArrowRight, SwitchButton, Wallet, Clock
} from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'
import { payMethodMap } from '@/constants/payMethod'

const router = useRouter()
const authStore = useCustomerAuthStore()

const profile = ref(null)
const coupons = ref([])
const cardCount = ref('--')

const displayBalance = computed(() => {
  if (!profile.value || profile.value.balance == null) return '--'
  return Number(profile.value.balance).toFixed(2)
})

// ── 消费记录 ──
const orders = ref([])
const orderPage = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)
const filterStartDate = ref('')
const filterEndDate = ref('')
const sortBy = ref('date')
const expandedId = ref(null)

// ── 评价 ──
const quickTags = ['态度好', '技术好', '环境好', '性价比高', '等待短']
const ratingForm = reactive({})
const ratingSubmitting = ref(false)

function payMethodLabel(m) {
  return payMethodMap[m] || '其他'
}

function formatDate(d) {
  if (!d) return ''
  return d.substring(0, 16).replace('T', ' ')
}

function initRatingForm(orderId) {
  if (!ratingForm[orderId]) {
    ratingForm[orderId] = { rating: 0, tags: [], comment: '' }
  }
}

function setRating(orderId, val) {
  initRatingForm(orderId)
  ratingForm[orderId].rating = val
}

function toggleTag(orderId, tag) {
  initRatingForm(orderId)
  const tags = ratingForm[orderId].tags
  const idx = tags.indexOf(tag)
  if (idx >= 0) tags.splice(idx, 1); else tags.push(tag)
}

async function toggleDetail(order) {
  if (expandedId.value === order.id) {
    expandedId.value = null
    return
  }
  expandedId.value = order.id
  initRatingForm(order.id)
  // 获取详情（含items）并检查是否已评价
  try {
    const detail = await customerApi.getOrderDetail(order.id)
    if (detail?.items) order.items = detail.items
  } catch { ElMessage.error('操作失败') }
  try {
    const rate = await customerApi.getOrderRate(order.id)
    if (rate) {
      order.rated = true
      order.ratingValue = rate.rating
      order.ratingTags = rate.tags ? JSON.parse(rate.tags).join(' · ') : ''
    }
  } catch { ElMessage.error('操作失败') }
}

async function submitRate(orderId) {
  const form = ratingForm[orderId]
  if (!form?.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  ratingSubmitting.value = true
  try {
    await customerApi.rateOrder(orderId, {
      rating: form.rating,
      tags: form.tags.length > 0 ? JSON.stringify(form.tags) : null,
      comment: form.comment || null
    })
    ElMessage.success('评价成功')
    const order = orders.value.find(o => o.id === orderId)
    if (order) {
      order.rated = true
      order.ratingValue = form.rating
      order.ratingTags = form.tags.join(' · ')
    }
  } catch { ElMessage.error('加载失败') }
  ratingSubmitting.value = false
}

async function loadOrders(page = 1) {
  orderPage.value = page
  try {
    const res = await customerApi.getOrders(page, orderPageSize.value,
      filterStartDate.value || undefined,
      filterEndDate.value || undefined,
      sortBy.value)
    orders.value = (res?.list || []).map(o => ({ ...o, rated: false }))
    orderTotal.value = res?.total || 0
  } catch { ElMessage.error('操作失败') }
}

onMounted(async () => {
  try {
    const [p, couponList, cards] = await Promise.all([
      customerApi.getProfile(),
      customerApi.getCoupons(),
      customerApi.getServiceCards()
    ])
    profile.value = p
    coupons.value = Array.isArray(couponList) ? couponList : []
    cardCount.value = Array.isArray(cards) ? cards.length : '--'
  } catch { ElMessage.error('操作失败') }
  loadOrders()
})

function scrollTo(className) {
  document.querySelector('.' + className)?.scrollIntoView({ behavior: 'smooth' })
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    router.push('/h5/login')
  } catch { ElMessage.error('操作失败') }
}
</script>

<style scoped>
.customer-profile {
  padding: 16px;
}

.profile-header {
  text-align: center;
  padding: 24px 0 20px;
}

.avatar-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--customer-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  margin: 0 auto 12px;
}

.profile-name {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.profile-phone {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.asset-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 24px;
}

.asset-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 14px 6px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.asset-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.asset-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.menu-section {
  background: var(--bg-white);
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-light);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: var(--bg-page);
}

.menu-item--logout {
  justify-content: center;
  color: var(--customer-danger);
}

.menu-icon {
  font-size: 20px;
  color: var(--text-secondary);
}

.menu-item--logout .menu-icon {
  color: var(--customer-danger);
}

.menu-label {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
}

.menu-item--logout .menu-label {
  color: var(--customer-danger);
  flex: none;
}

.menu-arrow {
  font-size: 14px;
  color: var(--text-muted);
}

/* ── 消费记录 ── */
.order-section {
  margin-top: 12px;
}

.section-header {
  padding: 0 4px 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
}

.filter-sep {
  color: var(--text-muted);
  font-size: 12px;
}

.sort-row {
  margin-top: 8px;
}

.empty-state {
  padding: 24px 0;
}

.order-card {
  background: var(--bg-white);
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  cursor: pointer;
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.order-date {
  font-size: 12px;
  color: var(--text-muted);
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.order-status {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ── 订单详情 ── */
.order-detail {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

.detail-items {
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  padding: 4px 0;
}

/* ── 评价 ── */
.rating-section {
  margin-top: 8px;
}

.rated-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rated-stars {
  font-size: 18px;
  color: var(--customer-primary);
}

.rated-tags {
  font-size: 12px;
  color: var(--text-muted);
}

.rating-prompt {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.star-row {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.star {
  font-size: 24px;
  cursor: pointer;
  color: var(--border-light);
  transition: color 0.15s;
}

.star.active {
  color: var(--customer-primary);
}

.tag-row {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.rate-tag {
  cursor: pointer;
}

/* ── 优惠券 ── */
.coupon-section {
  margin-top: 20px;
}

.coupon-card {
  display: flex;
  background: var(--bg-white);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.coupon-card.expired {
  opacity: 0.5;
}

.coupon-left {
  width: 90px;
  background: var(--primary-gradient);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14px 8px;
  color: var(--text-light);
  flex-shrink: 0;
}

.coupon-value {
  font-size: 22px;
  font-weight: 700;
}

.coupon-condition {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

.coupon-right {
  flex: 1;
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.coupon-expire {
  font-size: 11px;
  color: var(--text-muted);
}

@media (max-width: 480px) {
  .asset-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

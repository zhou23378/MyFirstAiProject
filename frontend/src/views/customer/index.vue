<template>
  <div class="customer-home">
    <div class="user-greeting">
      <div class="greeting-text">
        <span class="greeting-hi">Hi, {{ authStore.name || '顾客' }}</span>
        <span class="greeting-sub">欢迎回来</span>
      </div>
      <div class="member-badge" v-if="authStore.phone">
        <el-tag type="warning" size="small">会员</el-tag>
      </div>
    </div>

    <div class="quick-cards">
      <div class="quick-card" @click="go('/h5/profile')">
        <div class="card-icon card-icon--points">
          <el-icon><Star /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-label">我的积分</span>
          <span class="card-value">{{ profile?.points ?? '--' }}</span>
        </div>
      </div>
      <div class="quick-card" @click="go('/h5/recharge')">
        <div class="card-icon card-icon--balance">
          <el-icon><Wallet /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-label">账户余额</span>
          <span class="card-value">&yen;{{ displayBalance }}</span>
        </div>
      </div>
      <div class="quick-card" @click="go('/h5/profile')">
        <div class="card-icon card-icon--coupon">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-label">优惠券</span>
          <span class="card-value">{{ couponCount }}</span>
        </div>
      </div>
    </div>

    <div class="action-grid">
      <div class="action-card" @click="go('/h5/booking')">
        <el-icon class="action-icon"><Calendar /></el-icon>
        <span class="action-label">在线预约</span>
        <span class="action-desc">选择服务和时间</span>
      </div>
      <div class="action-card" @click="go('/h5/progress')">
        <el-icon class="action-icon"><Clock /></el-icon>
        <span class="action-label">服务进度</span>
        <span class="action-desc">实时查看排队计时</span>
      </div>
      <div class="action-card" @click="go('/h5/profile')">
        <el-icon class="action-icon"><UserFilled /></el-icon>
        <span class="action-label">个人中心</span>
        <span class="action-desc">查看消费与资产</span>
      </div>
      <div class="action-card" @click="go('/h5/recharge')">
        <el-icon class="action-icon"><Wallet /></el-icon>
        <span class="action-label">余额充值</span>
        <span class="action-desc">在线充值余额</span>
      </div>
      <div class="action-card" @click="go('/h5/service-cards')">
        <el-icon class="action-icon"><Present /></el-icon>
        <span class="action-label">次卡中心</span>
        <span class="action-desc">购买与管理次卡</span>
      </div>
      <div class="action-card" @click="handleLogout">
        <el-icon class="action-icon"><SwitchButton /></el-icon>
        <span class="action-label">退出登录</span>
        <span class="action-desc">安全退出</span>
      </div>
    </div>

    <div class="recommend-section">
      <h3 class="section-title">推荐服务</h3>
      <div class="service-list" v-if="services.length > 0">
        <div class="service-item" v-for="s in services" :key="s.id" @click="go('/h5/booking')">
          <div class="service-avatar">
            <el-icon :size="24"><Scissor /></el-icon>
          </div>
          <span class="service-name">{{ s.name }}</span>
          <span class="service-price">¥{{ s.price }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无推荐服务" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/store/customerAuth'
import { ElMessageBox } from 'element-plus'
import { Star, Wallet, Ticket, Calendar, UserFilled, SwitchButton, Clock, Present, Scissor } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'

const router = useRouter()
const authStore = useCustomerAuthStore()

const profile = ref(null)
const couponCount = ref('--')
const services = ref([])

const displayBalance = computed(() => {
  if (!profile.value || profile.value.balance == null) return '--'
  return Number(profile.value.balance).toFixed(2)
})

onMounted(async () => {
  try {
    const [p, coupons] = await Promise.all([
      customerApi.getProfile(),
      customerApi.getCoupons()
    ])
    profile.value = p
    couponCount.value = Array.isArray(coupons) ? coupons.length : '--'
  } catch { /* 静默降级 */ }
  try {
    const cats = await customerApi.getServiceCategoryTree()
    const items = []
    if (Array.isArray(cats)) {
      cats.forEach(c => (c.children || []).forEach(sc => (sc.services || []).forEach(s => items.push(s))))
    }
    services.value = items.slice(0, 4)
  } catch { /* 静默降级 */ }
})

function go(path) {
  router.push(path)
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
  } catch { /* 静默降级 */ }
}
</script>

<style scoped>
.customer-home {
  padding: 16px;
}

.user-greeting {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.greeting-hi {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  display: block;
}

.greeting-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 2px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.quick-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 14px 10px;
  text-align: center;
  box-shadow: var(--shadow-card);
}

.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  font-size: 18px;
  color: var(--text-light);
}

.card-icon--points,
.card-icon--balance,
.card-icon--coupon { background: var(--primary-color); }

.card-label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
}

.card-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 2px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 24px;
}

.action-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 18px 10px;
  text-align: center;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: transform 0.2s;
}

.action-card:active {
  transform: scale(0.96);
}

.action-icon {
  font-size: 24px;
  color: var(--primary-color);
  margin-bottom: 6px;
}

.action-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.action-desc {
  font-size: 11px;
  color: var(--text-muted);
}

.recommend-section {
  margin-top: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.service-item {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: var(--shadow-card);
}

.service-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--bg-page);
  margin: 0 auto 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.service-name {
  font-size: 13px;
  color: var(--text-muted);
}

@media (max-width: 400px) {
  .quick-cards,
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

<template>
  <div class="recharge-page">
    <div class="page-header">
      <el-icon class="back-icon" @click="$router.back()"><ArrowLeft /></el-icon>
      <span class="page-title">余额充值</span>
    </div>

    <div class="current-balance">
      <span class="balance-label">当前余额</span>
      <span class="balance-amount">&yen;{{ profile?.balance || '0.00' }}</span>
    </div>

    <div class="amount-section">
      <span class="section-label">选择充值金额</span>
      <div class="amount-grid">
        <div
          v-for="opt in amountOptions"
          :key="opt.value"
          class="amount-card"
          :class="{ active: amount === opt.value }"
          @click="amount = opt.value"
        >
          <span class="amount-num">&yen;{{ opt.value }}</span>
          <span v-if="opt.label" class="amount-tag">{{ opt.label }}</span>
        </div>
      </div>
      <el-input
        v-model="customAmount"
        placeholder="自定义金额"
        class="custom-input"
        @focus="amount = null"
      />
    </div>

    <div class="pay-section">
      <span class="section-label">支付方式</span>
      <div class="pay-options">
        <div
          v-for="p in payMethods"
          :key="p.value"
          class="pay-card"
          :class="{ active: payMethod === p.value }"
          @click="payMethod = p.value"
        >
          <span class="pay-name">{{ p.label }}</span>
        </div>
      </div>
    </div>

    <el-button
      type="primary"
      class="submit-btn"
      :loading="loading"
      @click="handleRecharge"
    >
      确认充值
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'

const router = useRouter()

const profile = ref(null)
const amount = ref(200)
const customAmount = ref('')
const payMethod = ref(3)
const loading = ref(false)

const amountOptions = [
  { value: 50 },
  { value: 100, label: '推荐' },
  { value: 200 },
  { value: 300 },
  { value: 500 },
  { value: 1000 }
]

const payMethods = [
  { value: 3, label: '微信支付' },
  { value: 4, label: '支付宝' }
]

onMounted(async () => {
  try {
    profile.value = await customerApi.getProfile()
  } catch { ElMessage.error('充值失败') }
})

async function handleRecharge() {
  const finalAmount = amount.value || Number(customAmount.value)
  if (!finalAmount || finalAmount <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  loading.value = true
  try {
    await customerApi.recharge({ amount: finalAmount, payMethod: payMethod.value })
    ElMessage.success('充值成功')
    router.back()
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.recharge-page {
  padding: 16px;
  min-height: 100vh;
  background: var(--bg-page);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.back-icon {
  font-size: 20px;
  color: var(--text-primary);
  cursor: pointer;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.current-balance {
  text-align: center;
  padding: 24px 0;
}

.balance-label {
  display: block;
  font-size: 13px;
  color: var(--text-muted);
}

.balance-amount {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-color);
  margin-top: 4px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: block;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}

.amount-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 14px 8px;
  text-align: center;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s;
}

.amount-card.active {
  border-color: var(--primary-color);
}

.amount-num {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.amount-tag {
  font-size: 11px;
  color: var(--primary-color);
  background: var(--primary-light);
  padding: 1px 6px;
  border-radius: 4px;
}

.custom-input {
  margin-bottom: 20px;
}

.pay-section {
  margin-bottom: 24px;
}

.pay-options {
  display: flex;
  gap: 10px;
}

.pay-card {
  flex: 1;
  background: var(--bg-white);
  border-radius: 12px;
  padding: 14px;
  text-align: center;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s;
}

.pay-card.active {
  border-color: var(--primary-color);
}

.pay-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
}

@media (max-width: 400px) {
  .amount-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

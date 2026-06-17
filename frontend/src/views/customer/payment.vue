<template>
  <div class="customer-payment">
    <div class="payment-amount">
      <span class="amount-label">支付金额</span>
      <span class="amount-value">¥{{ amount }}</span>
    </div>

    <div class="payment-methods">
      <h4 class="section-title">选择支付方式</h4>
      <div class="method-list">
        <div
          v-for="m in methods"
          :key="m.key"
          class="method-card"
          :class="{ selected: selectedMethod === m.key }"
          @click="selectedMethod = m.key"
        >
          <el-icon class="method-icon" :style="{ color: m.color }">
            <component :is="m.icon" />
          </el-icon>
          <span class="method-name">{{ m.name }}</span>
          <el-icon v-if="selectedMethod === m.key" class="method-check" color="var(--primary-color)">
            <CircleCheckFilled />
          </el-icon>
        </div>
      </div>
    </div>

    <el-button
      type="primary"
      size="large"
      class="pay-btn"
      :loading="paying"
      @click="handlePay"
    >
      {{ paying ? '支付中...' : `确认支付 ¥${amount}` }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, Phone, CreditCard, CircleCheckFilled } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'

const route = useRoute()
const router = useRouter()

const amount = ref('0.00')
const selectedMethod = ref('BALANCE')
const paying = ref(false)

const methods = [
  { key: 'BALANCE', name: '余额支付', icon: Wallet, color: 'var(--customer-danger)' },
  { key: 'WECHAT', name: '微信支付', icon: Phone, color: '#07c160' },
  { key: 'ALIPAY', name: '支付宝', icon: CreditCard, color: '#1677ff' }
]

onMounted(() => {
  const amt = route.query.amount
  if (amt) amount.value = Number(amt).toFixed(2)
})

async function handlePay() {
  if (selectedMethod.value !== 'BALANCE') {
    ElMessage.info('微信/支付宝支付即将上线，请使用余额支付')
    return
  }
  paying.value = true
  try {
    const res = await customerApi.pay({
      amount: Number(amount.value),
      payMethod: selectedMethod.value
    })
    ElMessage.success(res.message || '支付成功')
    router.back()
  } catch (e) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.customer-payment {
  overflow-x: hidden;
  padding: 16px;
}

.payment-amount {
  text-align: center;
  padding: 32px 0;
}

.amount-label {
  display: block;
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.amount-value {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.method-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-white);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.method-card.selected {
  border-color: var(--primary-color);
}

.method-icon {
  font-size: 28px;
}

.method-name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.method-check {
  font-size: 20px;
}

.pay-btn {
  position: fixed;
  bottom: calc(80px + var(--safe-area-bottom, 0px));
  left: 16px;
  right: 16px;
  height: 48px;
  padding-bottom: var(--safe-area-bottom, 0px);
  border-radius: 12px;
  font-size: 16px;
}
</style>

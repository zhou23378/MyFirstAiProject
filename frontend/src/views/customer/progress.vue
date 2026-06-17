<template>
  <div class="progress-page">
    <div class="page-header">
      <el-icon class="back-icon" @click="$router.back()"><ArrowLeft /></el-icon>
      <span class="page-title">服务进度</span>
    </div>

    <!-- IDLE: 空闲状态 -->
    <div v-if="data?.status === 'IDLE'" class="state-idle">
      <div class="idle-circle">
        <el-icon :size="48"><CoffeeCup /></el-icon>
      </div>
      <span class="idle-text">当前无进行中的服务</span>
      <span class="idle-sub">可以前往预约心仪的服务</span>
      <el-button type="primary" class="booking-btn" @click="$router.push('/h5/booking')">去预约</el-button>
    </div>

    <!-- IN_SERVICE: 服务中 -->
    <div v-else-if="data?.status === 'IN_SERVICE'" class="state-service">
      <div class="timer-circle" :class="{ paused: data.timerStatus === 'PAUSED' }">
        <span class="timer-minutes">{{ displayMinutes }}</span>
        <span class="timer-unit">分钟</span>
        <span class="timer-label">{{ data.timerStatus === 'PAUSED' ? '已暂停' : '进行中' }}</span>
      </div>
      <div class="service-info">
        <div class="info-row">
          <span class="info-label">服务技师</span>
          <span class="info-value">{{ data.technicianName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">服务项目</span>
          <span class="info-value">{{ data.serviceName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">计划时长</span>
          <span class="info-value">{{ data.plannedDuration }} 分钟</span>
        </div>
        <div class="info-row">
          <span class="info-label">已进行</span>
          <span class="info-value">{{ formatSeconds(data.elapsedSeconds) }}</span>
        </div>
      </div>
    </div>

    <!-- WAITING: 排队中 -->
    <div v-else-if="data?.status === 'WAITING'" class="state-waiting">
      <div class="queue-card">
        <span class="queue-number">{{ data.queueNumber }}</span>
        <span class="queue-label">您的排队号</span>
      </div>
      <div class="waiting-info">
        <span class="waiting-text">前方还有 <b>{{ data.waitingAhead }}</b> 人等待</span>
        <span class="waiting-sub">请留意叫号，不要走远</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-else class="state-loading">
      <el-skeleton :rows="4" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowLeft, CoffeeCup } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'

const data = ref(null)
let timer = null

const displayMinutes = computed(() => {
  if (!data.value || data.value.status !== 'IN_SERVICE') return '--'
  const remaining = data.value.remainingSeconds || 0
  return Math.max(1, Math.ceil(remaining / 60))
})

function formatSeconds(s) {
  if (!s || s <= 0) return '0分'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? `${m}分${sec}秒` : `${sec}秒`
}

async function fetchProgress() {
  try {
    data.value = await customerApi.getProgress()
  } catch {}
}

onMounted(async () => {
  await fetchProgress()
  timer = setInterval(fetchProgress, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.progress-page {
  overflow-x: hidden;
  padding: 16px;
  min-height: 100vh;
  background: var(--bg-page);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
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

/* IDLE */
.state-idle {
  text-align: center;
  padding-top: 60px;
}

.idle-circle {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: var(--bg-white);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: var(--text-muted);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.idle-text {
  display: block;
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.idle-sub {
  display: block;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.booking-btn {
  width: 200px;
  height: 44px;
  border-radius: 12px;
}

/* IN_SERVICE */
.state-service {
  text-align: center;
}

.timer-circle {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  border: 4px solid var(--primary-color);
}

.timer-circle.paused {
  border-color: var(--text-muted);
  background: var(--bg-white);
}

.timer-minutes {
  font-size: 36px;
  font-weight: 700;
  color: var(--primary-color);
}

.timer-circle.paused .timer-minutes {
  color: var(--text-muted);
}

.timer-unit {
  font-size: 14px;
  color: var(--text-secondary);
}

.timer-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.service-info {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 4px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-light);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--text-muted);
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

/* WAITING */
.state-waiting {
  text-align: center;
  padding-top: 40px;
}

.queue-card {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: var(--text-light);
}

.queue-number {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}

.queue-label {
  font-size: 14px;
  margin-top: 4px;
  opacity: 0.9;
}

.waiting-text {
  display: block;
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.waiting-text b {
  color: var(--primary-color);
  font-size: 20px;
}

.waiting-sub {
  display: block;
  font-size: 13px;
  color: var(--text-muted);
}
</style>

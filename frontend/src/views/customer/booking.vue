<template>
  <div class="customer-booking">
    <el-steps :active="step" align-center class="booking-steps">
      <el-step title="选服务" />
      <el-step title="选时间" />
      <el-step title="确认" />
    </el-steps>

    <div class="step-content">
      <!-- 步骤1：选择服务 -->
      <div v-if="step === 0" class="step-panel">
        <div class="service-list" v-loading="servicesLoading">
          <div
            v-for="item in services"
            :key="item.id"
            class="service-card"
            :class="{ selected: selectedService?.id === item.id }"
            @click="selectedService = item"
          >
            <span class="service-name">{{ item.name }}</span>
            <span class="service-price">¥{{ item.price }}</span>
            <span class="service-duration">{{ item.duration }}分钟</span>
          </div>
          <el-empty v-if="!servicesLoading && services.length === 0" description="暂无可用服务" />
        </div>
        <el-button type="primary" class="next-btn" :disabled="!selectedService" @click="step = 1">
          下一步
        </el-button>
      </div>

      <!-- 步骤2：选择日期和时间 -->
      <div v-if="step === 1" class="step-panel">
        <div class="date-picker-row">
          <span class="date-label">预约日期</span>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </div>
        <div class="time-slots" v-if="selectedDate" v-loading="slotsLoading">
          <h4 class="slot-title">可选时段</h4>
          <div class="slot-grid" v-if="timeSlots.length > 0">
            <button
              v-for="t in timeSlots"
              :key="t.value"
              class="slot-btn"
              :class="{ selected: selectedTime === t.value }"
              :disabled="!t.available"
              @click="selectedTime = t.value"
            >
              {{ t.label }}
            </button>
          </div>
          <el-empty v-else-if="!slotsLoading" description="该日期暂无可用时段" />
        </div>
        <div class="step-actions">
          <el-button @click="step = 0">上一步</el-button>
          <el-button type="primary" :disabled="!selectedTime" @click="step = 2">
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤3：确认提交 -->
      <div v-if="step === 2" class="step-panel">
        <div class="confirm-card">
          <div class="confirm-item">
            <span class="confirm-label">服务项目</span>
            <span class="confirm-value">{{ selectedService?.name }}</span>
          </div>
          <div class="confirm-item">
            <span class="confirm-label">预约日期</span>
            <span class="confirm-value">{{ selectedDate }}</span>
          </div>
          <div class="confirm-item">
            <span class="confirm-label">预约时间</span>
            <span class="confirm-value">{{ selectedTime }}</span>
          </div>
          <div class="confirm-item">
            <span class="confirm-label">预计费用</span>
            <span class="confirm-value confirm-price">¥{{ selectedService?.price }}</span>
          </div>
        </div>
        <div class="step-actions">
          <el-button @click="step = 1">上一步</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确认预约
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { customerApi } from '@/api/customer'

const step = ref(0)
const selectedService = ref(null)
const selectedDate = ref('')
const selectedTime = ref('')

function disabledDate(time) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}
const submitting = ref(false)

const services = ref([])
const servicesLoading = ref(false)

const timeSlots = ref([])
const slotsLoading = ref(false)

async function fetchSlots() {
  if (!selectedService.value || !selectedDate.value) return
  slotsLoading.value = true
  try {
    const slots = await customerApi.getAvailableSlots(selectedDate.value, selectedService.value.id)
    timeSlots.value = (slots || []).map(s => ({
      value: s.time,
      label: s.time,
      available: s.available
    }))
  } catch { timeSlots.value = [] }
  finally { slotsLoading.value = false }
}

watch([selectedService, selectedDate], fetchSlots)
watch(step, (val) => { if (val === 1) fetchSlots() })

onMounted(async () => {
  servicesLoading.value = true
  try {
    const cats = await customerApi.getServiceCategoryTree()
    const items = []
    if (Array.isArray(cats)) {
      cats.forEach(c => (c.children || []).forEach(sc => (sc.services || []).forEach(s => items.push(s))))
    }
    services.value = items
  } catch { /* 静默降级，显示空状态 */ }
  finally { servicesLoading.value = false }
})

async function handleSubmit() {
  submitting.value = true
  try {
    await customerApi.createAppointment({
      serviceItemId: selectedService.value.id,
      date: selectedDate.value,
      startTime: selectedTime.value + ':00'
    })
    ElMessage.success('预约提交成功')
    step.value = 0
    selectedService.value = null
    selectedTime.value = ''
  } catch (e) {
    ElMessage.error(e.message || '预约失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.customer-booking {
  padding: 16px;
}

.booking-steps {
  margin-bottom: 20px;
}

.booking-steps :deep(.el-step__title) {
  font-size: 12px;
}

.step-content {
  min-height: 300px;
}

.step-panel {
  padding-bottom: 80px;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.service-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.service-card.selected {
  border-color: var(--primary-color);
}

.service-name {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.service-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--customer-danger);
  margin-right: 12px;
}

.service-duration {
  font-size: 12px;
  color: var(--text-muted);
}

.next-btn {
  position: fixed;
  bottom: calc(80px + var(--safe-area-bottom, 0px));
  left: 16px;
  right: 16px;
  padding-bottom: var(--safe-area-bottom, 0px);
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
}

.date-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.date-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  flex-shrink: 0;
}

.slot-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.slot-btn {
  padding: 9px 0;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-white);
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.slot-btn:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
}

.slot-btn.selected {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-light);
}

.step-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.step-actions .el-button {
  flex: 1;
  height: 44px;
}

.confirm-card {
  background: var(--bg-white);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-label {
  font-size: 14px;
  color: var(--text-muted);
}

.confirm-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.confirm-price {
  color: var(--customer-danger);
  font-size: 18px;
}

@media (max-width: 480px) {
  .slot-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>

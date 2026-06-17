<template>
  <div class="technician-page">
    <PageHeader title="技师看板">
      <template #extra>
        <span class="refresh-hint">自动刷新 {{ countdown }}s</span>
      </template>
    </PageHeader>

    <div class="tech-grid" v-loading="loading">
      <div
        v-for="t in techs"
        :key="t.employeeId"
        class="tech-card"
        :class="'tech-card--' + t.status.toLowerCase()"
      >
        <div class="tech-header">
          <div class="tech-avatar">
            <span>{{ t.employeeName?.charAt(0) }}</span>
          </div>
          <div class="tech-info">
            <div class="tech-name-row">
              <span class="tech-name">{{ t.employeeName }}</span>
              <el-tag :type="statusTagType(t.status)" size="small">{{ statusText(t.status) }}</el-tag>
            </div>
            <span class="tech-position">{{ t.position || '技师' }}</span>
          </div>
          <div class="tech-actions">
            <el-dropdown trigger="click" @command="(status) => changeStatus(t, status)">
              <el-button size="small" circle :icon="Switch" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="AVAILABLE">设为空闲</el-dropdown-item>
                  <el-dropdown-item command="BUSY">设为忙碌</el-dropdown-item>
                  <el-dropdown-item command="BREAK">设为休息</el-dropdown-item>
                  <el-dropdown-item command="OFF_DUTY">设为下班</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="tech-detail" v-if="t.status === 'BUSY' && activeTimers[t.employeeId]">
          <div class="service-row">
            <span class="tech-customer">{{ t.currentCustomerName || '-' }}</span>
            <span class="tech-service">{{ t.currentServiceName || '-' }}</span>
          </div>
          <ServiceTimer
            :planned-duration="activeTimers[t.employeeId].plannedDuration"
            :started-at="activeTimers[t.employeeId].startedAt"
            :status="activeTimers[t.employeeId].status"
            :show-controls="true"
            @pause="handlePause(activeTimers[t.employeeId].id)"
            @resume="handleResume(activeTimers[t.employeeId].id)"
            @complete="handleComplete(activeTimers[t.employeeId].id)"
          />
        </div>

        <div class="tech-perf">
          <span class="perf-item"><span class="perf-num">{{ perfMap[t.employeeId]?.todayOrders || 0 }}</span>单</span>
          <span class="perf-item">¥{{ formatRevenue(perfMap[t.employeeId]?.todayRevenue) }}</span>
        </div>
      </div>
      <EmptyState v-if="techs.length === 0" description="暂无在职技师" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { technicianStatusApi } from '@/api/technicianStatus'
import { serviceTimerApi } from '@/api/serviceTimer'
import { ElMessage } from 'element-plus'
import { Switch } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ServiceTimer from '@/components/ServiceTimer.vue'

const loading = ref(false)
const techs = ref([])
const activeTimers = ref({})
const perfMap = ref({})
const countdown = ref(10)
let pollTimer = null
let cdTimer = null

const statusMap = { AVAILABLE: '空闲', BUSY: '忙碌', BREAK: '休息', OFF_DUTY: '下班' }
const statusTagMap = { AVAILABLE: 'success', BUSY: 'danger', BREAK: 'warning', OFF_DUTY: 'info' }

function statusText(s) { return statusMap[s] || s }
function statusTagType(s) { return statusTagMap[s] || 'info' }
function formatRevenue(v) {
  if (v == null) return "0";
  const n = Number(v);
  return n >= 10000 ? (n / 10000).toFixed(1) + "w" : n.toFixed(0);
}

async function loadData() {
  loading.value = true
  try {
    try {
      techs.value = await technicianStatusApi.list()
    } catch { /* 轮询静默降级 */ }
    try {
      const timers = await serviceTimerApi.active()
      const map = {}
      timers.forEach(t => { map[t.employeeId] = t })
      activeTimers.value = map
    } catch { /* 轮询静默降级 */ }
    try {
      const perfList = await technicianStatusApi.performance()
      const pMap = {}
      perfList.forEach(p => { pMap[p.employeeId] = p })
      perfMap.value = pMap
    } catch { /* 轮询静默降级 */ }
  } finally { loading.value = false }
}

async function changeStatus(t, newStatus) {
  try {
    await technicianStatusApi.changeStatus({
      employeeId: t.employeeId,
      status: newStatus,
    })
    ElMessage.success(`已设为${statusText(newStatus)}`)
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function handlePause(id) {
  try {
    await serviceTimerApi.pause(id)
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleResume(id) {
  try {
    await serviceTimerApi.resume(id)
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleComplete(id) {
  try {
    await serviceTimerApi.complete(id)
    ElMessage.success('服务已完成')
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}

function startPolling() {
  loadData()
  pollTimer = setInterval(loadData, 10000)
  cdTimer = setInterval(() => {
    countdown.value = countdown.value <= 1 ? 10 : countdown.value - 1
  }, 1000)
}

onMounted(startPolling)
onBeforeUnmount(() => {
  clearInterval(pollTimer)
  clearInterval(cdTimer)
})
</script>

<style scoped>
.technician-page { overflow-x: hidden; max-width: 1200px; margin: 0 auto; }

.refresh-hint { font-size: 12px; color: var(--text-muted); }

.tech-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.tech-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: var(--shadow-card);
  transition: all var(--transition-normal);
  border-left: 4px solid var(--text-muted);
  min-height: 200px;
}
.tech-card--available { border-left-color: var(--success-color); }
.tech-card--busy { border-left-color: var(--danger-color); }
.tech-card--break { border-left-color: var(--warning-color); }
.tech-card--off_duty { border-left-color: var(--text-muted); }

.tech-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tech-avatar {
  width: 44px; height: 44px;
  border-radius: 50%;
  background: var(--primary-color);
  color: var(--text-light);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 600;
  flex-shrink: 0;
}

.tech-info {
  flex: 1;
  min-width: 0;
}

.tech-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tech-name {
  font-size: 15px; font-weight: 600; color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.tech-position { font-size: 12px; color: var(--text-muted); margin-top: 2px; display: block; }

.tech-actions { flex-shrink: 0; }

.tech-detail {
  display: flex; flex-direction: column; gap: 8px;
  padding: 10px 12px;
  background: var(--bg-overlay-subtle);
  border-radius: var(--radius-sm);
}

.service-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.tech-customer { color: var(--text-primary); font-weight: 500; }
.tech-service { color: var(--text-muted); }
.tech-customer::after { content: "·"; margin-left: 6px; color: var(--text-muted); }

.tech-perf {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
  font-size: 13px;
  color: var(--text-secondary);
}

.perf-num { font-weight: 700; color: var(--text-primary); font-variant-numeric: tabular-nums; }

@media (max-width: 1200px) { .tech-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1023px) { .tech-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px) {
  .technician-page { padding: 0; }
  .tech-grid { grid-template-columns: 1fr; gap: 12px; }
  .tech-card { padding: 16px; min-height: 180px; }
}
</style>

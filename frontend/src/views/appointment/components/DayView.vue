<template>
  <div class="day-view">
    <div v-if="loading" v-loading="loading" style="min-height: 300px;" />

    <template v-else-if="technicians.length === 0">
      <el-empty description="当日无在职技师" />
    </template>

    <template v-else>
      <div class="day-grid" ref="gridRef">
        <!-- Header row: time label + technician names -->
        <div class="grid-header">
          <div class="time-header" />
          <div
            v-for="tech in technicians"
            :key="tech.id"
            class="tech-header"
            :class="`tech-status-${tech.status?.toLowerCase()}`"
            @click="$emit('select-tech', tech)"
          >
            <div class="tech-avatar">{{ tech.name?.charAt(0) }}</div>
            <div class="tech-name">{{ tech.name }}</div>
            <div class="tech-status-dot" />
          </div>
        </div>

        <!-- Scrollable body -->
        <div class="grid-body" ref="bodyRef">
          <!-- Time axis -->
          <div class="time-axis">
            <div
              v-for="slot in timeSlots"
              :key="slot.label"
              class="time-slot"
              :class="{ 'time-slot-hour': slot.isHour }"
            >
              <span v-if="slot.isHour" class="time-label">{{ slot.label }}</span>
            </div>
          </div>

          <!-- Technician columns -->
          <div
            v-for="tech in technicians"
            :key="tech.id"
            class="tech-column"
            @click.self="$emit('create-appointment', { date: currentDate, startTime: getClickedTime($event, tech), employeeId: tech.id })"
          >
            <!-- Slot background grid -->
            <div
              v-for="slot in timeSlots"
              :key="slot.label"
              class="slot-row"
              :class="{ 'slot-hour': slot.isHour }"
            />

            <!-- Appointment cards -->
            <AppointmentCard
              v-for="apt in tech.appointments"
              :key="apt.id"
              :appointment="apt"
              :style="getCardStyle(apt)"
              @click="$emit('select-appointment', apt)"
            />
          </div>
        </div>
      </div>

      <!-- Stats footer -->
      <div class="day-stats" v-if="stats">
        <span class="stat-item">总计 <b>{{ stats.total }}</b></span>
        <span class="stat-item stat-booked">已预约 <b>{{ stats.booked }}</b></span>
        <span class="stat-item stat-arrived">已到店 <b>{{ stats.arrived }}</b></span>
        <span class="stat-item stat-completed">已完成 <b>{{ stats.completed }}</b></span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import AppointmentCard from './AppointmentCard.vue'

const props = defineProps({
  technicians: { type: Array, default: () => [] },
  currentDate: { type: String, default: '' },
  stats: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

defineEmits(['select-appointment', 'create-appointment', 'select-tech'])

const gridRef = ref(null)
const bodyRef = ref(null)

const SLOT_MINUTES = 30
const START_HOUR = 8
const END_HOUR = 21
const SLOT_HEIGHT = 40 // px per 30-min slot

const timeSlots = computed(() => {
  const slots = []
  for (let h = START_HOUR; h < END_HOUR; h++) {
    for (let m = 0; m < 60; m += SLOT_MINUTES) {
      const label = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
      slots.push({ label, isHour: m === 0 })
    }
  }
  return slots
})

function timeToMinutes(timeStr) {
  if (!timeStr) return 0
  const [h, m] = timeStr.split(':').map(Number)
  return (h - START_HOUR) * 60 + m
}

function getCardStyle(apt) {
  const top = timeToMinutes(apt.startTime) / SLOT_MINUTES * SLOT_HEIGHT
  const startMin = timeToMinutes(apt.startTime)
  const endMin = timeToMinutes(apt.endTime)
  const duration = endMin > startMin ? endMin - startMin : SLOT_MINUTES
  const height = Math.max(duration / SLOT_MINUTES * SLOT_HEIGHT, 24)
  return {
    top: `${top}px`,
    height: `${height}px`
  }
}

function getClickedTime(event, tech) {
  if (!bodyRef.value) return ''
  const rect = bodyRef.value.getBoundingClientRect()
  const y = event.clientY - rect.top
  const totalMinutes = Math.floor(y / SLOT_HEIGHT) * SLOT_MINUTES
  const hour = START_HOUR + Math.floor(totalMinutes / 60)
  const min = totalMinutes % 60
  return `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`
}
</script>

<style scoped>
.day-view {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.day-grid {
  display: flex;
  flex-direction: column;
}

/* Header */
.grid-header {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
  position: sticky;
  top: 0;
  z-index: 5;
}

.time-header {
  width: 60px;
  flex-shrink: 0;
}

.tech-header {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 4px 8px;
  border-left: 1px solid var(--border-light);
  gap: 2px;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.tech-header:hover {
  background: var(--bg-card-hover);
}

.tech-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-gradient);
  color: var(--text-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.tech-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success-color);
}

.tech-status-busy .tech-status-dot { background: var(--danger-color); }
.tech-status-break .tech-status-dot { background: var(--warning-color); }
.tech-status-off_duty .tech-status-dot { background: var(--text-muted); }

.tech-name {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* Body */
.grid-body {
  display: flex;
  overflow-y: auto;
  max-height: calc(100vh - 380px);
  min-height: 400px;
  position: relative;
}

.time-axis {
  width: 60px;
  flex-shrink: 0;
  position: sticky;
  left: 0;
  background: var(--bg-card);
  z-index: 3;
}

.time-slot {
  height: 20px;
  border-top: 1px solid var(--border-light);
}

.time-slot-hour {
  height: 20px;
  border-top-color: var(--border-color);
}

.time-label {
  font-size: 10px;
  color: var(--text-muted);
  padding-right: 6px;
  display: block;
  text-align: right;
  line-height: 1;
  transform: translateY(-7px);
}

.tech-column {
  flex: 1;
  min-width: 0;
  position: relative;
  border-left: 1px solid var(--border-light);
}

.slot-row {
  height: 20px;
  border-top: 1px solid var(--border-light);
}

.slot-hour {
  border-top-color: var(--border-color);
}

/* Stats */
.day-stats {
  display: flex;
  gap: 16px;
  padding: 10px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.stat-item {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.stat-item b {
  color: var(--text-primary);
}

.stat-booked b { color: var(--primary-color); }
.stat-arrived b { color: var(--success-color); }
.stat-completed b { color: var(--text-muted); }

/* Responsive */
@media (max-width: 767px) {
  .time-header { width: 44px; }
  .time-axis { width: 44px; }
  .time-label { font-size: 9px; }
  .tech-avatar { width: 28px; height: 28px; font-size: 11px; }
  .day-stats { gap: 10px; flex-wrap: wrap; }
}
</style>

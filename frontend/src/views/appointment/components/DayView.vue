<template>
  <div class="day-view">
    <div v-if="loading" v-loading="loading" class="day-loading" />

    <template v-else-if="technicians.length === 0 && unassignedAppointments.length === 0">
      <el-empty description="当日无在职技师" />
    </template>

    <template v-else>
      <div class="unassigned-strip" v-if="unassignedAppointments.length > 0">
        <div class="strip-title">
          <span>未分配预约</span>
          <el-tag size="small" type="warning">{{ unassignedAppointments.length }}</el-tag>
        </div>
        <div class="strip-list">
          <button
            v-for="apt in unassignedAppointments"
            :key="apt.id"
            class="strip-card"
            type="button"
            @click="$emit('select-appointment', { ...apt, technicianName: '未分配' })"
          >
            <span class="strip-time">{{ formatTimeRange(apt) }}</span>
            <span class="strip-member">{{ apt.memberName }}</span>
            <span class="strip-service">{{ apt.serviceItemName }}</span>
          </button>
        </div>
      </div>

      <el-empty
        v-if="!hasAppointments"
        class="day-empty"
        description="当日暂无预约"
      />

      <div class="mobile-timeline" v-if="timelineAppointments.length > 0">
        <button
          v-for="item in timelineAppointments"
          :key="item.id"
          type="button"
          class="timeline-item"
          :class="`status-${item.status}`"
          @click="$emit('select-appointment', item)"
        >
          <div class="timeline-time">{{ formatTimeRange(item) }}</div>
          <div class="timeline-main">
            <div class="timeline-title">
              <span>{{ item.memberName }}</span>
              <el-tag size="small" :type="statusTagType(item.status)">
                {{ statusText(item.status) }}
              </el-tag>
            </div>
            <div class="timeline-meta">
              <span>{{ item.serviceItemName || '未选择项目' }}</span>
              <span>{{ item.technicianName || '未分配技师' }}</span>
            </div>
            <div v-if="item.remark" class="timeline-remark">{{ item.remark }}</div>
          </div>
        </button>
      </div>

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
              @click.stop="$emit('create-appointment', { date: currentDate, startTime: slot.label, employeeId: tech.id })"
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
  unassignedAppointments: { type: Array, default: () => [] },
  currentDate: { type: String, default: '' },
  stats: { type: Object, default: null },
  businessHours: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

defineEmits(['select-appointment', 'create-appointment', 'select-tech'])

const gridRef = ref(null)
const bodyRef = ref(null)

const SLOT_MINUTES = 30
const DEFAULT_START_HOUR = 8
const DEFAULT_END_HOUR = 21
const SLOT_HEIGHT = 32 // must match .time-slot and .slot-row height

const statusMap = { 1: '已预约', 2: '已到店', 3: '已完成', 4: '已取消', 5: '爽约' }
const statusTagMap = { 1: 'primary', 2: 'success', 3: 'info', 4: 'warning', 5: 'danger' }

const startHour = computed(() => parseHour(props.businessHours?.start, DEFAULT_START_HOUR))
const endHour = computed(() => parseHour(props.businessHours?.end, DEFAULT_END_HOUR))

const hasAppointments = computed(() => timelineAppointments.value.length > 0)

const timelineAppointments = computed(() => {
  const assigned = props.technicians.flatMap(tech =>
    (tech.appointments || []).map(apt => ({
      ...apt,
      technicianName: tech.name
    }))
  )
  const unassigned = props.unassignedAppointments.map(apt => ({
    ...apt,
    technicianName: '未分配'
  }))
  return [...assigned, ...unassigned].sort((a, b) => {
    const diff = timeToMinutes(a.startTime) - timeToMinutes(b.startTime)
    if (diff !== 0) return diff
    return (a.id || 0) - (b.id || 0)
  })
})

const timeSlots = computed(() => {
  const slots = []
  for (let h = startHour.value; h < endHour.value; h++) {
    for (let m = 0; m < 60; m += SLOT_MINUTES) {
      const label = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
      slots.push({ label, isHour: m === 0 })
    }
  }
  return slots
})

function parseHour(timeStr, fallback) {
  if (!timeStr) return fallback
  const hour = Number(String(timeStr).split(':')[0])
  return Number.isFinite(hour) ? hour : fallback
}

function timeToMinutes(timeStr) {
  if (!timeStr) return 0
  const [h, m] = timeStr.split(':').map(Number)
  return (h - startHour.value) * 60 + m
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
  const y = event.clientY - rect.top + bodyRef.value.scrollTop
  const totalMinutes = Math.floor(y / SLOT_HEIGHT) * SLOT_MINUTES
  const maxMinutes = (endHour.value - startHour.value) * 60 - SLOT_MINUTES
  const boundedMinutes = Math.max(0, Math.min(totalMinutes, maxMinutes))
  const hour = startHour.value + Math.floor(boundedMinutes / 60)
  const min = boundedMinutes % 60
  return `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`
}

function formatTimeRange(apt) {
  const start = apt.startTime?.slice(0, 5) || '--:--'
  const end = apt.endTime?.slice(0, 5) || '--:--'
  return `${start}-${end}`
}

function statusText(status) {
  return statusMap[status] || '未知'
}

function statusTagType(status) {
  return statusTagMap[status] || 'info'
}
</script>

<style scoped>
.day-view {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.day-loading {
  min-height: 300px;
}

.day-empty {
  border-bottom: 1px solid var(--border-light);
}

.unassigned-strip {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
  background: var(--warning-light);
}

.strip-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.strip-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.strip-card {
  display: grid;
  grid-template-columns: auto minmax(72px, 1fr);
  gap: 2px 8px;
  min-width: 220px;
  padding: 8px 10px;
  border: 1px solid var(--border-color);
  border-left: 3px solid var(--warning-color);
  border-radius: var(--radius-xs);
  background: var(--bg-card);
  color: var(--text-primary);
  text-align: left;
  cursor: pointer;
}

.strip-card:hover {
  box-shadow: var(--shadow-sm);
}

.strip-time {
  grid-row: span 2;
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  white-space: nowrap;
}

.strip-member,
.strip-service {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.strip-member {
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.strip-service {
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.mobile-timeline {
  display: none;
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
  height: 32px;
  border-top: 1px solid var(--border-light);
}

.time-slot-hour {
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
  height: 32px;
  border-top: 1px solid var(--border-light);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.slot-row:hover {
  background: var(--bg-primary-ghost);
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
  .day-view {
    border-radius: var(--radius-sm);
  }

  .unassigned-strip {
    padding: 10px 12px;
  }

  .strip-list {
    flex-direction: column;
    overflow: visible;
  }

  .strip-card {
    width: 100%;
    min-width: 0;
  }

  .day-grid {
    display: none;
  }

  .mobile-timeline {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 12px;
  }

  .timeline-item {
    display: grid;
    grid-template-columns: 78px minmax(0, 1fr);
    gap: 10px;
    width: 100%;
    padding: 12px;
    border: 1px solid var(--border-color);
    border-left: 4px solid var(--primary-color);
    border-radius: var(--radius-sm);
    background: var(--bg-card);
    color: var(--text-primary);
    text-align: left;
    cursor: pointer;
  }

  .timeline-item.status-2 {
    border-left-color: var(--success-color);
  }

  .timeline-item.status-3 {
    border-left-color: var(--text-muted);
  }

  .timeline-item.status-4 {
    border-left-color: var(--warning-color);
  }

  .timeline-item.status-5 {
    border-left-color: var(--danger-color);
  }

  .timeline-time {
    color: var(--text-muted);
    font-size: var(--font-size-xs);
    line-height: 1.4;
    white-space: nowrap;
  }

  .timeline-main {
    min-width: 0;
  }

  .timeline-title,
  .timeline-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-width: 0;
  }

  .timeline-title span:first-child,
  .timeline-meta span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .timeline-title {
    font-size: var(--font-size-mobile-base);
    font-weight: 600;
  }

  .timeline-meta {
    margin-top: 4px;
    color: var(--text-secondary);
    font-size: var(--font-size-mobile-sm);
  }

  .timeline-remark {
    margin-top: 6px;
    color: var(--text-muted);
    font-size: var(--font-size-xs);
    line-height: 1.4;
    word-break: break-word;
  }

  .day-stats {
    gap: 10px;
    flex-wrap: wrap;
  }
}
</style>

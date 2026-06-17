<template>
  <div class="week-view">
    <div v-if="loading" v-loading="loading" class="week-loading" />

    <template v-else-if="days.length === 0">
      <el-empty description="本周暂无预约" />
    </template>

    <template v-else>
      <div class="week-grid">
        <div
          v-for="day in days"
          :key="day.date"
          class="week-day"
          :class="{ 'is-today': day.date === today, 'is-off': day.totalCount === 0 }"
          @click="$emit('select-date', day.date)"
        >
          <div class="day-header">
            <span class="day-name">{{ day.dayOfWeek }}</span>
            <span class="day-date">{{ formatDate(day.date) }}</span>
          </div>
          <div class="day-count">
            <span class="count-number">{{ day.totalCount }}</span>
            <span class="count-label">个预约</span>
          </div>
          <div class="day-status-dots" v-if="day.totalCount > 0">
            <span v-if="day.statusCounts?.booked" class="dot dot-booked" />
            <span v-if="day.statusCounts?.arrived" class="dot dot-arrived" />
            <span v-if="day.statusCounts?.completed" class="dot dot-completed" />
            <span v-if="day.statusCounts?.cancelled" class="dot dot-cancelled" />
          </div>
          <div class="day-appointments">
            <div
              v-for="apt in day.appointments?.slice(0, 4)"
              :key="apt.id"
              class="day-apt-row"
              :class="`apt-status-${apt.status}`"
            >
              <span class="apt-time">{{ apt.startTime }}</span>
              <span class="apt-name">{{ apt.memberName }}</span>
              <span class="apt-service">{{ apt.serviceItemName }}</span>
            </div>
            <div v-if="day.appointments?.length > 4" class="day-more">
              +{{ day.appointments.length - 4 }} 条
            </div>
          </div>
        </div>
      </div>

      <div class="week-legend">
        <span class="legend-item"><span class="dot dot-booked" /> 已预约</span>
        <span class="legend-item"><span class="dot dot-arrived" /> 已到店</span>
        <span class="legend-item"><span class="dot dot-completed" /> 已完成</span>
        <span class="legend-item"><span class="dot dot-cancelled" /> 已取消</span>
        <span class="legend-item"><span class="dot dot-noshow" /> 爽约</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

defineProps({
  days: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

defineEmits(['select-date'])

const today = new Date().toISOString().slice(0, 10)

function formatDate(dateStr) {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return `${parseInt(parts[1])}/${parseInt(parts[2])}`
}
</script>

<style scoped>
.week-view {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
  border: 1px solid var(--border-color);
}

.week-loading {
  min-height: 200px;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.week-day {
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  padding: 12px 10px;
  min-width: 0;
  cursor: pointer;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
  border: 2px solid transparent;
}

.week-day:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.week-day.is-today {
  border-color: var(--primary-color);
  background: var(--bg-primary-ghost);
}

.week-day.is-off {
  opacity: 0.5;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.day-name {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.day-date {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  white-space: nowrap;
}

.day-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 6px;
}

.count-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}

.count-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.day-status-dots {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.dot-booked { background: var(--primary-color); }
.dot-arrived { background: var(--success-color); }
.dot-completed { background: var(--text-muted); }
.dot-cancelled { background: var(--warning-color); }
.dot-noshow { background: var(--danger-color); }

.day-appointments {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.day-apt-row {
  display: flex;
  gap: 4px;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 11px;
  overflow: hidden;
}

.apt-time {
  color: var(--text-muted);
  flex-shrink: 0;
}

.apt-name {
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.apt-service {
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 1;
}

.apt-status-4 .apt-name { text-decoration: line-through; color: var(--text-muted); }
.apt-status-5 .apt-name { color: var(--danger-color); }

.day-more {
  font-size: 11px;
  color: var(--primary-color);
  text-align: center;
  margin-top: 2px;
  cursor: pointer;
}

.week-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
  justify-content: center;
}

.legend-item {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 767px) {
  .week-view {
    padding: 12px;
  }

  .week-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .week-day {
    min-height: 132px;
    padding: 8px;
  }

  .day-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 2px;
  }

  .count-number {
    font-size: 22px;
  }

  .count-label,
  .apt-service {
    display: none;
  }

  .day-apt-row {
    gap: 3px;
    padding: 2px 0;
  }

  .apt-name {
    flex: 1;
  }

  .week-legend {
    justify-content: flex-start;
    gap: 8px 12px;
  }
}
</style>

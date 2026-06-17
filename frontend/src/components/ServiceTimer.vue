<template>
  <div class="service-timer" :class="{ 'timer--warn': warnLevel === 80, 'timer--danger': warnLevel === 100 }">
    <span class="timer-display">{{ display }}</span>
    <div class="timer-controls" v-if="showControls">
      <el-button v-if="status === 1" size="small" text @click="$emit('pause')">暂停</el-button>
      <el-button v-if="status === 2" size="small" text type="success" @click="$emit('resume')">继续</el-button>
      <el-button size="small" text type="primary" @click="$emit('complete')">完成</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  plannedDuration: { type: Number, default: 30 },
  startedAt: { type: String, default: '' },
  pausedAt: { type: String, default: null },
  status: { type: Number, default: 1 },
  showControls: { type: Boolean, default: false },
})

defineEmits(['pause', 'resume', 'complete', 'alert80', 'alert100'])

const now = ref(Date.now())
let timer = null

onMounted(() => { timer = setInterval(() => { now.value = Date.now() }, 1000) })
onBeforeUnmount(() => { clearInterval(timer) })

const display = computed(() => {
  if (!props.startedAt) return '--:--'
  const start = new Date(props.startedAt).getTime()
  const elapsed = Math.floor((now.value - start) / 1000)
  const remaining = Math.max(0, props.plannedDuration * 60 - elapsed)
  const sign = remaining > 0 ? '' : '+'
  const abs = Math.abs(remaining)
  const mm = String(Math.floor(abs / 60)).padStart(2, '0')
  const ss = String(abs % 60).padStart(2, '0')
  return `${sign}${mm}:${ss}`
})

const warnLevel = computed(() => {
  if (!props.startedAt || props.status !== 1) return 0
  const start = new Date(props.startedAt).getTime()
  const elapsed = Math.floor((now.value - start) / 1000)
  const total = props.plannedDuration * 60
  if (total === 0) return 0
  const pct = (elapsed / total) * 100
  if (pct >= 100) return 100
  if (pct >= 80) return 80
  return 0
})
</script>

<style scoped>
.service-timer {
  display: flex;
  align-items: center;
  gap: 8px;
}
.timer-display {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}
.timer--warn .timer-display {
  color: var(--warning-color);
  animation: pulse-warn 1s ease-in-out infinite;
}
.timer--danger .timer-display {
  color: var(--danger-color);
  animation: pulse-danger 0.5s ease-in-out infinite;
}
.timer-controls {
  display: flex;
  gap: 4px;
}
@keyframes pulse-warn {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
@keyframes pulse-danger {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>

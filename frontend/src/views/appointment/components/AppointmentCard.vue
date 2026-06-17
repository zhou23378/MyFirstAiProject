<template>
  <div
    class="appointment-card"
    :class="`status-${appointment.status}`"
    @click="$emit('click', appointment)"
  >
    <div class="card-status-bar" />
    <div class="card-body">
      <div class="card-member">{{ appointment.memberName }}</div>
      <div class="card-service">{{ appointment.serviceItemName }}</div>
      <div v-if="appointment.remark" class="card-remark">{{ appointment.remark }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  appointment: { type: Object, required: true }
})

defineEmits(['click'])
</script>

<style scoped>
.appointment-card {
  position: absolute;
  left: 2px;
  right: 2px;
  border-radius: var(--radius-xs);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
  z-index: 2;
  display: flex;
  min-height: 28px;
}

.appointment-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
  z-index: 10;
}

.card-status-bar {
  width: 3px;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  padding: 3px 6px;
  min-width: 0;
}

.card-member {
  font-size: var(--font-size-sm);
  font-weight: 600;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-service {
  font-size: var(--font-size-xs);
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-remark {
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1.2;
  margin-top: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Status colors */
.status-1 { background: var(--bg-primary-ghost); }
.status-1 .card-status-bar { background: var(--primary-color); }
.status-1 .card-member { color: var(--primary-color); }

.status-2 { background: var(--bg-success-ghost); }
.status-2 .card-status-bar { background: var(--success-color); }
.status-2 .card-member { color: var(--success-color); }

.status-3 { background: var(--bg-disabled); }
.status-3 .card-status-bar { background: var(--text-muted); }
.status-3 .card-member { color: var(--text-secondary); }

.status-4 { background: var(--warning-light); }
.status-4 .card-status-bar { background: var(--warning-color); }
.status-4 .card-member { color: var(--warning-color); text-decoration: line-through; }

.status-5 { background: var(--danger-light); }
.status-5 .card-status-bar { background: var(--danger-color); }
.status-5 .card-member { color: var(--danger-color); }
</style>

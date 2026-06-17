<template>
  <div class="skeleton-table">
    <div v-for="row in rows" :key="row" class="skeleton-row" :style="{ animationDelay: `${row * 0.05}s` }">
      <div v-for="col in cols" :key="col" class="skeleton-cell" :style="{ width: randomWidth(col, row) }">
        <div class="skeleton-bar" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  rows: { type: Number, default: 5 },
  cols: { type: Number, default: 4 }
})

const widths = ['60%', '75%', '55%', '90%', '65%', '80%', '50%', '70%']
function randomWidth(col, row) {
  return widths[(col * 3 + row * 7) % widths.length]
}
</script>

<style scoped>
.skeleton-table {
  padding: 8px 0;
}
.skeleton-row {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}
.skeleton-cell {
  flex: 1;
  min-width: 40px;
}
.skeleton-bar {
  height: 16px;
  border-radius: 4px;
  background: var(--skeleton-shimmer);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>

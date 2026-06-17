<template>
  <Teleport to="body">
    <div v-if="visible" class="command-overlay" @click.self="close">
      <div class="command-panel">
        <div class="command-input-wrap">
          <el-icon class="command-search-icon"><Search /></el-icon>
          <input
            ref="inputRef"
            v-model="query"
            class="command-input"
            placeholder="搜索页面、功能..."
            @keydown="onKeydown"
          />
          <kbd class="command-kbd">Esc</kbd>
        </div>
        <div class="command-results" v-if="filtered.length > 0">
          <div
            v-for="(item, idx) in filtered"
            :key="item.index"
            class="command-item"
            :class="{ active: idx === selectedIdx }"
            @click="navigate(item)"
            @mouseenter="selectedIdx = idx"
          >
            <el-icon class="command-item-icon"><component :is="item.icon" /></el-icon>
            <span class="command-item-title">{{ item.title }}</span>
            <span class="command-item-group">{{ item.group }}</span>
          </div>
        </div>
        <div v-else class="command-empty">未找到匹配的页面</div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  items: { type: Array, default: () => [] },
})

const emit = defineEmits(['close'])

const router = useRouter()
const query = ref('')
const selectedIdx = ref(0)
const inputRef = ref(null)

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return props.items
  return props.items.filter(
    item =>
      item.title.toLowerCase().includes(q) ||
      item.group.toLowerCase().includes(q)
  )
})

function onKeydown(e) {
  if (e.key === 'Escape') {
    close()
  } else if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIdx.value = Math.min(selectedIdx.value + 1, filtered.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIdx.value = Math.max(selectedIdx.value - 1, 0)
  } else if (e.key === 'Enter') {
    e.preventDefault()
    if (filtered.value[selectedIdx.value]) {
      navigate(filtered.value[selectedIdx.value])
    }
  }
}

function navigate(item) {
  router.push(item.index)
  close()
}

function close() {
  emit('close')
}

watch(() => props.visible, (v) => {
  if (v) {
    query.value = ''
    selectedIdx.value = 0
    nextTick(() => inputRef.value?.focus())
  }
})
</script>

<style scoped>
.command-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  padding-top: 15vh;
}

.command-panel {
  width: 480px;
  max-width: calc(100vw - 40px);
  max-height: 420px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-subtle);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.command-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.command-search-icon {
  color: var(--text-muted);
  font-size: 18px;
  flex-shrink: 0;
}

.command-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
  color: var(--text-primary);
  min-width: 0;
}

.command-input::placeholder {
  color: var(--text-muted);
}

.command-kbd {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--bg-page);
  color: var(--text-muted);
  border: 1px solid var(--border-subtle);
  font-family: inherit;
  flex-shrink: 0;
}

.command-results {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.command-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.command-item.active {
  background: var(--bg-primary-ghost);
}

.command-item-icon {
  color: var(--text-muted);
  font-size: 16px;
  flex-shrink: 0;
}

.command-item-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.command-item-group {
  font-size: 12px;
  color: var(--text-muted);
}

.command-empty {
  padding: 32px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
}
</style>

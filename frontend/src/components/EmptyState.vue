<template>
  <div class="empty-state">
    <el-icon :size="56" class="empty-icon" :class="{ 'is-error': type === 'error' }">
      <component :is="currentIcon" />
    </el-icon>
    <p class="empty-text">{{ description }}</p>
    <el-button v-if="type === 'error'" type="primary" size="small" class="retry-btn" @click="$emit('retry')">
      重试
    </el-button>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { FolderOpened, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  description: { type: String, default: '暂无数据' },
  icon: { type: Object, default: undefined },
  type: { type: String, default: 'empty', validator: v => ['empty', 'error'].includes(v) }
})

defineEmits(['retry'])

const currentIcon = computed(() => {
  if (props.icon) return props.icon
  return props.type === 'error' ? WarningFilled : FolderOpened
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
}
.empty-icon {
  color: var(--text-muted);
  opacity: 0.35;
}
.empty-icon.is-error {
  color: var(--warning-color);
  opacity: 0.6;
}
.empty-text {
  margin-top: 12px;
  color: var(--text-muted);
  font-size: 14px;
}
.retry-btn {
  margin-top: 16px;
}
</style>

<template>
  <div class="drawer-filter" :class="{ 'is-mobile': isMobile }">
    <!-- 桌面端 — 行内筛选栏 -->
    <div v-if="!isMobile" class="filter-inline">
      <slot name="filters" />
      <div v-if="$slots.actions" class="filter-actions">
        <slot name="actions" />
      </div>
    </div>

    <!-- 移动端 — 筛选按钮 + 底部抽屉 -->
    <div v-else class="filter-mobile">
      <div class="filter-toolbar">
        <slot name="toolbar" />
        <el-button
          class="filter-toggle-btn"
          :class="{ 'has-active': hasActiveFilters }"
          @click="drawerVisible = true"
        >
          <el-icon><Filter /></el-icon>
          <span>筛选</span>
          <span v-if="activeCount > 0" class="filter-badge">{{ activeCount }}</span>
        </el-button>
      </div>

      <el-drawer
        v-model="drawerVisible"
        direction="btt"
        :size="drawerHeight"
        :with-header="true"
        class="filter-drawer"
      >
        <template #header>
          <div class="drawer-header">
            <span class="drawer-title">筛选条件</span>
            <el-button text type="primary" @click="onReset">重置</el-button>
          </div>
        </template>
        <div class="drawer-body">
          <slot name="filters" />
        </div>
        <template #footer>
          <div class="drawer-footer">
            <el-button class="confirm-btn" type="primary" @click="onConfirm">确认</el-button>
          </div>
        </template>
      </el-drawer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { Filter } from "@element-plus/icons-vue";

const props = defineProps({
  breakpoint: { type: Number, default: 768 },
  hasActiveFilters: { type: Boolean, default: false },
  activeCount: { type: Number, default: 0 },
  drawerHeight: { type: String, default: "60%" },
});

const emit = defineEmits(["reset", "confirm"]);

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < props.breakpoint);
const drawerVisible = ref(false);

function onResize() {
  windowWidth.value = window.innerWidth;
}

onMounted(() => window.addEventListener("resize", onResize));
onUnmounted(() => window.removeEventListener("resize", onResize));

function onReset() {
  emit("reset");
}

function onConfirm() {
  drawerVisible.value = false;
  emit("confirm");
}
</script>

<style scoped>
/* ========== 桌面 — 行内筛选 ========== */
.filter-inline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

/* ========== 移动 — 筛选按钮 ========== */
.filter-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.filter-toolbar > :first-child {
  flex: 1;
}

.filter-toggle-btn {
  flex-shrink: 0;
  height: 36px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-mobile-sm);
  position: relative;
}

.filter-toggle-btn.has-active {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.filter-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  background: var(--danger-color);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

/* ========== 抽屉 ========== */
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.drawer-title {
  font-size: 16px;
  font-weight: 600;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.drawer-footer {
  padding: 12px 0;
}

.confirm-btn {
  width: 100%;
}
</style>

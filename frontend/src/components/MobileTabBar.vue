<template>
  <nav class="mobile-tab-bar">
    <button
      v-for="tab in tabs"
      :key="tab.path"
      class="tab-item"
      :class="{ active: isActive(tab) }"
      @click="onTabClick(tab)"
    >
      <el-icon class="tab-icon"><component :is="tab.icon" /></el-icon>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<script setup>
import { useRoute, useRouter } from "vue-router";
import { HomeFilled, UserFilled, ShoppingCart, Calendar, MoreFilled } from "@element-plus/icons-vue";

const emit = defineEmits(["toggle-menu"]);
const route = useRoute();
const router = useRouter();

const tabs = [
  { path: "/", label: "首页", icon: HomeFilled, match: () => route.path === "/" },
  { path: "/member", label: "会员", icon: UserFilled, match: () => route.path.startsWith("/member") },
  { path: "/consumption", label: "收银", icon: ShoppingCart, match: () => route.path.startsWith("/consumption") },
  { path: "/appointment", label: "预约", icon: Calendar, match: () => route.path.startsWith("/appointment") },
  { path: "#more", label: "更多", icon: MoreFilled, match: () => false },
];

function isActive(tab) {
  return tab.match();
}

function onTabClick(tab) {
  if (tab.path === "#more") {
    emit("toggle-menu");
  } else {
    router.push(tab.path);
  }
}
</script>

<style scoped>
.mobile-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--mobile-bottom-bar-height);
  padding-bottom: var(--safe-area-bottom);
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  -webkit-tap-highlight-color: transparent;
  min-height: var(--touch-target-min);
  padding: 4px 0;
}

.tab-item:active {
  transform: scale(0.95);
}

.tab-item.active {
  color: var(--primary-color);
}

.tab-icon {
  font-size: 20px;
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
}
</style>

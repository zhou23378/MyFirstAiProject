<template>
  <div class="customer-layout">
    <header class="customer-topbar">
      <button v-if="showBack" class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <span class="topbar-title">{{ title }}</span>
      <div class="topbar-spacer" />
    </header>

    <main class="customer-content">
      <router-view />
    </main>

    <nav class="customer-tab-bar">
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Calendar, UserFilled, Coin, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const tabs = [
  { path: '/h5', label: '首页', icon: HomeFilled },
  { path: '/h5/booking', label: '预约', icon: Calendar },
  { path: '/h5/points-mall', label: '积分', icon: Coin },
  { path: '/h5/group-buy', label: '拼团', icon: ShoppingCart },
  { path: '/h5/profile', label: '我的', icon: UserFilled }
]

const showBack = computed(() => route.path !== '/h5' && route.path !== '/h5/login')

const title = computed(() => {
  const meta = route.meta
  return meta?.title || '美发沙龙'
})

function isActive(tab) {
  if (tab.path === '/h5') return route.path === '/h5'
  return route.path.startsWith(tab.path)
}

function onTabClick(tab) {
  router.push(tab.path)
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.customer-layout {
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.customer-topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 0 12px;
  flex-shrink: 0;
}

.back-btn {
  position: absolute;
  left: 12px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  min-height: var(--touch-target-min);
}

.topbar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.topbar-spacer {
  width: 32px;
}

.customer-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 64px;
}

.customer-tab-bar {
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

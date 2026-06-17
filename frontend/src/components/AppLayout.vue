<template>
  <div class="layout" :class="{ 'layout--mobile': isMobile, 'layout--tablet': isTablet }">
    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && mobileMenuOpen" class="sidebar-overlay" @click="closeMobileMenu" />

    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{
      collapsed: isCollapsed && !isMobile,
      'sidebar--mobile-open': isMobile && mobileMenuOpen
    }">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <span v-show="!isCollapsed || isMobile" class="sidebar-title">Salon Manager</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed && !isMobile"
        :router="true"
        class="sidebar-menu"
        background-color="transparent"
        text-color="var(--text-sidebar)"
        active-text-color="var(--text-sidebar-active)"
        @select="onMenuItemSelect"
      >
        <el-sub-menu
          v-for="group in menuGroups"
          :key="group.label"
          :index="`_group_${group.label}`"
        >
          <template #title>
            <el-icon><component :is="group.icon" /></el-icon>
            <span v-show="!isCollapsed || isMobile">{{ group.label }}</span>
          </template>
          <el-menu-item v-for="item in group.items" :key="item.index" :index="item.index">
            {{ item.title }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item
          v-for="item in topLevelItems"
          :key="item.index"
          :index="item.index"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button text class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
        </el-button>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main-area" :class="{ expanded: isCollapsed }">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button v-if="isMobile" class="hamburger-btn" @click="toggleMobileMenu" :aria-label="mobileMenuOpen ? '关闭菜单' : '打开菜单'">
            <span class="hamburger-line" :class="{ open: mobileMenuOpen }" />
          </button>
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="topbar-right">
          <button class="search-trigger" @click="commandVisible = true">
            <el-icon class="search-trigger-icon"><Search /></el-icon>
            <span class="search-trigger-text">搜索</span>
            <kbd class="search-trigger-kbd">Ctrl+K</kbd>
          </button>
          <el-button
            circle
            class="theme-toggle"
            :icon="isDark ? Sunny : Moon"
            @click="toggleTheme"
          />
          <el-popover
            placement="bottom-end"
            :width="360"
            trigger="click"
            @show="loadNotifications"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
                <el-button circle :icon="Bell" class="bell-btn" />
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <span class="notification-title">通知中心</span>
                <el-button v-if="unreadCount > 0" text type="primary" size="small" @click="markAllRead">全部已读</el-button>
              </div>
              <div class="notification-list" v-if="notifications.length > 0">
                <div
                  v-for="item in notifications"
                  :key="item.id"
                  class="notification-item"
                  :class="{ unread: item.isRead === 0 }"
                  @click="markRead(item)"
                >
                  <div class="notification-dot" :class="item.status === 1 ? 'success' : item.status === 2 ? 'danger' : 'info'" />
                  <div class="notification-body">
                    <div class="notification-text">{{ item.content || item.type }}</div>
                    <div class="notification-meta">
                      <span class="notification-type">{{ typeLabel(item.type) }}</span>
                      <span class="notification-time">{{ formatTime(item.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <EmptyState v-else description="暂无通知" />
            </div>
          </el-popover>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">A</el-avatar>
              <span class="user-name">管理员</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>

    <!-- 移动端底部标签栏 -->
    <MobileTabBar v-if="isMobile" @toggle-menu="toggleMobileMenu" />

    <!-- 全局命令面板 -->
    <CommandPalette
      :visible="commandVisible"
      :items="commandItems"
      @close="commandVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";
import MobileTabBar from "./MobileTabBar.vue";
import EmptyState from "./EmptyState.vue";
import CommandPalette from "./CommandPalette.vue";
import { notificationApi } from "../api/notification";
import { menuConfigApi } from "../api/menuConfig";
import { allMenuItems as menuItemsDef, menuGroupDefs } from "../config/menuItems";
import {
  Fold,
  Expand,
  ArrowDown,
  User,
  SwitchButton,
  Bell,
  Sunny,
  Moon,
  Search,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isCollapsed = ref(false);
const windowWidth = ref(window.innerWidth);
const mobileMenuOpen = ref(false);

const isMobile = computed(() => windowWidth.value < 768);
const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 1024);

function onResize() {
  windowWidth.value = window.innerWidth;
  // 平板自动折叠
  if (isTablet.value) {
    isCollapsed.value = true;
  } else if (!isMobile.value) {
    isCollapsed.value = false;
  }
  // 移动端菜单关闭时不做操作
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value;
}

function closeMobileMenu() {
  mobileMenuOpen.value = false;
}

function onMenuItemSelect() {
  if (isMobile.value) {
    closeMobileMenu();
  }
}

onMounted(() => {
  window.addEventListener("resize", onResize);
  onResize();
  loadMenuConfig();
});

onUnmounted(() => {
  window.removeEventListener("resize", onResize);
});

const menuPermissions = {
  admin: ['/', '/member', '/service', '/consumption', '/orders', '/employee', '/member-level', '/appointment', '/report', '/inventory', '/schedule', '/coupon', '/admin', '/technician-status', '/service-queue', '/daily-close', '/marketing/tag-rules', '/commission', '/points', '/group-buy', '/menu-config', '/quick-actions-config', '/stat-cards-config'],
  manager: ['/', '/member', '/service', '/consumption', '/orders', '/employee', '/member-level', '/appointment', '/report', '/inventory', '/schedule', '/coupon', '/technician-status', '/service-queue', '/daily-close', '/marketing/tag-rules', '/commission', '/points', '/group-buy'],
  technician: ['/', '/appointment', '/schedule', '/member', '/technician-status'],
  cashier: ['/', '/member', '/consumption', '/orders', '/coupon', '/service-queue', '/points']
};

const visibleMenus = computed(() => {
  return menuPermissions[authStore.role] || menuPermissions.cashier;
});

const allMenuItems = ref([...menuItemsDef])

async function loadMenuConfig() {
  try {
    const configs = await menuConfigApi.list()
    if (configs && configs.length > 0) {
      const map = Object.fromEntries(configs.map(c => [c.menuIndex, c.groupName]))
      allMenuItems.value.forEach(item => {
        if (map[item.index]) item.group = map[item.index]
      })
    }
  } catch { /* API 失败则使用硬编码默认值 */ }
}

const menuGroups = computed(() => {
  const visibleSet = new Set(visibleMenus.value)
  return menuGroupDefs
    .map(g => ({
      ...g,
      items: allMenuItems.value.filter(item => item.group === g.label && visibleSet.has(item.index))
    }))
    .filter(g => g.items.length > 0)
})

const topLevelItems = computed(() => {
  const groupedLabels = new Set(menuGroupDefs.map(g => g.label))
  return allMenuItems.value.filter(
    item => !groupedLabels.has(item.group) && visibleMenus.value.includes(item.index)
  )
})

const pageTitle = computed(() => {
  const map = {
    "/": "仪表盘",
    "/member": "会员管理",
    "/service": "服务项目",
    "/consumption": "消费收银",
    "/employee": "员工管理",
    "/member-level": "会员等级",
    "/appointment": "预约管理",
    "/report": "营业报表",
    "/inventory": "库存管理",
    "/schedule": "排班考勤",
    "/coupon": "优惠券管理",
    "/technician-status": "技师看板",
    "/service-queue": "轮牌排队",
    "/daily-close": "财务日结",
    "/marketing/tag-rules": "标签规则",
    "/marketing/birthday-config": "生日营销",
    "/commission": "提成结算",
    "/points": "积分商城",
    "/group-buy": "拼团活动",
    "/admin": "系统管理",
    "/orders": "消费订单",
    "/menu-config": "菜单配置",
    "/quick-actions-config": "快捷入口配置",
    "/stat-cards-config": "统计卡片配置",
  };
  return map[route.path] || "Salon Manager";
});

const unreadCount = ref(0);
const notifications = ref([]);
let unreadTimer = null;

const typeLabels = {
  BIRTHDAY: "生日祝福", APPOINTMENT_REMINDER: "预约提醒", PAYMENT: "支付通知",
  RECHARGE: "充值通知", MARKETING: "营销推送", STOCK_ALERT: "库存告警",
};

function typeLabel(type) {
  return typeLabels[type] || type || "系统通知";
}

function formatTime(t) {
  if (!t) return "";
  const d = new Date(t);
  const now = new Date();
  const diff = now - d;
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return Math.floor(diff / 60000) + "分钟前";
  if (diff < 86400000) return Math.floor(diff / 3600000) + "小时前";
  return d.toLocaleDateString("zh-CN");
}

async function loadUnreadCount() {
  try {
    const count = await notificationApi.unreadCount();
    unreadCount.value = count || 0;
  } catch { /* ignore */ }
}

async function loadNotifications() {
  try {
    const page = await notificationApi.list(1, 10);
    notifications.value = page?.list || [];
  } catch { /* ignore */ }
}

async function markRead(item) {
  if (item.isRead === 0) {
    try {
      await notificationApi.markRead(item.id);
      item.isRead = 1;
      if (unreadCount.value > 0) unreadCount.value--;
    } catch { /* ignore */ }
  }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead();
    unreadCount.value = 0;
    notifications.value.forEach(n => n.isRead = 1);
  } catch { /* ignore */ }
}

const isDark = ref(false);

function applyTheme() {
  if (isDark.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  applyTheme()
}

const commandVisible = ref(false);

const commandItems = computed(() => {
  return allMenuItems.value
    .filter(item => visibleMenus.value.includes(item.index))
    .map(item => ({ ...item, group: item.group || '' }))
})

function onKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    commandVisible.value = true
  }
}

onMounted(() => {
  isDark.value = localStorage.getItem('theme') === 'dark'
  applyTheme()
  loadUnreadCount();
  unreadTimer = setInterval(loadUnreadCount, 30000);
  window.addEventListener('keydown', onKeydown);
});

onUnmounted(() => {
  clearInterval(unreadTimer);
  window.removeEventListener('keydown', onKeydown);
});

function handleCommand(command) {
  if (command === "logout") {
    authStore.logout();
    router.push("/login");
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  background: var(--bg-main);
}

/* ========== 侧边栏遮罩 ========== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 150;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ========== 侧边栏 ========== */
.sidebar {
  width: 240px;
  background: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 100;
  transition: width var(--transition-normal);
  overflow-x: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.03);
}

.sidebar.collapsed {
  width: 64px;
}

/* 侧边栏头部 */
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  min-height: 68px;
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  min-width: 36px;
  background: var(--primary-gradient);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.3);
}

.sidebar-logo svg {
  width: 20px;
  height: 20px;
  color: var(--text-light);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-light);
  white-space: nowrap;
  letter-spacing: 0.5px;
}

/* 侧边栏菜单 */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none !important;
  padding: 12px 8px;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  border-radius: 10px !important;
  margin: 2px 0;
  height: 44px;
  line-height: 44px;
  transition: all var(--transition-fast);
  position: relative;
  color: var(--text-sidebar) !important;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background: var(--bg-sidebar-hover) !important;
  color: var(--text-sidebar-active) !important;
}

.sidebar-menu .el-menu-item.is-active,
.sidebar-menu .el-sub-menu.is-opened .el-sub-menu__title {
  color: var(--text-sidebar-active) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: var(--bg-sidebar-active) !important;
  box-shadow: 0 0 20px rgba(108, 92, 231, 0.15);
}

/* 子菜单内部容器 */
.sidebar-menu .el-sub-menu .el-menu {
  background: transparent !important;
}

/* 子菜单内的菜单项略微向右缩进 */
.sidebar-menu .el-sub-menu .el-menu-item {
  padding-left: 56px !important;
}

/* 菜单选中指示条 */
.sidebar-menu .el-menu-item.is-active::before {
  content: "";
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-light) 100%);
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px rgba(108, 92, 231, 0.5);
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 8px;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  display: flex;
  justify-content: center;
}

.collapse-btn {
  width: 100%;
  height: 36px;
  border-radius: 10px !important;
  color: var(--text-sidebar) !important;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--bg-sidebar-hover) !important;
  color: var(--text-sidebar-active) !important;
}

/* ========== 主区域 ========== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 顶栏 ========== */
.topbar {
  height: 64px;
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

/* ========== 汉堡按钮 ========== */
.hamburger-btn {
  display: none;
  width: 36px;
  height: 36px;
  padding: 8px 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.hamburger-btn:hover {
  background: rgba(108, 92, 231, 0.08);
}

.hamburger-line,
.hamburger-line::before,
.hamburger-line::after {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--text-primary);
  border-radius: 2px;
  transition: all 0.3s ease;
  position: relative;
}

.hamburger-line::before,
.hamburger-line::after {
  content: "";
  position: absolute;
  left: 0;
}

.hamburger-line::before {
  top: -6px;
}

.hamburger-line::after {
  top: 6px;
}

.hamburger-line.open {
  background: transparent;
}

.hamburger-line.open::before {
  top: 0;
  transform: rotate(45deg);
}

.hamburger-line.open::after {
  top: 0;
  transform: rotate(-45deg);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: all var(--transition-fast);
}

.user-info:hover {
  background: rgba(108, 92, 231, 0.06);
}

.user-avatar {
  background: var(--primary-gradient) !important;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-icon {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 搜索触发器 */
.search-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 10px;
  width: 180px;
  flex-shrink: 0;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  background: var(--bg-page);
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
}

.search-trigger:hover {
  border-color: var(--primary-light);
  color: var(--text-primary);
  background: var(--bg-card);
}

.search-trigger-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.search-trigger-text {
  flex: 1;
  text-align: left;
}

.search-trigger-kbd {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--bg-overlay-subtle);
  border: 1px solid var(--border-subtle);
  font-family: inherit;
  line-height: 1.4;
  flex-shrink: 0;
}

.search-trigger:hover .search-trigger-kbd {
  border-color: var(--primary-light);
  color: var(--primary-color);
}

@media (max-width: 1023px) {
  .search-trigger {
    width: 36px;
    padding: 0;
    justify-content: center;
  }
  .search-trigger-text,
  .search-trigger-kbd {
    display: none;
  }
}

/* 通知铃铛 */
.notification-badge {
  margin-right: 8px;
}

.theme-toggle,
.bell-btn {
  border: none;
  background: transparent;
  font-size: 20px;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.theme-toggle:hover,
.bell-btn:hover {
  color: var(--primary-color);
}

.notification-panel {
  max-height: 360px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 8px;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.notification-list {
  display: flex;
  flex-direction: column;
  max-height: 280px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.notification-item:hover {
  background: rgba(108, 92, 231, 0.04);
}

.notification-item.unread {
  background: rgba(108, 92, 231, 0.06);
}

.notification-item.unread:hover {
  background: rgba(108, 92, 231, 0.1);
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
}

.notification-dot.success { background: var(--success-color); }
.notification-dot.danger { background: var(--danger-color); }
.notification-dot.info { background: var(--warning-color); }

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.4;
  word-break: break-all;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.notification-type {
  font-size: 11px;
  color: var(--primary-color);
  background: rgba(108, 92, 231, 0.08);
  padding: 1px 6px;
  border-radius: 8px;
}

.notification-time {
  font-size: 11px;
  color: var(--text-muted);
}

/* ========== 内容区 ========== */
.content {
  flex: 1;
  overflow-y: auto;
  padding: var(--content-padding-desktop);
  animation: fadeInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1) both;
}

/* ========================================
   响应式 — 平板 (768px - 1023px)
   侧边栏自动折叠为图标模式
   ======================================== */
@media (max-width: 1023px) {
  .sidebar {
    width: 64px;
  }

  .sidebar:not(.sidebar--mobile-open) .sidebar-title {
    display: none;
  }

  .sidebar-header {
    padding: 20px 14px;
    justify-content: center;
  }

  /* 平板和手机都不显示折叠按钮 */
  .sidebar-footer {
    display: none !important;
  }

  .topbar {
    padding: 0 20px;
  }

  .content {
    padding: 20px;
  }
}

/* ========================================
   响应式 — 手机 (< 768px)
   侧边栏变为抽屉式浮层，底部标签栏导航
   ======================================== */
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 200;
    width: 260px;
    transform: translateX(-100%);
    transition: transform var(--transition-normal);
    box-shadow: var(--shadow-lg);
  }

  .sidebar--mobile-open {
    transform: translateX(0);
  }

  .hamburger-btn {
    display: flex;
  }

  .topbar {
    height: var(--mobile-header-height);
    min-height: var(--mobile-header-height);
    padding: 0 16px;
  }

  .page-title {
    font-size: 16px;
  }

  .user-name {
    display: none;
  }

  .user-info {
    padding: 4px;
  }

  .content {
    padding: var(--content-padding-mobile);
    padding-bottom: calc(var(--content-padding-mobile) + var(--mobile-bottom-bar-height) + var(--safe-area-bottom));
  }
}
</style>

<template>
  <div class="page-header" :class="{ 'is-mobile': isMobile }">
    <div class="page-header-top">
      <el-breadcrumb v-if="breadcrumb.length > 0" separator="/">
        <el-breadcrumb-item v-for="(item, idx) in breadcrumb" :key="idx" :to="item.path">
          {{ item.label }}
        </el-breadcrumb-item>
      </el-breadcrumb>
      <div class="page-header-main">
        <div class="page-header-left">
          <el-button
            v-if="isMobile && showBack"
            class="back-btn"
            text
            @click="goBack"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h2 class="page-title">{{ title }}</h2>
        </div>
        <div v-if="$slots.extra" class="page-header-right">
          <slot name="extra" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";

const props = defineProps({
  title: { type: String, required: true },
  showBack: { type: Boolean, default: undefined },
  breakpoint: { type: Number, default: 768 },
  breadcrumb: { type: Array, default: () => [] },
});

const route = useRoute();
const router = useRouter();

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < props.breakpoint);

const showBack = computed(() => {
  if (props.showBack !== undefined) return props.showBack;
  return route.path !== "/" && route.path !== "";
});

function onResize() {
  windowWidth.value = window.innerWidth;
}

onMounted(() => window.addEventListener("resize", onResize));
onUnmounted(() => window.removeEventListener("resize", onResize));

function goBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push("/");
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header-top {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-header-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

/* 面包屑 */
:deep(.el-breadcrumb) {
  font-size: var(--font-size-sm);
}

:deep(.el-breadcrumb__inner) {
  color: var(--text-muted);
  font-weight: 400;
  transition: color var(--transition-fast);
}

:deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--primary-color);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-secondary);
}

:deep(.el-breadcrumb__separator) {
  color: var(--text-muted);
  margin: 0 6px;
}

.back-btn {
  padding: 6px;
  color: var(--text-secondary);
  min-width: var(--touch-target-min);
  min-height: var(--touch-target-min);
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn .el-icon {
  font-size: 20px;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ========== 移动端 ========== */
@media (max-width: 767px) {
  .page-header {
    margin-bottom: 14px;
  }

  .page-title {
    font-size: 17px;
  }
}
</style>

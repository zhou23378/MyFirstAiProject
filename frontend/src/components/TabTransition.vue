<template>
  <Transition name="tab-fade" mode="out-in">
    <div :key="tabKey" class="tab-content">
      <slot />
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  tabKey: { type: String, required: true }
})
</script>

<style scoped>
.tab-content {
  overflow: hidden;
}
.tab-fade-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.tab-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.tab-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

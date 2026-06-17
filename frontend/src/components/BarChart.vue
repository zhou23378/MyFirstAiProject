<template>
  <div ref="chartRef" class="echart-container" :style="{ height: height }" />
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from "vue";
import * as echarts from "echarts";

const props = defineProps({
  option: { type: Object, required: true },
  autoresize: { type: Boolean, default: true },
  height: { type: String, default: "300px" },
});

const chartRef = ref(null);
let chart = null;
let resizeObserver = null;

function initChart() {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value);
  chart.setOption(props.option || {});
  if (props.autoresize) {
    resizeObserver = new ResizeObserver(() => {
      chart?.resize();
    });
    resizeObserver.observe(chartRef.value);
  }
}

watch(() => props.option, (opt) => {
  chart?.setOption(opt, true);
}, { deep: true });

onMounted(() => {
  initChart();
});

onUnmounted(() => {
  resizeObserver?.disconnect();
  chart?.dispose();
});
</script>

<style scoped>
.echart-container {
  width: 100%;
}
</style>

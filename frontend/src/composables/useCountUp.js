import { ref, onBeforeUnmount } from "vue";

export function useCountUp(duration = 1200) {
  const display = ref("0");
  let raf = null;

  function animate(from, to, fmt = (v) => String(v)) {
    if (raf) cancelAnimationFrame(raf);
    const start = performance.now();
    const diff = to - from;

    function tick(now) {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = from + diff * eased;
      display.value = fmt(current);
      if (progress < 1) {
        raf = requestAnimationFrame(tick);
      }
    }
    raf = requestAnimationFrame(tick);
  }

  function animateNumber(target, options = {}) {
    const num = Number(target) || 0;
    const { prefix = "", suffix = "", decimals = 0 } = options;
    const fmt = (v) => {
      const fixed = v.toFixed(decimals);
      return prefix + fixed + suffix;
    };
    animate(0, num, fmt);
  }

  function animateMoney(target) {
    const num = Number(target) || 0;
    animate(0, num, (v) => `¥${Math.round(v)}`);
  }

  onBeforeUnmount(() => {
    if (raf) cancelAnimationFrame(raf);
  });

  return { display, animateNumber, animateMoney };
}

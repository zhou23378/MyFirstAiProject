<template>
  <div class="login-page">
    <!-- 动态粒子背景 -->
    <div class="particles">
      <div
        v-for="(p, i) in particles"
        :key="i"
        class="particle"
        :style="{
          left: p.left,
          width: p.width,
          height: p.height,
          animationDelay: p.animationDelay,
          animationDuration: p.animationDuration,
          opacity: p.opacity,
        }"
      />
    </div>

    <!-- 装饰圆 -->
    <div class="deco-circle deco-circle-1" />
    <div class="deco-circle deco-circle-2" />
    <div class="deco-circle deco-circle-3" />

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <h1 class="login-title">Salon Manager</h1>
        <p class="login-subtitle">美发沙龙管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? "登录中..." : "登 录" }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);
const loading = ref(false);

const particles = Array.from({ length: 20 }, () => ({
  left: `${Math.random() * 100}%`,
  width: `${Math.random() * 6 + 2}px`,
  height: `${Math.random() * 6 + 2}px`,
  animationDelay: `${Math.random() * 8}s`,
  animationDuration: `${Math.random() * 6 + 4}s`,
  opacity: Math.random() * 0.5 + 0.2,
}));

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await authStore.login(form);
    router.push("/");
  } catch {
    ElMessage.error("用户名或密码错误");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  overflow-x: hidden;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-login);
  position: relative;
  overflow: hidden;
}

/* ========== 动态粒子 ========== */
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  bottom: -10px;
  background: rgba(162, 155, 254, 0.6);
  border-radius: 50%;
  animation: particleRise linear infinite;
}

@keyframes particleRise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) scale(0);
    opacity: 0;
  }
}

/* ========== 装饰圆 ========== */
.deco-circle {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}

.deco-circle-1 {
  width: 400px;
  height: 400px;
  background: rgba(108, 92, 231, 0.15);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.deco-circle-2 {
  width: 300px;
  height: 300px;
  background: rgba(0, 184, 148, 0.1);
  bottom: -50px;
  left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.deco-circle-3 {
  width: 200px;
  height: 200px;
  background: rgba(253, 203, 110, 0.08);
  top: 50%;
  left: 60%;
  animation: float 6s ease-in-out infinite;
}

/* ========== 登录卡片 ========== */
.login-card {
  width: 420px;
  max-width: calc(100vw - 32px);
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.05) inset;
  position: relative;
  z-index: 1;
  animation: scaleIn 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ========== 头部 ========== */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: var(--primary-gradient);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(108, 92, 231, 0.4);
  animation: float 3s ease-in-out infinite;
}

.logo-icon svg {
  width: 28px;
  height: 28px;
  color: var(--text-light);
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-light);
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.login-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 2px;
}

/* ========== 表单 ========== */
.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  border-radius: 12px !important;
  height: 48px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(162, 155, 254, 0.4) inset !important;
  background: rgba(255, 255, 255, 0.12) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(108, 92, 231, 0.6) inset !important;
  background: rgba(255, 255, 255, 0.15) !important;
}

.login-form :deep(.el-input__inner) {
  color: var(--text-light) !important;
  font-size: 15px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35) !important;
}

.login-form :deep(.el-input__prefix-inner) {
  color: rgba(255, 255, 255, 0.4) !important;
  font-size: 18px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px !important;
  font-size: 16px !important;
  letter-spacing: 4px;
  border-radius: 12px !important;
  margin-top: 8px;
  background: var(--primary-gradient) !important;
  box-shadow: 0 4px 16px rgba(108, 92, 231, 0.4) !important;
  transition: all var(--transition-bounce) !important;
}

.login-btn:hover {
  box-shadow: 0 8px 28px rgba(108, 92, 231, 0.5) !important;
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(0) scale(0.98);
}

/* ========== 底部提示 ========== */
.login-footer {
  text-align: center;
}

.login-footer span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  letter-spacing: 0.5px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px 28px;
  }

  .login-title {
    font-size: 20px;
  }

  .deco-circle-1 {
    width: 200px;
    height: 200px;
    top: -50px;
    right: -50px;
  }

  .deco-circle-2 {
    width: 150px;
    height: 150px;
  }

  .deco-circle-3 {
    width: 120px;
    height: 120px;
  }
}
</style>

<template>
  <div class="customer-login">
    <div class="login-brand">
      <h1 class="brand-name">美发沙龙</h1>
      <p class="brand-slogan">专业美发，触手可及</p>
    </div>

    <div class="login-card">
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            maxlength="11"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-row">
            <el-input
              v-model="form.code"
              placeholder="验证码"
              size="large"
              maxlength="6"
              class="code-input"
              :prefix-icon="Key"
            />
            <el-button
              :disabled="countdown > 0"
              :loading="sending"
              class="send-btn"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/store/customerAuth'
import { ElMessage } from 'element-plus'
import { Phone, Key } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useCustomerAuthStore()
const formRef = ref(null)
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  code: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

let countdownTimer = null

async function handleSendCode() {
  const valid = await formRef.value.validateField('phone').catch(() => false)
  if (!valid) return

  sending.value = true
  try {
    await authStore.sendCode(form.phone)
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login({ phone: form.phone, code: form.code })
    router.push('/h5/home')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.customer-login {
  overflow-x: hidden;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--customer-gradient);
}

.login-brand {
  text-align: center;
  margin-bottom: 40px;
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-light);
  letter-spacing: 4px;
  margin-bottom: 8px;
}

.brand-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px;
}

.login-card {
  width: 100%;
  max-width: 360px;
  padding: 32px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.code-row {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.send-btn {
  min-width: 110px;
  height: 40px;
  font-size: 13px;
  border-radius: 8px;
}

.login-btn {
  width: 100%;
  height: 48px !important;
  font-size: 16px !important;
  letter-spacing: 4px;
  border-radius: 10px !important;
  margin-top: 8px;
  background: var(--customer-gradient) !important;
  border: none !important;
}
</style>

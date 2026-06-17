<template>
  <div class="birthday-config-page">
    <PageHeader title="生日营销配置" description="配置生日礼券自动发放规则" />

    <el-card shadow="never" v-loading="loading">
      <el-form label-width="120px" style="max-width: 500px">
        <el-form-item label="启用生日营销">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="发放优惠券">
          <el-select v-model="form.couponTemplateId" placeholder="选择优惠券模板" clearable style="width: 100%">
            <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送短信">
          <el-switch v-model="form.smsEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span>使用说明</span></template>
      <div class="help-text">
        <p>1. 先在"优惠券管理"中创建一个优惠券模板（如"生日专享50元代金券"）</p>
        <p>2. 在此页面选择该模板并启用</p>
        <p>3. 系统每天 8:00 自动扫描当天生日的会员，发放优惠券并发送短信</p>
        <p>4. 每个会员每天仅发放一次（已做幂等处理）</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { birthdayConfigApi } from '@/api/birthdayConfig'
import { templateApi } from '@/api/coupon'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const saving = ref(false)
const templates = ref([])

const form = ref({
  enabled: 1,
  couponTemplateId: null,
  smsEnabled: 1,
})

onMounted(async () => {
  loading.value = true
  try {
    const config = await birthdayConfigApi.get()
    if (config) {
      form.value.enabled = config.enabled ?? 1
      form.value.couponTemplateId = config.couponTemplateId
      form.value.smsEnabled = config.smsEnabled ?? 1
    }
    templates.value = await templateApi.list() || []
  } catch { ElMessage.error('保存失败') }
  loading.value = false
})

async function save() {
  saving.value = true
  try {
    await birthdayConfigApi.update({
      enabled: form.value.enabled,
      couponTemplateId: form.value.couponTemplateId,
      smsEnabled: form.value.smsEnabled,
    })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
  saving.value = false
}
</script>

<style scoped>
.birthday-config-page {
  overflow-x: hidden;
  max-width: 800px;
  margin: 0 auto;
}

.help-text p {
  margin: 6px 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>

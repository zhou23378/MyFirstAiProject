<template>
  <div class="dailyclose-page">
    <PageHeader title="财务日结" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '商品与财务'}]" />

    <div class="close-layout">
      <!-- 系统汇总（左） -->
      <div class="close-panel">
        <h3 class="panel-title">系统汇总</h3>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="日期">{{ summary.closeDate || today }}</el-descriptions-item>
          <el-descriptions-item label="现金">¥{{ formatMoney(summary.systemCash) }}</el-descriptions-item>
          <el-descriptions-item label="微信">¥{{ formatMoney(summary.systemWechat) }}</el-descriptions-item>
          <el-descriptions-item label="支付宝">¥{{ formatMoney(summary.systemAlipay) }}</el-descriptions-item>
          <el-descriptions-item label="余额支付">¥{{ formatMoney(summary.systemBalance) }}</el-descriptions-item>
          <el-descriptions-item label="储值卡">¥{{ formatMoney(summary.systemCard) }}</el-descriptions-item>
          <el-descriptions-item label="合计">
            <strong>¥{{ formatMoney(summary.systemTotal) }}</strong>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 人工录入（右） -->
      <div class="close-panel">
        <h3 class="panel-title">人工录入</h3>
        <el-form label-width="80px" v-if="!isLocked">
          <el-form-item label="现金">
            <el-input-number v-model="manualForm.cash" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="微信">
            <el-input-number v-model="manualForm.wechat" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="支付宝">
            <el-input-number v-model="manualForm.alipay" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="manualForm.remark" placeholder="选填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存录入</el-button>
            <el-button v-if="summary.id" type="success" :loading="locking" @click="handleLock">锁定日结</el-button>
          </el-form-item>
        </el-form>
        <div v-else class="locked-msg">
          <el-tag type="warning" size="large">已锁定</el-tag>
          <p>锁定人: {{ summary.lockedBy || '-' }}</p>
        </div>
      </div>

      <!-- 差异 -->
      <div class="close-panel diff-panel" v-if="summary.manualTotal">
        <h3 class="panel-title">差异</h3>
        <div class="diff-value" :class="{ 'diff-red': diffAbs > 0.01, 'diff-green': diffAbs <= 0.01 }">
          ¥{{ formatMoney(summary.diffAmount) }}
        </div>
      </div>
    </div>

    <!-- 历史日结 -->
    <el-card shadow="never" style="margin-top:20px">
      <template #header>历史日结</template>
      <el-table :data="history" stripe v-loading="historyLoading">
        <el-table-column prop="closeDate" label="日期" min-width="120" />
        <el-table-column prop="systemTotal" label="系统合计" min-width="120">
          <template #default="{ row }">¥{{ formatMoney(row.systemTotal) }}</template>
        </el-table-column>
        <el-table-column prop="manualTotal" label="人工合计" min-width="120">
          <template #default="{ row }">¥{{ formatMoney(row.manualTotal) }}</template>
        </el-table-column>
        <el-table-column prop="diffAmount" label="差异" min-width="120">
          <template #default="{ row }">
            <span :style="{ color: Math.abs(row.diffAmount || 0) > 0.01 ? 'var(--danger-color)' : 'var(--success-color)' }">
              ¥{{ formatMoney(row.diffAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'warning' : 'info'" size="small">
              {{ row.status === 1 ? '已锁定' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <template #empty>
          <el-empty description="暂无日结记录" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { dailyCloseApi } from '@/api/dailyClose'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const today = new Date().toISOString().split('T')[0]
const summary = ref({})
const historyLoading = ref(false)
const history = ref([])
const saving = ref(false)
const locking = ref(false)
const manualForm = reactive({ cash: 0, wechat: 0, alipay: 0, remark: '' })

const isLocked = computed(() => summary.value.status === 1)
const diffAbs = computed(() => Math.abs(Number(summary.value.diffAmount) || 0))

function formatMoney(v) {
  const n = Number(v)
  return isNaN(n) ? '0.00' : n.toFixed(2)
}

async function loadToday() {
  try {
    const data = await dailyCloseApi.today()
    summary.value = data
    if (data.manualCash != null) manualForm.cash = data.manualCash
    if (data.manualWechat != null) manualForm.wechat = data.manualWechat
    if (data.manualAlipay != null) manualForm.alipay = data.manualAlipay
    if (data.remark) manualForm.remark = data.remark
  } catch { /* 加载失败不影响页面展示 */ }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await dailyCloseApi.history({ page: 1, pageSize: 20 })
    history.value = res.list || []
  } finally { historyLoading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    await dailyCloseApi.save({
      closeDate: today,
      manualCash: manualForm.cash,
      manualWechat: manualForm.wechat,
      manualAlipay: manualForm.alipay,
      remark: manualForm.remark,
    })
    ElMessage.success('保存成功')
    loadToday()
  } catch { ElMessage.error('保存失败，请重试') }
  finally { saving.value = false }
}

async function handleLock() {
  if (!summary.value.id) return
  locking.value = true
  try {
    await dailyCloseApi.lock(summary.value.id)
    ElMessage.success('日结已锁定')
    loadToday()
  } catch { ElMessage.error('锁定失败，请重试') }
  finally { locking.value = false }
}

onMounted(() => { loadToday(); loadHistory() })
</script>

<style scoped>
.dailyclose-page { overflow-x: hidden; max-width: 1000px; margin: 0 auto; }

.close-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.diff-panel { grid-column: 1 / -1; }

.panel-title { font-size: 15px; font-weight: 600; margin: 0 0 12px 0; color: var(--text-primary); }

.diff-value {
  font-size: 28px; font-weight: 700; text-align: center; padding: 20px;
}
.diff-green { color: var(--success-color); }
.diff-red { color: var(--danger-color); }

.locked-msg { text-align: center; padding: 20px; }
.locked-msg p { color: var(--text-muted); margin-top: 8px; }

@media (max-width: 767px) {
  .dailyclose-page { padding: 0; }
  .close-layout { grid-template-columns: 1fr; }
}
</style>

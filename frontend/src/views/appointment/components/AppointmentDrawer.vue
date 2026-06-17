<template>
  <el-drawer
    v-model="visible"
    title="预约详情"
    :size="drawerWidth"
    :close-on-click-modal="true"
    @closed="$emit('closed')"
  >
    <template v-if="!appointment">
      <el-empty description="请选择预约" />
    </template>

    <template v-else>
      <!-- Status tag -->
      <div class="drawer-section">
        <el-tag :type="statusTagType" size="large">{{ statusLabel }}</el-tag>
      </div>

      <!-- Core info -->
      <div class="drawer-section info-grid">
        <div class="info-item">
          <span class="info-label">会员</span>
          <span class="info-value">{{ appointment.memberName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ appointment.memberPhone || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">服务项目</span>
          <span class="info-value">{{ appointment.serviceItemName }}</span>
        </div>
        <div v-if="appointment.servicePrice" class="info-item">
          <span class="info-label">价格</span>
          <span class="info-value">¥{{ appointment.servicePrice }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">日期</span>
          <span class="info-value">{{ currentDate }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">时段</span>
          <span class="info-value">{{ appointment.startTime }} - {{ appointment.endTime }}</span>
        </div>
        <div v-if="technicianName" class="info-item">
          <span class="info-label">技师</span>
          <span class="info-value">{{ technicianName }}</span>
        </div>
        <div v-if="appointment.remark" class="info-item full-width">
          <span class="info-label">备注</span>
          <span class="info-value">{{ appointment.remark }}</span>
        </div>
      </div>

      <!-- Actions -->
      <div class="drawer-section actions">
        <template v-if="appointment.status === 1">
          <el-button type="success" @click="handleAction('arrive')" :loading="actionLoading">
            确认到店
          </el-button>
          <el-button @click="handleAction('cancel')" :loading="actionLoading">
            取消预约
          </el-button>
          <el-button type="danger" plain @click="handleAction('noshow')" :loading="actionLoading">
            标记爽约
          </el-button>
          <el-button type="primary" @click="handleAction('convert')" :loading="actionLoading">
            转消费
          </el-button>
        </template>
        <template v-else-if="appointment.status === 2">
          <el-button type="success" @click="handleAction('complete')" :loading="actionLoading">
            完成服务
          </el-button>
          <el-button type="danger" plain @click="handleAction('noshow')" :loading="actionLoading">
            标记爽约
          </el-button>
          <el-button type="primary" @click="handleAction('convert')" :loading="actionLoading">
            转消费
          </el-button>
        </template>
        <template v-else>
          <el-tag type="info" size="large">已完成</el-tag>
          <span v-if="appointment.status === 3" style="margin-left:8px;color:var(--text-muted)">此预约已转为消费或完成</span>
          <span v-else-if="appointment.status === 4" style="margin-left:8px;color:var(--text-muted)">此预约已取消</span>
          <span v-else-if="appointment.status === 5" style="margin-left:8px;color:var(--text-muted)">此预约已标记为爽约</span>
        </template>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appointmentApi } from '@/api/appointment'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  appointment: { type: Object, default: null },
  currentDate: { type: String, default: '' },
  technicianName: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'action-done', 'closed'])

const actionLoading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const drawerWidth = computed(() => {
  return typeof window !== 'undefined' && window.innerWidth < 768 ? '100%' : '400px'
})

const statusLabel = computed(() => {
  const map = { 1: '已预约', 2: '已到店', 3: '已完成', 4: '已取消', 5: '爽约' }
  return map[props.appointment?.status] || '-'
})

const statusTagType = computed(() => {
  const map = { 1: 'primary', 2: 'success', 3: 'info', 4: 'warning', 5: 'danger' }
  return map[props.appointment?.status] || 'info'
})

async function handleAction(action) {
  if (!props.appointment) return

  if (action === 'convert') {
    try {
      await ElMessageBox.prompt('请输入支付方式（1现金/2余额/3微信/4支付宝/5银行卡/6储值卡/7团购/8混合）', '转消费', {
        confirmButtonText: '确认转换',
        inputValue: '1'
      })
      const payMethod = 1 // default cash for quick convert from drawer
      actionLoading.value = true
      await appointmentApi.convertToOrder(props.appointment.id, { payMethod })
      ElMessage.success('已转为消费订单')
      emit('action-done')
      visible.value = false
    } catch (e) {
      if (e !== 'cancel') {
        // error handled by interceptor
      }
    } finally {
      actionLoading.value = false
    }
    return
  }

  const statusMap = {
    arrive: 2,
    complete: 3,
    cancel: 4,
    noshow: 5
  }
  const newStatus = statusMap[action]
  if (newStatus == null) return

  actionLoading.value = true
  try {
    if (action === 'arrive') {
      await appointmentApi.arrive(props.appointment.id)
    } else {
      await appointmentApi.updateStatus(props.appointment.id, newStatus)
    }
    const labels = { arrive: '已到店', complete: '已完成', cancel: '已取消', noshow: '已标记爽约' }
    ElMessage.success(labels[action])
    emit('action-done')
  } catch {
    // handled by interceptor
  } finally {
    actionLoading.value = false
  }
}
</script>

<style scoped>
.drawer-section {
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.info-value {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  font-weight: 500;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}
</style>

<template>
  <div class="cards-page">
    <div class="page-header">
      <el-icon class="back-icon" @click="$router.back()"><ArrowLeft /></el-icon>
      <span class="page-title">我的次卡</span>
    </div>

    <div v-loading="loading" class="cards-list">
      <div v-if="!loading && cards.length === 0" class="empty-state">
        <el-empty description="暂无次卡" />
        <el-button type="primary" @click="showPurchase = true">立即购买</el-button>
      </div>

      <div v-for="card in cards" :key="card.id" class="card-item">
        <div class="card-left">
          <span class="card-name">{{ card.serviceItemName || '次卡' }}</span>
          <span class="card-count">
            剩余 <b>{{ card.remainingCount }}</b> / {{ card.totalCount }} 次
          </span>
        </div>
        <div class="card-right">
          <span class="card-status" :class="{ expired: card.status === 2 }">
            {{ statusMap[card.status] || '未知' }}
          </span>
        </div>
      </div>
    </div>

    <el-dialog v-model="showPurchase" title="购买次卡" width="90%">
      <el-form label-position="top">
        <el-form-item label="服务项目">
          <el-select v-model="purchaseForm.serviceItemId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="s in services"
              :key="s.id"
              :label="`${s.name} &yen;${s.price}`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="购买次数">
          <el-input-number v-model="purchaseForm.totalCount" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPurchase = false">取消</el-button>
        <el-button type="primary" :loading="purchasing" @click="handlePurchase">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'

const loading = ref(false)
const cards = ref([])
const showPurchase = ref(false)
const purchasing = ref(false)
const services = ref([])

const purchaseForm = ref({
  serviceItemId: null,
  totalCount: 10
})

const statusMap = { 1: '有效', 2: '已用完', 3: '已过期' }

onMounted(async () => {
  loading.value = true
  try {
    const [cardData, serviceData] = await Promise.all([
      customerApi.getServiceCards(),
      customerApi.getServiceCategoryTree()
    ])
    cards.value = cardData
    const cats = Array.isArray(serviceData) ? serviceData : (serviceData.data || [])
    services.value = cats.flatMap(c => (c.children || []).flatMap(sc => sc.services || []))
  } catch { ElMessage.error('操作失败') } finally {
    loading.value = false
  }
})

async function handlePurchase() {
  if (!purchaseForm.value.serviceItemId) {
    ElMessage.warning('请选择服务项目')
    return
  }
  purchasing.value = true
  try {
    await customerApi.purchaseServiceCard(purchaseForm.value)
    ElMessage.success('购买成功')
    showPurchase.value = false
    cards.value = await customerApi.getServiceCards()
  } catch { ElMessage.error('操作失败') } finally {
    purchasing.value = false
  }
}
</script>

<style scoped>
.cards-page {
  overflow-x: hidden;
  padding: 16px;
  min-height: 100vh;
  background: var(--bg-page);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.back-icon {
  font-size: 20px;
  color: var(--text-primary);
  cursor: pointer;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.cards-list {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-white);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-count {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.card-count b {
  color: var(--primary-color);
}

.card-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  background: var(--primary-light);
  color: var(--primary-color);
}

.card-status.expired {
  background: var(--bg-expired);
  color: var(--text-expired);
}
</style>

<template>
  <div class="points-mall-page">
    <div class="page-header">
      <el-icon class="back-icon" @click="$router.back()"><ArrowLeft /></el-icon>
      <span class="page-title">积分商城</span>
      <span class="my-points">我的积分：<b>{{ myPoints }}</b></span>
    </div>

    <div v-loading="loading" class="products-grid">
      <div v-if="!loading && products.length === 0" class="empty-state">
        <el-empty description="暂无商品" />
      </div>

      <div v-for="product in products" :key="product.id" class="product-card" @click="openDetail(product)">
        <div class="product-image">
          <img v-if="product.imageUrl && !product._imgError" :src="product.imageUrl" :alt="product.name" @error="product._imgError = true" />
          <el-icon v-else :size="48" class="placeholder-icon"><Present /></el-icon>
        </div>
        <div class="product-info">
          <span class="product-name">{{ product.name }}</span>
          <div class="product-price">
            <span class="points-price">{{ product.pointsPrice }} 积分</span>
            <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
          </div>
          <span class="product-stock">库存：{{ product.stockQty }}</span>
        </div>
      </div>
    </div>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="selectedProduct?.name" width="90%">
      <div v-if="selectedProduct" class="detail-content">
        <div class="detail-image">
          <img v-if="selectedProduct.imageUrl && !detailImgError" :src="selectedProduct.imageUrl" :alt="selectedProduct.name" @error="detailImgError = true" />
          <el-icon v-else :size="64" class="placeholder-icon"><Present /></el-icon>
        </div>
        <div class="detail-meta">
          <div class="detail-price">
            <span class="points-price">{{ selectedProduct.pointsPrice }} 积分</span>
            <span v-if="selectedProduct.originalPrice" class="original-price">原价 ¥{{ selectedProduct.originalPrice }}</span>
          </div>
          <span class="detail-stock">剩余库存：{{ selectedProduct.stockQty }}</span>
          <span v-if="selectedProduct.exchangedCount" class="detail-exchanged">已兑换 {{ selectedProduct.exchangedCount }} 次</span>
        </div>
        <p v-if="selectedProduct.description" class="detail-desc">{{ selectedProduct.description }}</p>

        <div class="exchange-section">
          <div class="quantity-row">
            <span>兑换数量</span>
            <el-input-number v-model="exchangeQty" :min="1" :max="selectedProduct.stockQty" size="small" />
          </div>
          <div class="total-cost">
            需消耗 <b>{{ selectedProduct.pointsPrice * exchangeQty }}</b> 积分
          </div>
          <el-button
            type="primary"
            size="large"
            class="exchange-btn"
            :loading="exchanging"
            :disabled="!canExchange"
            @click="doExchange"
          >
            {{ canExchange ? '立即兑换' : '积分不足' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Present } from '@element-plus/icons-vue'
import { customerApi } from '../../api/customer'

const loading = ref(false)
const products = ref([])
const myPoints = ref(0)

const detailVisible = ref(false)
const detailImgError = ref(false)
const selectedProduct = ref(null)
const exchangeQty = ref(1)
const exchanging = ref(false)

const canExchange = computed(() => {
  if (!selectedProduct.value) return false
  const cost = selectedProduct.value.pointsPrice * exchangeQty.value
  return myPoints.value >= cost
})

async function fetchProducts() {
  loading.value = true
  try {
    const data = await customerApi.getPointsProducts()
    products.value = data || []
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

async function fetchMyPoints() {
  try {
    const profile = await customerApi.getProfile()
    myPoints.value = profile?.points || 0
  } catch {
    myPoints.value = 0
  }
}

function openDetail(product) {
  selectedProduct.value = product
  exchangeQty.value = 1
  detailImgError.value = false
  detailVisible.value = true
}

async function doExchange() {
  try {
    await ElMessageBox.confirm(
      `确认使用 ${selectedProduct.value.pointsPrice * exchangeQty.value} 积分兑换「${selectedProduct.value.name}」×${exchangeQty.value}？`,
      '确认兑换',
      { confirmButtonText: '确认兑换', type: 'info' }
    )
  } catch {
    return
  }

  exchanging.value = true
  try {
    await customerApi.exchangePoints({
      productId: selectedProduct.value.id,
      quantity: exchangeQty.value
    })
    ElMessage.success('兑换成功！请到店领取')
    detailVisible.value = false
    fetchProducts()
    fetchMyPoints()
  } finally {
    exchanging.value = false
  }
}

onMounted(() => {
  fetchProducts()
  fetchMyPoints()
})
</script>

<style scoped>
.points-mall-page {
  overflow-x: hidden;
  padding: 16px;
  min-height: 100vh;
  background: var(--bg-main);
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
  font-weight: 700;
  color: var(--text-primary);
  flex: 1;
}

.my-points {
  font-size: 14px;
  color: var(--primary-color);
}

.my-points b {
  font-size: 18px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.product-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s;
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}

.product-card:active {
  transform: scale(0.97);
}

.product-image {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light), var(--primary-color));
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-icon {
  color: var(--text-light);
}

.product-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.points-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--danger-color);
}

.original-price {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: line-through;
}

.product-stock {
  font-size: 12px;
  color: var(--text-muted);
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

/* ────── 详情弹窗 ────── */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-image {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light), var(--primary-color));
  border-radius: var(--radius-md);
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.detail-stock, .detail-exchanged {
  font-size: 13px;
  color: var(--text-muted);
}

.detail-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.exchange-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: var(--text-primary);
}

.total-cost {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: right;
}

.total-cost b {
  color: var(--danger-color);
  font-size: 18px;
}

.exchange-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
</style>

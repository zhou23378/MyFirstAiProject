<template>
  <div class="points-page" v-loading="loading">
    <PageHeader title="积分商城" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '营销中心'}]" />
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="商品管理" name="product" />
      <el-tab-pane label="兑换记录" name="record" />
    </el-tabs>

    <!-- Tab 1: 商品管理 -->
    <div v-if="activeTab === 'product'">
      <div class="action-bar">
        <el-input v-model="productFilter.name" placeholder="商品名称" clearable style="width: 200px" />
        <el-select v-model="productFilter.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchProducts" style="">查询</el-button>
        <el-button type="success" @click="openCreateDialog">新增商品</el-button>
      </div>

      <el-table :data="productList" stripe v-loading="productLoading">
        <el-table-column prop="id" label="ID" min-width="60" />
        <el-table-column prop="name" label="商品名称" min-width="140" />
        <el-table-column prop="pointsPrice" label="积分价格" min-width="100" />
        <el-table-column prop="stockQty" label="库存" min-width="80" />
        <el-table-column prop="exchangedCount" label="已兑换" min-width="80" />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="70" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              link
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState description="暂无商品" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="productPager.page"
        v-model:page-size="productPager.size"
        :total="productPager.total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        @current-change="fetchProducts"
        @size-change="fetchProducts"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>

    <!-- Tab 2: 兑换记录 -->
    <div v-if="activeTab === 'record'">
      <div class="action-bar">
        <el-input v-model="recordFilter.memberPhone" placeholder="会员手机号" clearable style="width: 180px" />
        <el-select v-model="recordFilter.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待领取" :value="0" />
          <el-option label="已领取" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-date-picker
          v-model="recordFilter.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" @click="fetchRecords" style="">查询</el-button>
      </div>

      <el-table :data="recordList" stripe v-loading="recordLoading">
        <el-table-column prop="id" label="ID" min-width="60" />
        <el-table-column prop="memberName" label="会员" min-width="100" />
        <el-table-column prop="memberPhone" label="手机号" min-width="130" />
        <el-table-column prop="productName" label="商品" min-width="140" />
        <el-table-column prop="pointsCost" label="消耗积分" min-width="100" />
        <el-table-column prop="quantity" label="数量" min-width="70" />
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exchangedAt" label="兑换时间" min-width="170" />
        <el-table-column prop="claimedAt" label="领取时间" min-width="170" />
        <el-table-column label="操作" min-width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              link
              @click="doClaim(row)"
            >
              标记已领取
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState description="暂无兑换记录" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="recordPager.page"
        v-model:page-size="recordPager.size"
        :total="recordPager.total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        @current-change="fetchRecords"
        @size-change="fetchRecords"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>

    <!-- 商品编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="图片URL（选填）" />
        </el-form-item>
        <el-form-item label="积分价格" prop="pointsPrice">
          <el-input-number v-model="form.pointsPrice" :min="1" :step="10" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQty">
          <el-input-number v-model="form.stockQty" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述（选填）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pointsProductApi, pointsExchangeRecordApi } from '../../api/points'
import PageHeader from '../../components/PageHeader.vue'
import EmptyState from '../../components/EmptyState.vue'

const activeTab = ref('product')
const loading = ref(false)

// ────── 商品管理 ──────
const productList = ref([])
const productLoading = ref(false)
const productFilter = reactive({ name: '', status: null })
const productPager = reactive({ page: 1, size: 10, total: 0 })

async function fetchProducts() {
  productLoading.value = true
  try {
    const data = await pointsProductApi.page({
      page: productPager.page,
      size: productPager.size,
      name: productFilter.name || undefined,
      status: productFilter.status
    })
    productList.value = data.list || []
    productPager.total = data.total || 0
  } finally {
    productLoading.value = false
  }
}

// ────── 兑换记录 ──────
const recordList = ref([])
const recordLoading = ref(false)
const recordFilter = reactive({ memberPhone: '', status: null, dateRange: null })
const recordPager = reactive({ page: 1, size: 10, total: 0 })

async function fetchRecords() {
  recordLoading.value = true
  try {
    const params = {
      page: recordPager.page,
      size: recordPager.size,
      memberPhone: recordFilter.memberPhone || undefined,
      status: recordFilter.status
    }
    if (recordFilter.dateRange && recordFilter.dateRange.length === 2) {
      params.startDate = recordFilter.dateRange[0]
      params.endDate = recordFilter.dateRange[1]
    }
    const data = await pointsExchangeRecordApi.page(params)
    recordList.value = data.list || []
    recordPager.total = data.total || 0
  } finally {
    recordLoading.value = false
  }
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'info'
}

function statusLabel(status) {
  if (status === 0) return '待领取'
  if (status === 1) return '已领取'
  return '已取消'
}

async function doClaim(row) {
  try {
    await ElMessageBox.confirm('确认标记为已领取？', '提示', { type: 'info' })
  } catch { return }
  await pointsExchangeRecordApi.claim(row.id)
  ElMessage.success('已标记为已领取')
  fetchRecords()
}

// ────── 商品弹窗 ──────
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  name: '',
  imageUrl: '',
  pointsPrice: 100,
  originalPrice: null,
  stockQty: 0,
  description: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  pointsPrice: [{ required: true, message: '请输入积分价格', trigger: 'blur' }],
  stockQty: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

function openCreateDialog() {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, {
    name: '', imageUrl: '', pointsPrice: 100, originalPrice: null,
    stockQty: 0, description: '', sortOrder: 0, status: 1
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, imageUrl: row.imageUrl || '', pointsPrice: row.pointsPrice,
    originalPrice: row.originalPrice, stockQty: row.stockQty,
    description: row.description || '', sortOrder: row.sortOrder, status: row.status
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await pointsProductApi.update(editingId.value, { ...form })
      ElMessage.success('商品已更新')
    } else {
      await pointsProductApi.create({ ...form })
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    fetchProducts()
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 0 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确认${label}该商品？`, '提示', { type: 'info' })
  } catch { return }
  await pointsProductApi.updateStatus(row.id, newStatus)
  ElMessage.success(`已${label}`)
  fetchProducts()
}

// ────── Tab 切换 ──────
function onTabChange(tab) {
  if (tab === 'product') fetchProducts()
  else fetchRecords()
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.points-page {
  overflow-x: hidden;
}

@media (max-width: 767px) {
  .points-page {
    padding: 0;
  }
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .action-bar .el-input,
  .action-bar .el-select,
  .action-bar .el-button {
    width: 100% !important;
    margin-left: 0 !important;
  }
}
</style>

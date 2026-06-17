<template>
  <div class="purchase-tab">
    <div class="action-bar">
      <el-select v-model="query.status" placeholder="状态" clearable @change="fetchData" style="width:130px">
        <el-option :value="0" label="草稿" />
        <el-option :value="1" label="已提交" />
        <el-option :value="2" label="已审批" />
        <el-option :value="3" label="已收货" />
        <el-option :value="4" label="已取消" />
      </el-select>
      <el-select v-model="query.supplierId" placeholder="供应商" clearable @change="fetchData" style="width:150px">
        <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="openDialog()">新建采购单</el-button>
    </div>

    <el-card shadow="never">
      <template v-if="loading">
        <SkeletonTable :rows="5" :cols="9" />
      </template>
      <template v-else>
        <el-table :data="list" stripe>
          <el-table-column prop="orderNo" label="订单编号" min-width="180" />
          <el-table-column prop="supplierName" label="供应商" min-width="120" />
          <el-table-column label="采购金额" min-width="110">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column prop="itemCount" label="种类" min-width="60" />
          <el-table-column prop="totalQty" label="总数量" min-width="80" />
          <el-table-column label="状态" min-width="80">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicant" label="申请人" min-width="80" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" min-width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openDialog(row)">详情</el-button>
              <el-button v-if="row.status === 0" type="success" size="small" text @click="handleSubmit(row.id)">提交</el-button>
              <el-button v-if="row.status === 1" type="success" size="small" text @click="handleApprove(row.id)">审核</el-button>
              <el-button v-if="row.status === 2" type="warning" size="small" text @click="handleReceive(row.id)">收货</el-button>
              <el-button v-if="row.status === 0 || row.status === 1" type="danger" size="small" text @click="handleCancel(row.id)">取消</el-button>
              <el-button v-if="row.status === 0" type="danger" size="small" text @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState description="暂无采购订单" />
          </template>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model="query.page" :page-size="query.pageSize" :total="total"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
            @current-change="(p) => { query.page = p; fetchData(); }"
            @size-change="(s) => { query.pageSize = s; query.page = 1; fetchData(); }"
          />
        </div>
      </template>
    </el-card>

    <el-dialog v-model="dialog" :title="editing ? '编辑采购单' : '新建采购单'" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" style="width:100%" :disabled="editing">
            <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :disabled="editing" />
        </el-form-item>
        <el-form-item label="采购明细">
          <el-table :data="form.items" border size="small">
            <el-table-column label="商品" min-width="150">
              <template #default="{ row, $index }">
                <el-select v-model="row.productId" filterable placeholder="搜索商品" style="width:100%" :disabled="editing" @change="(v) => onProductChange($index, v)">
                  <el-option v-for="p in allProducts" :key="p.id" :label="`${p.name} (库存:${p.stock_qty})`" :value="p.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="单位" min-width="70">
              <template #default="{ row }">{{ row.unit }}</template>
            </el-table-column>
            <el-table-column label="数量" min-width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.qty" :min="1" size="small" :disabled="editing" />
              </template>
            </el-table-column>
            <el-table-column label="单价" min-width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.price" :min="0" :precision="2" size="small" :disabled="editing" />
              </template>
            </el-table-column>
            <el-table-column label="小计" min-width="100">
              <template #default="{ row }">¥{{ ((row.qty || 0) * (row.price || 0)).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column v-if="!editing" label="操作" min-width="60">
              <template #default="{ $index }">
                <el-button type="danger" size="small" text @click="form.items.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button v-if="!editing" type="primary" size="small" style="margin-top:8px" @click="addItem">+ 添加商品</el-button>
        </el-form-item>
        <el-form-item label="合计">
          <span style="font-size:18px;font-weight:700;color:var(--primary-color)">¥{{ totalAmount }}</span>
          <span style="margin-left:16px;color:var(--text-secondary)">共 {{ totalQty }} 件</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">关闭</el-button>
        <el-button v-if="!editing" type="primary" :loading="submitting" @click="handleCreate">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { purchaseOrderApi } from '@/api/inventory'
import { ElMessage, ElMessageBox } from 'element-plus'
import SkeletonTable from '@/components/SkeletonTable.vue'
import EmptyState from '@/components/EmptyState.vue'

const props = defineProps({
  suppliers: { type: Array, default: () => [] },
  allProducts: { type: Array, default: () => [] }
})

const emit = defineEmits(['products-changed'])

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const query = reactive({ page: 1, pageSize: 10, status: null, supplierId: null })
const form = reactive({ supplierId: null, remark: '', items: [] })
const rules = { supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }] }

const statusMap = { 0: '草稿', 1: '已提交', 2: '已审批', 3: '已收货', 4: '已取消' }
const statusTagMap = { 0: 'info', 1: '', 2: 'success', 3: '', 4: 'danger' }
function statusText(s) { return statusMap[s] || '未知' }
function statusTag(s) { return statusTagMap[s] || 'info' }

const totalAmount = computed(() => form.items.reduce((sum, item) => sum + (item.qty || 0) * (item.price || 0), 0).toFixed(2))
const totalQty = computed(() => form.items.reduce((sum, item) => sum + (item.qty || 0), 0))

function addItem() { form.items.push({ productId: null, productName: '', unit: '', qty: 1, price: 0 }) }
function onProductChange(index, productId) {
  const p = props.allProducts.find(x => x.id === productId)
  if (p) { form.items[index].productName = p.name; form.items[index].unit = p.unit || '' }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await purchaseOrderApi.page({ ...query })
    list.value = res.list || []
    total.value = res.total || 0
  } catch { ElMessage.error('加载采购订单失败') } finally { loading.value = false }
}

function openDialog(row) {
  formRef.value?.resetFields()
  if (row) {
    editing.value = true; editId.value = row.id
    form.supplierId = row.supplierId; form.remark = row.remark || ''
    form.items = (row.items || []).map(i => ({ productId: i.productId, productName: i.productName, unit: i.unit, qty: i.qty, price: i.price }))
  } else {
    editing.value = false; editId.value = null
    form.supplierId = null; form.remark = ''; form.items = []
    addItem()
  }
  dialog.value = true
}

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.items.length === 0) { ElMessage.warning('请添加采购商品'); return }
  submitting.value = true
  try {
    await purchaseOrderApi.create({
      supplierId: form.supplierId, remark: form.remark,
      items: form.items.map(i => ({ productId: i.productId, productName: i.productName, unit: i.unit, qty: i.qty, price: i.price }))
    })
    ElMessage.success('创建成功')
    dialog.value = false
    fetchData()
  } catch { ElMessage.error('保存采购单失败') } finally { submitting.value = false }
}

async function handleSubmit(id) {
  try {
    await ElMessageBox.confirm('确认提交审核？', '提示', { type: 'warning' })
    await purchaseOrderApi.submit(id)
    ElMessage.success('提交成功')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function handleApprove(id) {
  try {
    await ElMessageBox.confirm('确认审核通过？', '提示', { type: 'warning' })
    await purchaseOrderApi.approve(id)
    ElMessage.success('审核通过')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function handleReceive(id) {
  try {
    await ElMessageBox.confirm('确认收货？将自动增加库存。', '提示', { type: 'warning' })
    await purchaseOrderApi.receive(id)
    ElMessage.success('收货成功，库存已更新')
    emit('products-changed')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确认取消该采购订单？', '提示', { type: 'warning' })
    await purchaseOrderApi.cancel(id)
    ElMessage.success('取消成功')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该草稿订单吗？', '提示', { type: 'warning' })
    await purchaseOrderApi.remove(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

defineExpose({ fetchData })
</script>

<template>
  <div class="group-buy-page">
    <PageHeader title="拼团活动" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '营销中心'}]" />
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="模板管理" name="template" />
      <el-tab-pane label="团单记录" name="order" />
    </el-tabs>

    <!-- ==================== 模板管理 ==================== -->
    <div v-if="activeTab === 'template'">
      <div class="action-bar">
        <el-button type="primary" @click="openTemplateDialog()">新增模板</el-button>
      </div>

      <el-card shadow="never">
        <el-table :data="templateList" stripe v-loading="templateLoading">
          <el-table-column prop="id" label="ID" min-width="60" />
          <el-table-column prop="name" label="活动名称" min-width="140" />
          <el-table-column label="拼团价" min-width="90">
            <template #default="{ row }">¥{{ row.groupPrice }}</template>
          </el-table-column>
          <el-table-column label="原价" min-width="80">
            <template #default="{ row }">¥{{ row.originalPrice }}</template>
          </el-table-column>
          <el-table-column prop="groupSize" label="成团人数" min-width="80" />
          <el-table-column prop="expireHours" label="有效时长(h)" min-width="90" />
          <el-table-column label="发行" min-width="100">
            <template #default="{ row }">
              {{ row.issuedQty || 0 }}<template v-if="row.totalQty > 0"> / {{ row.totalQty }}</template>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="160">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openTemplateDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleTemplateDisable(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState description="暂无拼团模板" />
          </template>
        </el-table>
      </el-card>

      <el-dialog v-model="templateDialog" :title="templateEdit ? '编辑模板' : '新增模板'" width="520px" :close-on-click-modal="false">
        <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="90px">
          <el-form-item label="名称" prop="name">
            <el-input v-model="templateForm.name" />
          </el-form-item>
          <el-form-item label="拼团价" prop="groupPrice">
            <el-input-number v-model="templateForm.groupPrice" :min="0.01" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="原价" prop="originalPrice">
            <el-input-number v-model="templateForm.originalPrice" :min="0.01" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="成团人数" prop="groupSize">
            <el-input-number v-model="templateForm.groupSize" :min="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="有效时长(时)" prop="expireHours">
            <el-input-number v-model="templateForm.expireHours" :min="1" :max="720" style="width:100%" />
          </el-form-item>
          <el-form-item label="发行总量">
            <el-input-number v-model="templateForm.totalQty" :min="0" style="width:100%" />
            <div class="form-tip">填 0 表示不限量</div>
          </el-form-item>
          <el-form-item label="活动时间">
            <el-date-picker v-model="templateTimeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%" />
          </el-form-item>
          <el-form-item label="图片URL">
            <el-input v-model="templateForm.imageUrl" placeholder="活动图片地址" />
          </el-form-item>
          <el-form-item label="活动说明">
            <el-input v-model="templateForm.description" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="templateStatusSwitch" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="templateDialog = false">取消</el-button>
          <el-button type="primary" :loading="templateSubmitting" @click="handleTemplateSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- ==================== 团单记录 ==================== -->
    <div v-if="activeTab === 'order'">
      <div class="action-bar">
        <el-select v-model="orderFilter.status" placeholder="状态" clearable @change="fetchOrders" style="width:140px">
          <el-option :value="1" label="拼团中" />
          <el-option :value="2" label="已成团" />
          <el-option :value="3" label="已核销" />
          <el-option :value="4" label="已过期" />
          <el-option :value="5" label="已取消" />
        </el-select>
        <el-button type="primary" @click="fetchOrders">查询</el-button>
      </div>

      <el-card shadow="never">
        <el-table :data="orderList" stripe v-loading="orderLoading" @row-click="openOrderDetail" style="cursor:pointer">
          <el-table-column prop="id" label="ID" min-width="60" />
          <el-table-column prop="orderNo" label="团单号" min-width="160" />
          <el-table-column prop="leaderName" label="团长" min-width="100" />
          <el-table-column label="拼团价" min-width="80">
            <template #default="{ row }">¥{{ row.groupPrice }}</template>
          </el-table-column>
          <el-table-column label="人数" min-width="70">
            <template #default="{ row }">{{ row.currentSize }}/{{ row.groupSize }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="80">
            <template #default="{ row }">
              <el-tag :type="orderStatusTag(row.status)" size="small">{{ orderStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column prop="expireTime" label="过期时间" min-width="160" />
          <template #empty>
            <EmptyState description="暂无团单" />
          </template>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model="orderPager.page" :page-size="orderPager.size" :total="orderPager.total"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
            @current-change="(p) => { orderPager.page = p; fetchOrders(); }"
            @size-change="(s) => { orderPager.size = s; orderPager.page = 1; fetchOrders(); }"
          />
        </div>
      </el-card>

      <el-dialog v-model="orderDetailVisible" title="团单详情" width="600px">
        <template v-if="orderDetail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="团单号">{{ orderDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="orderStatusTag(orderDetail.status)" size="small">{{ orderStatusText(orderDetail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="团长">{{ orderDetail.leaderName }} ({{ orderDetail.leaderPhone }})</el-descriptions-item>
            <el-descriptions-item label="拼团价">¥{{ orderDetail.groupPrice }}</el-descriptions-item>
            <el-descriptions-item label="人数">{{ orderDetail.currentSize }}/{{ orderDetail.groupSize }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ orderDetail.expireTime }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="成团时间">{{ orderDetail.completeTime || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h4 style="margin: 16px 0 8px">参团列表</h4>
          <el-table :data="orderDetail.participants" stripe size="small">
            <el-table-column prop="memberName" label="会员" min-width="100" />
            <el-table-column prop="memberPhone" label="手机号" min-width="120" />
            <el-table-column label="角色" min-width="60">
              <template #default="{ row }">
                <el-tag size="small" :type="row.isLeader ? 'warning' : 'info'">{{ row.isLeader ? '团长' : '团员' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag :type="participantStatusTag(row.status)" size="small">{{ participantStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="参与时间" min-width="160" />
            <el-table-column label="操作" min-width="80">
              <template #default="{ row }">
                <el-button v-if="row.status === 2" type="primary" size="small" text @click="doRedeem(row)">核销</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { groupBuyTemplateApi, groupBuyOrderApi } from '../../api/groupBuy'
import PageHeader from '../../components/PageHeader.vue'
import EmptyState from '../../components/EmptyState.vue'

const activeTab = ref('template')

function formatLocalDateTime(date) {
  if (!date) return null
  // Handle dayjs object (Element Plus date picker returns dayjs, not Date)
  if (typeof date === 'object' && typeof date.format === 'function') {
    return date.format('YYYY-MM-DD HH:mm:ss')
  }
  // Handle string (already formatted)
  if (typeof date === 'string') {
    return date.includes('T') ? date.replace('T', ' ') : date
  }
  // Handle native Date object (fallback)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function parseToDate(str) {
  if (!str) return null
  // Handle both "YYYY-MM-DD HH:mm:ss" and "YYYY-MM-DDTHH:mm:ss"
  const normalized = str.replace('T', ' ')
  const [datePart, timePart] = normalized.split(' ')
  const [y, m, d] = datePart.split('-').map(Number)
  const [hh, mm, ss] = (timePart || '00:00:00').split(':').map(Number)
  return new Date(y, m - 1, d, hh, mm, ss)
}

// ========== 模板管理 ==========
const templateList = ref([])
const templateLoading = ref(false)
const templateDialog = ref(false)
const templateEdit = ref(false)
const templateEditId = ref(null)
const templateSubmitting = ref(false)
const templateFormRef = ref(null)
const templateForm = reactive({ name: '', groupPrice: 29.9, originalPrice: 68.0, groupSize: 3, expireHours: 24, totalQty: 0, imageUrl: '', description: '', status: 1 })
const templateTimeRange = ref(null)
const templateRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  groupPrice: [{ required: true, message: '请输入拼团价', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  groupSize: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  expireHours: [{ required: true, message: '请输入有效时长', trigger: 'blur' }]
}
const templateStatusSwitch = computed({
  get: () => templateForm.status === 1,
  set: (v) => { templateForm.status = v ? 1 : 0 }
})

async function fetchTemplates() {
  templateLoading.value = true
  try {
    const res = await groupBuyTemplateApi.page({ page: 1, size: 100 })
    templateList.value = res.list || []
  } catch { ElMessage.error('加载模板失败') } finally { templateLoading.value = false }
}

function openTemplateDialog(row) {
  templateFormRef.value?.resetFields()
  if (row) {
    templateEdit.value = true
    templateEditId.value = row.id
    Object.assign(templateForm, {
      name: row.name, groupPrice: row.groupPrice, originalPrice: row.originalPrice,
      groupSize: row.groupSize, expireHours: row.expireHours, totalQty: row.totalQty || 0,
      imageUrl: row.imageUrl || '', description: row.description || '', status: row.status
    })
    if (row.startTime && row.endTime) {
      templateTimeRange.value = [parseToDate(row.startTime), parseToDate(row.endTime)]
    } else {
      templateTimeRange.value = null
    }
  } else {
    templateEdit.value = false
    templateEditId.value = null
    Object.assign(templateForm, { name: '', groupPrice: 29.9, originalPrice: 68.0, groupSize: 3, expireHours: 24, totalQty: 0, imageUrl: '', description: '', status: 1 })
    templateTimeRange.value = null
  }
  templateDialog.value = true
}

async function handleTemplateSubmit() {
  const valid = await templateFormRef.value.validate().catch(() => false)
  if (!valid) return
  templateSubmitting.value = true
  try {
    const data = { ...templateForm }
    if (templateTimeRange.value) {
      data.startTime = formatLocalDateTime(templateTimeRange.value[0])
      data.endTime = formatLocalDateTime(templateTimeRange.value[1])
    }
    if (templateEdit.value) {
      await groupBuyTemplateApi.update(templateEditId.value, data)
      ElMessage.success('修改成功')
    } else {
      await groupBuyTemplateApi.create(data)
      ElMessage.success('新增成功')
    }
    templateDialog.value = false
    fetchTemplates()
  } catch { ElMessage.error('保存失败') } finally { templateSubmitting.value = false }
}

async function handleTemplateDisable(row) {
  try {
    await ElMessageBox.confirm('确定删除该模板吗？', '提示', { type: 'warning' })
    await groupBuyTemplateApi.updateStatus(row.id, 0)
    ElMessage.success('已停用')
    fetchTemplates()
  } catch { ElMessage.error('操作失败') }
}

// ========== 团单记录 ==========
const orderList = ref([])
const orderLoading = ref(false)
const orderFilter = reactive({ status: null })
const orderPager = reactive({ page: 1, size: 10, total: 0 })
const orderDetailVisible = ref(false)
const orderDetail = ref(null)

const orderStatusMap = { 1: '拼团中', 2: '已成团', 3: '已核销', 4: '已过期', 5: '已取消' }
const orderStatusTagMap = { 1: 'warning', 2: 'success', 3: '', 4: 'info', 5: 'danger' }
function orderStatusText(s) { return orderStatusMap[s] || '未知' }
function orderStatusTag(s) { return orderStatusTagMap[s] || 'info' }

const participantStatusMap = { 1: '待成团', 2: '已成团', 3: '已核销', 4: '已退款' }
const participantStatusTagMap = { 1: 'warning', 2: 'success', 3: '', 4: 'info' }
function participantStatusText(s) { return participantStatusMap[s] || '未知' }
function participantStatusTag(s) { return participantStatusTagMap[s] || 'info' }

async function fetchOrders() {
  orderLoading.value = true
  try {
    const res = await groupBuyOrderApi.page({ page: orderPager.page, size: orderPager.size, status: orderFilter.status })
    orderList.value = res.list || []
    orderPager.total = res.total || 0
  } catch { ElMessage.error('加载团单失败') } finally { orderLoading.value = false }
}

async function openOrderDetail(row) {
  try {
    orderDetail.value = await groupBuyOrderApi.getDetail(row.id)
    orderDetailVisible.value = true
  } catch { ElMessage.error('加载团单详情失败') }
}

async function doRedeem(row) {
  try {
    await ElMessageBox.confirm('确认核销该参团记录吗？', '提示', { type: 'warning' })
    await groupBuyOrderApi.redeem(row.id)
    ElMessage.success('核销成功')
    orderDetailVisible.value = false
    fetchOrders()
  } catch { ElMessage.error('操作失败') }
}

function onTabChange(name) {
  if (name === 'template') fetchTemplates()
  else if (name === 'order') fetchOrders()
}

onMounted(() => { fetchTemplates() })
</script>

<style scoped>
.group-buy-page {
  overflow-x: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

@media (max-width: 767px) {
  .group-buy-page {
    padding: 0;
  }
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

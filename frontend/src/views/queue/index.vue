<template>
  <div class="queue-page">
    <PageHeader title="轮牌排队" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '店务管理'}]">
      <template #extra>
        <el-button type="primary" @click="openEnqueueDialog" v-if="!isMobile">会员入队</el-button>
      </template>
    </PageHeader>

    <div class="queue-layout" v-loading="loading">
      <!-- 左侧：排队队列 -->
      <div class="queue-panel">
        <div class="panel-head">
          <h3>等待队列 ({{ queueList.length }})</h3>
          <el-button v-if="isMobile" type="primary" size="small" @click="openEnqueueDialog">入队</el-button>
        </div>
        <div class="queue-cards">
          <div
            v-for="q in queueList.filter(x => x.status === 1)"
            :key="q.id"
            class="queue-card"
            :class="{ 'queue-card--selected': selectedQueueId === q.id }"
            @click="selectQueue(q)"
          >
            <span class="queue-num">{{ q.queueNumber }}</span>
            <div class="queue-info">
              <span class="queue-name">{{ q.memberName }}</span>
              <span class="queue-wait">等待 {{ q.waitMinutes }} 分钟</span>
            </div>
            <span class="queue-service" v-if="q.serviceItemName">{{ q.serviceItemName }}</span>
            <div class="queue-card-actions" v-if="selectedQueueId === q.id && !isMobile">
              <el-button size="small" type="danger" text @click="skipQueue(q.id)">跳过</el-button>
              <el-button size="small" text @click="cancelQueue(q.id)">取消</el-button>
            </div>
          </div>
          <EmptyState v-if="queueList.filter(x => x.status === 1).length === 0" description="暂无排队" />
        </div>

        <!-- 已分配列表 -->
        <div class="panel-head" style="margin-top:16px"><h3>已分配</h3></div>
        <div class="assigned-list">
          <div v-for="q in queueList.filter(x => x.status === 2)" :key="q.id" class="assigned-item">
            <el-tag size="small" type="success">{{ q.queueNumber }}号</el-tag>
            <span>{{ q.memberName }}</span>
            <span class="assigned-tech">
              <el-tag size="small" type="info">{{ techNameMap[q.assignedEmployeeId] || '技师' }}</el-tag>
            </span>
          </div>
        </div>
      </div>

      <!-- 右侧：可选技师 -->
      <div class="tech-panel">
        <h3 class="panel-head">空闲技师</h3>
        <div class="tech-select-grid">
          <div
            v-for="t in availableTechs"
            :key="t.employeeId"
            class="tech-select-card"
            :class="{ 'tech-select-card--chosen': selectedTechId === t.employeeId }"
            @click="selectedTechId = t.employeeId"
          >
            <span class="tech-select-name">{{ t.employeeName }}</span>
            <el-tag size="small" type="success">{{ statusText(t.status) }}</el-tag>
          </div>
          <EmptyState v-if="availableTechs.length === 0" description="暂无空闲技师" />
        </div>
        <el-button
          v-if="selectedQueueId && selectedTechId"
          type="primary"
          style="width:100%;margin-top:16px"
          @click="handleAssign"
        >
          分配 {{ selectedTechName }} → {{ selectedQueueName }}
        </el-button>
      </div>
    </div>

    <!-- 入队弹窗 -->
    <el-dialog v-model="enqueueVisible" title="会员入队" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="会员">
          <el-select v-model="enqueueForm.memberId" filterable placeholder="搜索会员" @focus="loadMembers" style="width:100%">
            <el-option v-for="m in members" :key="m.id" :label="`${m.name} ${m.phone || ''}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目">
          <el-select v-model="enqueueForm.serviceItemId" filterable placeholder="可选" clearable style="width:100%" @focus="loadServices">
            <el-option v-for="s in serviceItems" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="enqueueVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEnqueue">确认入队</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { serviceQueueApi } from '@/api/serviceQueue'
import { technicianStatusApi } from '@/api/technicianStatus'
import { memberApi } from '@/api/member'
import { serviceApi } from '@/api/service'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const loading = ref(false)
const queueList = ref([])
const techs = ref([])
const selectedQueueId = ref(null)
const selectedTechId = ref(null)
const enqueueVisible = ref(false)
const members = ref([])
const serviceItems = ref([])
const enqueueForm = ref({ memberId: null, serviceItemId: null })

const availableTechs = computed(() =>
  techs.value.filter(t => t.status === 'AVAILABLE'))

const selectedTechName = computed(() => {
  const t = techs.value.find(x => x.employeeId === selectedTechId.value)
  return t ? t.employeeName : ''
})
const selectedQueueName = computed(() => {
  const q = queueList.value.find(x => x.id === selectedQueueId.value)
  return q ? `${q.queueNumber}号 ${q.memberName}` : ''
})

const statusText = (s) => ({ AVAILABLE: '空闲', BUSY: '忙碌', BREAK: '休息', OFF_DUTY: '下班' }[s] || s)
const techNameMap = computed(() => {
  const map = {}
  techs.value.forEach(t => { map[t.employeeId] = t.employeeName })
  return map
})

function selectQueue(q) {
  selectedQueueId.value = selectedQueueId.value === q.id ? null : q.id
}

async function loadData() {
  loading.value = true
  try {
    try { queueList.value = await serviceQueueApi.list() } catch { ElMessage.error('操作失败，请重试') }
    try { techs.value = await technicianStatusApi.list() } catch { ElMessage.error('操作失败，请重试') }
  } finally { loading.value = false }
}

async function handleAssign() {
  try {
    await serviceQueueApi.assign({ queueId: selectedQueueId.value, employeeId: selectedTechId.value })
    ElMessage.success('分配成功')
    selectedQueueId.value = null
    selectedTechId.value = null
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function skipQueue(id) {
  try { await serviceQueueApi.skip(id); loadData() } catch { ElMessage.error('操作失败，请重试') }
}
async function cancelQueue(id) {
  try { await serviceQueueApi.cancel(id); loadData() } catch { ElMessage.error('操作失败，请重试') }
}

function openEnqueueDialog() {
  enqueueForm.value = { memberId: null, serviceItemId: null }
  enqueueVisible.value = true
}

async function handleEnqueue() {
  if (!enqueueForm.value.memberId) { ElMessage.warning('请选择会员'); return }
  const m = members.value.find(x => x.id === enqueueForm.value.memberId)
  const s = serviceItems.value.find(x => x.id === enqueueForm.value.serviceItemId)
  try {
    await serviceQueueApi.enqueue({
      memberId: m.id,
      memberName: m.name,
      serviceItemId: s?.id,
      serviceItemName: s?.name,
    })
    ElMessage.success(`${m.name} 已入队`)
    enqueueVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败，请重试') }
}

async function loadMembers() {
  if (members.value.length > 0) return
  try {
    const res = await memberApi.page({ page: 1, pageSize: 200 })
    members.value = res.list || []
  } catch { ElMessage.error("加载会员列表失败"); }
}
async function loadServices() {
  if (serviceItems.value.length > 0) return
  try { serviceItems.value = await serviceApi.getItems() } catch { ElMessage.error("加载服务项目失败"); }
}

onMounted(loadData)
</script>

<style scoped>
.queue-page { overflow-x: hidden; max-width: 1400px; margin: 0 auto; }

.queue-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
}
.panel-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.panel-head h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin: 0; }

.queue-cards { display: flex; flex-direction: column; gap: 8px; }

.queue-card {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 2px solid transparent;
}
.queue-card:hover { transform: translateX(4px); }
.queue-card--selected { border-color: var(--primary-color); }

.queue-num {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: var(--primary-color);
  color: var(--text-light);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px; flex-shrink: 0;
}
.queue-info { flex: 1; display: flex; flex-direction: column; }
.queue-name { font-weight: 500; color: var(--text-primary); }
.queue-wait { font-size: 12px; color: var(--text-muted); }
.queue-service { font-size: 12px; color: var(--text-secondary); }

.assigned-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; font-size: 13px; color: var(--text-secondary);
}

.tech-select-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.tech-select-card {
  padding: 12px; border-radius: var(--radius-sm);
  background: var(--bg-card); border: 2px solid transparent;
  cursor: pointer; transition: all var(--transition-fast);
  display: flex; justify-content: space-between; align-items: center;
}
.tech-select-card:hover { border-color: var(--primary-color); }
.tech-select-card--chosen { border-color: var(--primary-color); background: var(--bg-primary-ghost); }

@media (max-width: 767px) {
  .queue-page { padding: 0; }
  .queue-layout { grid-template-columns: 1fr; }
  .queue-card { padding: 14px; min-height: var(--touch-target-min); }
  .tech-select-grid { grid-template-columns: 1fr; }
}
</style>

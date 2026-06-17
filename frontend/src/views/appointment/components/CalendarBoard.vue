<template>
  <div class="calendar-board">
    <!-- Toolbar -->
    <div class="board-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button size="small" @click="prevDate" :icon="'ArrowLeft'" :disabled="loading">{{ prevButtonLabel }}</el-button>
          <el-button size="small" @click="goToday" :disabled="loading">今天</el-button>
          <el-button size="small" @click="nextDate" :disabled="loading">{{ nextButtonLabel }}</el-button>
        </el-button-group>
        <el-date-picker
          v-model="currentDate"
          type="date"
          class="toolbar-date-picker"
          @change="onDateChange"
          :disabled="loading"
          value-format="YYYY-MM-DD"
        />
      </div>

      <div class="toolbar-center">
        <el-radio-group v-model="viewMode" @change="onViewChange" size="small">
          <el-radio-button value="day">日视图</el-radio-button>
          <el-radio-button value="week">周视图</el-radio-button>
        </el-radio-group>
      </div>

      <div class="toolbar-right">
        <el-select
          v-model="filterEmployeeId"
          placeholder="全部技师"
          clearable
          size="small"
          class="toolbar-select"
          @change="fetchData"
        >
          <el-option
            v-for="e in employeeList"
            :key="e.id"
            :label="e.name"
            :value="e.id"
          />
        </el-select>
        <el-select
          v-model="filterStatus"
          placeholder="全部状态"
          clearable
          size="small"
          class="toolbar-select status-select"
          @change="fetchData"
        >
          <el-option :value="1" label="已预约" />
          <el-option :value="2" label="已到店" />
          <el-option :value="3" label="已完成" />
          <el-option :value="4" label="已取消" />
          <el-option :value="5" label="爽约" />
        </el-select>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      class="calendar-error"
      type="error"
      show-icon
      :closable="false"
      :title="errorMessage"
    >
      <template #default>
        <el-button type="primary" text size="small" @click="fetchData">重试</el-button>
      </template>
    </el-alert>

    <!-- View content -->
    <DayView
      v-if="viewMode === 'day'"
      :technicians="dayData.technicians"
      :unassigned-appointments="dayData.unassignedAppointments"
      :current-date="currentDate"
      :stats="dayData.stats"
      :business-hours="dayData.businessHours"
      :loading="loading"
      @select-appointment="openAppointment"
      @create-appointment="openCreateDialog"
      @select-tech="onSelectTech"
    />

    <WeekView
      v-else
      :days="weekData.days"
      :loading="loading"
      @select-date="goDate"
    />

    <!-- Create dialog -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建预约"
      width="min(500px, 92vw)"
      :close-on-click-modal="false"
      @closed="resetCreateForm"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="日期">
          <span class="form-static">{{ createForm.appointmentDate }}</span>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="createForm.startTime"
            start="08:00"
            step="00:30"
            end="21:00"
            placeholder="选择开始时间"
            class="field-full"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="createForm.endTime"
            start="08:00"
            step="00:30"
            end="21:00"
            placeholder="选择结束时间"
            class="field-full"
          />
        </el-form-item>
        <el-form-item label="技师">
          <el-select v-model="createForm.employeeId" placeholder="选择技师" clearable class="field-full">
            <el-option
              v-for="e in employeeList"
              :key="e.id"
              :label="e.name"
              :value="e.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会员" required>
          <el-select
            v-model="createForm.memberId"
            filterable
            remote
            reserve-keyword
            placeholder="搜索会员"
            :remote-method="searchMembers"
            :loading="memberSearchLoading"
            class="field-full"
          >
            <el-option
              v-for="m in memberOptions"
              :key="m.id"
              :label="`${m.name} (${m.phone || '-'})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目" required>
          <el-select v-model="createForm.serviceItemId" placeholder="选择服务项目" class="field-full">
            <el-option
              v-for="s in serviceOptions"
              :key="s.id"
              :label="`${s.name} (¥${s.price})`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- Convert to order dialog -->
    <el-dialog
      v-model="convertDialogVisible"
      title="转消费订单"
      width="min(400px, 92vw)"
    >
      <el-form :model="convertForm" label-width="80px">
        <el-form-item label="支付方式" required>
          <el-select v-model="convertForm.payMethod" class="field-full">
            <el-option :value="1" label="现金" />
            <el-option :value="2" label="余额" />
            <el-option :value="3" label="微信" />
            <el-option :value="4" label="支付宝" />
            <el-option :value="5" label="银行卡" />
            <el-option :value="6" label="储值卡" />
            <el-option :value="7" label="团购券" />
            <el-option :value="8" label="混合" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="convertForm.payRemark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConvert" :loading="convertLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- Detail drawer -->
    <AppointmentDrawer
      v-model="drawerVisible"
      :appointment="selectedAppointment"
      :current-date="currentDate"
      :technician-name="selectedTechnicianName"
      @action-done="onActionDone"
      @closed="selectedAppointment = null"
    />
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { appointmentApi } from '@/api/appointment'
import { memberApi } from '@/api/member'
import { getEmployeeList } from '@/api/employee'
import { serviceApi } from '@/api/service'
import DayView from './DayView.vue'
import WeekView from './WeekView.vue'
import AppointmentDrawer from './AppointmentDrawer.vue'

const viewMode = ref('day')
const currentDate = ref(new Date().toISOString().slice(0, 10))
const loading = ref(false)
const errorMessage = ref('')

const filterEmployeeId = ref(null)
const filterStatus = ref(null)

const dayData = ref({ technicians: [], unassignedAppointments: [], stats: null, businessHours: null })
const weekData = ref({ days: [] })

const employeeList = ref([])
const memberOptions = ref([])
const serviceOptions = ref([])
const memberSearchLoading = ref(false)

// Create dialog
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  appointmentDate: '',
  startTime: '',
  endTime: '',
  employeeId: null,
  memberId: null,
  serviceItemId: null,
  remark: ''
})

// Convert dialog
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const convertForm = reactive({ payMethod: 1, payRemark: '' })
const convertAppointmentId = ref(null)

// Detail drawer
const drawerVisible = ref(false)
const selectedAppointment = ref(null)
const selectedTechnicianName = ref('')

const prevButtonLabel = computed(() => viewMode.value === 'day' ? '前一天' : '上一周')
const nextButtonLabel = computed(() => viewMode.value === 'day' ? '后一天' : '下一周')

function getWeekStart(dateStr) {
  const d = new Date(dateStr)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  return d.toISOString().slice(0, 10)
}

async function fetchDayData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = { date: currentDate.value }
    if (filterEmployeeId.value) params.employeeId = filterEmployeeId.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await appointmentApi.calendarDay(params)
    dayData.value = {
      technicians: res?.technicians || [],
      unassignedAppointments: res?.unassignedAppointments || [],
      stats: res?.stats || null,
      businessHours: res?.businessHours || null
    }
  } catch {
    errorMessage.value = '日历数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function fetchWeekData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = { startDate: getWeekStart(currentDate.value) }
    if (filterEmployeeId.value) params.employeeId = filterEmployeeId.value
    const res = await appointmentApi.calendarWeek(params)
    weekData.value = { days: res?.days || [] }
  } catch {
    errorMessage.value = '周视图数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function fetchData() {
  if (viewMode.value === 'day') {
    await fetchDayData()
  } else {
    await fetchWeekData()
  }
}

async function loadEmployees() {
  try {
    const res = await getEmployeeList({ page: 1, pageSize: 200 })
    employeeList.value = (res.list || []).filter(e => e.status === 1)
  } catch { /* ignore */ }
}

onMounted(async () => {
  await Promise.all([loadEmployees(), fetchDayData()])
})

function prevDate() {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'day') {
    d.setDate(d.getDate() - 1)
  } else {
    d.setDate(d.getDate() - 7)
  }
  currentDate.value = d.toISOString().slice(0, 10)
  fetchData()
}

function nextDate() {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'day') {
    d.setDate(d.getDate() + 1)
  } else {
    d.setDate(d.getDate() + 7)
  }
  currentDate.value = d.toISOString().slice(0, 10)
  fetchData()
}

function goToday() {
  currentDate.value = new Date().toISOString().slice(0, 10)
  fetchData()
}

function goDate(dateStr) {
  currentDate.value = dateStr
  viewMode.value = 'day'
  fetchDayData()
}

function onDateChange() {
  fetchData()
}

function onViewChange() {
  fetchData()
}

function onSelectTech(tech) {
  filterEmployeeId.value = tech.id
  fetchDayData()
}

function openAppointment(apt) {
  selectedAppointment.value = apt
  const tech = dayData.value.technicians?.find(t =>
    t.appointments?.some(a => a.id === apt.id)
  )
  selectedTechnicianName.value = tech?.name || apt.technicianName || ''
  drawerVisible.value = true
}

function openCreateDialog(ctx) {
  createForm.appointmentDate = ctx.date || currentDate.value
  createForm.startTime = ctx.startTime || ''
  createForm.employeeId = ctx.employeeId || null
  createForm.endTime = ''
  createForm.memberId = null
  createForm.serviceItemId = null
  createForm.remark = ''
  loadServiceOptions()
  createDialogVisible.value = true
}

function resetCreateForm() {
  createForm.memberId = null
  createForm.serviceItemId = null
}

async function searchMembers(query) {
  if (!query) return
  memberSearchLoading.value = true
  try {
    const res = await memberApi.list({ page: 1, pageSize: 20, name: query })
    memberOptions.value = res.list || []
  } catch { /* ignore */ } finally {
    memberSearchLoading.value = false
  }
}

async function loadServiceOptions() {
  try {
    const res = await serviceApi.getItems()
    serviceOptions.value = (res || []).filter(s => s.status === 1)
  } catch { /* ignore */ }
}

async function submitCreate() {
  if (!createForm.memberId || !createForm.serviceItemId) {
    ElMessage.warning('请选择会员和服务项目')
    return
  }
  if (!createForm.startTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  createLoading.value = true
  try {
    await appointmentApi.create({
      memberId: createForm.memberId,
      serviceItemId: createForm.serviceItemId,
      employeeId: createForm.employeeId,
      appointmentDate: createForm.appointmentDate,
      startTime: createForm.startTime ? createForm.startTime + ':00' : null,
      endTime: createForm.endTime ? createForm.endTime + ':00' : null,
      remark: createForm.remark,
      status: 1
    })
    ElMessage.success('预约创建成功')
    createDialogVisible.value = false
    fetchDayData()
  } catch {
    // handled by interceptor
  } finally {
    createLoading.value = false
  }
}

function onActionDone() {
  fetchDayData()
  drawerVisible.value = false
}
</script>

<style scoped>
.calendar-board {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.board-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  border: 1px solid var(--border-color);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-date-picker {
  width: 160px;
  margin-left: 8px;
}

.toolbar-select {
  width: 140px;
}

.status-select {
  width: 110px;
  margin-left: 8px;
}

.calendar-error {
  border-radius: var(--radius-sm);
}

.field-full {
  width: 100%;
}

.form-static {
  color: var(--text-primary);
  font-weight: 500;
}

@media (max-width: 767px) {
  .board-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .toolbar-left,
  .toolbar-right {
    flex-wrap: wrap;
  }

  .toolbar-center,
  .toolbar-date-picker,
  .toolbar-select,
  .status-select {
    width: 100%;
    margin-left: 0;
  }

  .toolbar-left :deep(.el-button-group) {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    width: 100%;
  }

  .toolbar-left :deep(.el-button) {
    min-width: 0;
  }

  .toolbar-center :deep(.el-radio-group) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    width: 100%;
  }

  .toolbar-center :deep(.el-radio-button__inner) {
    width: 100%;
  }
}
</style>

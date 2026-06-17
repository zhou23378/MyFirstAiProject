<template>
  <div class="schedule-page">
    <PageHeader title="排班考勤" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '人员管理'}]">
      <template #extra>
        <el-button type="primary" @click="dialogShift = true">班次模板</el-button>
      </template>
    </PageHeader>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="排班管理" name="schedule" />
      <el-tab-pane label="考勤打卡" name="clock" />
      <el-tab-pane label="考勤记录" name="record" />
    </el-tabs>

    <!-- ==================== 排班管理 ==================== -->
    <TabTransition v-if="activeTab === 'schedule'" tab-key="schedule">
      <div class="action-bar">
        <el-button @click="prevWeek"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="week-label">{{ weekLabel }}</span>
        <el-button @click="nextWeek"><el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <el-card shadow="never" v-loading="scheduleLoading">
        <div class="schedule-grid" v-if="employees.length > 0">
          <div class="grid-header">
            <div class="grid-cell header-cell">员工</div>
            <div v-for="d in weekDays" :key="d.date" class="grid-cell header-cell" :class="{ today: d.isToday }">
              {{ d.label }}<br />{{ d.date.substring(5) }}
            </div>
          </div>
          <div v-for="emp in employees" :key="emp.id" class="grid-row">
            <div class="grid-cell emp-cell">{{ emp.name }}</div>
            <div v-for="d in weekDays" :key="d.date" class="grid-cell data-cell" :class="{ today: d.isToday }" @click="setSchedule(emp, d.date)">
              <el-tag v-if="getScheduleTag(emp.id, d.date)" :color="getScheduleTag(emp.id, d.date).color" size="small" effect="dark" closable @close="removeSchedule(emp.id, d.date)">
                {{ getScheduleTag(emp.id, d.date).name }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 选择班次弹窗 -->
      <el-dialog v-model="dialogSetShift" title="选择班次" width="300px">
        <el-radio-group v-model="selectedShiftId" style="display:flex;flex-direction:column;gap:8px">
          <el-radio v-for="s in shifts" :key="s.id" :value="s.id">{{ s.name }}（{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}）</el-radio>
          <el-radio :value="null">休息</el-radio>
        </el-radio-group>
        <template #footer>
          <el-button @click="dialogSetShift = false">取消</el-button>
          <el-button type="primary" @click="confirmSetSchedule">确定</el-button>
        </template>
      </el-dialog>

      <!-- 班次模板弹窗 -->
      <el-dialog v-model="dialogShift" title="班次模板" width="480px">
        <el-table :data="shifts" size="small">
          <el-table-column prop="name" label="名称" min-width="80" />
          <el-table-column label="时间" min-width="160">
            <template #default="{ row }">{{ row.startTime?.substring(0,5) }} - {{ row.endTime?.substring(0,5) }}</template>
          </el-table-column>
          <el-table-column label="颜色" min-width="60">
            <template #default="{ row }"><div :style="{ width:'20px',height:'20px',background:row.color,borderRadius:'4px' }" /></template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button size="small" text @click="editShift(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleShiftDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" size="small" @click="openShiftDialog()" style="margin-top:12px">新增班次</el-button>
      </el-dialog>

      <!-- 班次CRUD弹窗 -->
      <el-dialog v-model="dialogShiftForm" :title="shiftEdit ? '编辑班次' : '新增班次'" width="420px">
        <el-form :model="shiftForm" label-width="80px">
          <el-form-item label="名称"><el-input v-model="shiftForm.name" /></el-form-item>
          <el-form-item label="开始时间"><el-time-picker v-model="shiftForm.startTime" format="HH:mm" value-format="HH:mm:ss" /></el-form-item>
          <el-form-item label="结束时间"><el-time-picker v-model="shiftForm.endTime" format="HH:mm" value-format="HH:mm:ss" /></el-form-item>
          <el-form-item label="颜色"><el-color-picker v-model="shiftForm.color" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogShiftForm = false">取消</el-button>
          <el-button type="primary" :loading="shiftSubmitting" @click="handleShiftSubmit">确定</el-button>
        </template>
      </el-dialog>
    </TabTransition>

    <!-- ==================== 考勤打卡 ==================== -->
    <TabTransition v-if="activeTab === 'clock'" tab-key="clock">
      <el-card shadow="never" style="max-width:480px">
        <el-form label-width="80px">
          <el-form-item label="员工">
            <el-select v-model="clockEmployeeId" filterable placeholder="选择员工" style="width:100%">
              <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="clockIn" :loading="clocking" style="width:120px">上班打卡</el-button>
            <el-button type="warning" size="large" @click="clockOut" :loading="clocking" style="width:120px;margin-left:20px">下班打卡</el-button>
          </el-form-item>
        </el-form>

        <el-divider />
        <div class="clock-today">
          <h4>今日打卡记录</h4>
          <template v-if="recordLoading">
            <SkeletonTable :rows="3" :cols="4" />
          </template>
          <template v-else>
            <el-table :data="todayRecords" size="small">
              <el-table-column prop="employeeName" label="员工" min-width="100" />
              <el-table-column prop="clock_in" label="上班" min-width="160" />
              <el-table-column prop="clock_out" label="下班" min-width="160" />
              <el-table-column label="状态" min-width="80">
                <template #default="{ row }">
                  <el-tag :type="clockStatusTag(row.status)" size="small">{{ clockStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <template #empty>
                <EmptyState description="暂无打卡记录" />
              </template>
            </el-table>
          </template>
        </div>
      </el-card>
    </TabTransition>

    <!-- ==================== 考勤记录 ==================== -->
    <TabTransition v-if="activeTab === 'record'" tab-key="record">
      <div class="action-bar">
        <el-date-picker v-model="recordQuery.date" placeholder="日期" clearable @change="fetchRecords" style="width:160px" />
        <el-select v-model="recordQuery.employeeId" placeholder="员工" clearable @change="fetchRecords" style="width:160px">
          <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-button type="primary" @click="fetchRecords">查询</el-button>
      </div>

      <el-card shadow="never">
        <template v-if="recordLoading">
          <SkeletonTable :rows="5" :cols="6" />
        </template>
        <template v-else>
          <el-table :data="recordList" stripe>
            <el-table-column prop="employeeName" label="员工" min-width="100" />
            <el-table-column prop="date" label="日期" min-width="120" />
            <el-table-column prop="clock_in" label="上班打卡" min-width="160" />
            <el-table-column prop="clock_out" label="下班打卡" min-width="160" />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag :type="clockStatusTag(row.status)" size="small">{{ clockStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" />
            <template #empty>
              <EmptyState description="暂无考勤记录" />
            </template>
          </el-table>

          <div class="pagination">
          <el-pagination
            v-model="recordQuery.page" :page-size="recordQuery.pageSize" :total="recordTotal"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
            @current-change="(p) => { recordQuery.page = p; fetchRecords(); }"
            @size-change="(s) => { recordQuery.pageSize = s; recordQuery.page = 1; fetchRecords(); }"
          />
        </div>
        </template>
      </el-card>
    </TabTransition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { shiftTemplateApi, scheduleApi, attendanceApi } from "@/api/schedule";
import { getEmployeeList } from "@/api/employee";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowLeft, ArrowRight } from "@element-plus/icons-vue";
import TabTransition from "@/components/TabTransition.vue";
import PageHeader from "@/components/PageHeader.vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import EmptyState from "@/components/EmptyState.vue";

const activeTab = ref("schedule");

// ========== Common ==========
const employees = ref([]);
async function fetchEmployees() {
  try {
    const empRes = await getEmployeeList({ pageSize: 200, status: 1 });
    employees.value = empRes.list || [];
  } catch { ElMessage.error("加载员工列表失败"); }
}

// ========== Week navigation ==========
const weekOffset = ref(0);
const weekDays = computed(() => {
  const today = new Date();
  const day = today.getDay();
  const monday = new Date(today);
  monday.setDate(today.getDate() - (day === 0 ? 6 : day - 1) + weekOffset.value * 7);
  const days = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const ds = d.toISOString().split("T")[0];
    days.push({
      date: ds,
      label: ["周一", "周二", "周三", "周四", "周五", "周六", "周日"][i],
      isToday: ds === today.toISOString().split("T")[0]
    });
  }
  return days;
});
const weekLabel = computed(() => `${weekDays.value[0].date} ~ ${weekDays.value[6].date}`);
function prevWeek() { weekOffset.value--; fetchWeekSchedule(); }
function nextWeek() { weekOffset.value++; fetchWeekSchedule(); }

// ========== Shifts ==========
const shifts = ref([]);
const scheduleLoading = ref(false);
const scheduleMap = ref({});
const dialogSetShift = ref(false);
const dialogShift = ref(false);
const dialogShiftForm = ref(false);
const shiftEdit = ref(false);
const shiftEditId = ref(null);
const shiftSubmitting = ref(false);
const selectedShiftId = ref(null);
const pendingEmpId = ref(null);
const pendingDate = ref(null);
const shiftForm = reactive({ name: "", startTime: "", endTime: "", color: "#6c5ce7" });

async function fetchShifts() {
  try { shifts.value = await shiftTemplateApi.list(); } catch { ElMessage.error("加载班次失败"); }
}
async function fetchWeekSchedule() {
  scheduleLoading.value = true;
  try {
    const list = await scheduleApi.week({
      startDate: weekDays.value[0].date,
      endDate: weekDays.value[6].date
    });
    const map = {};
    list.forEach(s => {
      map[`${s.employeeId}_${s.date}`] = { name: s.shiftName, color: s.color, id: s.id };
    });
    scheduleMap.value = map;
  } catch { ElMessage.error("加载排班失败"); } finally { scheduleLoading.value = false; }
}

function getScheduleTag(empId, date) {
  return scheduleMap.value[`${empId}_${date}`] || null;
}

function setSchedule(emp, date) {
  pendingEmpId.value = emp.id;
  pendingDate.value = date;
  const existing = getScheduleTag(emp.id, date);
  selectedShiftId.value = existing ? existing.id : null;
  dialogSetShift.value = true;
}

async function confirmSetSchedule() {
  try {
    if (selectedShiftId.value === null) {
      const existing = getScheduleTag(pendingEmpId.value, pendingDate.value);
      if (existing && existing.id) {
        await scheduleApi.remove(existing.id);
      }
    } else {
      await scheduleApi.batchSet(pendingEmpId.value, [{
        date: pendingDate.value,
        shiftId: selectedShiftId.value
      }]);
    }
    dialogSetShift.value = false;
    ElMessage.success("排班已更新");
    fetchWeekSchedule();
  } catch { ElMessage.error("排班操作失败"); }
}

async function removeSchedule(empId, date) {
  try {
    const existing = getScheduleTag(empId, date);
    if (existing && existing.id) {
      await scheduleApi.remove(existing.id);
      ElMessage.success("已删除");
      fetchWeekSchedule();
    }
  } catch { ElMessage.error("删除排班失败"); }
}

// Shift template CRUD
function openShiftDialog(row) {
  if (row) {
    shiftEdit.value = true;
    shiftEditId.value = row.id;
    shiftForm.name = row.name;
    shiftForm.startTime = row.startTime;
    shiftForm.endTime = row.endTime;
    shiftForm.color = row.color;
  } else {
    shiftEdit.value = false;
    shiftEditId.value = null;
    shiftForm.name = "";
    shiftForm.startTime = "";
    shiftForm.endTime = "";
    shiftForm.color = "#409EFF";
  }
  dialogShiftForm.value = true;
}
function editShift(row) { openShiftDialog(row); }
async function handleShiftSubmit() {
  shiftSubmitting.value = true;
  try {
    if (shiftEdit.value) {
      await shiftTemplateApi.update(shiftEditId.value, { ...shiftForm });
    } else {
      await shiftTemplateApi.create({ ...shiftForm });
    }
    ElMessage.success(shiftEdit.value ? "修改成功" : "新增成功");
    dialogShiftForm.value = false;
    fetchShifts();
  } catch { ElMessage.error('保存班次失败') } finally { shiftSubmitting.value = false; }
}
async function handleShiftDelete(row) {
  try {
    await ElMessageBox.confirm("删除班次模板？", "提示", { type: "warning" });
    await shiftTemplateApi.remove(row.id);
    ElMessage.success("删除成功");
    fetchShifts();
  } catch { ElMessage.error('删除失败') }
}

// ========== Clock ==========
const clockEmployeeId = ref(null);
const clocking = ref(false);
const todayRecords = ref([]);

async function clockIn() {
  if (!clockEmployeeId.value) { ElMessage.warning("请选择员工"); return; }
  clocking.value = true;
  try {
    await attendanceApi.clock({ employeeId: clockEmployeeId.value, type: 1 });
    ElMessage.success("上班打卡成功");
    fetchTodayRecords();
  } catch { ElMessage.error('打卡失败') } finally { clocking.value = false; }
}
async function clockOut() {
  if (!clockEmployeeId.value) { ElMessage.warning("请选择员工"); return; }
  clocking.value = true;
  try {
    await attendanceApi.clock({ employeeId: clockEmployeeId.value, type: 2 });
    ElMessage.success("下班打卡成功");
    fetchTodayRecords();
  } catch { ElMessage.error('打卡失败') } finally { clocking.value = false; }
}
async function fetchTodayRecords() {
  try {
    const today = new Date().toISOString().split("T")[0];
    const res = await attendanceApi.page({ date: today, pageSize: 50 });
    todayRecords.value = res.list || [];
  } catch { ElMessage.error("加载今日打卡记录失败"); }
}

// ========== Records ==========
const recordList = ref([]);
const recordTotal = ref(0);
const recordLoading = ref(false);
const recordQuery = reactive({ page: 1, pageSize: 10, date: null, employeeId: null });

async function fetchRecords() {
  recordLoading.value = true;
  try {
    const params = { ...recordQuery };
    if (params.date) params.date = new Date(params.date).toISOString().split("T")[0];
    const res = await attendanceApi.page(params);
    recordList.value = res.list || [];
    recordTotal.value = res.total || 0;
  } catch { ElMessage.error("加载考勤记录失败"); } finally { recordLoading.value = false; }
}

function clockStatusText(s) { return { 1: "正常", 2: "迟到", 3: "早退", 4: "缺卡" }[s] || "未知"; }
function clockStatusTag(s) { return { 1: "success", 2: "warning", 3: "warning", 4: "danger" }[s] || "info"; }

// ========== Tab switch ==========
function onTabChange(name) {
  if (name === "schedule") { fetchEmployees(); fetchShifts(); fetchWeekSchedule(); }
  else if (name === "clock") { fetchEmployees(); fetchTodayRecords(); }
  else if (name === "record") { fetchEmployees(); fetchRecords(); }
}

onMounted(() => {
  fetchEmployees();
  fetchShifts();
  fetchWeekSchedule();
});
</script>

<style scoped>
.schedule-page {
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

.week-label {
  font-weight: 600;
  min-width: 200px;
  text-align: center;
}

.schedule-grid {
  overflow-x: auto;
}

.grid-header {
  display: flex;
}

.grid-row {
  display: flex;
}

.grid-cell {
  flex: 1;
  min-width: 120px;
  padding: 8px;
  border: 1px solid var(--border-subtle);
  text-align: center;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-cell {
  background: var(--bg-overlay-subtle);
  font-weight: 600;
  font-size: 13px;
}

.emp-cell {
  font-weight: 600;
  background: var(--bg-overlay-subtle);
}

.data-cell {
  cursor: pointer;
}

.data-cell:hover {
  background: var(--bg-primary-ghost);
}

.today {
  background: rgba(var(--primary-rgb), 0.06);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.clock-today {
  margin-top: 12px;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .schedule-page {
    padding: 0;
  }

  .action-bar {
    flex-wrap: wrap;
  }
}
</style>

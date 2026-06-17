<template>
  <div class="appointment-page">
    <PageHeader title="预约管理" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '会员管理', path: '/member'}]">
      <template #extra>
        <el-button v-if="activeTab === 'list'" type="primary" @click="openDialog()">新增预约</el-button>
      </template>
    </PageHeader>

    <!-- 视图切换 -->
    <el-tabs v-model="activeTab" class="appointment-tabs">
      <el-tab-pane label="列表视图" name="list" />
      <el-tab-pane label="日历视图" name="calendar" />
    </el-tabs>

    <template v-if="activeTab === 'list'">

    <!-- 筛选栏 -->
    <DrawerFilter
      :has-active-filters="hasActiveFilters"
      :active-count="activeFilterCount"
      @reset="resetQuery"
      @confirm="fetchData"
    >
      <template #toolbar>
        <el-input
          v-model="quickSearch"
          placeholder="搜索会员姓名…"
          clearable
          @keyup.enter="onQuickSearch"
          @clear="onQuickSearch"
        />
      </template>

      <template #filters>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option :value="1" label="已预约" />
            <el-option :value="2" label="已到店" />
            <el-option :value="3" label="已完成" />
            <el-option :value="4" label="已取消" />
            <el-option :value="5" label="爽约" />
          </el-select>
        </el-form-item>
      </template>

      <template #actions>
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </template>
    </DrawerFilter>

    <!-- 数据列表 -->
    <template v-if="loading">
      <SkeletonTable :rows="5" :cols="8" />
    </template>
    <ResponsiveDataList
      v-else
      :data="tableData"
      :columns="columns"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      primary-field="memberName"
      empty-text="暂无预约数据"
      actions-width="160"
      @page-change="onPageChange"
    >
      <template #timeSlot="{ row }">
        {{ row.startTime }} - {{ row.endTime }}
      </template>
      <template #status="{ row }">
        <el-tag :type="statusTagType(row.status)" size="small">
          {{ statusText(row.status) }}
        </el-tag>
      </template>
      <template #actions="{ row }">
        <div class="action-cell">
          <template v-if="row.status === 1">
            <el-dropdown trigger="click" @command="(cmd) => changeStatus(row, cmd)">
              <el-button type="primary" size="small" plain>
                状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="2">到店</el-dropdown-item>
                  <el-dropdown-item :command="3">完成</el-dropdown-item>
                  <el-dropdown-item :command="4">取消</el-dropdown-item>
                  <el-dropdown-item :command="5">爽约</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else-if="row.status === 2">
            <el-button type="primary" size="small" plain @click="changeStatus(row, 3)">完成</el-button>
          </template>
          <el-button
            v-if="row.status === 1 || row.status === 2"
            type="success"
            size="small"
            plain
            @click="openConvertDialog(row)"
          >转消费</el-button>
          <el-dropdown trigger="click">
            <el-button size="small" text circle :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openDialog(row)">编辑</el-dropdown-item>
                <el-dropdown-item @click="handleDelete(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
    </ResponsiveDataList>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑预约' : '新增预约'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="会员" prop="memberId">
          <el-select v-model="form.memberId" filterable placeholder="搜索会员" @focus="loadMembers">
            <el-option v-for="m in members" :key="m.id" :label="m.name + ' ' + m.phone" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目">
          <el-select v-model="form.serviceItemId" filterable placeholder="选择服务" @focus="loadServices">
            <el-option v-for="s in serviceItems" :key="s.id" :label="`${s.name} ¥${s.price} ${s.duration}分钟`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="技师">
          <el-select v-model="form.employeeId" filterable placeholder="选择技师" @focus="loadEmployees">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="appointmentDate">
          <el-date-picker v-model="form.appointmentDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-row :gutter="12" style="width:100%">
            <el-col :span="12">
              <el-time-select v-model="form.startTime" placeholder="开始" start="08:00" step="00:30" end="21:00" style="width:100%" />
            </el-col>
            <el-col :span="12">
              <el-time-select v-model="form.endTime" placeholder="结束" start="08:00" step="00:30" end="21:00" style="width:100%" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="1" label="已预约" />
            <el-option :value="2" label="已到店" />
            <el-option :value="3" label="已完成" />
            <el-option :value="4" label="已取消" />
            <el-option :value="5" label="爽约" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 转消费弹窗 -->
    <el-dialog v-model="convertDialogVisible" title="预约转消费" width="400px" :close-on-click-modal="false">
      <div class="convert-info" v-if="convertAppointment">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="会员">{{ convertAppointment.memberName }}</el-descriptions-item>
          <el-descriptions-item label="服务项目">{{ convertAppointment.serviceItemName }}</el-descriptions-item>
          <el-descriptions-item label="技师">{{ convertAppointment.employeeName || "未指定" }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ convertAppointment.appointmentDate }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form label-width="80px" style="margin-top:16px">
        <el-form-item label="支付方式">
          <el-select v-model="convertPayMethod" style="width:100%">
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
          <el-input v-model="convertPayRemark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="converting" @click="handleConvert">确认转消费</el-button>
      </template>
    </el-dialog>
    </template>

    <!-- 日历视图 -->
    <CalendarBoard v-else />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { appointmentApi } from "@/api/appointment";
import { memberApi } from "@/api/member";
import { serviceApi } from "@/api/service";
import { getEmployeeList } from "@/api/employee";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowDown, MoreFilled } from "@element-plus/icons-vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import PageHeader from "@/components/PageHeader.vue";
import DrawerFilter from "@/components/DrawerFilter.vue";
import ResponsiveDataList from "@/components/ResponsiveDataList.vue";
import CalendarBoard from "./components/CalendarBoard.vue";

const activeTab = ref("list");

const columns = [
  { prop: "id", label: "ID", width: "60", hideOnCard: true },
  { prop: "memberName", label: "会员", width: "100", cardPrimary: true },
  { prop: "serviceItemName", label: "服务项目", width: "120" },
  { prop: "employeeName", label: "技师", width: "100" },
  { prop: "appointmentDate", label: "预约日期", width: "120" },
  { prop: "timeSlot", label: "时间", width: "140" },
  { prop: "status", label: "状态", width: "90", format: (v) => statusMap[v] || "未知" },
  { prop: "remark", label: "备注", minWidth: "120", hideOnCard: true },
];

const loading = ref(false);
const submitting = ref(false);
const tableData = ref([]);
const total = ref(0);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editId = ref(null);
const formRef = ref(null);
const convertDialogVisible = ref(false);
const convertAppointment = ref(null);
const convertPayMethod = ref(1);
const convertPayRemark = ref("");
const converting = ref(false);
const members = ref([]);
const serviceItems = ref([]);
const employees = ref([]);
const quickSearch = ref("");

const query = reactive({
  page: 1,
  pageSize: 10,
  dateRange: null,
  status: null,
});

const form = reactive({
  memberId: null,
  serviceItemId: null,
  employeeId: null,
  appointmentDate: "",
  startTime: "",
  endTime: "",
  status: 1,
  remark: "",
});

const rules = {
  memberId: [{ required: true, message: "请选择会员", trigger: "change" }],
  appointmentDate: [{ required: true, message: "请选择日期", trigger: "change" }],
};

const statusMap = { 1: "已预约", 2: "已到店", 3: "已完成", 4: "已取消", 5: "爽约" };
const statusTagMap = { 1: "primary", 2: "success", 3: "info", 4: "warning", 5: "danger" };

function statusText(s) { return statusMap[s] || "未知"; }
function statusTagType(s) { return statusTagMap[s] || "info"; }

const hasActiveFilters = computed(() => {
  return !!(query.dateRange || query.status != null);
});

const activeFilterCount = computed(() => {
  let n = 0;
  if (query.dateRange) n++;
  if (query.status != null) n++;
  return n;
});

function onQuickSearch() {
  fetchData();
}

async function fetchData() {
  loading.value = true;
  try {
    const params = { page: query.page, pageSize: query.pageSize, status: query.status };
    if (query.dateRange) {
      params.startDate = query.dateRange[0].toISOString().split("T")[0];
      params.endDate = query.dateRange[1].toISOString().split("T")[0];
    }
    if (quickSearch.value) {
      params.memberName = quickSearch.value;
    }
    const res = await appointmentApi.page(params);
    tableData.value = res.list || [];
    total.value = res.total || 0;
  } catch { ElMessage.error("加载预约列表失败"); } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.page = 1;
  query.status = null;
  query.dateRange = null;
  quickSearch.value = "";
  fetchData();
}

function onPageChange(page) {
  query.page = page;
  fetchData();
}

async function loadMembers() {
  if (members.value.length > 0) return;
  try {
    const res = await memberApi.page({ page: 1, pageSize: 200 });
    members.value = res.list || [];
  } catch { ElMessage.error("加载会员列表失败"); }
}

async function loadServices() {
  if (serviceItems.value.length > 0) return;
  try { serviceItems.value = await serviceApi.getItems(); } catch { ElMessage.error("加载服务项目失败"); }
}

async function loadEmployees() {
  if (employees.value.length > 0) return;
  try {
    const empRes = await getEmployeeList({ pageSize: 200, status: 1 });
    employees.value = empRes.list || [];
  } catch { ElMessage.error("加载员工列表失败"); }
}

function openDialog(row) {
  resetForm();
  if (row) {
    isEdit.value = true;
    editId.value = row.id;
    form.memberId = row.memberId;
    form.serviceItemId = row.serviceItemId;
    form.employeeId = row.employeeId;
    form.appointmentDate = row.appointmentDate;
    form.startTime = row.startTime ? row.startTime.substring(0, 5) : "";
    form.endTime = row.endTime ? row.endTime.substring(0, 5) : "";
    form.status = row.status;
    form.remark = row.remark || "";
  } else {
    isEdit.value = false;
    editId.value = null;
  }
  dialogVisible.value = true;
}

function resetForm() {
  form.memberId = null;
  form.serviceItemId = null;
  form.employeeId = null;
  form.appointmentDate = "";
  form.startTime = "";
  form.endTime = "";
  form.status = 1;
  form.remark = "";
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    const data = { ...form };
    data.startTime = data.startTime ? data.startTime + ':00' : null;
    data.endTime = data.endTime ? data.endTime + ':00' : null;
    if (isEdit.value) {
      await appointmentApi.update(editId.value, data);
      ElMessage.success("修改成功");
    } else {
      await appointmentApi.create(data);
      ElMessage.success("新增成功");
    }
    dialogVisible.value = false;
    fetchData();
  } catch {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false;
  }
}

async function changeStatus(row, newStatus) {
  try {
    await appointmentApi.updateStatus(row.id, newStatus);
    ElMessage.success(`已更新为${statusText(newStatus)}`);
    fetchData();
  } catch { ElMessage.error('状态更新失败') }
}

function openConvertDialog(row) {
  convertAppointment.value = row;
  convertPayMethod.value = 1;
  convertPayRemark.value = "";
  convertDialogVisible.value = true;
}

async function handleConvert() {
  converting.value = true;
  try {
    await appointmentApi.convertToOrder(convertAppointment.value.id, {
      payMethod: convertPayMethod.value,
      payRemark: convertPayRemark.value || undefined,
    });
    ElMessage.success("已转为消费订单");
    convertDialogVisible.value = false;
    fetchData();
  } catch { ElMessage.error('转换失败，请重试') } finally { converting.value = false; }
}

function handleDelete(row) {
  ElMessageBox.confirm("确定删除该预约吗？", "提示", { type: "warning" })
    .then(async () => {
      await appointmentApi.remove(row.id);
      ElMessage.success("删除成功");
      fetchData();
    })
    .catch(() => {});
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.appointment-page {
  max-width: 1200px;
  margin: 0 auto;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

/* 卡片视图中的操作栏允许换行 */
:deep(.card-actions) {
  flex-wrap: wrap !important;
  justify-content: flex-end;
}

:deep(.card-actions .action-cell) {
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .appointment-page {
    padding: 0;
  }

  .action-cell {
    flex-wrap: wrap;
    gap: 4px;
    white-space: normal;
  }

  .action-cell .el-button {
    font-size: var(--font-size-mobile-sm);
    padding: 4px 8px;
  }

  .action-cell .el-button--small {
    min-height: 32px;
  }
}
</style>

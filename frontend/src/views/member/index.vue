<template>
  <div class="member-page">
    <PageHeader title="会员管理" :breadcrumb="[{label: '仪表盘', path: '/'}]">
      <template #extra>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增会员
        </el-button>
      </template>
    </PageHeader>

    <!-- AI 搜索结果 -->
    <div v-if="aiResults.length > 0" class="ai-results-bar">
      <span class="ai-results-title">AI 搜索结果：{{ aiResults.length }} 条</span>
      <div class="ai-result-tags">
        <el-tag
          v-for="(item, idx) in aiResults"
          :key="idx"
          :type="getSimType(item.similarity)"
          size="small"
          effect="dark"
          class="ai-result-tag"
          @click="router.push('/member/' + item.member_id)"
        >
          {{ item.name }} {{ item.similarity.toFixed(0) }}%
        </el-tag>
      </div>
      <el-button type="primary" link size="small" @click="clearAiResults">清除</el-button>
    </div>

    <!-- 筛选栏 -->
    <DrawerFilter
      :has-active-filters="hasActiveFilters"
      :active-count="activeFilterCount"
      @reset="handleReset"
      @confirm="handleSearch"
    >
      <template #toolbar>
        <div class="toolbar-row">
          <el-input
            v-model="query.name"
            placeholder="搜索姓名或手机号…"
            clearable
            class="toolbar-search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input
            v-model="aiQuery"
            placeholder="AI 搜索：高消费会员 / 流失客户…"
            clearable
            class="toolbar-ai"
            @keyup.enter="handleAiSearch"
            @clear="clearAiResults"
          >
            <template #prefix><el-icon><MagicStick /></el-icon></template>
            <template #append>
              <el-button :loading="aiSearching" @click="handleAiSearch">智能搜索</el-button>
            </template>
          </el-input>
        </div>
      </template>

      <template #filters>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <template v-if="showAdvanced">
          <el-form-item label="余额范围">
            <el-input-number v-model="query.balanceMin" :min="0" :precision="2" placeholder="最低" controls-position="right" style="width:130px" />
            <span style="margin:0 6px;color:var(--text-muted)">—</span>
            <el-input-number v-model="query.balanceMax" :min="0" :precision="2" placeholder="最高" controls-position="right" style="width:130px" />
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="query.level" placeholder="全部等级" clearable style="width:120px">
              <el-option v-for="lv in levelOptions" :key="lv.id" :label="lv.name" :value="lv.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="query.gender" placeholder="全部" clearable style="width:90px">
              <el-option :value="1" label="男" />
              <el-option :value="2" label="女" />
              <el-option :value="0" label="未知" />
            </el-select>
          </el-form-item>
          <el-form-item label="累计消费">
            <el-input-number v-model="query.totalConsumeMin" :min="0" :precision="2" placeholder="最低" controls-position="right" style="width:130px" />
            <span style="margin:0 6px;color:var(--text-muted)">—</span>
            <el-input-number v-model="query.totalConsumeMax" :min="0" :precision="2" placeholder="最高" controls-position="right" style="width:130px" />
          </el-form-item>
          <el-form-item label="未消费天数">
            <el-input-number v-model="query.lastConsumeDays" :min="1" placeholder="如60" controls-position="right" style="width:120px" />
            <span style="margin-left:4px;font-size:12px;color:var(--text-muted)">天以上</span>
          </el-form-item>
        </template>
      </template>

      <template #actions>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button text type="primary" @click="showAdvanced = !showAdvanced">
          <el-icon><Filter /></el-icon> 高级筛选
          <el-icon style="margin-left:2px">
            <ArrowDown v-if="!showAdvanced" />
            <ArrowUp v-else />
          </el-icon>
        </el-button>
      </template>
    </DrawerFilter>

    <!-- 操作栏 -->
    <div class="action-bar">
      <span class="churn-group">
        <el-button :type="churnMode ? 'warning' : ''" @click="toggleChurnMode">
          <el-icon><WarningFilled /></el-icon> {{ churnMode ? '返回全部' : '流失预警' }}
        </el-button>
        <el-input-number
          v-model="churnDays"
          :min="1"
          :max="999"
          controls-position="right"
          size="default"
          style="width: 110px"
          @change="onChurnDaysChange"
        />
        <span class="churn-days-label">天未消费</span>
      </span>
      <el-dropdown trigger="click">
        <el-button>
          更多操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleExport">
              <el-icon><Download /></el-icon> 导出 Excel
            </el-dropdown-item>
            <el-dropdown-item @click="handleDownloadTemplate">
              <el-icon><Download /></el-icon> 下载模板
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-upload
        :show-file-list="false"
        :before-upload="handleImport"
        accept=".xlsx,.xls"
        class="quick-upload"
      >
        <el-button><el-icon><Upload /></el-icon> 导入</el-button>
      </el-upload>
    </div>

    <!-- 数据列表 -->
    <template v-if="loading">
      <SkeletonTable :rows="5" :cols="7" />
    </template>
    <ResponsiveDataList
      v-else
      :data="tableData"
      :columns="columns"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.page"
      primary-field="name"
      empty-text="暂无会员数据"
      expandable
      @page-change="onPageChange"
      @size-change="(s) => { query.pageSize = s; query.page = 1; fetchData(); }"
    >
      <template #gender="{ row }">
        <el-tag
          :type="row.gender === 1 ? 'primary' : row.gender === 2 ? 'danger' : 'info'"
          size="small"
        >
          {{ row.gender === 1 ? "男" : row.gender === 2 ? "女" : "未知" }}
        </el-tag>
      </template>
      <template #level="{ row }">
        <el-tag :type="getLevelTagType(row.level)" size="small">
          {{ getLevelName(row.level) }}
        </el-tag>
      </template>
      <template #balance="{ row }">
        <span class="balance-text">¥{{ row.balance?.toFixed(2) || "0.00" }}</span>
      </template>
      <template #actions="{ row }">
        <el-button type="warning" link @click="openRechargeDialog(row)">
          <el-icon><Coin /></el-icon> 充值
        </el-button>
        <el-button type="primary" link @click="router.push(`/member/${row.id}`)">
          <el-icon><View /></el-icon> 详情
        </el-button>
        <el-button type="primary" link @click="openDialog(row)">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
        <el-button type="danger" link @click="handleDelete(row)">
          <el-icon><Delete /></el-icon> 删除
        </el-button>
      </template>
      <template #expand="{ row }">
        <div class="member-expand">
          <div class="expand-item">
            <span class="expand-label">标签</span>
            <span class="expand-value">
              <template v-if="row.tags">
                <el-tag
                  v-for="(t, i) in row.tags.split(',')"
                  :key="i"
                  size="small"
                  style="margin:2px"
                >{{ t.trim() }}</el-tag>
              </template>
              <span v-else class="expand-none">—</span>
            </span>
          </div>
          <div class="expand-item">
            <span class="expand-label">累计消费</span>
            <span class="expand-value">¥{{ (row.totalSpent || 0).toFixed(2) }}</span>
          </div>
          <div class="expand-item">
            <span class="expand-label">到店次数</span>
            <span class="expand-value">{{ row.visitCount ?? '—' }} 次</span>
          </div>
          <div class="expand-item">
            <span class="expand-label">最近到店</span>
            <span class="expand-value">{{ row.lastVisitTime || '—' }}</span>
          </div>
          <div class="expand-item">
            <span class="expand-label">生日</span>
            <span class="expand-value">{{ row.birthday || '—' }}</span>
          </div>
          <div class="expand-item" v-if="row.remark">
            <span class="expand-label">备注</span>
            <span class="expand-value">{{ row.remark }}</span>
          </div>
        </div>
      </template>
    </ResponsiveDataList>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会员' : '新增会员'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" placeholder="选择等级" clearable>
            <el-option v-for="lv in levelOptions" :key="lv.id" :label="lv.name" :value="lv.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="form.points" :min="0" />
        </el-form-item>
        <el-form-item label="余额">
          <el-input-number v-model="form.balance" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="逗号分隔，如：老客,高消费,烫染偏好" />
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择生日" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="生日类型">
          <el-radio-group v-model="form.birthdayType">
            <el-radio :value="1">公历</el-radio>
            <el-radio :value="2">农历</el-radio>
          </el-radio-group>
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

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="会员充值" width="400px" :close-on-click-modal="false">
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
        <el-form-item label="会员">
          <span style="font-weight:600">{{ rechargeMember?.name }}</span>
          <span style="margin-left:12px;color:var(--text-muted);font-size:13px">
            当前余额: ¥{{ rechargeMember?.balance?.toFixed(2) || "0.00" }}
          </span>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="rechargeForm.payMethod" style="width:100%">
            <el-option :value="1" label="现金" />
            <el-option :value="2" label="微信" />
            <el-option :value="3" label="支付宝" />
            <el-option :value="4" label="银行卡" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { memberApi } from "../../api/member";
import { memberLevelApi } from "../../api/memberLevel";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete, View, Coin, WarningFilled, Download, Upload, Search, MagicStick, Filter, ArrowDown, ArrowUp } from "@element-plus/icons-vue";
import { aiApi } from "../../api/ai";
import SkeletonTable from "../../components/SkeletonTable.vue";
import PageHeader from "../../components/PageHeader.vue";
import DrawerFilter from "../../components/DrawerFilter.vue";
import ResponsiveDataList from "../../components/ResponsiveDataList.vue";

const router = useRouter();

const columns = [
  { prop: "name", label: "姓名", minWidth: "120", cardPrimary: true },
  { prop: "phone", label: "手机号", minWidth: "130" },
  { prop: "gender", label: "性别", width: "70", format: (v) => v === 1 ? "男" : v === 2 ? "女" : "未知" },
  { prop: "level", label: "等级", minWidth: "100", format: (v) => levelMap.value[v] || `等级${v}` },
  { prop: "points", label: "积分", width: "70", hideOnCard: true },
  { prop: "balance", label: "余额", minWidth: "100", format: (v) => `¥${(v || 0).toFixed(2)}` },
  { prop: "createTime", label: "创建时间", minWidth: "160", hideOnCard: true },
];

const loading = ref(false);
const submitting = ref(false);
const tableData = ref([]);
const total = ref(0);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editId = ref(null);
const formRef = ref(null);
const rechargeDialogVisible = ref(false);
const rechargeMember = ref(null);
const recharging = ref(false);
const rechargeFormRef = ref(null);
const rechargeForm = reactive({ amount: 0, payMethod: 1, remark: "" });
const rechargeRules = {
  amount: [{ required: true, message: "请输入充值金额", trigger: "blur" }],
  payMethod: [{ required: true, message: "请选择支付方式", trigger: "change" }],
};
const levelOptions = ref([]);
const showAdvanced = ref(false);
const churnMode = ref(false);
const churnDays = ref(60);
const aiQuery = ref("");
const aiSearching = ref(false);
const aiResults = ref([]);

const hasActiveFilters = computed(() => {
  return !!(query.name || query.phone || query.level || query.gender != null ||
    query.balanceMin != null || query.balanceMax != null ||
    query.totalConsumeMin != null || query.totalConsumeMax != null ||
    query.lastConsumeDays != null);
});

const activeFilterCount = computed(() => {
  let n = 0;
  if (query.name) n++;
  if (query.phone) n++;
  if (query.level) n++;
  if (query.gender != null) n++;
  if (query.balanceMin != null || query.balanceMax != null) n++;
  if (query.totalConsumeMin != null || query.totalConsumeMax != null) n++;
  if (query.lastConsumeDays != null) n++;
  return n;
});

async function handleAiSearch() {
  const q = aiQuery.value.trim();
  if (!q) return;
  aiSearching.value = true;
  try {
    const res = await aiApi.search(q, 5);
    aiResults.value = res.results || [];
    if (aiResults.value.length === 0) {
      ElMessage.info("未找到匹配的会员，换种说法试试");
    }
  } catch {
    ElMessage.error("AI 搜索失败，请确认 AI 服务已启动");
  } finally {
    aiSearching.value = false;
  }
}

function clearAiResults() {
  aiResults.value = [];
  aiQuery.value = "";
}

function getSimType(sim) {
  if (sim >= 80) return "danger";
  if (sim >= 65) return "warning";
  return "info";
}

async function toggleChurnMode() {
  churnMode.value = !churnMode.value;
  loading.value = true;
  try {
    if (churnMode.value) {
      const list = await memberApi.churnRisk(churnDays.value);
      tableData.value = list || [];
      total.value = tableData.value.length;
    } else {
      await fetchData();
    }
  } finally {
    loading.value = false;
  }
}

function onChurnDaysChange() {
  if (churnMode.value) {
    toggleChurnMode();  // 关闭
    nextTick(() => toggleChurnMode()); // 用新天数重新打开
  }
}

async function handleExport() {
  try {
    const blob = await memberApi.exportExcel();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '会员列表.xlsx';
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch {
    ElMessage.error('导出失败');
  }
}

async function handleDownloadTemplate() {
  try {
    const blob = await memberApi.downloadTemplate();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '会员导入模板.xlsx';
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success('模板下载成功');
  } catch {
    ElMessage.error('模板下载失败');
  }
}

async function handleImport(file) {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const msg = await memberApi.importExcel(formData);
    ElMessage.success(msg || '导入成功');
    fetchData();
  } catch {
    ElMessage.error('导入失败');
  }
  return false;
}

const query = reactive({
  name: "",
  phone: "",
  balanceMin: null,
  balanceMax: null,
  level: null,
  gender: null,
  totalConsumeMin: null,
  totalConsumeMax: null,
  lastConsumeDays: null,
  page: 1,
  pageSize: 10,
});

const form = reactive({
  name: "",
  phone: "",
  gender: 0,
  level: null,
  points: 0,
  balance: 0,
  tags: "",
  birthday: "",
  birthdayType: 1,
  remark: "",
});

const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
};

const levelMap = ref({});

function getLevelName(levelId) {
  return levelMap.value[levelId] || `等级${levelId}`;
}

function getLevelTagType(levelId) {
  const map = { 1: "info", 2: "primary", 3: "warning", 4: "danger" };
  return map[levelId] || "info";
}

async function loadLevels() {
  try {
    const levels = await memberLevelApi.list();
    levelOptions.value = levels;
    const map = {};
    levels.forEach((lv) => { map[lv.id] = lv.name; });
    levelMap.value = map;
  } catch (e) {
    ElMessage.error("加载等级列表失败");
  }
}

async function fetchData() {
  loading.value = true;
  try {
    const res = await memberApi.page(query);
    tableData.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.page = 1;
  fetchData();
}

function handleReset() {
  query.name = "";
  query.phone = "";
  query.balanceMin = null;
  query.balanceMax = null;
  query.level = null;
  query.gender = null;
  query.totalConsumeMin = null;
  query.totalConsumeMax = null;
  query.lastConsumeDays = null;
  query.page = 1;
  fetchData();
}

function onPageChange(page) {
  query.page = page;
  fetchData();
}

function openDialog(row) {
  if (row) {
    isEdit.value = true;
    editId.value = row.id;
    Object.assign(form, row);
  } else {
    isEdit.value = false;
    editId.value = null;
    form.name = "";
    form.phone = "";
    form.gender = 0;
    form.level = null;
    form.points = 0;
    form.balance = 0;
  }
  dialogVisible.value = true;
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (isEdit.value) {
      await memberApi.update(editId.value, form);
      ElMessage.success("更新成功");
    } else {
      await memberApi.create(form);
      ElMessage.success("新增成功");
    }
    dialogVisible.value = false;
    fetchData();
  } finally {
    submitting.value = false;
  }
}

function openRechargeDialog(row) {
  rechargeMember.value = row;
  rechargeForm.amount = 0;
  rechargeForm.payMethod = 1;
  rechargeForm.remark = "";
  rechargeDialogVisible.value = true;
}

async function handleRecharge() {
  const valid = await rechargeFormRef.value.validate().catch(() => false);
  if (!valid) return;
  recharging.value = true;
  try {
    const updated = await memberApi.recharge(rechargeMember.value.id, { ...rechargeForm });
    ElMessage.success(`充值成功，当前余额 ¥${updated.balance?.toFixed(2)}`);
    rechargeDialogVisible.value = false;
    fetchData();
  } catch { ElMessage.error('充值失败') } finally { recharging.value = false; }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除会员「${row.name}」吗？`, "提示", {
    type: "warning",
    confirmButtonText: "确定删除",
    cancelButtonText: "取消",
  })
    .then(async () => {
      await memberApi.remove(row.id);
      ElMessage.success("删除成功");
      fetchData();
    })
    .catch(() => {});
}

onMounted(() => {
  loadLevels();
  fetchData();
});
</script>

<style scoped>
.member-page {
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== 工具栏 ===== */
.toolbar-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.toolbar-search { max-width: 300px; }
.toolbar-ai { flex: 1; }

/* ===== AI 结果条 ===== */
.ai-results-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: var(--bg-primary-ghost);
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.ai-results-title {
  color: var(--text-secondary);
  flex-shrink: 0;
}

.ai-result-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.ai-result-tag { cursor: pointer; }

/* ===== 操作栏 ===== */
.action-bar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.churn-group {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-right: auto;
}

.churn-days-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.quick-upload { display: inline-flex; }

.balance-text {
  font-weight: 600;
  color: var(--primary-color);
}

.member-expand {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
  padding: 4px 0;
}

.expand-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
}

.expand-label {
  color: var(--text-muted);
  flex-shrink: 0;
  min-width: 56px;
}

.expand-value {
  color: var(--text-primary);
  font-weight: 500;
}

.expand-none {
  color: var(--text-muted);
  font-weight: 400;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .member-page { padding: 0; }

  .toolbar-row {
    flex-direction: column;
    gap: 8px;
  }

  .toolbar-search { max-width: 100%; }

  .action-bar {
    flex-wrap: wrap;
    gap: 6px;
  }

  .ai-results-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

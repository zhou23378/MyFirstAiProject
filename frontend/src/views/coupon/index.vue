<template>
  <div class="coupon-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="券模板" name="template" />
      <el-tab-pane label="发券" name="issue" />
      <el-tab-pane label="核销记录" name="record" />
    </el-tabs>

    <!-- ==================== 券模板 ==================== -->
    <TabTransition v-if="activeTab === 'template'" tab-key="template">
      <div class="action-bar">
        <el-input
          v-model="templateSearch"
          placeholder="搜索模板名称…"
          clearable
          class="template-search"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="templateStatusFilter" placeholder="状态" clearable style="width:100px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="停用" />
        </el-select>
        <el-button type="primary" @click="openTemplateDialog()">新增模板</el-button>
      </div>

      <el-card shadow="never">
        <template v-if="templateLoading">
          <SkeletonTable :rows="5" :cols="9" />
        </template>
        <template v-else>
          <el-table :data="filteredTemplateList" stripe>
            <el-table-column prop="id" label="ID" min-width="60" />
            <el-table-column prop="name" label="模板名称" min-width="140" />
            <el-table-column label="类型" min-width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ typeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="门槛" min-width="90">
              <template #default="{ row }">
                {{ row.conditionAmount > 0 ? '¥' + row.conditionAmount : '无门槛' }}
              </template>
            </el-table-column>
            <el-table-column label="优惠值" min-width="90">
              <template #default="{ row }">
                {{ row.type === 2 ? (row.discountValue * 100).toFixed(0) + '%' : '¥' + row.discountValue }}
              </template>
            </el-table-column>
            <el-table-column prop="validDays" label="有效天数" min-width="80" />
            <el-table-column label="发放" min-width="100">
              <template #default="{ row }">
                {{ row.issuedQty || 0 }}<template v-if="row.totalQty > 0"> / {{ row.totalQty }}</template>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" />
            <el-table-column label="操作" min-width="140">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="openTemplateDialog(row)">编辑</el-button>
                <el-button type="danger" size="small" text @click="handleTemplateDelete(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <EmptyState description="暂无券模板" />
            </template>
          </el-table>
        </template>
      </el-card>

      <!-- 模板弹窗 -->
      <el-dialog v-model="templateDialog" :title="templateEdit ? '编辑模板' : '新增模板'" width="480px" :close-on-click-modal="false">
        <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="80px">
          <el-form-item label="名称" prop="name">
            <el-input v-model="templateForm.name" />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-radio-group v-model="templateForm.type">
              <el-radio :value="1">满减券</el-radio>
              <el-radio :value="2">折扣券</el-radio>
              <el-radio :value="3">代金券</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="使用门槛">
            <el-input-number v-model="templateForm.conditionAmount" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="优惠值" prop="discountValue">
            <el-input-number v-model="templateForm.discountValue" :min="0" :precision="2" style="width:100%" />
            <div class="form-tip">{{ templateForm.type === 2 ? '折扣券填小数，如 0.85 表示 85折' : '金额（元）' }}</div>
          </el-form-item>
          <el-form-item label="有效天数" prop="validDays">
            <el-input-number v-model="templateForm.validDays" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item label="发行总量">
            <el-input-number v-model="templateForm.totalQty" :min="0" style="width:100%" />
            <div class="form-tip">填 0 表示不限量</div>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="templateStatusSwitch" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="templateForm.remark" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="templateDialog = false">取消</el-button>
          <el-button type="primary" :loading="templateSubmitting" @click="handleTemplateSubmit">确定</el-button>
        </template>
      </el-dialog>
    </TabTransition>

    <!-- ==================== 发券 ==================== -->
    <TabTransition v-if="activeTab === 'issue'" tab-key="issue">
      <el-card shadow="never">
        <el-form :model="issueForm" :rules="issueRules" ref="issueFormRef" label-width="80px" style="max-width:480px">
          <el-form-item label="优惠券" prop="templateId">
            <el-select v-model="issueForm.templateId" placeholder="选择模板" style="width:100%">
              <el-option v-for="t in activeTemplates" :key="t.id" :label="`${t.name}（${typeText(t.type)} ¥${t.discountValue}）`" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="会员" prop="memberId">
            <el-select v-model="issueForm.memberId" filterable placeholder="搜索会员" style="width:100%" @focus="loadMembers">
              <el-option v-for="m in memberList" :key="m.id" :label="`${m.name}（${m.phone}）`" :value="m.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="issueSubmitting" @click="handleIssue">确认发券</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </TabTransition>

    <!-- ==================== 核销记录 ==================== -->
    <TabTransition v-if="activeTab === 'record'" tab-key="record">
      <div class="action-bar">
        <el-select v-model="recordQuery.memberId" placeholder="会员" filterable clearable @change="fetchRecords" style="width:180px" @focus="loadMembers">
          <el-option v-for="m in memberList" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
        <el-select v-model="recordQuery.status" placeholder="状态" clearable @change="fetchRecords" style="width:120px">
          <el-option :value="1" label="未使用" />
          <el-option :value="2" label="已使用" />
          <el-option :value="3" label="已过期" />
        </el-select>
        <el-button type="primary" @click="fetchRecords">查询</el-button>
      </div>

      <el-card shadow="never">
        <template v-if="recordLoading">
          <SkeletonTable :rows="5" :cols="10" />
        </template>
        <template v-else>
          <el-table :data="recordList" stripe>
            <el-table-column prop="id" label="ID" min-width="60" />
            <el-table-column prop="code" label="券码" min-width="120" />
            <el-table-column prop="templateName" label="模板" min-width="120" />
            <el-table-column prop="memberName" label="会员" min-width="100" />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="优惠值" min-width="80">
              <template #default="{ row }">¥{{ row.discountValue }}</template>
            </el-table-column>
            <el-table-column prop="receiveTime" label="领取时间" min-width="160" />
            <el-table-column prop="useTime" label="使用时间" min-width="160" />
            <el-table-column prop="expireTime" label="过期时间" min-width="160" />
            <el-table-column prop="orderId" label="关联订单" min-width="80" />
            <template #empty>
              <EmptyState description="暂无核销记录" />
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
import { templateApi, couponApi } from "@/api/coupon";
import { memberApi } from "@/api/member";
import { ElMessage, ElMessageBox } from "element-plus";
import TabTransition from "@/components/TabTransition.vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import EmptyState from "@/components/EmptyState.vue";

const activeTab = ref("template");

// ========== 类型/状态映射 ==========
const typeMap = { 1: "满减券", 2: "折扣券", 3: "代金券" };
function typeText(t) { return typeMap[t] || "未知"; }

const statusMap = { 1: "未使用", 2: "已使用", 3: "已过期" };
const statusTagMap = { 1: "primary", 2: "success", 3: "info" };
function statusText(s) { return statusMap[s] || "未知"; }
function statusTag(s) { return statusTagMap[s] || "info"; }

// ========== 券模板 ==========
const templateList = ref([]);
const templateLoading = ref(false);
const templateSearch = ref("");
const templateStatusFilter = ref(null);

const filteredTemplateList = computed(() => {
  let list = templateList.value;
  if (templateSearch.value) {
    const kw = templateSearch.value.toLowerCase();
    list = list.filter(t => t.name && t.name.toLowerCase().includes(kw));
  }
  if (templateStatusFilter.value != null) {
    list = list.filter(t => t.status === templateStatusFilter.value);
  }
  return list;
});
const templateDialog = ref(false);
const templateEdit = ref(false);
const templateEditId = ref(null);
const templateSubmitting = ref(false);
const templateFormRef = ref(null);
const templateForm = reactive({ name: "", type: 1, conditionAmount: 0, discountValue: 10, validDays: 30, totalQty: 0, status: 1, remark: "" });
const templateRules = {
  name: [{ required: true, message: "请输入模板名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择类型", trigger: "change" }],
  discountValue: [{ required: true, message: "请输入优惠值", trigger: "blur" }],
  validDays: [{ required: true, message: "请输入有效天数", trigger: "blur" }]
};
const templateStatusSwitch = computed({
  get: () => templateForm.status === 1,
  set: (v) => { templateForm.status = v ? 1 : 0; }
});

async function fetchTemplates() {
  templateLoading.value = true;
  try { templateList.value = await templateApi.list(); } catch { ElMessage.error("加载优惠券模板失败"); } finally { templateLoading.value = false; }
}

function openTemplateDialog(row) {
  templateFormRef.value?.resetFields();
  if (row) {
    templateEdit.value = true;
    templateEditId.value = row.id;
    templateForm.name = row.name;
    templateForm.type = row.type;
    templateForm.conditionAmount = row.conditionAmount || 0;
    templateForm.discountValue = row.discountValue;
    templateForm.validDays = row.validDays;
    templateForm.totalQty = row.totalQty || 0;
    templateForm.status = row.status;
    templateForm.remark = row.remark || "";
  } else {
    templateEdit.value = false;
    templateEditId.value = null;
    templateForm.name = "";
    templateForm.type = 1;
    templateForm.conditionAmount = 0;
    templateForm.discountValue = 10;
    templateForm.validDays = 30;
    templateForm.totalQty = 0;
    templateForm.status = 1;
    templateForm.remark = "";
  }
  templateDialog.value = true;
}

async function handleTemplateSubmit() {
  const valid = await templateFormRef.value.validate().catch(() => false);
  if (!valid) return;
  templateSubmitting.value = true;
  try {
    const data = { ...templateForm };
    if (templateEdit.value) {
      await templateApi.update(templateEditId.value, data);
      ElMessage.success("修改成功");
    } else {
      await templateApi.create(data);
      ElMessage.success("新增成功");
    }
    templateDialog.value = false;
    fetchTemplates();
  } catch { ElMessage.error('保存失败') } finally { templateSubmitting.value = false; }
}

async function handleTemplateDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该模板吗？", "提示", { type: "warning" });
    await templateApi.remove(row.id);
    ElMessage.success("删除成功");
    fetchTemplates();
  } catch { ElMessage.error('删除失败') }
}

// ========== 发券 ==========
const activeTemplates = computed(() => templateList.value.filter(t => t.status === 1 && (t.totalQty === 0 || t.issuedQty < t.totalQty)));
const memberList = ref([]);
const issueSubmitting = ref(false);
const issueFormRef = ref(null);
const issueForm = reactive({ templateId: null, memberId: null });
const issueRules = {
  templateId: [{ required: true, message: "请选择模板", trigger: "change" }],
  memberId: [{ required: true, message: "请选择会员", trigger: "change" }]
};

async function loadMembers() {
  if (memberList.value.length > 0) return;
  try {
    const res = await memberApi.page({ page: 1, pageSize: 200 });
    memberList.value = res.list || [];
  } catch { ElMessage.error("加载会员列表失败"); }
}

async function handleIssue() {
  const valid = await issueFormRef.value.validate().catch(() => false);
  if (!valid) return;
  issueSubmitting.value = true;
  try {
    const coupon = await couponApi.issue({ ...issueForm });
    ElMessage.success("发券成功，券码：" + coupon.code);
    issueForm.templateId = null;
    issueForm.memberId = null;
    fetchTemplates();
  } catch { ElMessage.error('发券失败') } finally { issueSubmitting.value = false; }
}

// ========== 核销记录 ==========
const recordList = ref([]);
const recordTotal = ref(0);
const recordLoading = ref(false);
const recordQuery = reactive({ page: 1, pageSize: 10, memberId: null, status: null });

async function fetchRecords() {
  recordLoading.value = true;
  try {
    const res = await couponApi.page({ ...recordQuery });
    recordList.value = res.list || [];
    recordTotal.value = res.total || 0;
  } catch { ElMessage.error("加载核销记录失败"); } finally { recordLoading.value = false; }
}

// ========== Tab 切换 ==========
function onTabChange(name) {
  if (name === "template") fetchTemplates();
  else if (name === "issue") { fetchTemplates(); loadMembers(); }
  else if (name === "record") fetchRecords();
}

onMounted(() => { fetchTemplates(); });
</script>

<style scoped>
.coupon-page {
  max-width: 1200px;
  margin: 0 auto;
  overflow-x: hidden;
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

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .coupon-page {
    padding: 0;
  }

}
</style>

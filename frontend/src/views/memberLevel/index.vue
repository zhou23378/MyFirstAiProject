<template>
  <div class="level-page">
    <PageHeader title="会员等级" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置', path: '/admin'}]" />
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增等级
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <template v-if="loading">
        <SkeletonTable :rows="4" :cols="5" />
      </template>
      <ResponsiveDataList
        v-else
        :data="tableData"
        :columns="columns"
        :total="tableData.length"
        :page-size="50"
        :current-page="1"
        primary-field="name"
        empty-text="暂无会员等级"
        actions-width="200"
      >
        <template #actions="{ row }">
          <el-button type="primary" text @click="openDialog(row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button type="danger" text @click="handleDelete(row)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </ResponsiveDataList>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑等级' : '新增等级'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="如：黄金会员" />
        </el-form-item>
        <el-form-item label="所需积分" prop="pointsRequired">
          <el-input-number v-model="form.pointsRequired" :min="0" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-slider
            v-model="discountPercent"
            :min="50"
            :max="100"
            :step="5"
            show-input
            show-input-controls
          />
          <div class="slider-hint">
            当前折扣：{{ discountPercent }}%（即
            {{ (discountPercent / 100).toFixed(2) }} 折）
          </div>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { memberLevelApi } from "../../api/memberLevel";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete } from "@element-plus/icons-vue";
import SkeletonTable from "../../components/SkeletonTable.vue";
import ResponsiveDataList from "../../components/ResponsiveDataList.vue";
import PageHeader from "../../components/PageHeader.vue";

const columns = [
  { prop: "name", label: "等级名称", width: "150", cardPrimary: true },
  { prop: "pointsRequired", label: "所需积分", width: "120" },
  { prop: "discountRate", label: "折扣率", width: "120", format: (v) => (v * 100).toFixed(0) + "%" },
  { prop: "sort", label: "排序号", width: "100" },
];

const loading = ref(false);
const submitting = ref(false);
const tableData = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editId = ref(null);
const formRef = ref(null);

const discountPercent = ref(100);

const form = reactive({
  name: "",
  pointsRequired: 0,
  discountRate: 1.0,
  sort: 0,
});

const rules = {
  name: [{ required: true, message: "请输入等级名称", trigger: "blur" }],
  pointsRequired: [
    { required: true, message: "请输入所需积分", trigger: "blur" },
  ],
};

// 监听滑块变化同步到表单
watch(discountPercent, (val) => {
  form.discountRate = val / 100;
});

async function fetchData() {
  loading.value = true;
  try {
    tableData.value = await memberLevelApi.list();
  } finally {
    loading.value = false;
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true;
    editId.value = row.id;
    Object.assign(form, row);
    discountPercent.value = Math.round(row.discountRate * 100);
  } else {
    isEdit.value = false;
    editId.value = null;
    form.name = "";
    form.pointsRequired = 0;
    form.discountRate = 1.0;
    form.sort = 0;
    discountPercent.value = 100;
  }
  dialogVisible.value = true;
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (isEdit.value) {
      await memberLevelApi.update(editId.value, form);
      ElMessage.success("更新成功");
    } else {
      await memberLevelApi.create(form);
      ElMessage.success("新增成功");
    }
    dialogVisible.value = false;
    fetchData();
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除等级「${row.name}」吗？`, "提示", {
    type: "warning",
    confirmButtonText: "确定删除",
    cancelButtonText: "取消",
  })
    .then(async () => {
      await memberLevelApi.remove(row.id);
      ElMessage.success("删除成功");
      fetchData();
    })
    .catch(() => {});
}

onMounted(fetchData);
</script>

<style scoped>
.level-page {
  overflow-x: hidden;
  max-width: 800px;
  margin: 0 auto;
}
.action-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}
.slider-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .level-page {
    padding: 0;
  }
}
</style>

<template>
  <div class="employee-page">
    <PageHeader title="员工管理" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '人员管理'}]">
      <template #extra>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增员工
        </el-button>
      </template>
    </PageHeader>

    <!-- 搜索栏 -->
    <div class="action-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名或手机号…"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- 数据列表 -->
    <template v-if="loading">
      <SkeletonTable :rows="5" :cols="6" />
    </template>
    <ResponsiveDataList
      v-else
      :data="list"
      :columns="columns"
      :total="total"
      :page-size="pageSize"
      :current-page="page"
      primary-field="name"
      empty-text="暂无员工数据"
      @page-change="(p) => { page = p; loadList(); }"
      @size-change="(s) => { pageSize = s; page = 1; loadList(); }"
    >
      <template #status="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
          {{ row.status === 1 ? '在职' : '离职' }}
        </el-tag>
      </template>
      <template #actions="{ row }">
        <el-button type="primary" link size="small" @click="openDialog(row)">
          <el-icon><Edit /></el-icon>
        </el-button>
        <el-button type="danger" link size="small" @click="handleDelete(row.id)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </template>
    </ResponsiveDataList>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑员工' : '新增员工'"
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
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="薪资">
          <el-input-number
            v-model="form.salary"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在职"
            inactive-text="离职"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import {
  getEmployeeList,
  createEmployee,
  updateEmployee,
  deleteEmployee,
} from "@/api/employee";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete, Search } from "@element-plus/icons-vue";
import PageHeader from "@/components/PageHeader.vue";
import SkeletonTable from "@/components/SkeletonTable.vue";
import ResponsiveDataList from "@/components/ResponsiveDataList.vue";

const searchKeyword = ref("");

const columns = [
  { prop: "name", label: "姓名", width: "120", cardPrimary: true },
  { prop: "phone", label: "手机号", width: "140" },
  { prop: "position", label: "职位", width: "120" },
  { prop: "salary", label: "薪资", width: "120", format: (v) => `¥${v || 0}` },
  { prop: "status", label: "状态", width: "80" },
];

const list = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const submitting = ref(false);
const form = ref({
  name: "",
  phone: "",
  position: "",
  salary: 0,
  status: 1,
});

const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  position: [{ required: true, message: "请输入职位", trigger: "blur" }],
};

const loadList = async () => {
  loading.value = true;
  try {
    const params = { page: page.value, pageSize: pageSize.value };
    if (searchKeyword.value) params.name = searchKeyword.value;
    const res = await getEmployeeList(params);
    list.value = res.list || [];
    total.value = res.total || 0;
  } catch { ElMessage.error("加载员工列表失败"); } finally {
    loading.value = false;
  }
};

function handleSearch() {
  page.value = 1;
  loadList();
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true;
    form.value = { ...row };
  } else {
    isEdit.value = false;
    form.value = { name: "", phone: "", position: "", salary: 0, status: 1 };
  }
  dialogVisible.value = true;
};

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (isEdit.value) {
      await updateEmployee(form.value.id, form.value);
      ElMessage.success("修改成功");
    } else {
      await createEmployee(form.value);
      ElMessage.success("新增成功");
    }
    dialogVisible.value = false;
    loadList();
  } catch {
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm("确认删除该员工？", "提示", {
      type: "warning",
      confirmButtonText: "确定删除",
      cancelButtonText: "取消",
    });
    await deleteEmployee(id);
    ElMessage.success("删除成功");
    loadList();
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error('删除失败') }
};

onMounted(loadList);
</script>

<style scoped>
.employee-page { overflow-x: hidden; max-width: 1200px; margin: 0 auto; }

.search-input { max-width: 320px; }

@media (max-width: 767px) {
  .employee-page { padding: 0; }
  .search-input { max-width: 100%; }
}
</style>

<template>
  <div class="admin-page">
    <PageHeader title="系统管理" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置'}]" />
    <div class="action-bar">
      <el-button type="primary" @click="openDialog()">新增管理员</el-button>
    </div>

    <el-card shadow="never">
      <template v-if="loading">
        <SkeletonTable :rows="4" :cols="4" />
      </template>
      <ResponsiveDataList
        v-else
        :data="list"
        :columns="columns"
        :total="list.length"
        :page-size="50"
        :current-page="1"
        primary-field="username"
        empty-text="暂无管理员数据"
        actions-width="160"
      >
        <template #role="{ row }">
          <el-tag :type="roleTag(row.role)" size="small">{{ roleText(row.role) }}</el-tag>
        </template>
        <template #actions="{ row }">
          <el-button type="primary" size="small" text @click="openDialog(row)">编辑</el-button>
          <el-button v-if="row.id !== 1" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </ResponsiveDataList>
    </el-card>

    <el-dialog v-model="dialog" :title="edit ? '编辑管理员' : '新增管理员'" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="edit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="edit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { adminApi } from "@/api/admin";
import { ElMessage, ElMessageBox } from "element-plus";
import SkeletonTable from "@/components/SkeletonTable.vue";
import ResponsiveDataList from "@/components/ResponsiveDataList.vue";
import PageHeader from "@/components/PageHeader.vue";

const columns = [
  { prop: "id", label: "ID", width: "60", hideOnCard: true },
  { prop: "username", label: "用户名", width: "150", cardPrimary: true },
  { prop: "role", label: "角色", width: "150" },
  { prop: "createTime", label: "创建时间", width: "160" },
];

const list = ref([]);
const loading = ref(false);
const dialog = ref(false);
const edit = ref(false);
const editId = ref(null);
const submitting = ref(false);

const roles = [
  { value: "admin", label: "管理员" },
  { value: "manager", label: "店长" },
  { value: "technician", label: "技师" },
  { value: "cashier", label: "前台" }
];

const formRef = ref(null);
const form = reactive({ username: "", password: "", role: "admin" });
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur", validator: (_rule, value, cb) => edit.value ? cb() : value ? cb() : cb(new Error("请输入密码")) }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
};

function roleText(r) {
  const found = roles.find(x => x.value === r);
  return found ? found.label : (r || "未知");
}
function roleTag(r) {
  const map = { admin: "primary", manager: "success", technician: "warning", cashier: "info" };
  return map[r] || "info";
}

async function fetchData() {
  loading.value = true;
  try { list.value = await adminApi.list(); } finally { loading.value = false; }
}
function openDialog(row) {
  if (row) {
    edit.value = true;
    editId.value = row.id;
    form.username = row.username;
    form.password = "";
    form.role = (row.role || "admin").toLowerCase();
  } else {
    edit.value = false;
    editId.value = null;
    form.username = "";
    form.password = "";
    form.role = "admin";
  }
  dialog.value = true;
}
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (edit.value) {
      await adminApi.update(editId.value, { role: form.role });
      ElMessage.success("修改成功");
    } else {
      await adminApi.create({ ...form });
      ElMessage.success("新增成功");
    }
    dialog.value = false;
    fetchData();
  } catch { ElMessage.error('保存失败') } finally { submitting.value = false; }
}
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该管理员吗？", "提示", { type: "warning" });
    await adminApi.remove(row.id);
    ElMessage.success("删除成功");
    fetchData();
  } catch { ElMessage.error('删除失败') }
}

onMounted(fetchData);
</script>

<style scoped>
.admin-page {
  overflow-x: hidden;
  max-width: 800px;
  margin: 0 auto;
}
.action-bar {
  margin-bottom: 16px;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .admin-page {
    padding: 0;
  }
}
</style>

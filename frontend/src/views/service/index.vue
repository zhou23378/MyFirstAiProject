<template>
  <div class="service-page">
    <PageHeader title="服务项目" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置', path: '/admin'}]" />
    <el-row :gutter="20">
      <!-- 左侧：服务分类 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>服务分类</span>
              <el-button
                type="primary"
                size="small"
                @click="openCategoryDialog()"
                >+ 新增</el-button
              >
            </div>
          </template>
          <template v-if="catLoading">
            <SkeletonTable :rows="3" :cols="2" />
          </template>
          <template v-else>
            <el-table :data="categories" stripe @row-click="selectCategory" highlight-current-row>
              <el-table-column prop="name" label="分类名称" />
              <el-table-column label="操作" min-width="120">
                <template #default="{ row }">
                  <el-button type="primary" text size="small" @click.stop="openCategoryDialog(row)">编辑</el-button>
                  <el-button type="danger" text size="small" @click.stop="handleDeleteCategory(row)">删除</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <EmptyState description="暂无分类" />
              </template>
            </el-table>
          </template>
        </el-card>
      </el-col>

      <!-- 右侧：服务项目 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span
                >服务项目
                {{ selectedCategory ? "- " + selectedCategory.name : "" }}</span
              >
              <el-button
                type="primary"
                size="small"
                :disabled="!selectedCategory"
                @click="openItemDialog()"
                >+ 新增项目</el-button
              >
            </div>
          </template>
          <template v-if="itemLoading">
            <SkeletonTable :rows="5" :cols="4" />
          </template>
          <template v-else>
            <el-table :data="filteredItems" stripe>
              <el-table-column prop="name" label="项目名称" />
              <el-table-column label="所属分类" min-width="120">
                <template #default="{ row }">{{ categoryMap[row.categoryId] || '-' }}</template>
              </el-table-column>
              <el-table-column prop="price" label="价格" min-width="120">
                <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="duration" label="时长(分钟)" min-width="100" />
              <el-table-column prop="status" label="状态" min-width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? "上架" : "下架" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="120">
                <template #default="{ row }">
                  <el-button type="primary" text size="small" @click="openItemDialog(row)">编辑</el-button>
                  <el-button type="danger" text size="small" @click="handleDeleteItem(row)">删除</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <EmptyState description="暂无服务项目，请先选择分类" />
              </template>
            </el-table>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类弹窗 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="isEditCategory ? '编辑分类' : '新增分类'"
      width="400px"
    >
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="handleCategorySubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 项目弹窗 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="isEditItem ? '编辑项目' : '新增项目'"
      width="500px"
    >
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="itemForm.name" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="itemForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="itemForm.duration" :min="5" :step="5" />
        </el-form-item>
        <el-form-item label="提成方式">
          <el-radio-group v-model="itemForm.commissionType">
            <el-radio :value="0">无提成</el-radio>
            <el-radio :value="1">固定金额</el-radio>
            <el-radio :value="2">百分比</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="itemForm.commissionType > 0" label="提成值">
          <el-input-number v-model="itemForm.commissionValue" :min="0" :precision="2" />
          <span style="margin-left:8px;color:var(--el-text-color-secondary)">{{ itemForm.commissionType === 2 ? '%' : '元' }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="itemForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitting" @click="handleItemSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { serviceApi } from "../../api/service";
import { ElMessage, ElMessageBox } from "element-plus";
import SkeletonTable from "../../components/SkeletonTable.vue";
import EmptyState from "../../components/EmptyState.vue";
import PageHeader from "../../components/PageHeader.vue";

const categories = ref([]);
const items = ref([]);
const catLoading = ref(false);
const itemLoading = ref(false);
const selectedCategory = ref(null);

// 分类ID → 名称映射
const categoryMap = computed(() => {
  const map = {};
  categories.value.forEach(c => { map[c.id] = c.name; });
  return map;
});

// 右侧显示：选中分类则过滤，未选则显示全部
const filteredItems = computed(() => {
  if (!selectedCategory.value) return items.value;
  return items.value.filter(i => i.categoryId === selectedCategory.value.id);
});

// 分类弹窗
const categoryFormRef = ref(null);
const categorySubmitting = ref(false);
const categoryDialogVisible = ref(false);
const isEditCategory = ref(false);
const categoryForm = ref({ name: "", sort: 0 });
const categoryEditId = ref(null);
const categoryRules = {
  name: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
};

// 项目弹窗
const itemFormRef = ref(null);
const itemSubmitting = ref(false);
const itemDialogVisible = ref(false);
const isEditItem = ref(false);
const itemForm = ref({ name: "", price: 0, duration: 30, status: 1, commissionType: 0, commissionValue: 0 });
const itemEditId = ref(null);
const itemRules = {
  name: [{ required: true, message: "请输入项目名称", trigger: "blur" }],
  price: [{ required: true, message: "请输入价格", trigger: "blur" }],
  duration: [{ required: true, message: "请输入时长", trigger: "blur" }],
};

async function fetchCategories() {
  catLoading.value = true;
  try { categories.value = await serviceApi.getCategories(); } finally { catLoading.value = false; }
}

async function fetchItems() {
  itemLoading.value = true;
  try { items.value = await serviceApi.getItems(); } finally { itemLoading.value = false; }
}

function selectCategory(row) {
  selectedCategory.value = row;
}

function openCategoryDialog(row) {
  if (row) {
    isEditCategory.value = true;
    categoryEditId.value = row.id;
    categoryForm.value = { name: row.name, sort: row.sort };
  } else {
    isEditCategory.value = false;
    categoryEditId.value = null;
    categoryForm.value = { name: "", sort: 0 };
  }
  categoryDialogVisible.value = true;
}

async function handleCategorySubmit() {
  const valid = await categoryFormRef.value.validate().catch(() => false);
  if (!valid) return;
  categorySubmitting.value = true;
  try {
    if (isEditCategory.value) {
      await serviceApi.updateCategory(categoryEditId.value, categoryForm.value);
      ElMessage.success("更新成功");
    } else {
      await serviceApi.createCategory(categoryForm.value);
      ElMessage.success("新增成功");
    }
    categoryDialogVisible.value = false;
    fetchCategories();
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    categorySubmitting.value = false;
  }
}

function handleDeleteCategory(row) {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      await serviceApi.deleteCategory(row.id);
      ElMessage.success("删除成功");
      fetchCategories();
    })
    .catch(() => {});
}

function openItemDialog(row) {
  if (row) {
    isEditItem.value = true;
    itemEditId.value = row.id;
    itemForm.value = {
      name: row.name,
      price: row.price,
      duration: row.duration,
      status: row.status,
      categoryId: row.categoryId,
      commissionType: row.commissionType || 0,
      commissionValue: row.commissionValue || 0,
    };
  } else {
    isEditItem.value = false;
    itemEditId.value = null;
    itemForm.value = {
      name: "",
      price: 0,
      duration: 30,
      status: 1,
      categoryId: null,
      commissionType: 0,
      commissionValue: 0,
    };
  }
  itemDialogVisible.value = true;
}

async function handleItemSubmit() {
  const valid = await itemFormRef.value.validate().catch(() => false);
  if (!valid) return;
  const data = { ...itemForm.value };
  if (!isEditItem.value) {
    data.categoryId = selectedCategory.value.id;
  }
  itemSubmitting.value = true;
  try {
    if (isEditItem.value) {
      await serviceApi.updateItem(itemEditId.value, data);
      ElMessage.success("更新成功");
    } else {
      await serviceApi.createItem(data);
      ElMessage.success("新增成功");
    }
    itemDialogVisible.value = false;
    fetchItems();
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    itemSubmitting.value = false;
  }
}

function handleDeleteItem(row) {
  ElMessageBox.confirm(`确定删除项目「${row.name}」吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      await serviceApi.deleteItem(row.id);
      ElMessage.success("删除成功");
      fetchItems();
    })
    .catch(() => {});
}

onMounted(() => {
  fetchCategories();
  fetchItems();
});
</script>

<style scoped>
/* 页面容器 */
.service-page {
  overflow-x: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .service-page {
    padding: 0;
  }

  .service-page .el-row {
    flex-direction: column;
    gap: 12px;
  }

  .service-page .el-col {
    max-width: 100% !important;
    width: 100% !important;
  }
}
</style>

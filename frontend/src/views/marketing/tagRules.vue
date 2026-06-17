<template>
  <div class="tag-rules-page">
    <PageHeader title="标签规则" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置'}]">
      <template #extra>
        <el-button type="primary" @click="openDialog()">新增规则</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <el-table :data="rules" stripe v-loading="loading">
        <el-table-column prop="name" label="规则名称" min-width="180" />
        <el-table-column prop="tagName" label="标签名" min-width="120" />
        <el-table-column prop="conditionsJson" label="条件" min-width="200">
          <template #default="{ row }">
            <template v-if="parsedConditions[row.id]">
              <el-tag
                v-for="(c, i) in parsedConditions[row.id]"
                :key="i"
                size="small"
                style="margin:2px"
              >
                {{ fieldLabel(c.field) }} {{ c.op }} {{ c.value }}
              </el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" min-width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 1"
              @change="toggleRule(row)"
            />
          </template>
        </el-table-column>
      <template #empty><el-empty description="暂无标签规则" /></template>
        <el-table-column label="操作" min-width="160">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="如：高价值流失风险" />
        </el-form-item>
        <el-form-item label="标签名" prop="tagName">
          <el-input v-model="form.tagName" placeholder="如：流失风险" />
        </el-form-item>
        <el-form-item label="条件">
          <div v-for="(c, i) in form.conditions" :key="i" class="condition-row">
            <el-select v-model="c.field" size="small" style="width:140px">
              <el-option value="last_visit_days" label="未到店天数" />
              <el-option value="total_spent" label="累计消费" />
              <el-option value="visit_count" label="到店次数" />
              <el-option value="balance" label="余额" />
              <el-option value="level" label="等级" />
            </el-select>
            <el-select v-model="c.op" size="small" style="width:70px">
              <el-option value=">" label=">" />
              <el-option value="<" label="<" />
              <el-option value=">=" label=">=" />
              <el-option value="<=" label="<=" />
              <el-option value="=" label="=" />
              <el-option value="!=" label="!=" />
            </el-select>
            <el-input-number v-model="c.value" size="small" :min="0" style="width:100px" />
            <el-button size="small" text type="danger" @click="form.conditions.splice(i, 1)" v-if="form.conditions.length > 1">×</el-button>
          </div>
          <el-button size="small" text type="primary" @click="addCondition" v-if="form.conditions.length < 3">
            + 添加条件
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const rules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  tagName: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
}

const form = reactive({
  name: '',
  tagName: '',
  conditions: [{ field: 'last_visit_days', op: '>', value: 60 }],
})

const fieldLabel = (f) => ({
  last_visit_days: '未到店天数', total_spent: '累计消费',
  visit_count: '到店次数', balance: '余额', level: '等级',
}[f] || f)

const parsedConditions = computed(() => {
  const result = {}
  rules.value.forEach(r => {
    try { result[r.id] = JSON.parse(r.conditionsJson) } catch { result[r.id] = [] }
  })
  return result
})

function addCondition() { form.conditions.push({ field: 'total_spent', op: '>', value: 5000 }) }

async function loadRules() {
  loading.value = true
  try { rules.value = await request.get('/api/tag-rules') } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.name = row.name
    form.tagName = row.tagName
    try { form.conditions = JSON.parse(row.conditionsJson) } catch { form.conditions = [{ field: 'last_visit_days', op: '>', value: 60 }] }
  } else {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.tagName = ''
    form.conditions = [{ field: 'last_visit_days', op: '>', value: 60 }]
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const data = {
    name: form.name,
    tagName: form.tagName,
    conditionsJson: JSON.stringify(form.conditions),
  }
  if (isEdit.value) {
    await request.put(`/api/tag-rules/${editId.value}`, data)
    ElMessage.success('修改成功')
  } else {
    await request.post('/api/tag-rules', data)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadRules()
}

async function toggleRule(row) {
  try {
    await request.post(`/api/tag-rules/${row.id}/toggle`)
    loadRules()
  } catch { ElMessage.error('操作失败') }
}

function handleDelete(id) {
  ElMessageBox.confirm('确定删除该规则吗？', '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/api/tag-rules/${id}`)
      ElMessage.success('删除成功')
      loadRules()
    })
    .catch(() => {})
}

onMounted(loadRules)
</script>

<style scoped>
.tag-rules-page { overflow-x: hidden; max-width: 1000px; margin: 0 auto; }

.condition-row {
  display: flex; align-items: center; gap: 6px; margin-bottom: 8px;
}

@media (max-width: 767px) {
  .tag-rules-page { padding: 0; }
}
</style>

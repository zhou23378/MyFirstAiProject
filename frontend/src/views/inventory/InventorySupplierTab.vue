<template>
  <div class="supplier-tab">
    <div class="action-bar">
      <el-button type="primary" @click="openDialog()">新增供应商</el-button>
    </div>

    <el-card shadow="never">
      <template v-if="loading">
        <SkeletonTable :rows="4" :cols="5" />
      </template>
      <template v-else>
        <el-table :data="list" stripe>
          <el-table-column prop="id" label="ID" min-width="60" />
          <el-table-column prop="name" label="名称" min-width="150" />
          <el-table-column prop="contact" label="联系人" min-width="100" />
          <el-table-column prop="phone" label="电话" min-width="120" />
          <el-table-column prop="address" label="地址" min-width="150" />
          <el-table-column prop="remark" label="备注" min-width="120" />
          <el-table-column label="操作" min-width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState description="暂无供应商数据" />
          </template>
        </el-table>
      </template>
    </el-card>

    <el-dialog v-model="dialog" :title="editing ? '编辑供应商' : '新增供应商'" width="480px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
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
import { ref, reactive, onMounted } from 'vue'
import { supplierApi } from '@/api/inventory'
import { ElMessage, ElMessageBox } from 'element-plus'
import SkeletonTable from '@/components/SkeletonTable.vue'
import EmptyState from '@/components/EmptyState.vue'

const emit = defineEmits(['changed'])

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const editId = ref(null)
const submitting = ref(false)
const form = reactive({ name: '', contact: '', phone: '', address: '', remark: '' })

async function fetchData() {
  loading.value = true
  try { list.value = await supplierApi.list() } finally { loading.value = false }
}

function openDialog(row) {
  editing.value = !!row
  if (row) {
    editId.value = row.id
    form.name = row.name
    form.contact = row.contact || ''
    form.phone = row.phone || ''
    form.address = row.address || ''
    form.remark = row.remark || ''
  } else {
    editId.value = null
    form.name = ''
    form.contact = ''
    form.phone = ''
    form.address = ''
    form.remark = ''
  }
  dialog.value = true
}

async function handleSubmit() {
  if (!form.name) { ElMessage.warning('请输入供应商名称'); return }
  submitting.value = true
  try {
    if (editing.value) {
      await supplierApi.update(editId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await supplierApi.create({ ...form })
      ElMessage.success('新增成功')
    }
    dialog.value = false
    emit('changed')
    fetchData()
  } catch { ElMessage.error('保存供应商失败') } finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该供应商吗？', '提示', { type: 'warning' })
    await supplierApi.remove(row.id)
    ElMessage.success('删除成功')
    emit('changed')
    fetchData()
  } catch { ElMessage.error('操作失败，请重试') }
}

onMounted(fetchData)
</script>

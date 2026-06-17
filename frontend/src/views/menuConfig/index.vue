<template>
  <div class="menu-config-page">
    <PageHeader title="菜单配置" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置', path: '/admin'}]" />

    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>菜单分组配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </div>
      </template>

      <template v-if="loading">
        <SkeletonTable :rows="10" :cols="4" />
      </template>

      <template v-else>
        <el-table :data="tableData" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="title" label="菜单名称" min-width="140" />
          <el-table-column prop="index" label="菜单路径" min-width="200" />
          <el-table-column label="所属分组" min-width="200">
            <template #default="{ row }">
              <el-select v-model="row.group" style="width: 100%">
                <el-option
                  v-for="label in groupLabels"
                  :key="label"
                  :label="label"
                  :value="label"
                />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { allMenuItems as menuItemsDef, groupLabels } from '@/config/menuItems'
import { menuConfigApi } from '@/api/menuConfig'
import PageHeader from '@/components/PageHeader.vue'
import SkeletonTable from '@/components/SkeletonTable.vue'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])

onMounted(async () => {
  loading.value = true
  try {
    // 深拷贝默认定义
    tableData.value = menuItemsDef.map(item => ({ ...item }))
    const configs = await menuConfigApi.list()
    if (configs && configs.length > 0) {
      const map = Object.fromEntries(configs.map(c => [c.menuIndex, c.groupName]))
      tableData.value.forEach(item => {
        if (map[item.index]) item.group = map[item.index]
      })
    }
  } catch { /* 加载失败使用默认值 */ }
  finally { loading.value = false }
})

async function handleSave() {
  saving.value = true
  try {
    const payload = tableData.value.map(item => ({
      menuIndex: item.index,
      groupName: item.group,
    }))
    await menuConfigApi.save({ items: payload })
    ElMessage.success('菜单配置保存成功，刷新页面后生效')
  } catch { /* 由拦截器处理 */ }
  finally { saving.value = false }
}
</script>

<style scoped>
.menu-config-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

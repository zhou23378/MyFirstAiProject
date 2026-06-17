<template>
  <div class="qa-config-page">
    <PageHeader title="快捷入口配置" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '系统设置', path: '/admin'}]" />

    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>配置仪表盘快捷入口（共 8 个槽位，拖拽调整顺序）</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </div>
      </template>

      <template v-if="loading">
        <SkeletonTable :rows="8" :cols="5" />
      </template>

      <template v-else>
        <el-table :data="tableData" stripe row-key="slot">
          <el-table-column label="排序" width="80" align="center">
            <template #default="{ $index }">
              <el-button text size="small" :disabled="$index === 0" @click="moveItem($index, -1)">
                <el-icon><Top /></el-icon>
              </el-button>
              <el-button text size="small" :disabled="$index === tableData.length - 1" @click="moveItem($index, 1)">
                <el-icon><Bottom /></el-icon>
              </el-button>
            </template>
          </el-table-column>
          <el-table-column type="index" label="位置" width="60" />
          <el-table-column label="预览" width="70" align="center">
            <template #default="{ row }">
              <div class="preview-icon" :style="{ background: row._color }">
                <el-icon :size="16"><component :is="row._icon" /></el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="快捷入口" min-width="250">
            <template #default="{ row }">
              <el-select v-model="row.path" style="width: 100%" placeholder="选择路由" @change="onPathChange(row)">
                <el-option
                  v-for="opt in routeOptions"
                  :key="opt.path"
                  :label="opt.label"
                  :value="opt.path"
                >
                  <div class="route-option">
                    <span class="route-dot" :style="{ background: opt.color }" />
                    <el-icon :size="14"><component :is="opt.icon" /></el-icon>
                    {{ opt.label }}
                    <span class="route-path">{{ opt.path }}</span>
                  </div>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="显示名称" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.label" placeholder="输入显示名称" />
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
import { Top, Bottom } from '@element-plus/icons-vue'
import { defaultQuickActions, routeOptions, routeMetaMap } from '@/config/dashboardQuickActions'
import { getQuickActions, saveQuickActions } from '@/api/dashboard'
import PageHeader from '@/components/PageHeader.vue'
import SkeletonTable from '@/components/SkeletonTable.vue'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])

function syncMeta(row) {
  const meta = routeMetaMap[row.path]
  row._icon = meta?.icon || null
  row._color = meta?.color || '#ccc'
}

onMounted(async () => {
  loading.value = true
  try {
    tableData.value = defaultQuickActions.map(item => {
      const row = { ...item }
      syncMeta(row)
      return row
    })
    const configs = await getQuickActions()
    if (configs && configs.length > 0) {
      const map = Object.fromEntries(configs.map(c => [c.slot, c]))
      tableData.value.forEach((item, i) => {
        if (map[i]) {
          item.label = map[i].label
          item.path = map[i].path
          syncMeta(item)
        }
      })
    }
  } catch { /* 加载失败使用默认值 */ }
  finally { loading.value = false }
})

function onPathChange(row) {
  syncMeta(row)
  const opt = routeOptions.find(r => r.path === row.path)
  if (opt && !row._labelTouched) {
    row.label = opt.label
  }
}

function moveItem(index, delta) {
  const newIndex = index + delta
  if (newIndex < 0 || newIndex >= tableData.value.length) return
  const items = [...tableData.value]
  const [item] = items.splice(index, 1)
  items.splice(newIndex, 0, item)
  tableData.value = items
}

async function handleSave() {
  saving.value = true
  try {
    const payload = tableData.value.map((item, i) => ({
      slot: i,
      label: item.label,
      path: item.path,
    }))
    await saveQuickActions({ items: payload })
    ElMessage.success('快捷入口保存成功，返回仪表盘即可查看')
  } catch { /* 由拦截器处理 */ }
  finally { saving.value = false }
}
</script>

<style scoped>
.qa-config-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-icon {
  width: 32px; height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto;
}

.route-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.route-path {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: auto;
}
</style>

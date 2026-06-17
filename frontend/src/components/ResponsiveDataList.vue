<template>
  <div class="responsive-data-list" :class="{ 'is-mobile': isMobile }">
    <!-- 桌面端 — 表格视图 -->
    <div v-if="!isMobile" class="table-view">
      <el-table
        :data="data"
        :stripe="stripe"
        :row-key="rowKey"
        :default-sort="defaultSort"
        v-bind="$attrs"
        @row-click="onRowClick"
        @selection-change="onSelectionChange"
      >
        <el-table-column v-if="showSelection" type="selection" width="48" />
        <el-table-column v-if="showIndex" type="index" width="60" :label="indexLabel" />
        <el-table-column v-if="expandable" type="expand" width="48">
          <template #default="{ row }">
            <slot name="expand" :row="row" />
          </template>
        </el-table-column>
        <el-table-column
          v-for="col in columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :sortable="col.sortable"
          :align="col.align || 'left'"
        >
          <template v-if="col.slot || $slots[col.prop]" #default="{ row }">
            <slot :name="col.prop" :row="row" :value="row[col.prop]" />
          </template>
        </el-table-column>
        <el-table-column v-if="$slots.actions" label="操作" :width="actionsWidth" fixed="right">
          <template #default="{ row }">
            <slot name="actions" :row="row" />
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="emptyText" />
        </template>
      </el-table>
    </div>

    <!-- 移动端 — 卡片视图 -->
    <div v-else class="card-view">
      <el-empty v-if="!data || data.length === 0" :description="emptyText" />
      <div
        v-for="(row, index) in data"
        :key="rowKey ? row[rowKey] : index"
        class="data-card"
        @click="onRowClick(row)"
      >
        <div class="card-primary">
          <span class="card-primary-text">{{ getPrimaryField(row) }}</span>
          <div class="card-primary-right">
            <div v-if="$slots.actions" class="card-actions">
              <slot name="actions" :row="row" />
            </div>
            <button
              v-if="expandable"
              class="expand-toggle"
              :class="{ expanded: isExpanded(row) }"
              @click.stop="toggleExpand(row)"
            >
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
        </div>
        <div class="card-fields">
          <div
            v-for="col in visibleCardColumns"
            :key="col.prop"
            class="card-field"
          >
            <span class="card-field-label">{{ col.cardLabel || col.label }}</span>
            <span class="card-field-value">{{ formatValue(row[col.prop], col.format) }}</span>
          </div>
        </div>
        <div v-if="expandable && isExpanded(row)" class="card-expand">
          <slot name="expand" :row="row" />
        </div>
      </div>
    </div>

    <!-- 分页（共享） -->
    <div v-if="total > 0" class="list-pagination">
      <el-pagination
        background
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { ArrowDown } from "@element-plus/icons-vue";

const props = defineProps({
  data: { type: Array, default: () => [] },
  columns: { type: Array, required: true },
  breakpoint: { type: Number, default: 768 },
  rowKey: { type: String, default: "id" },
  stripe: { type: Boolean, default: true },
  defaultSort: { type: Object, default: undefined },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 10 },
  currentPage: { type: Number, default: 1 },
  showSelection: { type: Boolean, default: false },
  showIndex: { type: Boolean, default: false },
  indexLabel: { type: String, default: "#" },
  emptyText: { type: String, default: "暂无数据" },
  actionsWidth: { type: String, default: "180" },
  primaryField: { type: String, default: "" },
  expandable: { type: Boolean, default: false },
});

const emit = defineEmits(["row-click", "selection-change", "page-change", "size-change"]);

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < props.breakpoint);

const expandedRows = reactive(new Set());

const visibleCardColumns = computed(() => {
  return props.columns.filter((col) => !col.hideOnCard);
});

function getRowId(row) {
  return props.rowKey ? row[props.rowKey] : undefined;
}

function toggleExpand(row) {
  const id = getRowId(row);
  if (expandedRows.has(id)) {
    expandedRows.delete(id);
  } else {
    expandedRows.add(id);
  }
}

function isExpanded(row) {
  const id = getRowId(row);
  return id !== undefined && expandedRows.has(id);
}

function onResize() {
  windowWidth.value = window.innerWidth;
}

onMounted(() => window.addEventListener("resize", onResize));
onUnmounted(() => window.removeEventListener("resize", onResize));

function getPrimaryField(row) {
  if (props.primaryField && row[props.primaryField] !== undefined) {
    return row[props.primaryField];
  }
  const primaryCol = props.columns.find((c) => c.cardPrimary);
  if (primaryCol) return row[primaryCol.prop] || "—";
  // Fallback to first column
  const first = props.columns[0];
  return first ? row[first.prop] || "—" : "—";
}

function formatValue(value, formatter) {
  if (typeof formatter === "function") return formatter(value);
  if (value === null || value === undefined) return "—";
  return value;
}

function onRowClick(row) {
  emit("row-click", row);
}

function onSelectionChange(selection) {
  emit("selection-change", selection);
}

function onPageChange(page) {
  emit("page-change", page);
}

function onSizeChange(size) {
  emit("size-change", size);
}
</script>

<style scoped>
.responsive-data-list {
  width: 100%;
}

/* ========== 卡片视图 ========== */
.card-view {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.data-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.data-card:active {
  background: var(--bg-card-hover);
  transform: scale(0.99);
}

.card-primary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.card-primary-text {
  font-size: var(--font-size-mobile-base);
  font-weight: 600;
  color: var(--text-primary);
}

.card-primary-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.expand-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-muted);
  transition: all var(--transition-fast);
  font-size: 14px;
}

.expand-toggle:active {
  transform: scale(0.92);
}

.expand-toggle.expanded {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.expand-toggle.expanded .el-icon {
  transform: rotate(180deg);
}

.expand-toggle .el-icon {
  transition: transform var(--transition-fast);
}

.card-expand {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}

.card-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
}

.card-field {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-field-label {
  font-size: var(--font-size-mobile-sm);
  color: var(--text-muted);
}

.card-field-value {
  font-size: var(--font-size-mobile-base);
  color: var(--text-primary);
  font-weight: 500;
}

/* ========== 分页 ========== */
.list-pagination {
  display: flex;
  justify-content: center;
  padding-top: 16px;
}

@media (max-width: 767px) {
  .list-pagination {
    padding-bottom: 8px;
  }
}
</style>

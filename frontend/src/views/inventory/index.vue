<template>
  <div class="inventory-page">
    <PageHeader title="库存管理" :breadcrumb="[{label: '仪表盘', path: '/'}, {label: '商品与财务'}]">
      <template #extra>
        <el-button type="primary" @click="openProductDialog()">新增商品</el-button>
      </template>
    </PageHeader>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="商品管理" name="product" />
      <el-tab-pane label="入库出库" name="stock" />
      <el-tab-pane label="库存流水" name="record" />
      <el-tab-pane label="供应链" name="supply-chain" />
    </el-tabs>

    <!-- ==================== 商品管理 ==================== -->
    <TabTransition tab-key="product" v-if="activeTab === 'product'">
      <DrawerFilter
        :has-active-filters="hasProductFilters"
        :active-count="productFilterCount"
        @reset="resetProductQuery"
        @confirm="fetchProducts"
      >
        <template #toolbar>
          <el-input v-model="productQuery.keyword" placeholder="搜索商品…" clearable @keyup.enter="fetchProducts" />
          <el-popover :width="300" trigger="click">
            <template #reference>
              <el-badge :value="lowStockTotal" :hidden="lowStockTotal === 0">
                <el-button type="warning">库存预警</el-button>
              </el-badge>
            </template>
            <el-table :data="lowStockList" size="small" max-height="300">
              <el-table-column prop="name" label="商品" />
              <el-table-column label="库存" min-width="70">
                <template #default="{ row }">
                  <span class="low-stock-text">{{ row.stock_qty }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="alert_qty" label="预警值" min-width="70" />
            </el-table>
          </el-popover>
        </template>
        <template #filters>
          <el-form-item label="分类">
            <el-select v-model="productQuery.categoryId" placeholder="全部" clearable @change="fetchProducts" style="width:150px">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </template>
        <template #actions>
          <el-button type="primary" @click="fetchProducts">查询</el-button>
          <el-button @click="resetProductQuery">重置</el-button>
        </template>
      </DrawerFilter>

      <el-card shadow="never">
        <template v-if="productLoading">
          <SkeletonTable :rows="5" :cols="7" />
        </template>
        <template v-else>
          <el-table :data="productList" stripe>
            <el-table-column prop="id" label="ID" min-width="60" />
            <el-table-column prop="name" label="商品名称" min-width="120" />
            <el-table-column prop="categoryName" label="分类" min-width="100" />
            <el-table-column prop="unit" label="单位" min-width="70" />
            <el-table-column prop="salePrice" label="售价" min-width="80" />
            <el-table-column label="库存" min-width="80">
              <template #default="{ row }">
                <span :class="{ 'low-stock-text': row.stock_qty <= row.alert_qty }">{{ row.stock_qty }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="alert_qty" label="预警值" min-width="80" />
            <el-table-column prop="supplierName" label="供应商" min-width="100" />
            <el-table-column label="状态" min-width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '在售' : '停售' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="160">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="openProductDialog(row)">编辑</el-button>
                <el-button type="danger" size="small" text @click="handleProductDelete(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <EmptyState description="暂无商品数据" />
            </template>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model="productQuery.page" :page-size="productQuery.pageSize" :total="productTotal"
              :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
              @current-change="(p) => { productQuery.page = p; fetchProducts(); }"
              @size-change="(s) => { productQuery.pageSize = s; productQuery.page = 1; fetchProducts(); }"
            />
          </div>
        </template>
      </el-card>

      <!-- 商品弹窗 -->
      <el-dialog v-model="productDialog" :title="productEdit ? '编辑商品' : '新增商品'" width="480px" :close-on-click-modal="false">
        <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="80px">
          <el-form-item label="名称" prop="name">
            <el-input v-model="productForm.name" />
          </el-form-item>
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="productForm.categoryId" style="width:100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="供应商">
            <el-select v-model="productForm.supplierId" clearable style="width:100%" @focus="loadSuppliers">
              <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="单位">
            <el-select v-model="productForm.unit" style="width:100%">
              <el-option label="瓶" value="瓶" />
              <el-option label="盒" value="盒" />
              <el-option label="支" value="支" />
              <el-option label="包" value="包" />
            </el-select>
          </el-form-item>
          <el-form-item label="售价">
            <el-input-number v-model="productForm.salePrice" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="库存量">
            <el-input-number v-model="productForm.stockQty" :min="0" style="width:100%" />
          </el-form-item>
          <el-form-item label="预警阈值">
            <el-input-number v-model="productForm.alertQty" :min="0" style="width:100%" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="productStatusSwitch" active-text="在售" inactive-text="停售" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="productDialog = false">取消</el-button>
          <el-button type="primary" :loading="productSubmitting" @click="handleProductSubmit">确定</el-button>
        </template>
      </el-dialog>
    </TabTransition>

    <!-- ==================== 入库出库 ==================== -->
    <TabTransition tab-key="stock" v-if="activeTab === 'stock'">
      <el-card shadow="never" class="stock-form-card">
        <el-form :model="stockForm" :rules="stockRules" ref="stockFormRef" label-width="80px" style="max-width:480px">
          <el-form-item label="类型" prop="type">
            <el-radio-group v-model="stockForm.type">
              <el-radio :value="1">入库</el-radio>
              <el-radio :value="2">出库</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="商品" prop="productId">
            <el-select v-model="stockForm.productId" filterable placeholder="搜索商品" style="width:100%" @focus="loadProductsForSelect">
              <el-option v-for="p in allProducts" :key="p.id" :label="`${p.name} (库存:${p.stock_qty})`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量" prop="qty">
            <el-input-number v-model="stockForm.qty" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item label="单价">
            <el-input-number v-model="stockForm.price" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="总金额">
            <el-input-number v-model="stockForm.totalAmount" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="供应商">
            <el-select v-model="stockForm.supplierId" clearable style="width:100%" @focus="loadSuppliers">
              <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="stockForm.remark" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="stockSubmitting" @click="handleStockSubmit">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </TabTransition>

    <!-- ==================== 库存流水 ==================== -->
    <TabTransition tab-key="record" v-if="activeTab === 'record'">
      <div class="action-bar">
        <el-select v-model="recordQuery.type" placeholder="类型" clearable @change="fetchRecords" style="width:120px">
          <el-option :value="1" label="入库" />
          <el-option :value="2" label="出库" />
          <el-option :value="3" label="退货" />
        </el-select>
        <el-button type="primary" @click="fetchRecords">查询</el-button>
      </div>

      <el-card shadow="never">
        <template v-if="recordLoading">
          <SkeletonTable :rows="5" :cols="8" />
        </template>
        <template v-else>
          <el-table :data="recordList" stripe>
          <el-table-column prop="id" label="ID" min-width="60" />
          <el-table-column prop="productName" label="商品" min-width="120" />
          <el-table-column label="类型" min-width="80">
            <template #default="{ row }">
              <el-tag :type="recordTypeTag(row.type)" size="small">{{ recordTypeText(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="qty" label="数量" min-width="80" />
          <el-table-column prop="price" label="单价" min-width="80" />
          <el-table-column prop="totalAmount" label="总金额" min-width="100" />
          <el-table-column prop="supplierName" label="供应商" min-width="100" />
          <el-table-column prop="operator" label="操作人" min-width="100" />
          <el-table-column prop="remark" label="备注" min-width="120" />
          <el-table-column prop="createTime" label="时间" min-width="160" />
          <template #empty>
            <EmptyState description="暂无库存流水" />
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

    <!-- ==================== 供应链（供应商 + 采购 + 退货） ==================== -->
    <TabTransition tab-key="supply-chain" v-if="activeTab === 'supply-chain'">
      <el-tabs v-model="supplyChainSubTab" @tab-change="onSupplyChainTabChange" style="margin-bottom:12px">
        <el-tab-pane label="供应商" name="supplier" />
        <el-tab-pane label="采购订单" name="purchase" />
        <el-tab-pane label="退货管理" name="return" />
      </el-tabs>

      <InventorySupplierTab v-if="supplyChainSubTab === 'supplier'" @changed="onSupplierChanged" />
      <InventoryPurchaseTab v-if="supplyChainSubTab === 'purchase'" :suppliers="suppliers" :all-products="allProducts" @products-changed="onProductsChanged" />

      <template v-if="supplyChainSubTab === 'return'">
      <div class="action-bar">
        <el-select v-model="returnQuery.status" placeholder="状态" clearable @change="fetchReturnOrders" style="width:130px">
          <el-option :value="0" label="草稿" />
          <el-option :value="1" label="已提交" />
          <el-option :value="2" label="已审批" />
          <el-option :value="3" label="已完成" />
          <el-option :value="4" label="已驳回" />
        </el-select>
        <el-select v-model="returnQuery.supplierId" placeholder="供应商" clearable @change="fetchReturnOrders" style="width:150px" @focus="loadSuppliers">
          <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-button type="primary" @click="fetchReturnOrders">查询</el-button>
        <el-button type="success" @click="openReturnDialog()">新建退货单</el-button>
      </div>

      <el-card shadow="never">
        <template v-if="returnLoading">
          <SkeletonTable :rows="5" :cols="9" />
        </template>
        <template v-else>
          <el-table :data="returnList" stripe>
            <el-table-column prop="orderNo" label="退货单号" min-width="180" />
            <el-table-column prop="supplierName" label="供应商" min-width="120" />
            <el-table-column label="退货金额" min-width="110">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="itemCount" label="种类" min-width="60" />
            <el-table-column prop="totalQty" label="总数量" min-width="80" />
            <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <el-tag :type="returnStatusTag(row.status)" size="small">{{ returnStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicant" label="申请人" min-width="80" />
            <el-table-column prop="createTime" label="创建时间" min-width="160" />
            <el-table-column label="操作" min-width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="openReturnDialog(row)">详情</el-button>
                <el-button v-if="row.status === 0" type="success" size="small" text @click="handleReturnSubmit(row.id)">提交</el-button>
                <el-button v-if="row.status === 1" type="success" size="small" text @click="handleReturnApprove(row.id)">审核</el-button>
                <el-button v-if="row.status === 1" type="warning" size="small" text @click="handleReturnReject(row.id)">驳回</el-button>
                <el-button v-if="row.status === 2" type="warning" size="small" text @click="handleReturnComplete(row.id)">完成</el-button>
                <el-button v-if="row.status === 0" type="danger" size="small" text @click="handleReturnCancel(row.id)">取消</el-button>
                <el-button v-if="row.status === 0" type="danger" size="small" text @click="handleReturnDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <EmptyState description="暂无退货订单" />
            </template>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model="returnQuery.page" :page-size="returnQuery.pageSize" :total="returnTotal"
              :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
              @current-change="(p) => { returnQuery.page = p; fetchReturnOrders(); }"
              @size-change="(s) => { returnQuery.pageSize = s; returnQuery.page = 1; fetchReturnOrders(); }"
            />
          </div>
        </template>
      </el-card>

      <!-- 退货订单弹窗 -->
      <el-dialog v-model="returnDialog" :title="returnEdit ? '编辑退货单' : '新建退货单'" width="700px" :close-on-click-modal="false">
        <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="80px">
          <el-form-item label="供应商" prop="supplierId">
            <el-select v-model="returnForm.supplierId" style="width:100%" @focus="loadSuppliers" :disabled="returnEdit">
              <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="退货原因">
            <el-input v-model="returnForm.reason" :disabled="returnEdit" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="returnForm.remark" :disabled="returnEdit" />
          </el-form-item>
          <el-form-item label="退货明细">
            <el-table :data="returnForm.items" border size="small">
              <el-table-column label="商品" min-width="150">
                <template #default="{ row, $index }">
                  <el-select v-model="row.productId" filterable placeholder="搜索商品" style="width:100%" :disabled="returnEdit" @change="(v) => onReturnProductChange($index, v)">
                    <el-option v-for="p in allProducts" :key="p.id" :label="`${p.name} (库存:${p.stock_qty})`" :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="单位" min-width="70">
                <template #default="{ row }">{{ row.unit }}</template>
              </el-table-column>
              <el-table-column label="数量" min-width="100">
                <template #default="{ row }">
                  <el-input-number v-model="row.qty" :min="1" size="small" :disabled="returnEdit" @change="calcReturnTotal" />
                </template>
              </el-table-column>
              <el-table-column label="单价" min-width="110">
                <template #default="{ row }">
                  <el-input-number v-model="row.price" :min="0" :precision="2" size="small" :disabled="returnEdit" @change="calcReturnTotal" />
                </template>
              </el-table-column>
              <el-table-column label="小计" min-width="100">
                <template #default="{ row }">¥{{ ((row.qty || 0) * (row.price || 0)).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column v-if="!returnEdit" label="操作" min-width="60">
                <template #default="{ $index }">
                  <el-button type="danger" size="small" text @click="returnForm.items.splice($index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!returnEdit" type="primary" size="small" style="margin-top:8px" @click="addReturnItem">+ 添加商品</el-button>
          </el-form-item>
          <el-form-item label="合计">
            <span style="font-size:18px;font-weight:700;color:var(--primary-color)">¥{{ returnTotalAmount }}</span>
            <span style="margin-left:16px;color:var(--text-secondary)">共 {{ returnTotalQty }} 件</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="returnDialog = false">关闭</el-button>
          <el-button v-if="!returnEdit" type="primary" :loading="returnSubmitting" @click="handleReturnCreate">保存草稿</el-button>
        </template>
      </el-dialog>
      </template>
    </TabTransition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { categoryApi, supplierApi, productApi, stockRecordApi, returnOrderApi } from "@/api/inventory";
import { ElMessage, ElMessageBox } from "element-plus";
import SkeletonTable from "@/components/SkeletonTable.vue";
import EmptyState from "@/components/EmptyState.vue";
import PageHeader from "@/components/PageHeader.vue";
import TabTransition from "@/components/TabTransition.vue";
import DrawerFilter from "@/components/DrawerFilter.vue";
import InventorySupplierTab from "./InventorySupplierTab.vue";
import InventoryPurchaseTab from "./InventoryPurchaseTab.vue";

const activeTab = ref("product");
const supplyChainSubTab = ref("supplier");

// ========== 分类 ==========
const categories = ref([]);
async function loadCategories() {
  try { categories.value = await categoryApi.list(); } catch { ElMessage.error("加载分类失败"); }
}

// ========== 供应商（共享数据，供各 Tab 下拉选择） ==========
const suppliers = ref([]);

async function loadSuppliers() {
  if (suppliers.value.length > 0) return;
  try { suppliers.value = await supplierApi.list(); } catch { ElMessage.error("加载供应商失败"); }
}

function onSupplierChanged() {
  suppliers.value = []; // 清除缓存，下次 loadSuppliers 重新拉取
}

// ========== 商品 ==========
const productList = ref([]);
const productTotal = ref(0);
const productLoading = ref(false);
const productDialog = ref(false);
const productEdit = ref(false);
const productEditId = ref(null);
const productSubmitting = ref(false);
const productFormRef = ref(null);
const allProducts = ref([]);

const productQuery = reactive({ page: 1, pageSize: 10, categoryId: null, keyword: "" });

const hasProductFilters = computed(() => {
  return !!(productQuery.categoryId || productQuery.keyword);
});

const productFilterCount = computed(() => {
  let n = 0;
  if (productQuery.categoryId) n++;
  if (productQuery.keyword) n++;
  return n;
});

function resetProductQuery() {
  productQuery.categoryId = null;
  productQuery.keyword = "";
  productQuery.page = 1;
  fetchProducts();
}
const productForm = reactive({ name: "", categoryId: null, supplierId: null, unit: "瓶", salePrice: 0, stockQty: 0, alertQty: 10, status: 1 });
const productRules = {
  name: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }]
};
const productStatusSwitch = computed({
  get: () => productForm.status === 1,
  set: (v) => { productForm.status = v ? 1 : 0; }
});

async function loadProductsForSelect() {
  if (allProducts.value.length > 0) return;
  try {
    const res = await productApi.page({ page: 1, pageSize: 200 });
    allProducts.value = res.list || [];
  } catch { ElMessage.error("加载商品失败"); }
}
async function fetchProducts() {
  productLoading.value = true;
  try {
    const res = await productApi.page({ ...productQuery });
    productList.value = res.list || [];
    productTotal.value = res.total || 0;
  } catch { ElMessage.error("加载商品列表失败"); } finally { productLoading.value = false; }
}
function openProductDialog(row) {
  productFormRef.value?.resetFields();
  if (row) {
    productEdit.value = true;
    productEditId.value = row.id;
    productForm.name = row.name;
    productForm.categoryId = row.categoryId;
    productForm.supplierId = row.supplierId;
    productForm.unit = row.unit || "瓶";
    productForm.salePrice = row.salePrice || 0;
    productForm.stockQty = row.stockQty || 0;
    productForm.alertQty = row.alertQty || 10;
    productForm.status = row.status !== undefined ? row.status : 1;
  } else {
    productEdit.value = false;
    productEditId.value = null;
    productForm.name = "";
    productForm.categoryId = null;
    productForm.supplierId = null;
    productForm.unit = "瓶";
    productForm.salePrice = 0;
    productForm.stockQty = 0;
    productForm.alertQty = 10;
    productForm.status = 1;
  }
  loadSuppliers();
  productDialog.value = true;
}
async function handleProductSubmit() {
  const valid = await productFormRef.value.validate().catch(() => false);
  if (!valid) return;
  productSubmitting.value = true;
  try {
    const data = { ...productForm };
    if (productEdit.value) {
      await productApi.update(productEditId.value, data);
      ElMessage.success("修改成功");
    } else {
      await productApi.create(data);
      ElMessage.success("新增成功");
    }
    productDialog.value = false;
    fetchProducts();
    allProducts.value = [];
    fetchLowStock();
  } catch { ElMessage.error('保存商品失败') } finally { productSubmitting.value = false; }
}
async function handleProductDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该商品吗？", "提示", { type: "warning" });
    await productApi.remove(row.id);
    ElMessage.success("删除成功");
    fetchProducts();
  } catch { ElMessage.error('操作失败，请重试') }
}

// ========== 库存预警 ==========
const lowStockList = ref([]);
const lowStockTotal = ref(0);
async function fetchLowStock() {
  try {
    const res = await productApi.lowStock({ page: 1, pageSize: 50 });
    lowStockList.value = res.list || [];
    lowStockTotal.value = res.total || 0;
  } catch { ElMessage.error("加载库存预警失败"); }
}

// ========== 入库出库 ==========
const stockSubmitting = ref(false);
const stockFormRef = ref(null);
const stockForm = reactive({ type: 1, productId: null, qty: 1, price: 0, totalAmount: 0, supplierId: null, remark: "" });
const stockRules = {
  productId: [{ required: true, message: "请选择商品", trigger: "change" }],
  qty: [{ required: true, message: "请输入数量", trigger: "blur" }]
};

async function handleStockSubmit() {
  const valid = await stockFormRef.value.validate().catch(() => false);
  if (!valid) return;
  stockSubmitting.value = true;
  try {
    await stockRecordApi.create({ ...stockForm });
    ElMessage.success(stockForm.type === 1 ? "入库成功" : "出库成功");
    stockForm.qty = 1;
    stockForm.price = 0;
    stockForm.totalAmount = 0;
    stockForm.supplierId = null;
    stockForm.remark = "";
    stockForm.productId = null;
    allProducts.value = [];
    fetchLowStock();
    if (activeTab.value === "record") fetchRecords();
  } catch { ElMessage.error('入库失败') } finally { stockSubmitting.value = false; }
}

// ========== 库存流水 ==========
const recordList = ref([]);
const recordTotal = ref(0);
const recordLoading = ref(false);
const recordQuery = reactive({ page: 1, pageSize: 10, type: null });

const recordTypeMap = { 1: "入库", 2: "出库", 3: "退货" };
const recordTypeTagMap = { 1: "success", 2: "danger", 3: "warning" };
function recordTypeText(t) { return recordTypeMap[t] || "未知"; }
function recordTypeTag(t) { return recordTypeTagMap[t] || "info"; }

async function fetchRecords() {
  recordLoading.value = true;
  try {
    const res = await stockRecordApi.page({ ...recordQuery });
    recordList.value = res.list || [];
    recordTotal.value = res.total || 0;
  } catch { ElMessage.error("加载库存流水失败"); } finally { recordLoading.value = false; }
}

// ========== 采购订单 → InventoryPurchaseTab ==========
function onProductsChanged() {
  allProducts.value = []; // 清除缓存，下次 loadProductsForSelect 重新拉取
}

// ========== 退货管理 ==========
const returnList = ref([]);
const returnTotal = ref(0);
const returnLoading = ref(false);
const returnDialog = ref(false);
const returnEdit = ref(false);
const returnEditId = ref(null);
const returnSubmitting = ref(false);
const returnFormRef = ref(null);

const returnQuery = reactive({ page: 1, pageSize: 10, status: null, supplierId: null });
const returnForm = reactive({ supplierId: null, reason: "", remark: "", items: [] });
const returnRules = {
  supplierId: [{ required: true, message: "请选择供应商", trigger: "change" }]
};

const returnStatusMap = { 0: "草稿", 1: "已提交", 2: "已审批", 3: "已完成", 4: "已驳回" };
const returnStatusTagMap = { 0: "info", 1: "", 2: "success", 3: "", 4: "danger" };
function returnStatusText(s) { return returnStatusMap[s] || "未知"; }
function returnStatusTag(s) { return returnStatusTagMap[s] || "info"; }

const returnTotalAmount = computed(() => {
  return returnForm.items.reduce((sum, item) => sum + (item.qty || 0) * (item.price || 0), 0).toFixed(2);
});
const returnTotalQty = computed(() => {
  return returnForm.items.reduce((sum, item) => sum + (item.qty || 0), 0);
});

function addReturnItem() {
  returnForm.items.push({ productId: null, productName: "", unit: "", qty: 1, price: 0 });
}
function onReturnProductChange(index, productId) {
  const p = allProducts.value.find(x => x.id === productId);
  if (p) {
    returnForm.items[index].productName = p.name;
    returnForm.items[index].unit = p.unit || "";
  }
}
function calcReturnTotal() { /* computed auto-updates */ }

async function fetchReturnOrders() {
  returnLoading.value = true;
  try {
    const res = await returnOrderApi.page({ ...returnQuery });
    returnList.value = res.list || [];
    returnTotal.value = res.total || 0;
  } catch { ElMessage.error("加载退货订单失败"); } finally { returnLoading.value = false; }
}
function openReturnDialog(row) {
  returnFormRef.value?.resetFields();
  loadSuppliers();
  loadProductsForSelect();
  if (row) {
    returnEdit.value = true;
    returnEditId.value = row.id;
    returnForm.supplierId = row.supplierId;
    returnForm.reason = row.reason || "";
    returnForm.remark = row.remark || "";
    returnForm.items = (row.items || []).map(i => ({
      productId: i.productId, productName: i.productName, unit: i.unit, qty: i.qty, price: i.price
    }));
  } else {
    returnEdit.value = false;
    returnEditId.value = null;
    returnForm.supplierId = null;
    returnForm.reason = "";
    returnForm.remark = "";
    returnForm.items = [];
    addReturnItem();
  }
  returnDialog.value = true;
}
async function handleReturnCreate() {
  const valid = await returnFormRef.value.validate().catch(() => false);
  if (!valid) return;
  if (returnForm.items.length === 0) { ElMessage.warning("请添加退货商品"); return; }
  returnSubmitting.value = true;
  try {
    await returnOrderApi.create({
      supplierId: returnForm.supplierId,
      reason: returnForm.reason,
      remark: returnForm.remark,
      items: returnForm.items.map(i => ({ productId: i.productId, productName: i.productName, unit: i.unit, qty: i.qty, price: i.price }))
    });
    ElMessage.success("创建成功");
    returnDialog.value = false;
    fetchReturnOrders();
  } catch { ElMessage.error('保存退货单失败') } finally { returnSubmitting.value = false; }
}
async function handleReturnSubmit(id) {
  try {
    await ElMessageBox.confirm("确认提交审核？", "提示", { type: "warning" });
    await returnOrderApi.submit(id);
    ElMessage.success("提交成功");
    fetchReturnOrders();
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleReturnApprove(id) {
  try {
    await ElMessageBox.confirm("确认审核通过？", "提示", { type: "warning" });
    await returnOrderApi.approve(id);
    ElMessage.success("审核通过");
    fetchReturnOrders();
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleReturnReject(id) {
  try {
    await ElMessageBox.confirm("确认驳回该退货申请？", "提示", { type: "warning" });
    await returnOrderApi.reject(id);
    ElMessage.success("已驳回");
    fetchReturnOrders();
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleReturnComplete(id) {
  try {
    await ElMessageBox.confirm("确认完成退货？将自动扣减库存。", "提示", { type: "warning" });
    await returnOrderApi.complete(id);
    ElMessage.success("退货完成，库存已更新");
    fetchReturnOrders();
    allProducts.value = [];
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleReturnCancel(id) {
  try {
    await ElMessageBox.confirm("确认取消该退货单？", "提示", { type: "warning" });
    await returnOrderApi.cancel(id);
    ElMessage.success("取消成功");
    fetchReturnOrders();
  } catch { ElMessage.error('操作失败，请重试') }
}
async function handleReturnDelete(id) {
  try {
    await ElMessageBox.confirm("确定删除该草稿退货单吗？", "提示", { type: "warning" });
    await returnOrderApi.remove(id);
    ElMessage.success("删除成功");
    fetchReturnOrders();
  } catch { ElMessage.error('操作失败，请重试') }
}

// ========== Tab 切换预加载 ==========
function onTabChange(name) {
  if (name === "product") { loadCategories(); fetchProducts(); fetchLowStock(); }
  else if (name === "stock") { loadSuppliers(); loadProductsForSelect(); }
  else if (name === "record") { fetchRecords(); }
  else if (name === "supply-chain") { loadSuppliers(); loadProductsForSelect(); onSupplyChainTabChange("supplier"); }
}

function onSupplyChainTabChange(name) {
  if (name === "return") { loadSuppliers(); loadProductsForSelect(); fetchReturnOrders(); }
}

onMounted(() => {
  loadCategories();
  fetchProducts();
  fetchLowStock();
});
</script>

<style scoped>
.inventory-page {
  overflow-x: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.stock-form-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.low-stock-text {
  color: var(--danger-color);
  font-weight: 600;
}

/* ========== 移动端适配 ========== */
@media (max-width: 767px) {
  .inventory-page {
    padding: 0;
  }

  .action-bar {
    flex-wrap: wrap;
  }
}
</style>

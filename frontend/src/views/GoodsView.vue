<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const dialog = ref(false)
const editing = ref(null)
const selected = ref([])
const discountRate = ref(0.9)

const form = ref({
  name: '', category: '鸭脖', unit: '称重', price: 0, spicy: '不辣', memberPrice: 0, onSale: true, stock: 0
})
const categories = ['鸭脖', '猪蹄', '卤蛋', '藕片', '海带', '豆干']
const units = ['称重', '按份']
const spices = ['不辣', '微辣', '中辣', '重辣']

const selectedIds = computed(() => selected.value.map(r => r.id))

async function load() {
  list.value = await api.listGoods()
}
function openCreate() {
  editing.value = null
  form.value = { name: '', category: '鸭脖', unit: '称重', price: 0, spicy: '不辣', memberPrice: 0, onSale: true, stock: 0 }
  dialog.value = true
}
function openEdit(r) {
  editing.value = r.id
  form.value = { ...r }
  dialog.value = true
}
async function save() {
  const payload = {
    ...form.value,
    price: Number(form.value.price),
    memberPrice: Number(form.value.memberPrice),
    stock: Number(form.value.stock)
  }
  if (editing.value) {
    await api.updateGoods(editing.value, payload)
    ElMessage.success('已更新')
  } else {
    await api.createGoods(payload)
    ElMessage.success('已上架')
  }
  dialog.value = false
  await load()
  emit('changed')
}
async function toggleSale(r) {
  try {
    await api.offSaleGoods(r.id, !r.onSale)
    ElMessage.success(r.onSale ? '已下架' : '已上架')
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

function onSelectionChange(rows) {
  selected.value = rows
}
async function batchOff() {
  if (!selectedIds.value.length) return
  try {
    const n = await api.batchOffGoods(selectedIds.value)
    ElMessage.success(`已批量下架 ${n} 个商品`)
    selected.value = []
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}
async function batchDiscount() {
  if (!selectedIds.value.length) return
  try {
    const n = await api.batchDiscountGoods(selectedIds.value, Number(discountRate.value))
    ElMessage.success(`已对 ${n} 个商品应用会员折扣`)
    selected.value = []
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2>卤味商品</h2>
      <el-button type="primary" @click="openCreate">新增商品</el-button>
    </div>

    <el-table :data="list" border @selection-change="onSelectionChange" row-key="id">
      <el-table-column type="selection" width="46" />
      <el-table-column prop="name" label="品名" min-width="140" />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="spicy" label="辣度" width="80" />
      <el-table-column label="售价" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="会员价" width="90">
        <template #default="{ row }">¥{{ row.memberPrice }}</template>
      </el-table-column>
      <el-table-column label="库存" prop="stock" width="80" />
      <el-table-column label="上架" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.onSale ? 'success' : 'info'">{{ row.onSale ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="toggleSale(row)">{{ row.onSale ? '下架' : '上架' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部浮条：勾选批量操作 -->
    <div class="float-bar" v-if="selectedIds.length">
      <span class="count">已选 <b>{{ selectedIds.length }}</b> 个商品</span>
      <el-input-number v-model="discountRate" :min="0.1" :max="1" :step="0.1" :precision="2" size="small" />
      <el-button size="small" type="warning" @click="batchDiscount">批量会员折扣</el-button>
      <el-button size="small" type="danger" @click="batchOff">批量下架</el-button>
      <el-button size="small" @click="selected = []">取消选择</el-button>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑商品' : '新增商品'">
      <el-form :model="form" label-width="80px">
        <el-form-item label="品名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="form.unit"><el-option v-for="u in units" :key="u" :label="u" :value="u" /></el-select>
        </el-form-item>
        <el-form-item label="辣度">
          <el-select v-model="form.spicy"><el-option v-for="s in spices" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="售价"><el-input-number v-model="form.price" :min="0" :step="1" /></el-form-item>
        <el-form-item label="会员价"><el-input-number v-model="form.memberPrice" :min="0" :step="1" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" :step="1" /></el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="form.onSale" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.float-bar {
  position: sticky; bottom: 0; margin-top: 14px;
  display: flex; align-items: center; gap: 12px;
  padding: 12px 18px; background: var(--primary-wash);
  border: 1px solid var(--line); border-radius: 12px;
}
.float-bar .count { font-size: 14px; }
.float-bar .count b { color: var(--primary); }
</style>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const available = ref({})
const dialog = ref(false)
const editing = ref(null)
const form = ref({
  supplier: '', material: '冻禽', qty: 0, unit: 'kg', acceptStatus: '待验收', batchNo: '', arrivalDate: ''
})
const materials = ['冻禽', '香料', '素菜', '油料']
const acceptStatuses = ['待验收', '合格', '退回']

async function load() {
  list.value = await api.listStockIns()
  try { available.value = await api.availableStock() } catch (e) {}
}
function openCreate() {
  editing.value = null
  form.value = { supplier: '', material: '冻禽', qty: 0, unit: 'kg', acceptStatus: '待验收', batchNo: '', arrivalDate: '' }
  dialog.value = true
}
function openEdit(r) {
  editing.value = r.id
  form.value = { ...r }
  dialog.value = true
}
async function save() {
  const payload = {
    supplier: form.value.supplier,
    material: form.value.material,
    qty: Number(form.value.qty),
    unit: form.value.unit,
    acceptStatus: form.value.acceptStatus,
    batchNo: form.value.batchNo,
    arrivalDate: form.value.arrivalDate || null
  }
  if (editing.value) {
    await api.updateStockIn(editing.value, payload)
    ElMessage.success('已更新')
  } else {
    await api.createStockIn(payload)
    ElMessage.success('已登记进货')
  }
  dialog.value = false
  await load()
  emit('changed')
}
async function accept(r, status) {
  try {
    await api.acceptStockIn(r.id, status)
    ElMessage.success(status === '合格' ? '已验收合格（计入可用原料）' : '已退回（不入库）')
    await load(); emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2>原料进货</h2>
      <el-button type="primary" @click="openCreate">登记进货</el-button>
    </div>

    <el-alert
      class="avail"
      type="success"
      :closable="false"
      title="可用原料统计（仅「合格」计入）"
      :description="Object.entries(available).map(([k,v]) => k + ' ' + v).join('　') || '暂无'"
    />

    <el-table :data="list" border style="margin-top:14px">
      <el-table-column prop="supplier" label="供应商" width="120" />
      <el-table-column prop="material" label="材料" width="90" />
      <el-table-column label="数量" width="100">
        <template #default="{ row }">{{ row.qty }} {{ row.unit }}</template>
      </el-table-column>
      <el-table-column label="验收" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.acceptStatus === '合格' ? 'success' : (row.acceptStatus === '退回' ? 'danger' : 'info')">{{ row.acceptStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="batchNo" label="到货批次" width="120" />
      <el-table-column label="到货日期" width="120">
        <template #default="{ row }">{{ row.arrivalDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" :disabled="row.acceptStatus !== '待验收'" @click="accept(row, '合格')">验收合格</el-button>
          <el-button link type="danger" :disabled="row.acceptStatus !== '待验收'" @click="accept(row, '退回')">退回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑进货' : '登记进货'">
      <el-form :model="form" label-width="80px">
        <el-form-item label="供应商"><el-input v-model="form.supplier" /></el-form-item>
        <el-form-item label="材料">
          <el-select v-model="form.material"><el-option v-for="m in materials" :key="m" :label="m" :value="m" /></el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.qty" :min="0.01" :step="1" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item>
        <el-form-item label="验收状态">
          <el-select v-model="form.acceptStatus"><el-option v-for="s in acceptStatuses" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="到货批次"><el-input v-model="form.batchNo" /></el-form-item>
        <el-form-item label="到货日期"><el-input v-model="form.arrivalDate" placeholder="2026-09-01" /></el-form-item>
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
.avail { margin-bottom: 4px; }
</style>

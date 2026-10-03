<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const dialog = ref(false)
const editing = ref(null)
const form = ref({
  batchNo: '', recipe: '', startTime: '', endTime: '', status: '卤制中', outputQty: 0, note: ''
})
const statuses = ['卤制中', '冷却中', '已封装', '已报废']
const nextStatus = { '卤制中': '冷却中', '冷却中': '已封装', '已封装': null, '已报废': null }

async function load() {
  list.value = await api.listBatches()
}
function openCreate() {
  editing.value = null
  form.value = { batchNo: '', recipe: '', startTime: '', endTime: '', status: '卤制中', outputQty: 0, note: '' }
  dialog.value = true
}
function openEdit(r) {
  editing.value = r.id
  form.value = { ...r, startTime: toLocal(r.startTime), endTime: toLocal(r.endTime) }
  dialog.value = true
}
function toLocal(s) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 19)
}
async function save() {
  const payload = {
    batchNo: form.value.batchNo,
    recipe: form.value.recipe,
    startTime: form.value.startTime || null,
    endTime: form.value.endTime || null,
    outputQty: Number(form.value.outputQty),
    note: form.value.note
  }
  if (editing.value) {
    payload.status = form.value.status
    await api.updateBatch(editing.value, payload)
    ElMessage.success('已更新')
  } else {
    await api.createBatch(payload)
    ElMessage.success('已登记批次')
  }
  dialog.value = false
  await load()
  emit('changed')
}
async function advance(r) {
  const to = nextStatus[r.status]
  if (!to) { ElMessage.warning(r.status === '已报废' ? '已报废不可再封装' : '已封装为终态'); return }
  try {
    await api.transferBatch(r.id, to)
    ElMessage.success(`已流转至「${to}」`)
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
      <h2>卤制批次</h2>
      <el-button type="primary" @click="openCreate">新增批次</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="batchNo" label="锅次号" width="140" />
      <el-table-column prop="recipe" label="配方名" min-width="140" />
      <el-table-column label="开始" width="170">
        <template #default="{ row }">{{ row.startTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="结束" width="170">
        <template #default="{ row }">{{ row.endTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === '已报废' ? 'danger' : (row.status === '已封装' ? 'success' : 'warning')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封装量" prop="outputQty" width="80" />
      <el-table-column prop="note" label="备注" min-width="120" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" :disabled="!nextStatus[row.status]" @click="advance(row)">
            {{ nextStatus[row.status] ? '流转·' + nextStatus[row.status] : '终态' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑批次' : '新增批次'">
      <el-form :model="form" label-width="80px">
        <el-form-item label="锅次号"><el-input v-model="form.batchNo" /></el-form-item>
        <el-form-item label="配方名"><el-input v-model="form.recipe" /></el-form-item>
        <el-form-item label="开始"><el-input v-model="form.startTime" placeholder="2026-09-01 09:00:00" /></el-form-item>
        <el-form-item label="结束"><el-input v-model="form.endTime" placeholder="2026-09-01 18:00:00" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option v-for="s in statuses" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="封装量"><el-input-number v-model="form.outputQty" :min="0" :step="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" /></el-form-item>
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
</style>

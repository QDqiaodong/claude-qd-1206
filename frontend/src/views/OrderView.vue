<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const goodsOptions = ref([])
const selected = ref([])
const createDialog = ref(false)
const detailDialog = ref(false)
const detailItems = ref([])

const form = ref({ channel: '堂食', paid: false, items: [] })
const channels = ['堂食', '外卖', '预订']

async function load() {
  list.value = await api.listOrders()
}
async function loadGoods() {
  goodsOptions.value = await api.listGoods(true) // 仅上架商品可下单
}
function onSelectionChange(rows) {
  selected.value = rows
}
async function openCreate() {
  await loadGoods()
  form.value = { channel: '堂食', paid: false, items: [] }
  createDialog.value = true
}
async function submitOrder() {
  if (!form.value.items.length) {
    ElMessage.warning('请至少勾选一个商品')
    return
  }
  const payload = {
    channel: form.value.channel,
    paid: form.value.paid,
    items: form.value.items.map(it => ({ goodsId: it.goodsId, qty: Number(it.qty) }))
  }
  try {
    await api.createOrder(payload)
    ElMessage.success('下单成功')
    createDialog.value = false
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}
async function pay(r) {
  try {
    await api.payOrder(r.id)
    ElMessage.success('已支付')
    await load(); emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}
async function refund(r) {
  try {
    await api.refundOrder(r.id)
    ElMessage.success('已退单并回补库存')
    await load(); emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}
async function showItems(r) {
  detailItems.value = await api.listOrderItems(r.id)
  detailDialog.value = true
}

// 底部浮条：勾选批量
async function batchPay() {
  for (const id of selected.value.map(r => r.id)) {
    try { await api.payOrder(id) } catch (e) { ElMessage.error(e.message) }
  }
  ElMessage.success('批量支付完成')
  selected.value = []
  await load(); emit('changed')
}
async function batchRefund() {
  for (const id of selected.value.map(r => r.id)) {
    try { await api.refundOrder(id) } catch (e) { ElMessage.error(e.message) }
  }
  ElMessage.success('批量退单完成')
  selected.value = []
  await load(); emit('changed')
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2>销售订单</h2>
      <el-button type="primary" @click="openCreate">新建订单</el-button>
    </div>

    <el-table :data="list" border @selection-change="onSelectionChange" row-key="id">
      <el-table-column type="selection" width="46" />
      <el-table-column label="订单号" prop="id" width="80" />
      <el-table-column prop="channel" label="渠道" width="90" />
      <el-table-column prop="takeNo" label="取餐号" width="100" />
      <el-table-column label="金额" width="90">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === '已退单' ? 'danger' : (row.status === '已完成' ? 'success' : 'warning')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.paid ? 'success' : 'info'">{{ row.paid ? '已付' : '未付' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <el-button link type="primary" @click="showItems(row)">明细</el-button>
          <el-button link type="success" :disabled="row.paid || row.status === '已退单'" @click="pay(row)">支付</el-button>
          <el-button link type="danger" :disabled="!row.paid || row.status !== '待取'" @click="refund(row)">退单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部浮条：勾选批量操作 -->
    <div class="float-bar" v-if="selected.length">
      <span class="count">已选 <b>{{ selected.length }}</b> 笔订单</span>
      <el-button size="small" type="success" @click="batchPay">批量支付</el-button>
      <el-button size="small" type="danger" @click="batchRefund">批量退单</el-button>
      <el-button size="small" @click="selected = []">取消选择</el-button>
    </div>

    <el-dialog v-model="createDialog" title="新建订单（勾选商品）">
      <el-form :model="form" label-width="80px" style="margin-bottom:12px">
        <el-form-item label="渠道">
          <el-select v-model="form.channel"><el-option v-for="c in channels" :key="c" :label="c" :value="c" /></el-select>
        </el-form-item>
        <el-form-item label="已支付"><el-switch v-model="form.paid" /></el-form-item>
      </el-form>
      <el-table :data="goodsOptions" border height="260"
        @selection-change="(rows) => form.items = rows.map(r => ({ goodsId: r.id, qty: 1 }))">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="name" label="品名" />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column label="售价" width="90"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="订单明细">
      <el-table :data="detailItems" border>
        <el-table-column prop="goodsId" label="商品ID" width="90" />
        <el-table-column label="数量" prop="qty" width="80" />
        <el-table-column label="单价" width="90"><template #default="{ row }">¥{{ row.unitPrice }}</template></el-table-column>
      </el-table>
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

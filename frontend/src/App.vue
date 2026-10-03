<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import router from './router'
import api from './api'

import GoodsView from './views/GoodsView.vue'
import BatchView from './views/BatchView.vue'
import OrderView from './views/OrderView.vue'
import StockInView from './views/StockInView.vue'

// ------------------------------------------------------------------
// 导航形态：顶部胶囊分段（segmented）。四个业务模块切换。
// 操作范式（在各 view 内）：勾选批量 + 底部浮条。
// ------------------------------------------------------------------

const modules = [
  { path: '/goods',   label: '卤味商品', comp: GoodsView },
  { path: '/batch',   label: '卤制批次', comp: BatchView },
  { path: '/order',   label: '销售订单', comp: OrderView },
  { path: '/stockin', label: '原料进货', comp: StockInView }
]

const currentPath = ref(router.currentRoute.value.path)
const counts = ref({})

const currentView = computed(() => {
  const m = modules.find(mm => mm.path === currentPath.value)
  return m ? m.comp : GoodsView
})

async function loadCounts() {
  try {
    const [g, b, o, s] = await Promise.all([
      api.listGoods(), api.listBatches(), api.listOrders(), api.listStockIns()
    ])
    counts.value = {
      '/goods': g.length,
      '/batch': b.length,
      '/order': o.length,
      '/stockin': s.length
    }
  } catch (e) { /* 计数失败不影响主流程 */ }
}

function onSelect(path) {
  if (path !== currentPath.value) router.push(path)
}

watch(() => router.currentRoute.value.path, (p) => { currentPath.value = p })

onMounted(async () => {
  currentPath.value = router.currentRoute.value.path
  await loadCounts()
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">卤</span>
        <div class="brand-text">
          <div class="brand-title">卤味熟食店</div>
          <div class="brand-sub">赭橙主题 · 顶部胶囊分段导航 · 勾选批量 + 底部浮条</div>
        </div>
      </div>
      <nav class="capsule">
        <button
          v-for="m in modules"
          :key="m.path"
          class="capsule-btn"
          :class="{ active: currentPath === m.path }"
          @click="onSelect(m.path)"
        >{{ m.label }}（{{ counts[m.path] ?? 0 }}）</button>
      </nav>
    </header>
    <main class="content">
      <component :is="currentView" @changed="loadCounts" />
    </main>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; flex-direction: column; }
.topbar {
  display: flex; align-items: center; gap: 20px;
  padding: 0 22px; height: 60px;
  background: linear-gradient(100deg, var(--primary-dark-2) 0%, var(--primary) 70%, var(--primary-light-3) 100%);
  color: #fff; box-shadow: 0 2px 12px rgba(160, 82, 45, 0.25);
}
.brand { display: flex; align-items: center; gap: 12px; }
.brand-mark {
  width: 38px; height: 38px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: #fff; color: var(--primary-dark-2); font-weight: 700; font-size: 20px;
}
.brand-title { font-size: 17px; font-weight: 600; }
.brand-sub { font-size: 12px; opacity: 0.85; }
.capsule {
  display: flex; gap: 6px; margin-left: auto;
  background: rgba(255, 255, 255, 0.18); padding: 4px; border-radius: 999px;
}
.capsule-btn {
  border: none; cursor: pointer; color: #fff;
  background: transparent; padding: 6px 16px; border-radius: 999px;
  font-size: 14px; transition: all .15s;
}
.capsule-btn.active { background: #fff; color: var(--primary-dark-2); font-weight: 600; }
.content { flex: 1; padding: 18px 22px 28px; }
</style>

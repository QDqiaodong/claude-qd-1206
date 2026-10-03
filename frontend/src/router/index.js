import { createRouter, createWebHashHistory } from 'vue-router'

// 注意：main.js 里没有 app.use(router)，路由实例由 App.vue 直接驱动，
// 所以这里全部用静态 import —— <component :is> 拿到的是组件对象本身。
import GoodsView from '../views/GoodsView.vue'
import BatchView from '../views/BatchView.vue'
import OrderView from '../views/OrderView.vue'
import StockInView from '../views/StockInView.vue'

const routes = [
  { path: '/', redirect: '/goods' },
  { path: '/goods', name: 'goods', component: GoodsView },
  { path: '/batch', name: 'batch', component: BatchView },
  { path: '/order', name: 'order', component: OrderView },
  { path: '/stockin', name: 'stockin', component: StockInView },
  { path: '/:pathMatch(.*)*', redirect: '/goods' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})

import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

const params = (o) => ({ params: o })

export default {
  // ---------------- 卤味商品 ----------------
  listGoods: (onSale) => call(http.get('/goods', onSale != null ? params({ onSale }) : {})),
  createGoods: (d) => call(http.post('/goods', d)),
  updateGoods: (id, d) => call(http.put(`/goods/${id}`, d)),
  offSaleGoods: (id, onSale) => call(http.post(`/goods/${id}/onsale`, null, params({ onSale }))),
  batchOffGoods: (ids) => call(http.post('/goods/batch-off', ids)),
  batchDiscountGoods: (ids, rate) => call(http.post('/goods/batch-discount', null, params({ ids, rate }))),

  // ---------------- 卤制批次 ----------------
  listBatches: (status) => call(http.get('/batches', status ? params({ status }) : {})),
  createBatch: (d) => call(http.post('/batches', d)),
  updateBatch: (id, d) => call(http.put(`/batches/${id}`, d)),
  transferBatch: (id, status) => call(http.post(`/batches/${id}/status`, null, params({ status }))),

  // ---------------- 销售订单 ----------------
  listOrders: (status) => call(http.get('/orders', status ? params({ status }) : {})),
  listOrderItems: (orderId) => call(http.get(`/orders/${orderId}/items`)),
  createOrder: (d) => call(http.post('/orders', d)),
  transferOrder: (id, status) => call(http.post(`/orders/${id}/status`, null, params({ status }))),
  payOrder: (id) => call(http.post(`/orders/${id}/pay`)),
  refundOrder: (id) => call(http.post(`/orders/${id}/refund`)),

  // ---------------- 原料进货 ----------------
  listStockIns: (material, acceptStatus) => {
    const p = {}
    if (material) p.material = material
    if (acceptStatus) p.acceptStatus = acceptStatus
    return call(http.get('/stock-ins', params(p)))
  },
  createStockIn: (d) => call(http.post('/stock-ins', d)),
  updateStockIn: (id, d) => call(http.put(`/stock-ins/${id}`, d)),
  acceptStockIn: (id, acceptStatus) => call(http.post(`/stock-ins/${id}/accept`, null, params({ acceptStatus }))),
  availableStock: () => call(http.get('/stock-ins/available'))
}

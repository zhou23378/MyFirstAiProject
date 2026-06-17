import request from './request'

export const categoryApi = {
  list: () => request.get('/api/product-category'),
  create: (data) => request.post('/api/product-category', data),
  update: (id, data) => request.put(`/api/product-category/${id}`, data),
  remove: (id) => request.delete(`/api/product-category/${id}`)
}

export const supplierApi = {
  list: () => request.get('/api/supplier'),
  create: (data) => request.post('/api/supplier', data),
  update: (id, data) => request.put(`/api/supplier/${id}`, data),
  remove: (id) => request.delete(`/api/supplier/${id}`)
}

export const productApi = {
  page: (params) => request.get('/api/product/page', { params }),
  getById: (id) => request.get(`/api/product/${id}`),
  create: (data) => request.post('/api/product', data),
  update: (id, data) => request.put(`/api/product/${id}`, data),
  remove: (id) => request.delete(`/api/product/${id}`),
  lowStock: (params) => request.get('/api/product/low-stock', { params })
}

export const stockRecordApi = {
  page: (params) => request.get('/api/stock-record/page', { params }),
  create: (data) => request.post('/api/stock-record', data)
}

export const purchaseOrderApi = {
  page: (params) => request.get('/api/purchase-order/page', { params }),
  getById: (id) => request.get(`/api/purchase-order/${id}`),
  create: (data) => request.post('/api/purchase-order', data),
  update: (id, data) => request.put(`/api/purchase-order/${id}`, data),
  submit: (id) => request.put(`/api/purchase-order/${id}/submit`),
  approve: (id) => request.put(`/api/purchase-order/${id}/approve`),
  receive: (id) => request.put(`/api/purchase-order/${id}/receive`),
  cancel: (id) => request.put(`/api/purchase-order/${id}/cancel`),
  remove: (id) => request.delete(`/api/purchase-order/${id}`)
}

export const returnOrderApi = {
  page: (params) => request.get('/api/return-order/page', { params }),
  getById: (id) => request.get(`/api/return-order/${id}`),
  create: (data) => request.post('/api/return-order', data),
  update: (id, data) => request.put(`/api/return-order/${id}`, data),
  submit: (id) => request.put(`/api/return-order/${id}/submit`),
  approve: (id) => request.put(`/api/return-order/${id}/approve`),
  reject: (id) => request.put(`/api/return-order/${id}/reject`),
  complete: (id) => request.put(`/api/return-order/${id}/complete`),
  cancel: (id) => request.put(`/api/return-order/${id}/cancel`),
  remove: (id) => request.delete(`/api/return-order/${id}`)
}

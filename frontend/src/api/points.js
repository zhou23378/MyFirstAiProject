import request from './request'

export const pointsProductApi = {
  page: (params) => request.get('/api/points/products', { params }),
  getById: (id) => request.get(`/api/points/products/${id}`),
  create: (data) => request.post('/api/points/products', data),
  update: (id, data) => request.put(`/api/points/products/${id}`, data),
  updateStatus: (id, status) => request.put(`/api/points/products/${id}/status`, null, { params: { status } })
}

export const pointsExchangeRecordApi = {
  page: (params) => request.get('/api/points/exchange-records', { params }),
  claim: (id) => request.put(`/api/points/exchange-records/${id}/claim`)
}

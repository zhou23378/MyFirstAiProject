import request from './request'

export const groupBuyTemplateApi = {
  page: (params) => request.get('/api/group-buy/templates', { params }),
  getById: (id) => request.get(`/api/group-buy/templates/${id}`),
  create: (data) => request.post('/api/group-buy/templates', data),
  update: (id, data) => request.put(`/api/group-buy/templates/${id}`, data),
  updateStatus: (id, status) => request.put(`/api/group-buy/templates/${id}/status`, null, { params: { status } })
}

export const groupBuyOrderApi = {
  page: (params) => request.get('/api/group-buy/orders', { params }),
  getDetail: (id) => request.get(`/api/group-buy/orders/${id}`),
  redeem: (participantId) => request.put(`/api/group-buy/participants/${participantId}/redeem`)
}

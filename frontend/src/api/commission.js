import request from './request'

export const commissionSettlementApi = {
  page: (params) => request.get('/api/commission/settlements', { params }),
  getById: (id) => request.get(`/api/commission/settlements/${id}`),
  create: (data) => request.post('/api/commission/settlements', data),
  confirm: (id) => request.put(`/api/commission/settlements/${id}/confirm`),
  pay: (id) => request.put(`/api/commission/settlements/${id}/pay`),
  remove: (id) => request.delete(`/api/commission/settlements/${id}`)
}

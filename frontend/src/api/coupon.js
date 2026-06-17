import request from './request'

export const templateApi = {
  list: () => request.get('/api/coupon-template'),
  getById: (id) => request.get(`/api/coupon-template/${id}`),
  create: (data) => request.post('/api/coupon-template', data),
  update: (id, data) => request.put(`/api/coupon-template/${id}`, data),
  remove: (id) => request.delete(`/api/coupon-template/${id}`)
}

export const couponApi = {
  page: (params) => request.get('/api/coupon/page', { params }),
  issue: (data) => request.post('/api/coupon/issue', data),
  verify: (data) => request.post('/api/coupon/verify', data)
}

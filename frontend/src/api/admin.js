import request from './request'

export const adminApi = {
  list: () => request.get('/api/admin'),
  create: (data) => request.post('/api/admin', data),
  update: (id, data) => request.put(`/api/admin/${id}`, data),
  remove: (id) => request.delete(`/api/admin/${id}`)
}

import request from './request'

export const memberLevelApi = {
    list: () => request.get('/api/member-level/list'),
    getById: (id) => request.get(`/api/member-level/${id}`),
    create: (data) => request.post('/api/member-level', data),
    update: (id, data) => request.put(`/api/member-level/${id}`, data),
    remove: (id) => request.delete(`/api/member-level/${id}`)
}

import request from './request'

export const dailyCloseApi = {
    today: () => request.get('/api/daily-close/today'),
    save: (data) => request.post('/api/daily-close/save', data),
    lock: (id, lockedBy = 'admin') => request.post(`/api/daily-close/${id}/lock?lockedBy=${lockedBy}`),
    history: (params) => request.get('/api/daily-close/history', { params }),
    getById: (id) => request.get(`/api/daily-close/${id}`),
}

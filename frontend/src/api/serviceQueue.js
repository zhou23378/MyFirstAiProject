import request from './request'

export const serviceQueueApi = {
    list: () => request.get('/api/service-queue/list'),
    enqueue: (data) => request.post('/api/service-queue/enqueue', data),
    assign: (data) => request.post('/api/service-queue/assign', data),
    skip: (id) => request.post(`/api/service-queue/${id}/skip`),
    cancel: (id) => request.post(`/api/service-queue/${id}/cancel`),
    todayStats: () => request.get('/api/service-queue/today-stats'),
}

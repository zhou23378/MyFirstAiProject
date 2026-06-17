import request from './request'

export const serviceTimerApi = {
    start: (data) => request.post('/api/service-timer/start', data),
    pause: (id) => request.post(`/api/service-timer/${id}/pause`),
    resume: (id) => request.post(`/api/service-timer/${id}/resume`),
    complete: (id, data) => request.post(`/api/service-timer/${id}/complete`, data),
    active: () => request.get('/api/service-timer/active'),
}

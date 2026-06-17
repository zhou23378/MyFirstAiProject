import request from './request'

export const technicianStatusApi = {
    list: () => request.get('/api/technician-status/list'),
    getByEmployeeId: (employeeId) => request.get(`/api/technician-status/${employeeId}`),
    changeStatus: (data) => request.post('/api/technician-status/change', data),
    performance: () => request.get('/api/technician-status/performance'),
}

import request from './request'

export const shiftTemplateApi = {
  list: () => request.get('/api/shift-template'),
  create: (data) => request.post('/api/shift-template', data),
  update: (id, data) => request.put(`/api/shift-template/${id}`, data),
  remove: (id) => request.delete(`/api/shift-template/${id}`)
}

export const scheduleApi = {
  week: (params) => request.get('/api/schedule/week', { params }),
  batchSet: (employeeId, data) => request.post(`/api/schedule/batch?employeeId=${employeeId}`, data),
  remove: (id) => request.delete(`/api/schedule/${id}`)
}

export const attendanceApi = {
  page: (params) => request.get('/api/attendance/page', { params }),
  clock: (data) => request.post('/api/attendance/clock', data)
}

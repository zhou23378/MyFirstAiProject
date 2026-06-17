import request from './request'

export function getEmployeeList(params = {}) {
    return request.get('/api/employee', { params })
}

export function getEmployee(id) {
    return request.get(`/api/employee/${id}`)
}

export function createEmployee(data) {
    return request.post('/api/employee', data)
}

export function updateEmployee(id, data) {
    return request.put(`/api/employee/${id}`, data)
}

export function deleteEmployee(id) {
    return request.delete(`/api/employee/${id}`)
}

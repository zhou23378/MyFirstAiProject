import request from './request'

export const appointmentApi = {
    /** 分页查询预约列表 */
    page: (params) => request.get('/api/appointment/page', { params }),
    /** 根据ID查询预约详情 */
    getById: (id) => request.get(`/api/appointment/${id}`),
    /** 新增预约 */
    create: (data) => request.post('/api/appointment', data),
    /** 修改预约 */
    update: (id, data) => request.put(`/api/appointment/${id}`, data),
    /** 更新预约状态 */
    updateStatus: (id, status) => request.put(`/api/appointment/${id}/status`, { status }),
    /** 删除预约 */
    remove: (id) => request.delete(`/api/appointment/${id}`),
    /** 预约到店 */
    arrive: (id) => request.put(`/api/appointment/${id}/arrive`),
    /** 预约转消费 */
    convertToOrder: (id, data) => request.post(`/api/appointment/${id}/convert-to-order`, data),
    /** 日历-日视图 */
    calendarDay: (params) => request.get('/api/appointment/calendar/day', { params }),
    /** 日历-周视图 */
    calendarWeek: (params) => request.get('/api/appointment/calendar/week', { params })
}

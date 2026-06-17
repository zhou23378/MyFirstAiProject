import request from './request'

export const consumptionApi = {
    /** 分页查询所有消费订单 */
    page: (params) => request.get('/api/consumption/page', { params }),
    /** 按会员ID分页查询消费记录 */
    pageByMember: (memberId, params) => request.get(`/api/consumption/member/${memberId}/page`, { params }),
    /** 查询订单明细 */
    getOrderItems: (orderId) => request.get(`/api/consumption/${orderId}/items`),
    /** 创建消费订单 */
    create: (data) => request.post('/api/consumption', data),
    /** 查询订单详情 */
    getById: (id) => request.get(`/api/consumption/${id}`),
    /** 退款 */
    refund: (id) => request.put(`/api/consumption/${id}/refund`),
    /** 挂单 */
    suspend: (data) => request.post('/api/consumption/suspend', data),
    /** 查询挂起订单 */
    listSuspended: () => request.get('/api/consumption/suspended'),
    /** 取单结算 */
    resume: (id, data) => request.put(`/api/consumption/${id}/resume`, data)
}

export const serviceCardApi = {
    page: (params) => request.get('/api/service-card/page', { params }),
    getById: (id) => request.get(`/api/service-card/${id}`),
    purchase: (data) => request.post('/api/service-card', data),
    deduct: (id) => request.put(`/api/service-card/${id}/deduct`),
    remove: (id) => request.delete(`/api/service-card/${id}`)
}




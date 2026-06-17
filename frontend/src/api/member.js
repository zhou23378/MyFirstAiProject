import request from './request'

export const memberApi = {
    page: (params) => request.get('/api/member/page', { params }),
    getById: (id) => request.get(`/api/member/${id}`),
    create: (data) => request.post('/api/member', data),
    update: (id, data) => request.put(`/api/member/${id}`, data),
    remove: (id) => request.delete(`/api/member/${id}`),
    /** 查询会员充值记录 */
    rechargeRecords: (id) => request.get(`/api/member/${id}/recharge-records`),
    /** 查询会员消费记录（分页） */
    orderPage: (memberId, params) => request.get(`/api/consumption/member/${memberId}/page`, { params }),
    /** 查询订单明细 */
    orderItems: (orderId) => request.get(`/api/consumption/${orderId}/items`),
    /** 会员充值 */
    recharge: (id, data) => request.post(`/api/member/${id}/recharge`, data),
    /** 流失预警，不传 days 则使用后端配置值 */
    churnRisk: (days) => request.get('/api/member/churn-risk', { params: days ? { days } : {} }),
    /** 导出会员（直接下载，不走解包拦截器） */
    exportExcel: async () => {
        const axios = (await import('axios')).default;
        const token = localStorage.getItem('token');
        const resp = await axios.get('/api/member/export', {
            responseType: 'blob',
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        return resp.data;
    },
    /** 导入会员 */
    importExcel: (formData) => request.post('/api/member/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
    /** 下载导入模板 */
    downloadTemplate: async () => {
        const axios = (await import('axios')).default;
        const token = localStorage.getItem('token');
        const resp = await axios.get('/api/member/template', {
            responseType: 'blob',
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        return resp.data;
    }

}



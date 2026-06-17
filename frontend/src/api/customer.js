import customerRequest from './customerRequest'

export const customerApi = {
    sendCode: (phone) => customerRequest.post('/api/customer/auth/send-code', { phone }),
    login: (data) => customerRequest.post('/api/customer/auth/login', data),
    getMe: () => customerRequest.get('/api/customer/auth/me'),
    logout: () => customerRequest.post('/api/customer/auth/logout'),

    // 个人中心
    getProfile: () => customerRequest.get('/api/customer/portal/profile'),
    updateProfile: (data) => customerRequest.put('/api/customer/portal/profile', data),
    getOrders: (page = 1, size = 10, startDate, endDate, sortBy) => customerRequest.get('/api/customer/portal/orders', { params: { page, size, startDate, endDate, sortBy } }),
    getOrderDetail: (id) => customerRequest.get(`/api/customer/portal/orders/${id}`),
    getCoupons: () => customerRequest.get('/api/customer/portal/coupons'),

    // 充值 & 余额
    recharge: (data) => customerRequest.post('/api/customer/portal/recharge', data),
    getBalanceRecords: () => customerRequest.get('/api/customer/portal/balance-records'),

    // 次卡
    getServiceCards: () => customerRequest.get('/api/customer/portal/service-cards'),
    purchaseServiceCard: (data) => customerRequest.post('/api/customer/portal/service-cards/purchase', data),

    // 服务进度
    getProgress: () => customerRequest.get('/api/customer/portal/progress'),

    // 积分商城
    getPointsProducts: () => customerRequest.get('/api/customer/portal/points/products'),
    getPointsProductDetail: (id) => customerRequest.get(`/api/customer/portal/points/products/${id}`),
    exchangePoints: (data) => customerRequest.post('/api/customer/portal/points/exchange', data),

    // 拼团活动
    getGroupBuyTemplates: () => customerRequest.get('/api/customer/portal/group-buy/templates'),
    getGroupBuyTemplateDetail: (id) => customerRequest.get(`/api/customer/portal/group-buy/templates/${id}`),
    getGroupBuyOrders: () => customerRequest.get('/api/customer/portal/group-buy/orders'),
    getGroupBuyOrderDetail: (id) => customerRequest.get(`/api/customer/portal/group-buy/orders/${id}`),
    createGroupBuy: (data) => customerRequest.post('/api/customer/portal/group-buy/orders', data),
    joinGroupBuy: (id) => customerRequest.post(`/api/customer/portal/group-buy/orders/${id}/join`),
    getMyGroupBuyOrders: (page = 1, size = 10) => customerRequest.get('/api/customer/portal/group-buy/my-orders', { params: { page, size } }),

    // 服务分类
    getServiceCategoryTree: () => customerRequest.get('/api/service-category/tree'),

    // 在线预约
    getAvailableSlots: (date, serviceItemId) => customerRequest.get('/api/customer/portal/slots', { params: { date, serviceItemId } }),
    createAppointment: (data) => customerRequest.post('/api/customer/portal/appointments', data),

    // 余额支付
    pay: (data) => customerRequest.post('/api/customer/portal/pay', data),

    // 服务评价
    rateOrder: (orderId, data) => customerRequest.post(`/api/customer/portal/orders/${orderId}/rate`, data),
    getOrderRate: (orderId) => customerRequest.get(`/api/customer/portal/orders/${orderId}/rate`)
}

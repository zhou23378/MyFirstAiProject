import request from './request'

export function getDailyReport(start, end) {
    return request.get('/api/report/daily', { params: { start, end } })
}

export function getMonthlyReport(year) {
    return request.get('/api/report/monthly', { params: { year } })
}

export function getServiceRank() {
    return request.get('/api/report/service-rank')
}

export function getCashierDaily(date) {
    return request.get('/api/report/cashier-daily', { params: { date } })
}

export function getEmployeePerformance(start, end) {
    return request.get('/api/report/employee-performance', { params: { start, end } })
}

export function getMemberSpending(start, end, limit) {
    return request.get('/api/report/member-spending', { params: { start, end, limit } })
}

export function getMemberTrend() {
    return request.get('/api/report/member-trend')
}

export function getCouponUsage() {
    return request.get('/api/report/coupon-usage')
}

import request from './request'

export function getDashboardStats() {
    return request.get('/api/dashboard/stats')
}

export function getRecentOrders() {
    return request.get('/api/dashboard/recent-orders')
}

export function getQuickActions() {
    return request.get('/api/dashboard/quick-actions')
}

export function saveQuickActions(data) {
    return request.put('/api/dashboard/quick-actions', data)
}

export function getStatCards() {
    return request.get('/api/dashboard/stat-cards')
}

export function saveStatCards(data) {
    return request.put('/api/dashboard/stat-cards', data)
}

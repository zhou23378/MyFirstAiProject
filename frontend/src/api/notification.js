import request from './request'

export const notificationApi = {
    list: (page = 1, pageSize = 10) => request.get('/api/notifications', { params: { page, pageSize } }),
    unreadCount: () => request.get('/api/notifications/unread-count'),
    markRead: (id) => request.put(`/api/notifications/${id}/read`),
    markAllRead: () => request.put('/api/notifications/read-all'),
}

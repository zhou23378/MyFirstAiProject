import request from './request'

export const aiApi = {
    search: (q, topK = 5) => request.get('/api/ai/search', { params: { q, topK } }),
    health: () => request.get('/api/ai/health'),
}

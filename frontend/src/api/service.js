import request from './request'

export const serviceApi = {
    // 服务分类
    getCategories: () => request.get('/api/service-category'),
    createCategory: (data) => request.post('/api/service-category', data),
    updateCategory: (id, data) => request.put(`/api/service-category/${id}`, data),
    deleteCategory: (id) => request.delete(`/api/service-category/${id}`),

    // 服务项目
    getItems: () => request.get('/api/service-item'),
    createItem: (data) => request.post('/api/service-item', data),
    updateItem: (id, data) => request.put(`/api/service-item/${id}`, data),
    deleteItem: (id) => request.delete(`/api/service-item/${id}`)
}

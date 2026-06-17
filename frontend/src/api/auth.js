import request from './request'

export const authApi = {
    login: (data) => request.post('/api/auth/login', data)
}

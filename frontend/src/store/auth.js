import { defineStore } from 'pinia'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        username: localStorage.getItem('username') || '',
        role: (localStorage.getItem('role') || '').toLowerCase()
    }),

    actions: {
        async login(loginForm) {
            const res = await authApi.login(loginForm)
            this.token = res.token
            this.username = res.username
            this.role = (res.role || 'admin').toLowerCase()
            localStorage.setItem('token', res.token)
            localStorage.setItem('username', res.username)
            localStorage.setItem('role', (res.role || 'admin').toLowerCase())
            ElMessage.success('登录成功')
        },

        logout() {
            this.token = ''
            this.username = ''
            this.role = ''
            localStorage.removeItem('token')
            localStorage.removeItem('username')
            localStorage.removeItem('role')
        }
    }
})

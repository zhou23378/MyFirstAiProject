import { defineStore } from 'pinia'
import { customerApi } from '../api/customer'
import { ElMessage } from 'element-plus'

export const useCustomerAuthStore = defineStore('customerAuth', {
    state: () => ({
        token: localStorage.getItem('customer_token') || '',
        memberId: localStorage.getItem('customer_memberId') || '',
        name: localStorage.getItem('customer_name') || '',
        phone: localStorage.getItem('customer_phone') || ''
    }),

    getters: {
        isLoggedIn: (state) => !!state.token
    },

    actions: {
        async sendCode(phone) {
            await customerApi.sendCode(phone)
            ElMessage.success('验证码已发送')
        },

        async login(loginForm) {
            const res = await customerApi.login(loginForm)
            this.token = res.token
            this.memberId = res.memberId
            this.name = res.name
            this.phone = res.phone
            localStorage.setItem('customer_token', res.token)
            localStorage.setItem('customer_memberId', res.memberId)
            localStorage.setItem('customer_name', res.name)
            localStorage.setItem('customer_phone', res.phone)
            ElMessage.success('登录成功')
        },

        logout() {
            this.token = ''
            this.memberId = ''
            this.name = ''
            this.phone = ''
            localStorage.removeItem('customer_token')
            localStorage.removeItem('customer_memberId')
            localStorage.removeItem('customer_name')
            localStorage.removeItem('customer_phone')
        }
    }
})

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    timeout: 10000
})

// 请求拦截器 - 自动携带 Token
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 响应拦截器 - 统一处理
request.interceptors.response.use(
    res => {
        const { code, msg, data } = res.data
        if (code === 200) {
            return data
        }
        const traceId = res.headers?.['x-trace-id'] || ''
        const suffix = traceId ? ` [${traceId}]` : ''
        ElMessage.error(`${msg || '请求失败'}${suffix}`)
        if (code === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('username')
            localStorage.removeItem('role')
            router.push('/login')
        }
        return Promise.reject(new Error(msg))
    },
    err => {
        const traceId = err.response?.headers?.['x-trace-id'] || ''
        const suffix = traceId ? ` [${traceId}]` : ''
        if (err.code === 'ECONNABORTED') {
            ElMessage.error(`请求超时${suffix}`)
        } else if (!err.response) {
            ElMessage.error(`网络错误，请检查后端服务是否启动${suffix}`)
        } else {
            ElMessage.error(`服务器错误${suffix}`)
        }
        return Promise.reject(err)
    }
)

export default request

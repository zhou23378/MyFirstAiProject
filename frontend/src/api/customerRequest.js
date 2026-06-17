import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const customerRequest = axios.create({
    timeout: 10000
})

customerRequest.interceptors.request.use(config => {
    const token = localStorage.getItem('customer_token')
    if (token) {
        config.headers['X-Customer-Token'] = token
    }
    return config
})

customerRequest.interceptors.response.use(
    res => {
        const { code, msg, data } = res.data
        if (code === 200) {
            return data
        }
        const traceId = res.headers?.['x-trace-id'] || ''
        const suffix = traceId ? ` [${traceId}]` : ''
        ElMessage.error(`${msg || '请求失败'}${suffix}`)
        if (code === 401) {
            localStorage.removeItem('customer_token')
            localStorage.removeItem('customer_name')
            localStorage.removeItem('customer_phone')
            router.push('/h5/login')
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

export default customerRequest

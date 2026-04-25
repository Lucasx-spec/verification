import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('verification-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.success === false) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(payload)
    }
    return payload
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    if (error.response?.status === 401) {
      localStorage.removeItem('verification-token')
      localStorage.removeItem('verification-user')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default http

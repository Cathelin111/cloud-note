import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('cn_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (resp) => {
    // 后端统一 {status,msg,data}; HTTP 401/403 时 body 也是统一结构
    const body = resp.data || {}
    if (body.status === 401) {
      localStorage.removeItem('cn_token')
      localStorage.removeItem('cn_user')
      if (!location.pathname.startsWith('/login')) {
        location.href = '/login'
      }
      return Promise.reject(new Error(body.msg || '未登录'))
    }
    return body
  },
  (err) => {
    const body = err.response && err.response.data
    const msg = (body && body.msg) || err.message || '请求失败'
    if (err.response && err.response.status === 403) {
      ElMessage.error(msg)
      if (err.response.config && String(err.response.config.url).startsWith('/admin')) {
        location.href = '/'
      }
      return Promise.reject(new Error(msg))
    }
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
)

export default request

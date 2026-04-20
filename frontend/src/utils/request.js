import axios from 'axios'

/**
 * Axios 实例配置
 * 封装后端 API 基础路径与 JWT 认证拦截器
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
})

// 请求拦截器：自动附加 JWT 令牌
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 从 Axios 错误对象中提取用户友好的错误消息
 * @param {import('axios').AxiosError} error
 * @returns {string}
 */
export function getErrorMessage(error) {
  if (error.response) {
    const status = error.response.status
    const data = error.response.data
    if (data?.message) {
      return data.message
    }
    if (status === 404) {
      return '请求的资源不存在，请检查服务是否启动'
    }
    if (status === 500) {
      return '服务器内部错误，请稍后重试'
    }
    return `请求失败 (${status})`
  }
  if (error.request) {
    return '无法连接到服务器，请检查网络或后端服务是否启动'
  }
  return error.message || '请求失败'
}

// 响应拦截器：统一处理 401 未授权
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request

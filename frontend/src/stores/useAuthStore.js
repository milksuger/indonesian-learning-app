import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request.js'

/**
 * 认证状态管理
 * 封装登录、注册、登出与令牌持久化
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(email, password) {
    const { data } = await request.post('/auth/login', { email, password })
    token.value = data.token
    localStorage.setItem('token', data.token)
  }

  async function register(email, password, code) {
    await request.post('/auth/register', { email, password, verificationCode: code })
  }

  async function sendCode(email) {
    await request.post('/auth/register/send-code', { email })
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, isLoggedIn, login, register, sendCode, logout }
})

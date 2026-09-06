import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cn_token') || '',
    user: JSON.parse(localStorage.getItem('cn_user') || 'null')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isAdmin: (s) => !!(s.user && s.user.cn_user_role === 'admin')
  },
  actions: {
    async login(username, password) {
      const res = await authApi.login({ username, password })
      if (res.status === 0) {
        this.token = res.data.token
        this.user = res.data.user
        localStorage.setItem('cn_token', this.token)
        localStorage.setItem('cn_user', JSON.stringify(this.user))
        return res.data.user
      }
      throw new Error(res.msg || '登录失败')
    },
    async register(data) {
      const res = await authApi.register(data)
      if (res.status !== 0) throw new Error(res.msg || '注册失败')
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('cn_token')
      localStorage.removeItem('cn_user')
    }
  }
})

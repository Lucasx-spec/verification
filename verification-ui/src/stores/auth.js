import { defineStore } from 'pinia'
import { authApi } from '../api'

const TOKEN_KEY = 'verification-token'
const USER_KEY = 'verification-user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.roleCode === 'ADMIN',
  },
  actions: {
    init() {
      if (!this.token) {
        this.token = localStorage.getItem(TOKEN_KEY) || ''
      }
      if (!this.user) {
        const raw = localStorage.getItem(USER_KEY)
        this.user = raw ? JSON.parse(raw) : null
      }
    },
    setSession(token, user = null) {
      this.token = token || ''
      this.user = user
      if (token) {
        localStorage.setItem(TOKEN_KEY, token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
      if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    async login(payload) {
      const response = await authApi.login(payload)
      this.setSession(response.data.token)
      await this.fetchCurrentUser()
      return response.data
    },
    async register(payload) {
      const response = await authApi.register(payload)
      this.setSession(response.data.token)
      await this.fetchCurrentUser()
      return response.data
    },
    async fetchCurrentUser() {
      const response = await authApi.me()
      this.user = response.data
      localStorage.setItem(USER_KEY, JSON.stringify(response.data))
      return response.data
    },
    async updateKeyPair(payload) {
      const response = await authApi.updateKeyPair(payload)
      this.user = response.data
      localStorage.setItem(USER_KEY, JSON.stringify(response.data))
      return response.data
    },
    logout() {
      this.setSession('', null)
    },
  },
})

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string>(localStorage.getItem('jt_token') || '')

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    token.value = res.token
    userInfo.value = res.user
    localStorage.setItem('jt_token', res.token)
  }

  async function fetchUserInfo() {
    userInfo.value = await authApi.getUserInfo()
  }

  async function logout() {
    try { await authApi.logout() } catch {}
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('jt_token')
  }

  return { userInfo, token, login, fetchUserInfo, logout }
})

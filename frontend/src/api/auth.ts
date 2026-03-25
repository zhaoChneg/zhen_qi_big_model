import request from '@/utils/request'
import type { UserInfo } from '@/types'

export const authApi = {
  login(params: { username: string; password: string }): Promise<{ token: string; user: UserInfo }> {
    return request.post('/auth/login', params)
  },

  logout(): Promise<void> {
    return request.post('/auth/logout')
  },

  getUserInfo(): Promise<UserInfo> {
    return request.get('/auth/me')
  },

  changePassword(params: { oldPassword: string; newPassword: string }): Promise<void> {
    return request.put('/auth/password', params)
  },
}

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat/ChatLayout.vue'),
        meta: { title: '得心', module: 'chat' },
      },
      {
        path: 'write',
        name: 'Write',
        component: () => import('@/views/Write/WriteLayout.vue'),
        meta: { title: '应手', module: 'write' },
      },
      {
        path: 'data-query',
        name: 'DataQuery',
        component: () => import('@/views/DataQuery/DataQueryLayout.vue'),
        meta: { title: '问数', module: 'data' },
      },
      {
        path: 'agent',
        name: 'Agent',
        component: () => import('@/views/Agent/AgentLayout.vue'),
        meta: { title: '知事', module: 'agent' },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/LoginPage.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/chat',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 交投科技政企大模型` : '交投科技政企大模型'
  const token = localStorage.getItem('jt_token')
  if (!token && to.name !== 'Login') {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router

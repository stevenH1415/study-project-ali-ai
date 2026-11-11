import { createRouter, createWebHashHistory } from 'vue-router'
import BasicLayout from '../layout/BasicLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'chat',
        name: 'chat',
        component: () => import('../views/Chat.vue'),
        meta: { title: 'AI 聊天' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || ''} · SpringAli 学习平台`
  const token = localStorage.getItem('springali_token')
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return { path: '/home' }
  }
  return true
})

export default router

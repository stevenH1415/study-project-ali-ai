import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 60000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('springali_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('springali_token')
      localStorage.removeItem('springali_user')
      if (!location.hash.startsWith('#/login')) {
        location.hash = '#/login'
      }
    }
    return Promise.reject(error)
  }
)

export function login(payload) {
  return http.post('/auth/login', payload)
}

export function getUserInfo() {
  return http.get('/auth/info')
}

export function logout() {
  return http.post('/auth/logout')
}

export function chat(payload) {
  return http.post('/chat', payload)
}

export function clearChat(conversationId) {
  return http.delete(`/chat/${encodeURIComponent(conversationId)}`)
}

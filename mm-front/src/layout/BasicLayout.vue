<template>
  <a-layout class="app-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible :width="220" class="app-sider">
      <div class="logo">
        <div class="logo-badge">SA</div>
        <span v-if="!collapsed" class="logo-text">SpringAli</span>
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="[activeKey]" @click="onMenuClick">
        <a-menu-item key="/home">
          <template #icon><HomeOutlined /></template>
          首页
        </a-menu-item>
        <a-menu-item key="/chat">
          <template #icon><RobotOutlined /></template>
          AI 聊天
        </a-menu-item>
      </a-menu>
      <div v-if="!collapsed" class="sider-footer">
        Spring Cloud Alibaba<br />+ Spring AI
      </div>
    </a-layout-sider>

    <a-layout class="app-main">
      <a-layout-header class="app-header">
        <div class="header-left">
          <a-breadcrumb>
            <a-breadcrumb-item>SpringAli</a-breadcrumb-item>
            <a-breadcrumb-item>{{ route.meta.title }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-tag color="blue">学习项目</a-tag>
          <a-dropdown>
            <a class="user-entry" @click.prevent>
              <a-avatar style="background-color: #1677ff">{{ avatarText }}</a-avatar>
              <span class="username">{{ user?.nickname || user?.username || '未登录' }}</span>
            </a>
            <template #overlay>
              <a-menu @click="onUserMenu">
                <a-menu-item key="logout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="app-content">
        <router-view />
      </a-layout-content>

      <a-layout-footer class="app-footer">
        SpringAli · Spring Cloud Alibaba + Spring AI 学习项目
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeOutlined, LogoutOutlined, RobotOutlined } from '@ant-design/icons-vue'
import { getUserInfo, logout } from '../api'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const user = ref(null)

try {
  user.value = JSON.parse(localStorage.getItem('springali_user') || 'null')
} catch {
  user.value = null
}

const activeKey = computed(() => (route.path.startsWith('/chat') ? '/chat' : '/home'))
const avatarText = computed(() => {
  const name = user.value?.nickname || user.value?.username || 'A'
  return name.slice(0, 1).toUpperCase()
})

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 0) {
      user.value = res.data
      localStorage.setItem('springali_user', JSON.stringify(res.data))
    }
  } catch {
    // 未登录或认证服务未启动时由路由守卫 / 拦截器处理
  }
})

function onMenuClick({ key }) {
  router.push(key)
}

async function onUserMenu({ key }) {
  if (key !== 'logout') return
  try {
    await logout()
  } catch {
    // 忽略退出接口异常
  }
  localStorage.removeItem('springali_token')
  localStorage.removeItem('springali_user')
  sessionStorage.removeItem('springali_conversation')
  router.replace('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-sider {
  position: sticky;
  top: 0;
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  overflow: hidden;
}

.logo-badge {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1677ff, #36cfc9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
  white-space: nowrap;
}

.sider-footer {
  position: absolute;
  bottom: 58px;
  width: 100%;
  text-align: center;
  font-size: 11px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.35);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.75);
}

.app-content {
  margin: 20px 24px;
}

.app-footer {
  padding: 12px 24px;
  text-align: center;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>

<template>
  <div class="login-page">
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>
    <div class="circle circle-3"></div>

    <div class="login-wrapper">
      <div class="login-brand">
        <div class="brand-badge">SA</div>
        <h1>SpringAli</h1>
        <p class="brand-sub">Spring Cloud Alibaba + Spring AI 学习平台</p>
        <ul class="brand-list">
          <li>Spring Cloud Alibaba 微服务骨架（网关 / 认证 / 聊天）</li>
          <li>Spring AI 聊天机器人，支持 SSE 流式输出</li>
          <li>FEBS 风格后台前端，登录 → 首页完整流程</li>
        </ul>
      </div>

      <a-card class="login-card" :bordered="false">
        <h2>欢迎登录</h2>
        <p class="login-sub">演示账号：admin / 123456</p>

        <a-form layout="vertical" :model="form" @finish="handleSubmit">
          <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
            <a-input v-model:value="form.username" size="large" placeholder="admin" autocomplete="username">
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>
          <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password
              v-model:value="form.password"
              size="large"
              placeholder="123456"
              autocomplete="current-password"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>
          <a-button type="primary" html-type="submit" size="large" block :loading="loading">
            登 录
          </a-button>
        </a-form>

        <div class="login-tip">前端：Vue3 + Vite + Ant Design Vue（FEBS 风格）</div>
      </a-card>
    </div>

    <div class="login-footer">SpringAli · 学习项目 · 2026</div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { login } from '../api'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleSubmit() {
  loading.value = true
  try {
    const res = await login({ ...form })
    if (res.code !== 0) {
      throw new Error(res.msg || '登录失败')
    }
    localStorage.setItem('springali_token', res.data.token)
    localStorage.setItem('springali_user', JSON.stringify(res.data.user))
    message.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(redirect)
  } catch (error) {
    message.error(error.response?.data?.msg || error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #071a3d 0%, #0b3d91 45%, #1677ff 100%);
  padding: 24px;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.35;
}

.circle-1 {
  width: 420px;
  height: 420px;
  top: -160px;
  right: -80px;
  background: radial-gradient(circle, rgba(54, 207, 201, 0.7), transparent 70%);
}

.circle-2 {
  width: 520px;
  height: 520px;
  bottom: -260px;
  left: -140px;
  background: radial-gradient(circle, rgba(105, 177, 255, 0.6), transparent 70%);
}

.circle-3 {
  width: 220px;
  height: 220px;
  top: 42%;
  left: 12%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18), transparent 70%);
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 940px;
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 64px;
  align-items: center;
}

.login-brand {
  color: #fff;
}

.brand-badge {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #36cfc9, #1677ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
}

.login-brand h1 {
  margin: 24px 0 8px;
  font-size: 42px;
  letter-spacing: 1px;
  font-weight: 700;
}

.brand-sub {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.75);
}

.brand-list {
  margin: 32px 0 0;
  padding-left: 20px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 2;
  font-size: 14px;
}

.login-card {
  border-radius: 12px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.28);
  padding: 8px 4px;
}

.login-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
}

.login-sub {
  margin: 0 0 24px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}

.login-tip {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.35);
}

.login-footer {
  position: absolute;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

@media (max-width: 820px) {
  .login-wrapper {
    grid-template-columns: 1fr;
    max-width: 420px;
    gap: 28px;
  }

  .login-brand {
    text-align: center;
  }

  .brand-badge {
    margin: 0 auto;
  }

  .brand-list {
    display: none;
  }
}
</style>

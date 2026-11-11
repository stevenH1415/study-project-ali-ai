<template>
  <div class="home">
    <div class="welcome">
      <div class="welcome-text">
        <h1>你好，{{ nickname }} 👋</h1>
        <p>欢迎来到 SpringAli 学习平台 —— 从 Spring Cloud Alibaba 微服务到 Spring AI 聊天机器人。</p>
      </div>
      <a-button type="primary" size="large" class="welcome-btn" @click="router.push('/chat')">
        <RobotOutlined />
        去和 AI 聊聊
      </a-button>
    </div>

    <a-row :gutter="[16, 16]" class="stat-row">
      <a-col v-for="card in stats" :key="card.label" :xs="24" :sm="12" :lg="6">
        <a-card :bordered="false" class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <component :is="card.icon" />
          </div>
          <div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="14">
        <a-card :bordered="false" title="学习路线" class="panel-card">
          <a-timeline>
            <a-timeline-item color="green">
              <b>已完成</b> · 微服务骨架（Gateway + Auth + Chatbot）
            </a-timeline-item>
            <a-timeline-item color="green">
              <b>已完成</b> · Spring AI 聊天机器人（普通对话 + SSE 流式输出）
            </a-timeline-item>
            <a-timeline-item color="green">
              <b>已完成</b> · FEBS 风格前端：启动 → 登录 → 首页
            </a-timeline-item>
            <a-timeline-item color="green">
              <b>已完成</b> · PostgreSQL 接入（springali 库 + 登录用户 BCrypt）
            </a-timeline-item>
            <a-timeline-item color="gray">
              <b>计划中</b> · Nacos 注册中心 / 配置中心
            </a-timeline-item>
            <a-timeline-item color="gray">
              <b>计划中</b> · RocketMQ 消息队列学习
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card :bordered="false" title="技术栈" class="panel-card">
          <div class="tech-group">
            <div class="tech-title">后端</div>
            <div class="tech-tags">
              <a-tag color="blue">Spring Boot 3.5.16</a-tag>
              <a-tag color="blue">Spring Cloud 2025.0.3</a-tag>
              <a-tag color="blue">Spring Cloud Alibaba 2025.0.0.0</a-tag>
              <a-tag color="purple">Spring AI 1.1.8</a-tag>
              <a-tag color="geekblue">JDK 17</a-tag>
            </div>
          </div>
          <div class="tech-group">
            <div class="tech-title">前端</div>
            <div class="tech-tags">
              <a-tag color="green">Vue 3</a-tag>
              <a-tag color="green">Vite 6</a-tag>
              <a-tag color="green">Ant Design Vue 4</a-tag>
              <a-tag color="green">FEBS 风格布局</a-tag>
            </div>
          </div>
          <div class="tech-group">
            <div class="tech-title">基础设施</div>
            <div class="tech-tags">
              <a-tag color="green">PostgreSQL</a-tag>
              <a-tag color="orange">Nacos（可选）</a-tag>
              <a-tag color="orange">RocketMQ（待学习）</a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="panel-card service-card" title="服务与端口">
      <a-row :gutter="[16, 16]">
        <a-col v-for="svc in services" :key="svc.name" :xs="24" :sm="12" :lg="6">
          <div class="service-item">
            <div class="service-name">{{ svc.name }}</div>
            <div class="service-port">{{ svc.port }}</div>
            <div class="service-desc">{{ svc.desc }}</div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  ClusterOutlined,
  DatabaseOutlined,
  MessageOutlined,
  RobotOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

let currentUser = {}
try {
  currentUser = JSON.parse(localStorage.getItem('springali_user') || '{}')
} catch {
  currentUser = {}
}

const nickname = currentUser.nickname || currentUser.username || '同学'

const stats = [
  {
    label: '微服务模块',
    value: '3 个',
    icon: ClusterOutlined,
    bg: 'linear-gradient(135deg, #1677ff, #69b1ff)'
  },
  {
    label: 'AI 框架',
    value: 'Spring AI 1.1.8',
    icon: RobotOutlined,
    bg: 'linear-gradient(135deg, #722ed1, #b37feb)'
  },
  {
    label: '消息队列',
    value: '待接入',
    icon: MessageOutlined,
    bg: 'linear-gradient(135deg, #fa8c16, #ffc069)'
  },
  {
    label: '数据库',
    value: 'PostgreSQL',
    icon: DatabaseOutlined,
    bg: 'linear-gradient(135deg, #13c2c2, #5cdbd3)'
  }
]

const services = [
  { name: 'mm-gateway', port: '18081', desc: '统一入口 / 路由 / CORS' },
  { name: 'mm-aichatboot', port: '18082', desc: 'Spring AI 聊天 + SSE' },
  { name: 'mm-backend', port: '18083', desc: '业务后端 / 登录认证（PG）' },
  { name: 'mm-front', port: '18084', desc: 'FEBS 风格前端' }
]
</script>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  border-radius: 12px;
  background: linear-gradient(120deg, #0b3d91 0%, #1677ff 60%, #36cfc9 120%);
  color: #fff;
  box-shadow: 0 12px 32px rgba(22, 119, 255, 0.28);
}

.welcome h1 {
  margin: 0 0 8px;
  font-size: 26px;
}

.welcome p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
}

.welcome-btn {
  flex: none;
  background: #fff;
  color: #1677ff;
  border-color: #fff;
  font-weight: 600;
}

.welcome-btn:hover {
  background: #e6f4ff;
  color: #1677ff;
}

.stat-row {
  margin: 16px 0;
}

.stat-card {
  border-radius: 10px;
  display: flex;
  align-items: center;
}

.stat-card :deep(.ant-card-body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 20px;
}

.stat-icon {
  flex: none;
  width: 46px;
  height: 46px;
  border-radius: 10px;
  color: #fff;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-label {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
}

.stat-value {
  margin-top: 2px;
  font-size: 18px;
  font-weight: 600;
}

.panel-card {
  border-radius: 10px;
  height: 100%;
}

.tech-group + .tech-group {
  margin-top: 16px;
}

.tech-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.service-card {
  margin-top: 16px;
}

.service-item {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f7f9fc;
  border: 1px solid #eef2f8;
}

.service-name {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}

.service-port {
  margin: 4px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1677ff;
}

.service-desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>

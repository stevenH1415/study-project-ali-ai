<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="chat-title">
        <div class="chat-icon"><RobotOutlined /></div>
        <div>
          <div class="chat-name">AI 学习助手</div>
          <div class="chat-sub">
            Spring AI
            <span class="dot">·</span>
            {{ conversationId ? `会话 ${shortId}` : '新会话' }}
          </div>
        </div>
      </div>
      <div class="chat-actions">
        <span class="stream-label">流式输出</span>
        <a-switch v-model:checked="streamingEnabled" :disabled="sending" />
        <a-button size="small" :disabled="sending" @click="handleClear">
          <DeleteOutlined />
          清空会话
        </a-button>
      </div>
    </div>

    <div ref="listRef" class="chat-body">
      <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
        <a-avatar :style="{ backgroundColor: msg.role === 'user' ? '#1677ff' : '#52c41a' }">
          <template #icon>
            <UserOutlined v-if="msg.role === 'user'" />
            <RobotOutlined v-else />
          </template>
        </a-avatar>
        <div class="bubble">
          <span class="bubble-text">{{ msg.content }}</span>
          <span
            v-if="sending && msg.role === 'assistant' && index === messages.length - 1"
            class="cursor"
          >▍</span>
        </div>
      </div>

      <div v-if="messages.length === 1 && !sending" class="quick-prompts">
        <a-tag v-for="prompt in quickPrompts" :key="prompt" class="quick-tag" @click="usePrompt(prompt)">
          {{ prompt }}
        </a-tag>
      </div>
    </div>

    <div class="chat-composer">
      <a-textarea
        v-model:value="input"
        :auto-size="{ minRows: 2, maxRows: 5 }"
        :disabled="sending"
        placeholder="输入问题，Enter 发送，Shift + Enter 换行"
        @keydown="onKeydown"
      />
      <a-button type="primary" :loading="sending" :disabled="!input.trim()" @click="handleSend">
        <template #icon><SendOutlined /></template>
        发送
      </a-button>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  RobotOutlined,
  SendOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { chat, clearChat } from '../api'

const messages = ref([
  {
    role: 'assistant',
    content: '你好，我是 SpringAli 的 AI 学习助手。可以问我 Spring Cloud Alibaba、Spring AI，或者 MQ 相关的问题。'
  }
])

const input = ref('')
const sending = ref(false)
const streamingEnabled = ref(true)
const listRef = ref(null)
const conversationId = ref(sessionStorage.getItem('springali_conversation') || '')

const quickPrompts = [
  'Spring Cloud Alibaba 包含哪些组件？',
  'Spring AI 怎么接入 DeepSeek？',
  'RocketMQ 和 Kafka 怎么选？'
]

const shortId = computed(() => conversationId.value.slice(0, 8))

watch(conversationId, (value) => {
  if (value) {
    sessionStorage.setItem('springali_conversation', value)
  } else {
    sessionStorage.removeItem('springali_conversation')
  }
})

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

function usePrompt(prompt) {
  input.value = prompt
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
    event.preventDefault()
    handleSend()
  }
}

async function handleSend() {
  const text = input.value.trim()
  if (!text || sending.value) return

  input.value = ''
  messages.value.push({ role: 'user', content: text })
  messages.value.push({ role: 'assistant', content: '' })
  sending.value = true
  scrollToBottom()

  try {
    if (streamingEnabled.value) {
      await sendStream(text)
    } else {
      const res = await chat({
        conversationId: conversationId.value || null,
        message: text
      })
      conversationId.value = res.conversationId
      messages.value[messages.value.length - 1].content = res.reply
      sending.value = false
      scrollToBottom()
    }
  } catch (error) {
    messages.value[messages.value.length - 1].content =
      error.response?.data?.error || error.message || '请求失败'
    sending.value = false
    scrollToBottom()
  }
}

function sendStream(text) {
  return new Promise((resolve) => {
    const assistant = messages.value[messages.value.length - 1]
    const cid = conversationId.value
    const url = `/api/chat/stream?message=${encodeURIComponent(text)}${
      cid ? `&conversationId=${encodeURIComponent(cid)}` : ''
    }`
    const source = new EventSource(url)
    let finished = false

    const finish = () => {
      if (finished) return
      finished = true
      source.close()
      sending.value = false
      scrollToBottom()
      resolve()
    }

    source.addEventListener('conversation', (event) => {
      conversationId.value = event.data
    })

    source.onmessage = (event) => {
      if (event.data === '[DONE]') {
        finish()
        return
      }
      if (event.data.startsWith('[ERROR]')) {
        assistant.content += `${assistant.content ? '\n' : ''}${event.data}`
        finish()
        return
      }
      assistant.content += event.data
      scrollToBottom()
    }

    source.onerror = () => {
      if (finished) return
      if (!assistant.content) {
        assistant.content = '连接失败：请确认网关(18081)与聊天服务(18082)已启动，并已配置 AI_API_KEY。'
      }
      finish()
    }
  })
}

async function handleClear() {
  const cid = conversationId.value
  if (cid) {
    try {
      await clearChat(cid)
    } catch {
      // 后端会话不存在时忽略
    }
  }
  conversationId.value = ''
  messages.value = [
    { role: 'assistant', content: '会话已清空，我们重新开始吧。' }
  ]
  message.success('已清空当前会话')
}
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 168px);
  min-height: 480px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.chat-header {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1677ff, #36cfc9);
  color: #fff;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
}

.chat-sub {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.dot {
  margin: 0 4px;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stream-label {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.55);
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafcff;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.bubble {
  max-width: 72%;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #eef2f8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
  font-size: 14px;
  line-height: 1.7;
}

.msg-row.user .bubble {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
}

.bubble-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  color: #1677ff;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.quick-prompts {
  padding: 4px 44px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 14px;
}

.chat-composer {
  flex: none;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.chat-composer .ant-btn {
  height: 44px;
  padding: 0 22px;
}
</style>

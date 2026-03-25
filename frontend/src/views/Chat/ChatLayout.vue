<template>
  <div class="chat-layout">
    <WelcomeBanner v-if="showWelcome" @select="handleSuggest" />
    <ChatWindow v-else />
    <MessageInput @send="handleSend" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import WelcomeBanner from './components/WelcomeBanner.vue'
import ChatWindow from './components/ChatWindow.vue'
import MessageInput from './components/MessageInput.vue'
import { useChatStore } from '@/stores/chatStore'
import type { FileAttachment } from '@/types'

const chatStore = useChatStore()

const showWelcome = computed(() =>
  chatStore.currentMessages.length === 0 && !chatStore.isStreaming
)

async function handleSend(payload: { content: string; attachments?: FileAttachment[] }) {
  await chatStore.sendMessage(payload.content, payload.attachments)
}

function handleSuggest(text: string) {
  chatStore.sendMessage(text)
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #F8FAFC;
}
</style>

<template>
  <aside class="sidebar sidebar--chat">
    <!-- 新建对话 -->
    <div class="sidebar-header">
      <el-button type="primary" :icon="Plus" class="new-chat-btn" @click="createSession">
        新建对话
      </el-button>
    </div>

    <!-- 历史记录 -->
    <div class="sidebar-section-label">对话记录</div>

    <!-- 搜索 -->
    <div class="sidebar-search">
      <el-input
        v-model="searchText"
        placeholder="搜索历史记录"
        :prefix-icon="Search"
        clearable
        size="small"
      />
    </div>

    <!-- 对话列表 -->
    <div class="session-list" ref="listRef">
      <div
        v-for="session in filteredSessions"
        :key="session.id"
        class="session-item"
        :class="{ 'is-active': currentSessionId === session.id }"
        @click="selectSession(session.id)"
      >
        <el-icon class="session-icon"><ChatDotRound /></el-icon>
        <span class="session-title ellipsis">{{ session.title }}</span>
        <el-dropdown
          class="session-actions"
          @command="(cmd: string) => handleSessionAction(cmd, session.id)"
          @click.stop
          trigger="click"
        >
          <el-icon class="more-icon" @click.stop><More /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="rename">重命名</el-dropdown-item>
              <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div v-if="filteredSessions.length === 0" class="empty-sessions">
        <span>暂无对话记录</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Search, ChatDotRound, More } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chatStore'

const chatStore = useChatStore()
const searchText = ref('')
const listRef = ref<HTMLElement>()

const currentSessionId = computed(() => chatStore.currentSessionId)
const filteredSessions = computed(() => {
  const kw = searchText.value.trim().toLowerCase()
  if (!kw) return chatStore.sessions
  return chatStore.sessions.filter(s => s.title.toLowerCase().includes(kw))
})

async function createSession() {
  await chatStore.createSession()
}

function selectSession(id: string) {
  chatStore.selectSession(id)
}

async function handleSessionAction(command: string, id: string) {
  if (command === 'delete') {
    await ElMessageBox.confirm('确认删除该对话？', '提示', { type: 'warning' })
    await chatStore.deleteSession(id)
    ElMessage.success('已删除')
  } else if (command === 'rename') {
    const { value } = await ElMessageBox.prompt('请输入新名称', '重命名', {
      inputPattern: /\S/,
      inputErrorMessage: '名称不能为空',
    })
    await chatStore.renameSession(id, value)
  }
}

onMounted(() => {
  chatStore.loadSessions()
})
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--bg-white);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px 0;
}

.sidebar-header {
  padding: 0 12px 12px;
}
.new-chat-btn {
  width: 100%;
  border-radius: var(--radius-md);
}

.sidebar-section-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 16px 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-search {
  padding: 0 12px 8px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
  group: true;
}
.session-item:hover {
  background: var(--bg-hover);
}
.session-item.is-active {
  background: var(--primary-light);
  color: var(--primary-color);
}
.session-icon {
  font-size: 14px;
  flex-shrink: 0;
  color: var(--text-secondary);
}
.session-item.is-active .session-icon {
  color: var(--primary-color);
}
.session-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-regular);
}
.session-item.is-active .session-title {
  color: var(--primary-color);
  font-weight: 500;
}
.session-actions {
  opacity: 0;
  transition: opacity var(--transition-fast);
}
.session-item:hover .session-actions {
  opacity: 1;
}
.more-icon {
  font-size: 16px;
  color: var(--text-secondary);
  padding: 2px;
  border-radius: 4px;
}
.more-icon:hover {
  background: var(--border-color);
}

.empty-sessions {
  text-align: center;
  padding: 32px 0;
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>

<template>
  <header class="top-nav">
    <!-- Logo区域 -->
    <div class="nav-logo">
      <img src="/logo.svg" alt="logo" class="logo-img" />
      <span class="logo-text">交投科技政企大模型</span>
    </div>

    <!-- 模块Tab导航 -->
    <nav class="nav-tabs">
      <router-link
        v-for="tab in tabs"
        :key="tab.route"
        :to="tab.route"
        class="nav-tab"
        :class="[`nav-tab--${tab.module}`, { 'is-active': isActive(tab.route) }]"
      >
        <el-icon class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </router-link>
    </nav>

    <!-- 右侧操作区 -->
    <div class="nav-actions">
      <el-tooltip content="帮助文档" placement="bottom">
        <el-button :icon="QuestionFilled" circle text size="large" class="action-btn" />
      </el-tooltip>

      <el-dropdown @command="handleUserCommand" trigger="click">
        <div class="user-avatar">
          <el-avatar :size="32" :src="userInfo?.avatar" class="avatar-img">
            {{ userInfo?.displayName?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userInfo?.displayName }}</span>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> 个人信息
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ChatDotRound, EditPen, DataAnalysis, Tools,
  QuestionFilled, ArrowDown, User, Lock, SwitchButton,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const tabs = [
  { label: '得心', route: '/chat',       module: 'chat',  icon: ChatDotRound },
  { label: '应手', route: '/write',      module: 'write', icon: EditPen },
  { label: '问数', route: '/data-query', module: 'data',  icon: DataAnalysis },
  { label: '知事', route: '/agent',      module: 'agent', icon: Tools },
]

function isActive(tabRoute: string) {
  return route.path.startsWith(tabRoute)
}

async function handleUserCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  height: var(--top-nav-height);
  padding: 0 16px 0 20px;
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  z-index: var(--z-nav);
  flex-shrink: 0;
  gap: 0;
}

/* Logo */
.nav-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
  flex-shrink: 0;
}
.logo-img {
  width: 28px;
  height: 28px;
}
.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

/* Tabs */
.nav-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 20px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  position: relative;
}
.nav-tab:hover {
  color: var(--primary-color);
  background: var(--bg-hover);
}
.nav-tab.is-active {
  color: var(--primary-color);
  background: var(--primary-light);
  font-weight: 600;
}

/* 各模块激活色 */
.nav-tab--write.is-active { color: #7C3AED; background: #F5F3FF; }
.nav-tab--data.is-active  { color: #059669; background: #ECFDF5; }
.nav-tab--agent.is-active { color: #DC2626; background: #FEF2F2; }

.tab-icon { font-size: 15px; }
.tab-label { font-size: 14px; }

/* 右侧 */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.action-btn {
  color: var(--text-secondary);
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.user-avatar:hover {
  background: var(--bg-hover);
}
.user-name {
  font-size: 13px;
  color: var(--text-regular);
  max-width: 80px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.arrow-icon {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>

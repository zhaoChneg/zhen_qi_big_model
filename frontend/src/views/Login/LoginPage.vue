<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-circle bg-circle-1" />
      <div class="bg-circle bg-circle-2" />
    </div>

    <div class="login-card">
      <!-- Logo -->
      <div class="login-logo">
        <div class="logo-icon">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="10" fill="#1E6FE8"/>
            <path d="M10 18C10 13.5817 13.5817 10 18 10C22.4183 10 26 13.5817 26 18" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M18 26C18 26 12 22 12 18" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M18 26C18 26 24 22 24 18" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="18" cy="18" r="2.5" fill="white"/>
          </svg>
        </div>
        <div class="logo-texts">
          <span class="logo-main">交投科技</span>
          <span class="logo-sub">政企大模型</span>
        </div>
      </div>

      <h2 class="login-title">欢迎登录</h2>
      <p class="login-desc">请使用您的账号登录系统</p>

      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        class="login-form"
        @keydown.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :prefix-icon="User"
            placeholder="请输入用户名"
            size="large"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-btn"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <p class="login-footer">交投科技信息系统 · 内部使用</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e?.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #EBF3FF 0%, #F0F7FF 50%, #E8F5E9 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}
.bg-circle-1 {
  width: 500px; height: 500px;
  background: var(--primary-color);
  top: -200px; left: -150px;
}
.bg-circle-2 {
  width: 400px; height: 400px;
  background: #059669;
  bottom: -150px; right: -100px;
}

.login-card {
  width: 400px;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  padding: 40px;
  box-shadow: var(--shadow-lg);
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}
.logo-icon { flex-shrink: 0; }
.logo-texts {
  display: flex;
  flex-direction: column;
}
.logo-main {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.logo-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

.login-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.login-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 28px;
}

.login-form {
  width: 100%;
}
:deep(.el-form-item) { margin-bottom: 16px; }
:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border-color);
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-md);
  letter-spacing: 2px;
}

.login-footer {
  margin-top: 24px;
  font-size: 11px;
  color: var(--text-placeholder);
}
</style>

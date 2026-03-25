<template>
  <div class="welcome-wrap">
    <div class="welcome-content">
      <!-- 图标 -->
      <div class="welcome-icon">
        <el-icon><ChatDotRound /></el-icon>
      </div>

      <!-- 标题 -->
      <h2 class="welcome-title">速览文档，慧解内涵</h2>
      <p class="welcome-subtitle">让文件检索与解析伸手即来，更高效</p>

      <!-- 功能入口 -->
      <div class="feature-row">
        <div class="feature-tag" @click="openUpload">
          <el-icon><Upload /></el-icon>
          <span>上传文档问答</span>
        </div>
        <div class="feature-tag" @click="emit('select', '帮我总结最新的政策要点')">
          <el-icon><Document /></el-icon>
          <span>政策文件解读</span>
        </div>
      </div>

      <!-- 示例问题 -->
      <div class="suggest-grid">
        <div
          v-for="item in suggests"
          :key="item.text"
          class="suggest-card"
          @click="emit('select', item.text)"
        >
          <div class="suggest-tag">{{ item.tag }}</div>
          <p class="suggest-text">{{ item.text }}</p>
        </div>
      </div>

      <p class="disclaimer">所有内容由AI大模型生成，仅供参考，不代表官方立场。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Upload, Document } from '@element-plus/icons-vue'

const emit = defineEmits<{ select: [text: string] }>()

const suggests = [
  { tag: '政策法规', text: '当违法人违反登记声明内容或意思违背国家安全全被判决有罪时，选...' },
  { tag: '农村事务', text: '农村集体经济组织成员有哪些权利？' },
  { tag: '财政预算', text: '在每年预算报告中是否需要提前下达的地方政府债务限额？' },
  { tag: '企业经营', text: '销售淡旺季时，能否以低代价留充两代？' },
  { tag: '会议合规', text: '如果类书会议义主席发言当选人违反登记声明内容应如免责...' },
  { tag: '政策解读', text: '《国务院关于促进国家高新技术产业开发区高质量发展的若干...' },
]

function openUpload() {
  // 触发MessageInput上传
  document.getElementById('chat-file-input')?.click()
}
</script>

<style scoped>
.welcome-wrap {
  flex: 1;
  overflow-y: auto;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 20px 20px;
}

.welcome-content {
  width: 100%;
  max-width: 760px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  background: var(--primary-light);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--primary-color);
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.feature-row {
  display: flex;
  gap: 10px;
}
.feature-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.feature-tag:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-light);
}

.suggest-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
}

.suggest-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.suggest-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-card);
}

.suggest-tag {
  font-size: 11px;
  font-weight: 600;
  color: var(--primary-color);
  background: var(--primary-light);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  align-self: flex-start;
}

.suggest-text {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.disclaimer {
  font-size: 11px;
  color: var(--text-placeholder);
}
</style>

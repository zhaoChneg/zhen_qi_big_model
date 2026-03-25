<template>
  <div class="agent-layout">
    <!-- 工具卡片列表 -->
    <div v-if="!currentTool" class="agent-grid-view">
      <div class="grid-header">
        <h2 class="grid-title">智能工具集</h2>
        <p class="grid-sub">基于大模型的智能政务工具，提升工作效率</p>
      </div>
      <div class="agent-grid">
        <AgentCard
          v-for="tool in agentStore.filteredTools"
          :key="tool.id"
          :tool="tool"
          @click="agentStore.openTool(tool.id)"
        />
      </div>
    </div>

    <!-- 工具执行界面 -->
    <AgentRunner v-else :tool="currentTool" @close="agentStore.closeTool()" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import AgentCard from './components/AgentCard.vue'
import AgentRunner from './components/AgentRunner.vue'
import { useAgentStore } from '@/stores/agentStore'

const agentStore = useAgentStore()
const currentTool = computed(() => agentStore.currentTool)

onMounted(() => {
  agentStore.loadTools()
})
</script>

<style scoped>
.agent-layout {
  height: 100%;
  overflow: hidden;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}
.agent-grid-view {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
.grid-header {
  margin-bottom: 24px;
}
.grid-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.grid-sub {
  font-size: 13px;
  color: var(--text-secondary);
}
.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>

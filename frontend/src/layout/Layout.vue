<template>
  <div class="layout">
    <TopNav />
    <div class="layout-body">
      <component :is="currentSidebar" />
      <main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedViews">
            <component :is="Component" :key="$route.fullPath" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TopNav from './TopNav.vue'
import ChatSidebarWrapper from './sidebars/ChatSidebarWrapper.vue'
import WriteSidebarWrapper from './sidebars/WriteSidebarWrapper.vue'
import DataSidebarWrapper from './sidebars/DataSidebarWrapper.vue'
import AgentSidebarWrapper from './sidebars/AgentSidebarWrapper.vue'

const route = useRoute()

const cachedViews = ['ChatLayout', 'WriteLayout']

const currentSidebar = computed(() => {
  const module = route.meta.module
  const map: Record<string, any> = {
    chat: ChatSidebarWrapper,
    write: WriteSidebarWrapper,
    data: DataSidebarWrapper,
    agent: AgentSidebarWrapper,
  }
  return map[module as string] || null
})
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: var(--bg-page);
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.layout-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>

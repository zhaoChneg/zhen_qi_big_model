<template>
  <div class="data-query-layout">
    <!-- 查询输入区 -->
    <QueryInput @submit="handleQuery" />

    <!-- 主内容区 -->
    <div class="content-area">
      <!-- 空状态 -->
      <DataQueryWelcome v-if="showWelcome" @select="handleSuggest" />

      <!-- 查询结果 -->
      <template v-else>
        <!-- SQL预览 -->
        <SQLPreview
          v-if="generatedSQL || nl2sqlLoading"
          :sql="generatedSQL"
          :explanation="sqlExplanation"
          :loading="nl2sqlLoading"
          @execute="handleExecute"
        />

        <!-- 查询结果面板 -->
        <ResultPanel
          v-if="queryResult || queryLoading"
          :result="queryResult"
          :summary="aiSummary"
          :loading="queryLoading"
          :summary-loading="summaryLoading"
          :question="question"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import QueryInput from './components/QueryInput.vue'
import SQLPreview from './components/SQLPreview.vue'
import ResultPanel from './components/ResultPanel.vue'
import DataQueryWelcome from './components/DataQueryWelcome.vue'
import { useDataQueryStore } from '@/stores/dataQueryStore'

const store = useDataQueryStore()
const {
  generatedSQL, sqlExplanation, queryResult, aiSummary,
  nl2sqlLoading, queryLoading, summaryLoading, question,
} = storeToRefs(store)

const showWelcome = computed(() =>
  !generatedSQL.value && !nl2sqlLoading.value && !queryResult.value
)

async function handleQuery(q: string) {
  await store.generateSQL(q)
  // SQL生成后自动执行
  if (store.generatedSQL) {
    await store.executeQuery()
  }
}

function handleSuggest(text: string) {
  handleQuery(text)
}

async function handleExecute(sql: string) {
  await store.executeQuery(sql)
}
</script>

<style scoped>
.data-query-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #F8FAFC;
  overflow: hidden;
}
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px 24px;
}
</style>

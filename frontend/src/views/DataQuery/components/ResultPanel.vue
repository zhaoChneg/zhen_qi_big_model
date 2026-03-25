<template>
  <div class="result-panel">
    <!-- 加载中 -->
    <div v-if="loading" class="result-loading card">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else-if="result">
      <!-- AI数据解读 -->
      <div class="ai-summary card" v-if="summary || summaryLoading">
        <div class="summary-header">
          <el-icon class="summary-icon"><ChatDotRound /></el-icon>
          <span class="summary-title">数据解读</span>
          <div v-if="summaryLoading" class="thinking-dots">
            <span /><span /><span />
          </div>
        </div>
        <p v-if="summary" class="summary-text">{{ summary }}</p>
        <el-skeleton v-else :rows="2" animated />
      </div>

      <!-- 结果头部 -->
      <div class="result-header card">
        <div class="result-meta">
          <span class="result-count">共 <strong>{{ result.total }}</strong> 条记录</span>
          <span class="result-time" v-if="duration">耗时 {{ duration }}ms</span>
        </div>
        <div class="view-tabs">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table">
              <el-icon><Grid /></el-icon> 表格
            </el-radio-button>
            <el-radio-button value="chart" :disabled="!canShowChart">
              <el-icon><TrendCharts /></el-icon> 图表
            </el-radio-button>
          </el-radio-group>
          <el-button :icon="Download" size="small" @click="handleExport">导出</el-button>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'" class="table-wrap card">
        <el-table
          :data="result.rows"
          border
          stripe
          size="small"
          max-height="400"
          style="width: 100%"
        >
          <el-table-column
            v-for="col in result.columns"
            :key="col"
            :prop="col"
            :label="col"
            min-width="120"
            show-overflow-tooltip
          />
        </el-table>

        <!-- 分页 -->
        <div v-if="result.total > 20" class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="20"
            :total="result.total"
            layout="total, prev, pager, next"
            small
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 图表视图 -->
      <div v-if="viewMode === 'chart'" class="chart-wrap card">
        <div class="chart-toolbar">
          <el-select v-model="chartType" size="small" style="width: 120px">
            <el-option value="bar" label="柱状图" />
            <el-option value="line" label="折线图" />
            <el-option value="pie" label="饼图" />
          </el-select>
          <el-select v-model="xAxisCol" placeholder="X轴" size="small" style="width: 140px">
            <el-option v-for="col in result.columns" :key="col" :value="col" :label="col" />
          </el-select>
          <el-select v-model="yAxisCol" placeholder="Y轴（数值）" size="small" style="width: 140px">
            <el-option v-for="col in numericColumns" :key="col" :value="col" :label="col" />
          </el-select>
        </div>
        <v-chart :option="chartOption" style="height: 360px; width: 100%" autoresize />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChatDotRound, Grid, TrendCharts, Download } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { exportCsv } from '@/utils/export'
import type { QueryResult } from '@/types'

use([CanvasRenderer, BarChart, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const props = defineProps<{
  result: QueryResult | null
  summary: string
  loading: boolean
  summaryLoading: boolean
  question: string
  duration?: number
}>()

const viewMode = ref<'table' | 'chart'>('table')
const chartType = ref<'bar' | 'line' | 'pie'>('bar')
const currentPage = ref(1)
const xAxisCol = ref('')
const yAxisCol = ref('')

const numericColumns = computed(() => {
  if (!props.result) return []
  return props.result.columns.filter(col => {
    const val = props.result!.rows[0]?.[col]
    return typeof val === 'number' || (!isNaN(Number(val)) && val !== '')
  })
})

const canShowChart = computed(() =>
  props.result && props.result.rows.length > 0 && numericColumns.value.length > 0
)

const chartOption = computed(() => {
  if (!props.result || !xAxisCol.value || !yAxisCol.value) return {}
  const xData = props.result.rows.map(r => String(r[xAxisCol.value] ?? ''))
  const yData = props.result.rows.map(r => Number(r[yAxisCol.value] ?? 0))

  if (chartType.value === 'pie') {
    return {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: xData.map((name, i) => ({ name, value: yData[i] })),
        emphasis: { itemStyle: { shadowBlur: 10 } },
      }],
    }
  }

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: xData, axisLabel: { fontSize: 11, rotate: xData.length > 8 ? 30 : 0 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    series: [{
      type: chartType.value,
      data: yData,
      itemStyle: { color: '#059669' },
      smooth: chartType.value === 'line',
    }],
  }
})

function handleExport() {
  if (!props.result) return
  exportCsv(props.result.columns, props.result.rows, `查询结果_${Date.now()}`)
}

async function handlePageChange(_page: number) {
  // 通知父组件重新查询对应页
}
</script>

<style scoped>
.result-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 900px;
}
.result-loading { padding: 20px; }

.ai-summary { padding: 14px 16px; border-left: 3px solid #059669; }
.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.summary-icon { color: #059669; font-size: 16px; }
.summary-title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.summary-text { font-size: 13px; color: var(--text-regular); line-height: 1.7; }

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
}
.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.result-count { font-size: 13px; color: var(--text-secondary); }
.result-count strong { color: var(--text-primary); }
.result-time { font-size: 12px; color: var(--text-placeholder); }
.view-tabs { display: flex; align-items: center; gap: 8px; }

.table-wrap { padding: 16px; overflow: hidden; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }

.chart-wrap { padding: 16px; }
.chart-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>

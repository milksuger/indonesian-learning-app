<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NGrid, NGridItem, NStatistic, NSpin, NProgress } from 'naive-ui'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import request from '../utils/request.js'

/**
 * 学习统计页
 * 展示近 30 天学习趋势（ECharts 柱状图）+ 词书完成进度
 */
use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const { t } = useI18n()
const stats = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await request.get('/users/me/stats')
    stats.value = data
  } finally {
    loading.value = false
  }
})

// ECharts 柱状图配置
const chartOption = computed(() => {
  if (!stats.value) return {}
  const days = stats.value.dailyCounts
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    xAxis: {
      type: 'category',
      data: days.map(d => d.date.slice(5)), // 只显示 MM-DD
      axisLabel: { interval: 6, fontSize: 11 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: days.map(d => d.count),
      itemStyle: { color: '#18a058', borderRadius: [3, 3, 0, 0] }
    }]
  }
})
</script>

<template>
  <div class="stats-container">
    <n-spin :show="loading">
      <template v-if="stats">
        <!-- 概览数字 -->
        <n-grid :cols="2" :x-gap="12" :y-gap="12" style="margin-bottom: 16px">
          <n-grid-item>
            <n-card size="small">
              <n-statistic :label="t('totalLearned')" :value="stats.totalLearned" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small">
              <n-statistic :label="t('totalMemorized')" :value="stats.totalMemorized" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small">
              <n-statistic :label="t('totalMistakes')" :value="stats.totalMistakes" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card size="small">
              <n-statistic :label="t('totalFavorites')" :value="stats.totalFavorites" />
            </n-card>
          </n-grid-item>
        </n-grid>

        <!-- 近 30 天学习趋势 -->
        <n-card :title="t('last30Days')" style="margin-bottom: 16px">
          <v-chart :option="chartOption" style="height: 200px" autoresize />
        </n-card>

        <!-- 词书完成进度 -->
        <n-card :title="t('wordbookProgress')">
          <div
            v-for="wb in stats.wordbookProgress"
            :key="wb.name"
            class="wb-progress-item"
          >
            <div class="wb-progress-header">
              <span class="wb-name">{{ wb.name }}</span>
              <span class="wb-count">{{ wb.learned }} / {{ wb.total }}</span>
            </div>
            <n-progress
              type="line"
              :percentage="wb.total > 0 ? Math.round((wb.learned / wb.total) * 100) : 0"
              :show-indicator="false"
              style="margin-top: 4px"
            />
          </div>
          <div v-if="!stats.wordbookProgress.length" class="empty-hint">
            {{ t('noActiveWordbooks') }}
          </div>
        </n-card>
      </template>
    </n-spin>
  </div>
</template>

<style scoped>
.stats-container { max-width: 600px; margin: 16px auto; padding: 0 16px; }
.wb-progress-item { margin-bottom: 16px; }
.wb-progress-item:last-child { margin-bottom: 0; }
.wb-progress-header { display: flex; justify-content: space-between; align-items: center; }
.wb-name { font-size: 14px; font-weight: 500; }
.wb-count { font-size: 13px; color: #9ca3af; }
.empty-hint { text-align: center; color: #9ca3af; padding: 16px 0; font-size: 14px; }
</style>

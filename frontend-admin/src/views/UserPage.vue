<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import {
  NCard, NDataTable, NTag, NInput, NSpace, NSpin, NStatistic, NGrid, NGridItem
} from 'naive-ui'

/**
 * 用户管理页面
 * 展示所有用户列表及学习统计，支持关键词搜索
 */
const api = axios.create({ baseURL: '/api/v1', timeout: 15000 })

const users = ref([])
const stats = ref({ totalUsers: 0, totalWordbooks: 0, totalWords: 0, totalLearned: 0 })
const loading = ref(false)
const searchText = ref('')

// 过滤后的用户列表
const filteredUsers = computed(() => {
  const kw = searchText.value.trim().toLowerCase()
  if (!kw) return users.value
  return users.value.filter(u => u.email.toLowerCase().includes(kw))
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 70,
    sorter: (a, b) => a.id - b.id
  },
  {
    title: '邮箱',
    key: 'email',
    ellipsis: { tooltip: true }
  },
  {
    title: '界面语言',
    key: 'uiLanguage',
    width: 100,
    render: (row) => {
      const map = { 'zh-CN': '中文', 'en-US': 'English', 'id-ID': 'Indonesia' }
      return map[row.uiLanguage] || row.uiLanguage
    }
  },
  {
    title: '每日目标',
    key: 'dailyGoal',
    width: 90,
    sorter: (a, b) => a.dailyGoal - b.dailyGoal
  },
  {
    title: '连续打卡',
    key: 'streak',
    width: 90,
    sorter: (a, b) => a.streak - b.streak,
    render: (row) => row.streak > 0 ? `${row.streak} 天` : '-'
  },
  {
    title: '最后打卡',
    key: 'lastCheckinDate',
    width: 120,
    render: (row) => row.lastCheckinDate || '-'
  },
  {
    title: '已学单词',
    key: 'learnedCount',
    width: 100,
    sorter: (a, b) => a.learnedCount - b.learnedCount,
    render: (row) => {
      const n = row.learnedCount
      return n > 0 ? String(n) : '0'
    }
  },
  {
    title: '已掌握',
    key: 'memorizedCount',
    width: 90,
    sorter: (a, b) => a.memorizedCount - b.memorizedCount,
    render: (row) => {
      const n = row.memorizedCount
      if (n === 0) return '0'
      return n
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => {
      const active = row.learnedCount > 0
      return active
        ? h(NTag, { type: 'success', size: 'small' }, { default: () => '活跃' })
        : h(NTag, { type: 'default', size: 'small' }, { default: () => '未学习' })
    }
  }
]

import { h } from 'vue'

async function fetchData() {
  loading.value = true
  try {
    const [usersRes, statsRes] = await Promise.all([
      api.get('/admin/users'),
      api.get('/admin/stats')
    ])
    users.value = usersRes.data
    stats.value = statsRes.data
  } catch (err) {
    console.error('加载失败', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="page-wrapper">
    <!-- 概览统计卡片 -->
    <n-grid :cols="4" :x-gap="16" style="margin-bottom: 24px">
      <n-grid-item>
        <n-card size="small">
          <n-statistic label="总用户数" :value="stats.totalUsers" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card size="small">
          <n-statistic label="词书数量" :value="stats.totalWordbooks" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card size="small">
          <n-statistic label="单词总数" :value="stats.totalWords" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card size="small">
          <n-statistic label="累计学习次数" :value="stats.totalLearned" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 用户列表 -->
    <n-card title="用户列表">
      <template #header-extra>
        <n-input
          v-model:value="searchText"
          placeholder="搜索邮箱..."
          clearable
          style="width: 220px"
        />
      </template>

      <n-spin :show="loading">
        <n-data-table
          :columns="columns"
          :data="filteredUsers"
          :pagination="{ pageSize: 20 }"
          :bordered="false"
          striped
          size="small"
        />
      </n-spin>
    </n-card>
  </div>
</template>

<style scoped>
.page-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px;
}
</style>

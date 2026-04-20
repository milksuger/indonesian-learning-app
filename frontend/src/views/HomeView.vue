<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/useAuthStore.js'
import { useLearningStore } from '../stores/useLearningStore.js'
import { NButton, NCard, NSpace, NStatistic, NGrid, NGridItem } from 'naive-ui'

/**
 * 首页
 * 展示学习进度摘要与快捷入口
 */
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const learningStore = useLearningStore()

onMounted(() => {
  learningStore.fetchSummary()
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="home-container">
    <n-card :title="t('appName')">
      <template #header-extra>
        <n-button text @click="handleLogout">{{ t('logout') }}</n-button>
      </template>

      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-statistic :label="t('dailyGoal')" :value="learningStore.summary.dailyGoal" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic :label="t('learnedToday')" :value="learningStore.summary.learnedToday" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic :label="t('reviewsDue')" :value="learningStore.summary.reviewsDueToday" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic :label="t('streak')" :value="learningStore.summary.streak" />
        </n-grid-item>
      </n-grid>

      <n-space style="margin-top: 24px" justify="center">
        <n-button type="primary" size="large" @click="router.push('/learn')">
          {{ t('startLearning') }}
        </n-button>
        <n-button size="large" @click="router.push('/review')">
          {{ t('startReview') }}
        </n-button>
      </n-space>
    </n-card>

    <n-space vertical style="margin-top: 16px">
      <n-button block type="warning" @click="router.push('/random')">{{ t('randomMode') }}</n-button>
      <n-button block @click="router.push('/wordbooks')">{{ t('wordbooks') }}</n-button>
      <n-button block @click="router.push('/search')">{{ t('wordSearch') }}</n-button>
      <n-button block @click="router.push('/today')">{{ t('todayRecord') }}</n-button>
      <n-button block @click="router.push('/checkin')">{{ t('checkinCalendar') }}</n-button>
      <n-button block @click="router.push('/stats')">{{ t('stats') }}</n-button>
      <n-button block @click="router.push('/favorites')">{{ t('favorites') }}</n-button>
      <n-button block @click="router.push('/mistakes')">{{ t('mistakes') }}</n-button>
      <n-button block @click="router.push('/memorized')">{{ t('memorizedWords') }}</n-button>
      <n-button block @click="router.push('/settings')">{{ t('settings') }}</n-button>
      <n-button block @click="router.push('/profile')">{{ t('profile') }}</n-button>
    </n-space>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 600px;
  margin: 16px auto;
  padding: 0 16px;
}
</style>

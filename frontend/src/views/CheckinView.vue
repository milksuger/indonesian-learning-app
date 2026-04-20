<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NCalendar, NStatistic, NGrid, NGridItem, NButton } from 'naive-ui'
import request from '../utils/request.js'

/**
 * 打卡日历页
 * 展示历史打卡记录（已打卡日期高亮）和连续打卡天数
 */
const { t } = useI18n()

const checkinDates = ref(new Set())
const streak = ref(0)
const totalCheckins = ref(0)
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)

async function fetchCalendar(year, month) {
  const { data } = await request.get('/users/me/checkin-calendar', { params: { year, month } })
  data.checkinDates.forEach(d => checkinDates.value.add(d))
  streak.value = data.streak
  totalCheckins.value = checkinDates.value.size
}

onMounted(() => {
  // 加载近 3 个月
  const now = new Date()
  for (let i = 2; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    fetchCalendar(d.getFullYear(), d.getMonth() + 1)
  }
})

// NCalendar 使用时间戳，判断某天是否打卡
function isCheckin(ts) {
  const d = new Date(ts)
  const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return checkinDates.value.has(dateStr)
}
</script>

<template>
  <div class="checkin-container">
    <!-- 统计卡片 -->
    <n-grid :cols="2" :x-gap="12" style="margin-bottom: 16px">
      <n-grid-item>
        <n-card size="small">
          <n-statistic :label="t('streak')" :value="streak">
            <template #suffix>{{ t('days') }}</template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card size="small">
          <n-statistic :label="t('totalCheckins')" :value="totalCheckins">
            <template #suffix>{{ t('days') }}</template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 日历 -->
    <n-card :title="t('checkinCalendar')">
      <n-calendar>
        <template #default="{ year, month, date }">
          <div
            v-if="isCheckin(new Date(year, month - 1, date).getTime())"
            class="checkin-dot"
          />
        </template>
      </n-calendar>
    </n-card>
  </div>
</template>

<style scoped>
.checkin-container { max-width: 600px; margin: 16px auto; padding: 0 16px; }

.checkin-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #18a058;
  margin: 2px auto 0;
}
</style>

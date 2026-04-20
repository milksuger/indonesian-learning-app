import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request.js'

/**
 * 学习状态管理
 * 封装每日新词队列、学习反馈与进度摘要
 */
export const useLearningStore = defineStore('learning', () => {
  const queue = ref([])
  const currentIndex = ref(0)
  const summary = ref({ dailyGoal: 20, learnedToday: 0, reviewsDueToday: 0, streak: 0, canCheckin: false })

  const currentCard = computed(() => queue.value[currentIndex.value] || null)
  const hasMore = computed(() => currentIndex.value < queue.value.length)
  const progress = computed(() => ({
    current: currentIndex.value,
    total: queue.value.length,
  }))

  async function fetchQueue() {
    const { data } = await request.get('/learning/queue')
    queue.value = data
    currentIndex.value = 0
  }

  async function submitFeedback(wordId, known) {
    await request.post('/learning/feedback', { wordId, known })
    currentIndex.value++
  }

  async function toggleFavorite(wordId) {
    await request.post(`/users/me/favorites/${wordId}`)
    const idx = currentIndex.value
    if (queue.value[idx]) {
      queue.value[idx] = {
        ...queue.value[idx],
        isFavorited: !queue.value[idx].isFavorited,
      }
    }
  }

  async function toggleMemorized(wordId) {
    if (queue.value[currentIndex.value]?.isMemorized) {
      return
    }
    await request.post(`/users/me/memorized/${wordId}`)
    const idx = currentIndex.value
    if (queue.value[idx]) {
      queue.value[idx] = {
        ...queue.value[idx],
        isMemorized: true,
      }
    }
    currentIndex.value++
  }

  async function fetchSummary() {
    const { data } = await request.get('/learning/summary')
    summary.value = data
  }

  function reset() {
    queue.value = []
    currentIndex.value = 0
  }

  return {
    queue, currentIndex, summary,
    currentCard, hasMore, progress,
    fetchQueue, submitFeedback, toggleFavorite, toggleMemorized, fetchSummary, reset,
  }
})

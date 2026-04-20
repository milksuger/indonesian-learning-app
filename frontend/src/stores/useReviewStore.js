import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request.js'

/**
 * 复习状态管理
 * 封装每日复习队列与复习反馈
 */
export const useReviewStore = defineStore('review', () => {
  const queue = ref([])
  const currentIndex = ref(0)

  const currentCard = computed(() => queue.value[currentIndex.value] || null)
  const hasMore = computed(() => currentIndex.value < queue.value.length)
  const progress = computed(() => ({
    current: currentIndex.value,
    total: queue.value.length,
  }))

  async function fetchQueue() {
    const { data } = await request.get('/review/queue')
    queue.value = data
    currentIndex.value = 0
  }

  async function submitFeedback(wordId, known) {
    await request.post('/review/feedback', { wordId, known })
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

  function reset() {
    queue.value = []
    currentIndex.value = 0
  }

  return {
    queue, currentIndex,
    currentCard, hasMore, progress,
    fetchQueue, submitFeedback, toggleFavorite, toggleMemorized, reset,
  }
})

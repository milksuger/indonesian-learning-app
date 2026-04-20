import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 单词列表分页加载 composable
 * 使用 IntersectionObserver 监听底部哨兵元素，自动加载下一页
 *
 * @param {Function} fetchFn - 异步函数，接收 page 参数，返回单词数组
 * @param {number} pageSize  - 每页数量，默认 20
 */
export function useWordList(fetchFn, pageSize = 20) {
  const words = ref([])
  const page = ref(0)
  const loading = ref(false)
  const hasMore = ref(true)
  const sentinelRef = ref(null)

  let observer = null

  async function loadMore() {
    if (loading.value || !hasMore.value) return
    loading.value = true
    try {
      const result = await fetchFn(page.value, pageSize)
      words.value.push(...result)
      if (result.length < pageSize) {
        hasMore.value = false
      } else {
        page.value++
      }
    } finally {
      loading.value = false
    }
  }

  function reset() {
    words.value = []
    page.value = 0
    loading.value = false
    hasMore.value = true
  }

  function setupObserver() {
    if (!sentinelRef.value) return
    observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          loadMore()
        }
      },
      { threshold: 0.1 }
    )
    observer.observe(sentinelRef.value)
  }

  function destroyObserver() {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  }

  onMounted(() => {
    loadMore()
  })

  onUnmounted(() => {
    destroyObserver()
  })

  return {
    words,
    loading,
    hasMore,
    sentinelRef,
    loadMore,
    reset,
    setupObserver,
    destroyObserver,
  }
}

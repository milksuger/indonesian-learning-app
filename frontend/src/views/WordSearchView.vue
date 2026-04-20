<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NInput, NEmpty, NSpin } from 'naive-ui'
import WordListItem from '../components/WordListItem.vue'
import request from '../utils/request.js'

/**
 * 单词搜索页
 * 在用户激活词书内搜索单词，支持无限滚动分页
 */
const { t } = useI18n()

const keyword = ref('')
const words = ref([])
const total = ref(0)
const page = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const sentinelRef = ref(null)
const PAGE_SIZE = 20

let searchTimer = null
let observer = null

async function loadMore(reset = false) {
  if (loading.value || (!reset && !hasMore.value)) return
  if (reset) { words.value = []; page.value = 0; hasMore.value = true }
  loading.value = true
  try {
    const { data } = await request.get('/users/me/words/search', {
      params: { q: keyword.value.trim(), page: page.value, size: PAGE_SIZE }
    })
    words.value.push(...data.items)
    total.value = data.total
    if (data.items.length < PAGE_SIZE) hasMore.value = false
    else page.value++
  } finally {
    loading.value = false
  }
}

function onInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadMore(true), 300)
}

function setupObserver() {
  if (!sentinelRef.value) return
  observer = new IntersectionObserver(
    (entries) => { if (entries[0].isIntersecting) loadMore() },
    { threshold: 0.1 }
  )
  observer.observe(sentinelRef.value)
}

onMounted(async () => {
  await loadMore()
  await nextTick()
  setupObserver()
})

onUnmounted(() => { if (observer) observer.disconnect() })

async function handleToggleFavorite(wordId) {
  await request.post(`/users/me/favorites/${wordId}`)
  const w = words.value.find(w => w.wordId === wordId)
  if (w) w.isFavorited = !w.isFavorited
}

async function handleToggleMemorized(wordId) {
  await request.post(`/users/me/memorized/${wordId}`)
  const w = words.value.find(w => w.wordId === wordId)
  if (w) w.isMemorized = true
}
</script>

<template>
  <div class="search-container">
    <n-card>
      <template #header>
        <n-input
          v-model:value="keyword"
          :placeholder="t('searchPlaceholder')"
          clearable
          @input="onInput"
          @clear="() => loadMore(true)"
        />
      </template>

      <div v-if="words.length > 0">
        <div class="result-hint">{{ t('searchResults', { count: total }) }}</div>
        <div class="word-list">
          <word-list-item
            v-for="word in words"
            :key="word.wordId"
            :word="word"
            :is-favorited="word.isFavorited"
            :is-memorized="word.isMemorized"
            @toggle-favorite="handleToggleFavorite"
            @toggle-memorized="handleToggleMemorized"
          />
        </div>
      </div>

      <n-empty v-else-if="!loading && keyword" :description="t('noSearchResults')" />
      <n-empty v-else-if="!loading" :description="t('searchHint')" />

      <div ref="sentinelRef" class="sentinel">
        <n-spin v-if="loading" size="small" />
        <span v-else-if="!hasMore && words.length > 0" class="no-more">{{ t('noMoreWords') }}</span>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.search-container { max-width: 600px; margin: 16px auto; padding: 0 16px; }
.result-hint { font-size: 13px; color: #9ca3af; margin-bottom: 8px; }
.word-list { padding: 0 4px; }
.sentinel { display: flex; justify-content: center; padding: 16px 0 8px; min-height: 40px; }
.no-more { font-size: 13px; color: #9ca3af; }
</style>

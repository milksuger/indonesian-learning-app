<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NEmpty, NSpin, NButton, NButtonGroup, useMessage } from 'naive-ui'
import FlashCard from '../components/FlashCard.vue'
import WordListItem from '../components/WordListItem.vue'
import request from '../utils/request.js'

/**
 * 错题本页面
 * 支持闪卡模式和列表模式，列表模式使用分页无限滚动
 */
const router = useRouter()
const { t } = useI18n()
const message = useMessage()

const mode = ref(localStorage.getItem('mistakesMode') || 'list')
watch(mode, (val) => localStorage.setItem('mistakesMode', val))

// ---- 列表模式 ----
const words = ref([])
const page = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const sentinelRef = ref(null)
const PAGE_SIZE = 20

async function loadMore() {
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const { data } = await request.get('/users/me/mistakes/paged', {
      params: { page: page.value, size: PAGE_SIZE }
    })
    words.value.push(...data)
    if (data.length < PAGE_SIZE) hasMore.value = false
    else page.value++
  } finally {
    loading.value = false
  }
}

// ---- 闪卡模式 ----
const cardQueue = ref([])
const cardIndex = ref(0)
const cardLoading = ref(false)

async function loadCardQueue() {
  cardLoading.value = true
  try {
    const { data } = await request.get('/users/me/mistakes')
    cardQueue.value = data
    cardIndex.value = 0
  } finally {
    cardLoading.value = false
  }
}

const currentCard = () => cardQueue.value[cardIndex.value] || null

async function handleToggleFavorite(wordId) {
  try {
    await request.post(`/users/me/favorites/${wordId}`)
  } catch {
    message.error(t('operationFailed'))
  }
}

async function handleToggleMemorized(wordId) {
  try {
    await request.post(`/users/me/memorized/${wordId}`)
    message.success(t('markedMemorized'))
    // 从列表移除
    words.value = words.value.filter(w => w.wordId !== wordId)
    const idx = cardQueue.value.findIndex(w => w.wordId === wordId)
    if (idx !== -1) {
      cardQueue.value.splice(idx, 1)
      if (cardIndex.value >= cardQueue.value.length) cardIndex.value = Math.max(0, cardQueue.value.length - 1)
    }
  } catch {
    message.error(t('operationFailed'))
  }
}

// IntersectionObserver
let observer = null
function setupObserver() {
  if (!sentinelRef.value) return
  observer = new IntersectionObserver(
    (entries) => { if (entries[0].isIntersecting) loadMore() },
    { threshold: 0.1 }
  )
  observer.observe(sentinelRef.value)
}

onMounted(async () => {
  if (mode.value === 'list') {
    await loadMore()
    await nextTick()
    setupObserver()
  } else {
    loadCardQueue()
  }
})

onUnmounted(() => { if (observer) observer.disconnect() })

watch(mode, async (val) => {
  if (val === 'list' && words.value.length === 0) {
    await loadMore()
    await nextTick()
    setupObserver()
  } else if (val === 'card' && cardQueue.value.length === 0) {
    loadCardQueue()
  }
})
</script>

<template>
  <div class="collection-container">
    <n-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('mistakes') }}</span>
          <n-button-group size="small">
            <n-button :type="mode === 'card' ? 'primary' : 'default'" @click="mode = 'card'">
              {{ t('cardMode') }}
            </n-button>
            <n-button :type="mode === 'list' ? 'primary' : 'default'" @click="mode = 'list'">
              {{ t('listMode') }}
            </n-button>
          </n-button-group>
        </div>
      </template>

      <!-- 闪卡模式 -->
      <template v-if="mode === 'card'">
        <div v-if="currentCard()">
          <flash-card
            :word="{
              id: currentCard().wordId,
              indonesian: currentCard().indonesian,
              chinese: currentCard().chinese,
              exampleIndonesian: currentCard().exampleIndonesian,
              exampleChinese: currentCard().exampleChinese
            }"
            :current-index="cardIndex + 1"
            :total-count="cardQueue.length"
            :is-favorited="currentCard().isFavorited"
            :is-memorized="currentCard().isMemorized"
            @known="cardIndex++"
            @unknown="cardIndex++"
            @toggle-favorite="handleToggleFavorite"
            @toggle-memorized="handleToggleMemorized"
          />
        </div>
        <n-empty v-else-if="!cardLoading" :description="t('noMistakes')">
          <template #extra>
            <n-button @click="router.push('/')">{{ t('backToHome') }}</n-button>
          </template>
        </n-empty>
      </template>

      <!-- 列表模式 -->
      <template v-else>
        <div v-if="words.length > 0" class="word-list">
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
        <n-empty v-else-if="!loading" :description="t('noMistakes')" />

        <div ref="sentinelRef" class="sentinel">
          <n-spin v-if="loading" size="small" />
          <span v-else-if="!hasMore && words.length > 0" class="no-more">{{ t('noMoreWords') }}</span>
        </div>
      </template>
    </n-card>
  </div>
</template>

<style scoped>
.collection-container {
  max-width: 600px;
  margin: 16px auto;
  padding: 0 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.word-list { padding: 0 4px; }
.sentinel {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
  min-height: 40px;
}
.no-more { font-size: 13px; color: #9ca3af; }
</style>

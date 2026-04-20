<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NEmpty, NButton, NButtonGroup, useMessage } from 'naive-ui'
import FlashCard from '../components/FlashCard.vue'
import WordListItem from '../components/WordListItem.vue'
import request from '../utils/request.js'

/**
 * 随机探索页
 * 从激活词书中随机抽取未掌握单词，不计入 SRS，无限刷新
 */
const router = useRouter()
const { t } = useI18n()
const message = useMessage()

const mode = ref(localStorage.getItem('randomMode') || 'card')
const queue = ref([])
const currentIndex = ref(0)
const loading = ref(false)
const BATCH_SIZE = 20

const currentCard = () => queue.value[currentIndex.value] || null

async function loadBatch() {
  loading.value = true
  try {
    const { data } = await request.get('/learning/random', { params: { size: BATCH_SIZE } })
    queue.value = data
    currentIndex.value = 0
  } catch {
    message.error(t('operationFailed'))
  } finally {
    loading.value = false
  }
}

// 卡片模式：下一张，到末尾自动加载新一批
function nextCard() {
  if (currentIndex.value < queue.value.length - 1) {
    currentIndex.value++
  } else {
    loadBatch()
  }
}

async function handleToggleFavorite(wordId) {
  try {
    await request.post(`/users/me/favorites/${wordId}`)
    const card = queue.value.find(w => w.wordId === wordId)
    if (card) card.isFavorited = !card.isFavorited
  } catch { message.error(t('operationFailed')) }
}

async function handleToggleMemorized(wordId) {
  try {
    await request.post(`/users/me/memorized/${wordId}`)
    const card = queue.value.find(w => w.wordId === wordId)
    if (card) card.isMemorized = true
    message.success(t('markedMemorized'))
    nextCard()
  } catch { message.error(t('operationFailed')) }
}

onMounted(loadBatch)
</script>

<template>
  <div class="random-container">
    <n-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('randomExplore') }}</span>
          <n-button-group size="small">
            <n-button
              :type="mode === 'card' ? 'primary' : 'default'"
              @click="mode = 'card'; localStorage.setItem('randomMode', 'card')"
            >{{ t('cardMode') }}</n-button>
            <n-button
              :type="mode === 'list' ? 'primary' : 'default'"
              @click="mode = 'list'; localStorage.setItem('randomMode', 'list')"
            >{{ t('listMode') }}</n-button>
          </n-button-group>
        </div>
      </template>

      <!-- 卡片模式 -->
      <template v-if="mode === 'card'">
        <div v-if="currentCard() && !loading">
          <flash-card
            :word="{
              id: currentCard().wordId,
              indonesian: currentCard().indonesian,
              chinese: currentCard().chinese,
              exampleIndonesian: currentCard().exampleIndonesian,
              exampleChinese: currentCard().exampleChinese
            }"
            :current-index="currentIndex + 1"
            :total-count="queue.length"
            :is-favorited="currentCard().isFavorited"
            :is-memorized="currentCard().isMemorized"
            @known="nextCard"
            @unknown="nextCard"
            @toggle-favorite="handleToggleFavorite"
            @toggle-memorized="handleToggleMemorized"
          />
          <div class="refresh-hint">
            <n-button text size="small" @click="loadBatch">{{ t('refreshBatch') }}</n-button>
          </div>
        </div>
        <n-empty v-else-if="!loading" :description="t('noWordsToExplore')">
          <template #extra>
            <n-button @click="router.push('/wordbooks')">{{ t('activateWordbook') }}</n-button>
          </template>
        </n-empty>
      </template>

      <!-- 列表模式 -->
      <template v-else>
        <div v-if="queue.length > 0 && !loading">
          <div class="word-list">
            <word-list-item
              v-for="word in queue"
              :key="word.wordId"
              :word="word"
              :is-favorited="word.isFavorited"
              :is-memorized="word.isMemorized"
              @toggle-favorite="handleToggleFavorite"
              @toggle-memorized="handleToggleMemorized"
            />
          </div>
          <div class="refresh-bar">
            <n-button block @click="loadBatch">{{ t('refreshBatch') }}</n-button>
          </div>
        </div>
        <n-empty v-else-if="!loading" :description="t('noWordsToExplore')">
          <template #extra>
            <n-button @click="router.push('/wordbooks')">{{ t('activateWordbook') }}</n-button>
          </template>
        </n-empty>
      </template>
    </n-card>
  </div>
</template>

<style scoped>
.random-container { max-width: 600px; margin: 16px auto; padding: 0 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.word-list { padding: 0 4px; }
.refresh-hint { text-align: center; margin-top: 8px; }
.refresh-bar { margin-top: 16px; }
</style>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useLearningStore } from '../stores/useLearningStore.js'
import { NButton, NCard, NEmpty, useMessage, NButtonGroup } from 'naive-ui'
import FlashCard from '../components/FlashCard.vue'
import WordListItem from '../components/WordListItem.vue'

/**
 * 学习页面
 * 支持闪卡模式和列表模式切换
 */
const router = useRouter()
const { t } = useI18n()
const store = useLearningStore()
const message = useMessage()

/** 当前模式：'card' | 'list' */
const mode = ref(localStorage.getItem('learnMode') || 'card')

onMounted(() => {
  store.fetchQueue()
})

watch(mode, (val) => {
  localStorage.setItem('learnMode', val)
})

function switchMode(m) {
  mode.value = m
}

async function handleKnown() {
  if (store.currentCard) {
    await store.submitFeedback(store.currentCard.wordId, true)
  }
}

async function handleUnknown() {
  if (store.currentCard) {
    await store.submitFeedback(store.currentCard.wordId, false)
  }
}

async function handleToggleFavorite(wordId) {
  try {
    await store.toggleFavorite(wordId)
  } catch {
    message.error(t('operationFailed'))
  }
}

async function handleToggleMemorized(wordId) {
  try {
    await store.toggleMemorized(wordId)
    message.success(t('markedMemorized'))
  } catch {
    message.error(t('operationFailed'))
  }
}
</script>

<template>
  <div class="learn-container">
    <n-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('dailyNewWords') }}</span>
          <div class="mode-switch">
            <n-button-group size="small">
              <n-button
                :type="mode === 'card' ? 'primary' : 'default'"
                @click="switchMode('card')"
              >{{ t('cardMode') }}</n-button>
              <n-button
                :type="mode === 'list' ? 'primary' : 'default'"
                @click="switchMode('list')"
              >{{ t('listMode') }}</n-button>
            </n-button-group>
          </div>
        </div>
      </template>

      <!-- 闪卡模式 -->
      <template v-if="mode === 'card'">
        <div v-if="store.currentCard">
          <flash-card
            :word="{
              id: store.currentCard.wordId,
              indonesian: store.currentCard.indonesian,
              chinese: store.currentCard.chinese,
              exampleIndonesian: store.currentCard.exampleIndonesian,
              exampleChinese: store.currentCard.exampleChinese
            }"
            :current-index="store.currentIndex + 1"
            :total-count="store.queue.length"
            :is-favorited="store.currentCard.isFavorited"
            :is-memorized="store.currentCard.isMemorized"
            @known="handleKnown"
            @unknown="handleUnknown"
            @toggle-favorite="handleToggleFavorite"
            @toggle-memorized="handleToggleMemorized"
          />
        </div>
        <n-empty v-else :description="t('newWordsComplete')">
          <template #extra>
            <n-button @click="router.push('/')">{{ t('backToHome') }}</n-button>
          </template>
        </n-empty>
      </template>

      <!-- 列表模式 -->
      <template v-else>
        <div v-if="store.queue.length > 0" class="word-list">
          <word-list-item
            v-for="word in store.queue"
            :key="word.wordId"
            :word="word"
            :is-favorited="word.isFavorited"
            :is-memorized="word.isMemorized"
            @toggle-favorite="handleToggleFavorite"
            @toggle-memorized="handleToggleMemorized"
          />
        </div>
        <n-empty v-else :description="t('newWordsComplete')">
          <template #extra>
            <n-button @click="router.push('/')">{{ t('backToHome') }}</n-button>
          </template>
        </n-empty>
      </template>
    </n-card>
  </div>
</template>

<style scoped>
.learn-container {
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

.mode-switch {
  flex-shrink: 0;
}

.word-list {
  padding: 0 4px;
}
</style>

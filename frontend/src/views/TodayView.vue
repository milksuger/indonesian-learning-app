<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NEmpty, NButton } from 'naive-ui'
import WordListItem from '../components/WordListItem.vue'
import request from '../utils/request.js'

/**
 * 今日学习记录页
 * 展示今天首次学习的所有单词，按学习时间排序
 */
const router = useRouter()
const { t } = useI18n()
const words = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await request.get('/users/me/today-words')
    words.value = data
  } finally {
    loading.value = false
  }
})

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
  <div class="today-container">
    <n-card :title="t('todayWords')">
      <template #header-extra>
        <span class="count-hint">{{ words.length }} {{ t('words') }}</span>
      </template>

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

      <n-empty v-else-if="!loading" :description="t('noTodayWords')">
        <template #extra>
          <n-button type="primary" @click="router.push('/learn')">{{ t('startLearning') }}</n-button>
        </template>
      </n-empty>
    </n-card>
  </div>
</template>

<style scoped>
.today-container { max-width: 600px; margin: 16px auto; padding: 0 16px; }
.count-hint { font-size: 13px; color: #9ca3af; }
.word-list { padding: 0 4px; }
</style>

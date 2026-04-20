<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NEmpty, NSpin, NButton } from 'naive-ui'
import WordListItem from '../components/WordListItem.vue'
import request from '../utils/request.js'

/**
 * 词书单词浏览页
 * 分页加载词书中所有单词，使用 IntersectionObserver 实现无限滚动
 */
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const wordbookId = route.params.id
const wordbookName = route.query.name || ''

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
    const { data } = await request.get(`/wordbooks/${wordbookId}/words`, {
      params: { page: page.value, size: PAGE_SIZE }
    })
    words.value.push(...data)
    if (data.length < PAGE_SIZE) {
      hasMore.value = false
    } else {
      page.value++
    }
  } finally {
    loading.value = false
  }
}

let observer = null

function setupObserver() {
  if (!sentinelRef.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) loadMore()
    },
    { threshold: 0.1 }
  )
  observer.observe(sentinelRef.value)
}

onMounted(async () => {
  await loadMore()
  await nextTick()
  setupObserver()
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<template>
  <div class="wb-words-container">
    <n-card>
      <template #header>
        <div class="page-header">
          <n-button text @click="router.back()">← {{ t('back') }}</n-button>
          <span class="title">{{ wordbookName || t('wordbook') }}</span>
        </div>
      </template>

      <div v-if="words.length > 0" class="word-list">
        <word-list-item
          v-for="word in words"
          :key="word.wordId"
          :word="word"
          :show-actions="false"
        />
      </div>

      <n-empty v-else-if="!loading" :description="t('noWords')" />

      <!-- 底部哨兵 + 加载状态 -->
      <div ref="sentinelRef" class="sentinel">
        <n-spin v-if="loading" size="small" />
        <span v-else-if="!hasMore && words.length > 0" class="no-more">
          {{ t('noMoreWords') }}
        </span>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.wb-words-container {
  max-width: 600px;
  margin: 16px auto;
  padding: 0 16px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.word-list {
  padding: 0 4px;
}
.sentinel {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
  min-height: 40px;
}
.no-more {
  font-size: 13px;
  color: #9ca3af;
}
</style>

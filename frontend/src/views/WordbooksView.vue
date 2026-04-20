<script setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useWordbookStore } from '../stores/useWordbookStore.js'
import { NButton, NCard, NEmpty, NList, NListItem, NTag, NSpace } from 'naive-ui'

/**
 * 词书管理页面
 * 展示所有词书与当前激活状态，支持进入单词浏览
 */
const store = useWordbookStore()
const { t } = useI18n()
const router = useRouter()

onMounted(() => {
  store.fetchAll()
  store.fetchActive()
})

function isActive(wordbookId) {
  return store.activeIds.includes(wordbookId)
}

async function toggle(wordbookId) {
  if (isActive(wordbookId)) {
    await store.deactivate(wordbookId)
  } else {
    await store.activate(wordbookId)
  }
}

function browseWords(wb) {
  router.push({ name: 'wordbook-words', params: { id: wb.id }, query: { name: wb.name } })
}
</script>

<template>
  <div class="wordbooks-container">
    <n-card :title="t('wordbookManage')">
      <n-list v-if="store.allWordbooks.length > 0">
        <n-list-item v-for="wb in store.allWordbooks" :key="wb.id">
          <div class="wordbook-item">
            <div class="wordbook-info">
              <div class="wordbook-name">{{ wb.name }}</div>
              <div class="wordbook-meta">
                <n-tag size="small">{{ wb.level }}</n-tag>
                <span>{{ wb.wordCount }} {{ t('words') }}</span>
              </div>
            </div>
            <n-space>
              <n-button size="small" @click="browseWords(wb)">
                {{ t('browseWords') }}
              </n-button>
              <n-button
                size="small"
                :type="isActive(wb.id) ? 'error' : 'primary'"
                @click="toggle(wb.id)"
              >
                {{ isActive(wb.id) ? t('deactivate') : t('activate') }}
              </n-button>
            </n-space>
          </div>
        </n-list-item>
      </n-list>

      <n-empty v-else :description="t('noWordbooks')" />
    </n-card>
  </div>
</template>

<style scoped>
.wordbooks-container {
  max-width: 600px;
  margin: 16px auto;
  padding: 0 16px;
}
.wordbook-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.wordbook-info {
  flex: 1;
  min-width: 0;
}
.wordbook-name {
  font-size: 16px;
  font-weight: 500;
}
.wordbook-meta {
  margin-top: 4px;
  display: flex;
  gap: 8px;
  align-items: center;
  color: #666;
  font-size: 13px;
}
:global(.dark) .wordbook-meta {
  color: #aaa;
}
</style>

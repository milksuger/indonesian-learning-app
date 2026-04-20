<script setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/useUserStore.js'
import { NCard, NEmpty, NList, NListItem } from 'naive-ui'

/**
 * 个人中心页面
 * 展示用户资料、收藏、已掌握与错题本
 */
const userStore = useUserStore()
const { t } = useI18n()

onMounted(() => {
  userStore.fetchProfile()
  userStore.fetchFavorites()
  userStore.fetchMemorized()
  userStore.fetchMistakes()
})
</script>

<template>
  <div class="profile-container">
    <n-card :title="t('profile')">
      <div v-if="userStore.profile" class="profile-info">
        <p><strong>{{ t('email') }}：</strong>{{ userStore.profile.email }}</p>
        <p><strong>{{ t('dailyGoal') }}：</strong>{{ userStore.profile.dailyGoal }}</p>
        <p><strong>{{ t('language') }}：</strong>{{ userStore.profile.uiLanguage }}</p>
        <p><strong>{{ t('theme') }}：</strong>{{ userStore.profile.themeMode }}</p>
        <p><strong>{{ t('streak') }}：</strong>{{ userStore.profile.streak }} {{ t('days') }}</p>
      </div>
    </n-card>

    <n-card :title="t('favorites')" style="margin-top: 16px">
      <n-list v-if="userStore.favorites.length > 0">
        <n-list-item v-for="word in userStore.favorites" :key="word.wordId">
          <div>
            <strong>{{ word.indonesian }}</strong> — {{ word.chinese }}
          </div>
        </n-list-item>
      </n-list>
      <n-empty v-else :description="t('noFavorites')" />
    </n-card>

    <n-card :title="t('memorizedWords')" style="margin-top: 16px">
      <n-list v-if="userStore.memorized.length > 0">
        <n-list-item v-for="word in userStore.memorized" :key="word.wordId">
          <div>
            <strong>{{ word.indonesian }}</strong> — {{ word.chinese }}
          </div>
        </n-list-item>
      </n-list>
      <n-empty v-else :description="t('noMemorized')" />
    </n-card>

    <n-card :title="t('mistakes')" style="margin-top: 16px">
      <n-list v-if="userStore.mistakes.length > 0">
        <n-list-item v-for="word in userStore.mistakes" :key="word.wordId">
          <div>
            <strong>{{ word.indonesian }}</strong> — {{ word.chinese }}
          </div>
        </n-list-item>
      </n-list>
      <n-empty v-else :description="t('noMistakes')" />
    </n-card>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 16px auto;
  padding: 0 16px;
}
.profile-info p {
  margin: 8px 0;
}
</style>

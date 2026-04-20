<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/useUserStore.js'
import { useSpeech } from '../composables/useSpeech.js'
import { getErrorMessage } from '../utils/request.js'
import { NButton, NCard, NForm, NFormItem, NInputNumber, NSelect, useMessage } from 'naive-ui'
import i18n from '../i18n/index.js'

/**
 * 设置页面
 * 修改每日目标、界面语言、主题模式、语音语言
 */
const userStore = useUserStore()
const message = useMessage()
const { t } = useI18n()
const speech = useSpeech()

const dailyGoal = ref(20)
const uiLanguage = ref('zh-CN')
const themeMode = ref('light')
const speechLangLocal = ref(speech.speechLang.value)

const languageOptions = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
  { label: 'Bahasa Indonesia', value: 'id-ID' },
]

const themeOptions = [
  { label: t('light'), value: 'light' },
  { label: t('dark'), value: 'dark' },
]

const speechLangOptions = [
  { label: 'Bahasa Indonesia (id-ID)', value: 'id-ID' },
  { label: '普通话 (zh-CN)', value: 'zh-CN' },
  { label: 'English (en-US)', value: 'en-US' },
]

function applyLanguage(lang) {
  i18n.global.locale = lang
  localStorage.setItem('locale', lang)
}

function applyTheme(mode) {
  window.dispatchEvent(new CustomEvent('theme-change', { detail: mode }))
}

onMounted(async () => {
  await userStore.fetchProfile()
  if (userStore.profile) {
    dailyGoal.value = userStore.profile.dailyGoal
    uiLanguage.value = userStore.profile.uiLanguage
    themeMode.value = userStore.profile.themeMode
    applyLanguage(uiLanguage.value)
    applyTheme(themeMode.value)
  }
})

async function handleSave() {
  try {
    await userStore.updateSettings({
      dailyGoal: dailyGoal.value,
      uiLanguage: uiLanguage.value,
      themeMode: themeMode.value,
    })
    speech.setSpeechLang(speechLangLocal.value)
    applyLanguage(uiLanguage.value)
    applyTheme(themeMode.value)
    message.success(t('saveSuccess'))
  } catch (err) {
    message.error(getErrorMessage(err))
  }
}
</script>

<template>
  <div class="settings-container">
    <n-card :title="t('settings')">
      <n-form>
        <n-form-item :label="t('dailyGoal')">
          <n-input-number v-model:value="dailyGoal" :min="1" :max="100" />
        </n-form-item>
        <n-form-item :label="t('language')">
          <n-select v-model:value="uiLanguage" :options="languageOptions" />
        </n-form-item>
        <n-form-item :label="t('theme')">
          <n-select v-model:value="themeMode" :options="themeOptions" />
        </n-form-item>
        <n-form-item :label="t('speechLanguage')">
          <n-select v-model:value="speechLangLocal" :options="speechLangOptions" />
        </n-form-item>
        <n-button type="primary" block @click="handleSave">{{ t('save') }}</n-button>
      </n-form>
    </n-card>
  </div>
</template>

<style scoped>
.settings-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 16px;
}
</style>

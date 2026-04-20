<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/useAuthStore.js'
import { getErrorMessage } from '../utils/request.js'
import { NButton, NCard, NForm, NFormItem, NInput, NSpace, useMessage } from 'naive-ui'

/**
 * 登录页面
 */
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const message = useMessage()

const email = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) {
    message.warning(t('fillEmailPassword'))
    return
  }
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    message.success(t('loginSuccess'))
    router.push('/')
  } catch (err) {
    message.error(getErrorMessage(err))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <n-card :title="t('loginTitle')" style="max-width: 400px; margin: 80px auto;">
      <n-form>
        <n-form-item :label="t('email')">
          <n-input v-model:value="email" :placeholder="t('emailPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('password')">
          <n-input v-model:value="password" type="password" :placeholder="t('passwordPlaceholder')" />
        </n-form-item>
        <n-space vertical style="width: 100%">
          <n-button type="primary" block :loading="loading" @click="handleLogin">
            {{ t('loginBtn') }}
          </n-button>
          <n-button text block @click="router.push('/register')">
            {{ t('registerLink') }}
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

:global(.dark) .login-container {
  background: #121212;
}
</style>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/useAuthStore.js'
import { getErrorMessage } from '../utils/request.js'
import { NButton, NCard, NForm, NFormItem, NInput, NSpace, useMessage } from 'naive-ui'

/**
 * 注册页面
 */
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const message = useMessage()

const email = ref('')
const password = ref('')
const code = ref('')
const sending = ref(false)
const loading = ref(false)

async function handleSendCode() {
  if (!email.value) {
    message.warning(t('fillEmail'))
    return
  }
  sending.value = true
  try {
    await authStore.sendCode(email.value)
    message.success(t('codeSent'))
  } catch (err) {
    message.error(getErrorMessage(err))
  } finally {
    sending.value = false
  }
}

async function handleRegister() {
  if (!email.value || !password.value || !code.value) {
    message.warning(t('fillAllInfo'))
    return
  }
  loading.value = true
  try {
    await authStore.register(email.value, password.value, code.value)
    message.success(t('registerSuccess'))
    router.push('/login')
  } catch (err) {
    message.error(getErrorMessage(err))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <n-card :title="t('registerTitle')" style="max-width: 400px; margin: 80px auto;">
      <n-form>
        <n-form-item :label="t('email')">
          <n-input v-model:value="email" :placeholder="t('emailPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('password')">
          <n-input v-model:value="password" type="password" :placeholder="t('passwordPlaceholder')" />
        </n-form-item>
        <n-form-item :label="t('verificationCode')">
          <n-input v-model:value="code" :placeholder="t('verificationCode')">
            <template #suffix>
              <n-button text type="primary" :loading="sending" @click="handleSendCode">
                {{ t('sendCode') }}
              </n-button>
            </template>
          </n-input>
        </n-form-item>
        <n-space vertical style="width: 100%">
          <n-button type="primary" block :loading="loading" @click="handleRegister">
            {{ t('register') }}
          </n-button>
          <n-button text block @click="router.push('/login')">
            {{ t('hasAccount') }}
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

:global(.dark) .register-container {
  background: #121212;
}
</style>

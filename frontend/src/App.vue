<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  NMessageProvider, NDialogProvider, NNotificationProvider, NConfigProvider, darkTheme
} from 'naive-ui'
import AppNav from './components/AppNav.vue'

const theme = ref(localStorage.getItem('theme') || 'light')
const naiveTheme = computed(() => theme.value === 'dark' ? darkTheme : null)

function applyTheme(newTheme) {
  theme.value = newTheme
  localStorage.setItem('theme', newTheme)
  if (newTheme === 'dark') {
    document.documentElement.classList.add('dark')
    document.body.style.background = '#121212'
    document.body.style.color = '#e0e0e0'
  } else {
    document.documentElement.classList.remove('dark')
    document.body.style.background = '#f5f5f5'
    document.body.style.color = '#333'
  }
}

onMounted(() => {
  applyTheme(theme.value)
  window.addEventListener('theme-change', (e) => {
    applyTheme(e.detail)
  })
})
</script>

<template>
  <n-config-provider :theme="naiveTheme">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <div id="app">
            <!-- 全局导航栏 -->
            <app-nav />
            <!-- 页面内容 -->
            <div class="app-content">
              <router-view />
            </div>
          </div>
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style>
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f5f5;
  margin: 0;
}

/* PC 端：为顶部导航留空 */
@media (min-width: 769px) {
  .app-content {
    padding-top: 56px;
  }
}

/* 移动端：为底部导航留空 */
@media (max-width: 768px) {
  .app-content {
    padding-bottom: 56px;
  }
}
</style>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NIcon } from 'naive-ui'
import {
  HomeOutline,
  BookOutline,
  RefreshOutline,
  LibraryOutline,
  PersonOutline,
} from '@vicons/ionicons5'

/**
 * 全局响应式导航组件
 * PC 端显示顶部导航栏，移动端显示底部图标导航栏
 * 登录/注册页面不渲染
 */
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const isMobile = computed(() => window.innerWidth <= 768)
const isPublicPage = computed(() => {
  return ['/login', '/register'].includes(route.path)
})

const navItems = [
  { name: 'home', label: () => t('home'), path: '/', icon: HomeOutline },
  { name: 'learn', label: () => t('learn'), path: '/learn', icon: BookOutline },
  { name: 'review', label: () => t('review'), path: '/review', icon: RefreshOutline },
  { name: 'wordbooks', label: () => t('wordbooks'), path: '/wordbooks', icon: LibraryOutline },
  { name: 'profile', label: () => t('profile'), path: '/profile', icon: PersonOutline },
]

function isActive(path) {
  return route.path === path
}

function navigate(path) {
  router.push(path)
}
</script>

<template>
  <!-- 登录/注册页不显示导航 -->
  <template v-if="!isPublicPage">
    <!-- PC 端顶部导航 -->
    <nav v-if="!isMobile" class="top-nav">
      <div class="nav-inner">
        <div class="nav-brand" @click="navigate('/')">{{ t('appName') }}</div>
        <div class="nav-links">
          <div
            v-for="item in navItems"
            :key="item.name"
            class="nav-link"
            :class="{ active: isActive(item.path) }"
            @click="navigate(item.path)"
          >
            <n-icon :size="18" :component="item.icon" />
            <span>{{ item.label() }}</span>
          </div>
        </div>
      </div>
    </nav>

    <!-- 移动端底部导航 -->
    <nav v-else class="bottom-nav">
      <div
        v-for="item in navItems"
        :key="item.name"
        class="bottom-nav-item"
        :class="{ active: isActive(item.path) }"
        @click="navigate(item.path)"
      >
        <n-icon :size="22" :component="item.icon" />
        <span class="bottom-nav-label">{{ item.label() }}</span>
      </div>
    </nav>
  </template>
</template>

<style scoped>
/* ===== PC 顶部导航 ===== */
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  z-index: 1000;
  display: flex;
  align-items: center;
}

:global(.dark) .top-nav {
  background: #1e1e1e;
  border-bottom-color: #444;
}

.nav-inner {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  font-size: 18px;
  font-weight: 600;
  color: #18a058;
  cursor: pointer;
  user-select: none;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  user-select: none;
}

:global(.dark) .nav-link {
  color: #aaa;
}

.nav-link:hover {
  background: #f5f5f5;
  color: #333;
}

:global(.dark) .nav-link:hover {
  background: #333;
  color: #e0e0e0;
}

.nav-link.active {
  background: #e8f5e9;
  color: #18a058;
  font-weight: 500;
}

:global(.dark) .nav-link.active {
  background: #1a3a24;
  color: #4ade80;
}

/* ===== 移动端底部导航 ===== */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #e0e0e0;
  z-index: 1000;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

:global(.dark) .bottom-nav {
  background: #1e1e1e;
  border-top-color: #444;
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
  user-select: none;
}

:global(.dark) .bottom-nav-item {
  color: #888;
}

.bottom-nav-item.active {
  color: #18a058;
}

:global(.dark) .bottom-nav-item.active {
  color: #4ade80;
}

.bottom-nav-label {
  font-size: 11px;
}
</style>

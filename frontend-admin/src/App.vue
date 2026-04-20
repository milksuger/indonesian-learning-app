<script setup>
import { ref, computed, h } from 'vue'
import {
  NMessageProvider, NLayout, NLayoutSider, NLayoutContent,
  NMenu, NIcon
} from 'naive-ui'
import {
  CloudUploadOutline,
  BookOutline,
  PeopleOutline,
  GridOutline,
  SearchOutline
} from '@vicons/ionicons5'
import ImportPage from './views/ImportPage.vue'
import WordbookPage from './views/WordbookPage.vue'
import UserPage from './views/UserPage.vue'
import WordSearchPage from './views/WordSearchPage.vue'

/**
 * 管理后台根组件
 * 左侧导航 + 右侧内容区
 */
const currentKey = ref('import')

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  {
    label: '批量导入',
    key: 'import',
    icon: renderIcon(CloudUploadOutline)
  },
  {
    label: '词书管理',
    key: 'wordbooks',
    icon: renderIcon(BookOutline)
  },
  {
    label: '单词搜索',
    key: 'wordsearch',
    icon: renderIcon(SearchOutline)
  },
  {
    label: '用户管理',
    key: 'users',
    icon: renderIcon(PeopleOutline)
  }
]

const currentComponent = computed(() => {
  if (currentKey.value === 'import') return ImportPage
  if (currentKey.value === 'wordbooks') return WordbookPage
  if (currentKey.value === 'wordsearch') return WordSearchPage
  if (currentKey.value === 'users') return UserPage
  return ImportPage
})
</script>

<template>
  <n-message-provider>
    <n-layout has-sider style="height: 100vh">
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="64"
        :width="180"
        show-trigger
      >
        <div class="logo">
          <n-icon size="20" style="margin-right: 8px">
            <GridOutline />
          </n-icon>
          <span class="logo-text">管理后台</span>
        </div>
        <n-menu
          v-model:value="currentKey"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="menuOptions"
        />
      </n-layout-sider>

      <n-layout-content content-style="overflow-y: auto; height: 100vh;">
        <component :is="currentComponent" />
      </n-layout-content>
    </n-layout>
  </n-message-provider>
</template>

<style>
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f5f5;
  margin: 0;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #eee;
  padding: 0 16px;
  overflow: hidden;
  white-space: nowrap;
}

.logo-text {
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

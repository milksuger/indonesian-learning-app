<script setup>
/**
 * 单词列表行组件
 * 展示单词基本信息，支持展开例句、收藏、已掌握操作
 */
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NIcon } from 'naive-ui'
import { ChevronDownOutline, ChevronUpOutline, StarOutline, Star, CheckmarkCircleOutline } from '@vicons/ionicons5'

const { t } = useI18n()

const props = defineProps({
  word: { type: Object, required: true },
  isFavorited: { type: Boolean, default: false },
  isMemorized: { type: Boolean, default: false },
  /** 是否显示操作按钮（列表浏览模式下可隐藏） */
  showActions: { type: Boolean, default: true }
})

const emit = defineEmits(['toggleFavorite', 'toggleMemorized'])

const expanded = ref(false)

function toggleExpand() {
  expanded.value = !expanded.value
}
</script>

<template>
  <div class="word-list-item" :class="{ expanded }">
    <!-- 主行：单词 + 翻译 + 操作 -->
    <div class="item-main" @click="toggleExpand">
      <div class="item-content">
        <span class="indonesian">{{ word.indonesian }}</span>
        <span class="chinese">{{ word.chinese }}</span>
      </div>
      <div class="item-actions" @click.stop>
        <template v-if="showActions">
          <button
            class="action-btn"
            :class="{ active: isFavorited }"
            @click="emit('toggleFavorite', word.wordId)"
            :title="t('favorite')"
          >
            <n-icon size="16" :component="isFavorited ? Star : StarOutline" />
          </button>
          <button
            class="action-btn"
            :class="{ active: isMemorized }"
            @click="emit('toggleMemorized', word.wordId)"
            :title="t('memorized')"
          >
            <n-icon size="16" :component="CheckmarkCircleOutline" />
          </button>
        </template>
        <button class="action-btn expand-btn" @click.stop="toggleExpand">
          <n-icon size="16" :component="expanded ? ChevronUpOutline : ChevronDownOutline" />
        </button>
      </div>
    </div>

    <!-- 展开区：例句 -->
    <div v-if="expanded" class="item-detail">
      <div v-if="word.english" class="detail-row">
        <span class="detail-label">EN</span>
        <span class="detail-text">{{ word.english }}</span>
      </div>
      <div v-if="word.exampleIndonesian" class="detail-row example">
        <span class="detail-label">例</span>
        <div class="detail-text">
          <div>{{ word.exampleIndonesian }}</div>
          <div class="example-zh">{{ word.exampleChinese }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.word-list-item {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}

:global(.dark) .word-list-item {
  border-bottom-color: #333;
}

.word-list-item:last-child {
  border-bottom: none;
}

.item-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 4px;
  cursor: pointer;
  user-select: none;
}

.item-main:hover {
  background: #f9f9f9;
}

:global(.dark) .item-main:hover {
  background: #2a2a2a;
}

.item-content {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.indonesian {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

:global(.dark) .indonesian {
  color: #e0e0e0;
}

.chinese {
  font-size: 14px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.dark) .chinese {
  color: #9ca3af;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  transition: color 0.15s, background 0.15s;
}

.action-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

:global(.dark) .action-btn {
  color: #6b7280;
}

:global(.dark) .action-btn:hover {
  background: #374151;
  color: #d1d5db;
}

.action-btn.active {
  color: #f59e0b;
}

.action-btn.active:hover {
  color: #d97706;
}

.expand-btn {
  color: #9ca3af;
}

.item-detail {
  padding: 0 4px 12px 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.detail-label {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  background: #f3f4f6;
  border-radius: 4px;
  padding: 1px 5px;
  flex-shrink: 0;
  margin-top: 2px;
}

:global(.dark) .detail-label {
  background: #374151;
  color: #9ca3af;
}

.detail-text {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

:global(.dark) .detail-text {
  color: #9ca3af;
}

.example-zh {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 2px;
}
</style>

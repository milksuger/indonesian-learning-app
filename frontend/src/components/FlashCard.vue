<script setup>
/**
 * FlashCard 闪卡组件
 * 展示印尼语单词，支持点击翻转、认识/不认识反馈、收藏与已掌握标记、语音播放
 */
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSpeech } from '../composables/useSpeech.js'
import { NModal, NSwitch, NSlider, NButton } from 'naive-ui'
import { VolumeHighOutline, SettingsOutline, VolumeMuteOutline } from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

const { t } = useI18n()
const speech = useSpeech()

const props = defineProps({
  word: {
    type: Object,
    required: true
  },
  currentIndex: {
    type: Number,
    default: 1
  },
  totalCount: {
    type: Number,
    default: 1
  },
  isFavorited: {
    type: Boolean,
    default: false
  },
  isMemorized: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['flip', 'known', 'unknown', 'toggleFavorite', 'toggleMemorized'])

const isFlipped = ref(false)
const showSettings = ref(false)

// 自动发音：卡片切换时播放单词
watch(() => props.word.id, (newId) => {
  if (newId && speech.autoSpeak.value) {
    isFlipped.value = false
    speech.speak(props.word.indonesian)
  }
}, { immediate: true })

function handleFlip() {
  isFlipped.value = !isFlipped.value
  emit('flip')
  // 翻转到背面时自动播放例句
  if (isFlipped.value && speech.autoSpeak.value && props.word.exampleIndonesian) {
    speech.speak(props.word.exampleIndonesian)
  }
}

function handleKnown() {
  emit('known', props.word.id)
}

function handleUnknown() {
  emit('unknown', props.word.id)
}

function handleToggleFavorite() {
  emit('toggleFavorite', props.word.id)
}

function handleToggleMemorized() {
  emit('toggleMemorized', props.word.id)
}

function playWord() {
  speech.speak(props.word.indonesian)
}

function playExample() {
  if (props.word.exampleIndonesian) {
    speech.speak(props.word.exampleIndonesian)
  }
}
</script>

<template>
  <div class="flash-card-container">
    <!-- 顶部工具栏：进度 + 设置 -->
    <div class="top-bar">
      <span class="progress-text">{{ currentIndex }} / {{ totalCount }}</span>
      <div class="top-actions">
        <button
          class="icon-btn"
          :class="{ speaking: speech.isSpeaking.value }"
          @click.stop="showSettings = true"
        >
          <n-icon size="18" :component="SettingsOutline" />
        </button>
      </div>
    </div>

    <!-- 操作按钮栏 -->
    <div class="action-bar">
      <button
        data-testid="favorite-btn"
        class="icon-btn"
        :class="{ favorited: isFavorited }"
        @click.stop="handleToggleFavorite"
      >
        <span v-if="isFavorited">★</span>
        <span v-else>☆</span>
      </button>
      <button
        data-testid="memorized-btn"
        class="icon-btn"
        :class="{ memorized: isMemorized }"
        @click.stop="handleToggleMemorized"
      >
        <span>✓</span>
      </button>
    </div>

    <!-- 闪卡主体 -->
    <div
      class="flash-card"
      :class="{ flipped: isFlipped }"
      @click="handleFlip"
    >
      <!-- 正面 -->
      <div class="card-face card-front">
        <div class="word-header">
          <h2 class="indonesian-word">{{ word.indonesian }}</h2>
          <button class="play-btn" @click.stop="playWord">
            <n-icon size="20" :component="speech.isSpeaking.value ? VolumeMuteOutline : VolumeHighOutline" />
          </button>
        </div>
        <p class="hint">{{ t('tapToFlip') }}</p>
      </div>

      <!-- 背面 -->
      <div v-if="isFlipped" class="card-face card-back">
        <h3 class="translation">{{ word.chinese }}</h3>
        <div class="example-section" v-if="word.exampleIndonesian">
          <div class="example-header">
            <p class="example-indonesian">{{ word.exampleIndonesian }}</p>
            <button class="play-btn small" @click.stop="playExample">
              <n-icon size="16" :component="speech.isSpeaking.value ? VolumeMuteOutline : VolumeHighOutline" />
            </button>
          </div>
          <p class="example-chinese">{{ word.exampleChinese }}</p>
        </div>
      </div>
    </div>

    <!-- 反馈按钮 -->
    <div class="feedback-buttons">
      <button data-testid="unknown-btn" class="feedback-btn unknown" @click="handleUnknown">
        {{ t('unknown') }}
      </button>
      <button data-testid="known-btn" class="feedback-btn known" @click="handleKnown">
        {{ t('known') }}
      </button>
    </div>

    <!-- 语音设置弹窗 -->
    <n-modal v-model:show="showSettings" preset="card" style="width: 360px" :title="t('speechSettings')">
      <div class="settings-content">
        <div class="setting-item">
          <span>{{ t('autoSpeak') }}</span>
          <n-switch :value="speech.autoSpeak.value" @update:value="speech.toggleAutoSpeak()" />
        </div>
        <div class="setting-item vertical">
          <span>{{ t('speechRate') }}: {{ speech.speechRate.value }}x</span>
          <n-slider
            :value="speech.speechRate.value"
            :min="0.5"
            :max="1.5"
            :step="0.1"
            @update:value="speech.setSpeechRate"
          />
        </div>
        <div class="setting-item">
          <n-button size="small" @click="playWord">
            {{ t('testSpeech') }}
          </n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.flash-card-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.top-bar {
  width: 100%;
  max-width: 300px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-text {
  font-size: 14px;
  color: #666;
}

:global(.dark) .progress-text {
  color: #aaa;
}

.top-actions {
  display: flex;
  gap: 8px;
}

.action-bar {
  display: flex;
  gap: 12px;
}

.icon-btn {
  background: none;
  border: 1px solid #ddd;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

:global(.dark) .icon-btn {
  border-color: #555;
  color: #ccc;
  background: #2a2a2a;
}

.icon-btn.favorited {
  color: #f59e0b;
  border-color: #f59e0b;
}

:global(.dark) .icon-btn.favorited {
  background: #2a2a2a;
}

.icon-btn.memorized {
  color: #10b981;
  border-color: #10b981;
}

:global(.dark) .icon-btn.memorized {
  background: #2a2a2a;
}

.icon-btn.speaking {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.flash-card {
  width: 300px;
  height: 200px;
  perspective: 1000px;
  cursor: pointer;
  position: relative;
}

.card-face {
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  transition: transform 0.4s ease;
  position: absolute;
  top: 0;
  left: 0;
}

:global(.dark) .card-face {
  background: #1e1e1e;
  border-color: #444;
}

.card-back {
  transform: rotateY(180deg);
}

.flipped .card-front {
  transform: rotateY(180deg);
}

.flipped .card-back {
  transform: rotateY(0deg);
}

.word-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.indonesian-word {
  font-size: 32px;
  margin: 0;
  color: #1f2937;
}

:global(.dark) .indonesian-word {
  color: #e0e0e0;
}

.play-btn {
  background: none;
  border: 1px solid #ddd;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

:global(.dark) .play-btn {
  border-color: #555;
  color: #ccc;
}

.play-btn:hover {
  background: #f0f0f0;
  color: #18a058;
}

:global(.dark) .play-btn:hover {
  background: #333;
  color: #4ade80;
}

.play-btn.small {
  width: 28px;
  height: 28px;
}

.hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

:global(.dark) .hint {
  color: #888;
}

.translation {
  font-size: 24px;
  margin: 0 0 12px;
  color: #1f2937;
}

:global(.dark) .translation {
  color: #e0e0e0;
}

.example-section {
  text-align: center;
  padding: 0 16px;
}

.example-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.example-indonesian {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
}

:global(.dark) .example-indonesian {
  color: #aaa;
}

.example-chinese {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

:global(.dark) .example-chinese {
  color: #888;
}

.feedback-buttons {
  display: flex;
  gap: 16px;
}

.feedback-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.feedback-btn.unknown {
  background: #fee2e2;
  color: #dc2626;
}

:global(.dark) .feedback-btn.unknown {
  background: #3a1f1f;
  color: #ff7b7b;
}

.feedback-btn.known {
  background: #d1fae5;
  color: #059669;
}

:global(.dark) .feedback-btn.known {
  background: #1f3a2a;
  color: #5eeaad;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-item.vertical {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}
</style>

import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FlashCard from './FlashCard.vue'

/**
 * FlashCard 组件单元测试
 * 验证闪卡翻转、按钮事件与状态展示
 */

describe('FlashCard', () => {
  // 测试数据：一个印尼语单词
  const mockWord = {
    id: 1,
    indonesian: 'Apel',
    chinese: '苹果',
    english: 'Apple',
    exampleIndonesian: 'Saya suka makan apel.',
    exampleChinese: '我喜欢吃苹果。',
    exampleEnglish: 'I like to eat apples.'
  }

  // Feature: indonesian-learning-app, Property 45: 组件应渲染单词正面（印尼语）
  it('renders Indonesian word on front side', () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20 }
    })
    expect(wrapper.text()).toContain('Apel')
    expect(wrapper.text()).not.toContain('苹果')
  })

  // Feature: indonesian-learning-app, Property 46: 点击卡片后应翻转显示背面（中文释义与例句）
  it('flips to show back side when clicked', async () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20 }
    })
    await wrapper.find('.flash-card').trigger('click')
    expect(wrapper.text()).toContain('苹果')
    expect(wrapper.text()).toContain('我喜欢吃苹果')
    expect(wrapper.emitted()).toHaveProperty('flip')
  })

  // Feature: indonesian-learning-app, Property 47: 点击"认识"按钮应触发 known 事件
  it('emits known event when known button clicked', async () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20 }
    })
    await wrapper.find('[data-testid="known-btn"]').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('known')
    expect(wrapper.emitted('known')[0]).toEqual([1])
  })

  // Feature: indonesian-learning-app, Property 48: 点击"不认识"按钮应触发 unknown 事件
  it('emits unknown event when unknown button clicked', async () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20 }
    })
    await wrapper.find('[data-testid="unknown-btn"]').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('unknown')
    expect(wrapper.emitted('unknown')[0]).toEqual([1])
  })

  // Feature: indonesian-learning-app, Property 49: 点击收藏图标应触发 toggleFavorite 事件
  it('emits toggleFavorite event when favorite icon clicked', async () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20, isFavorited: false }
    })
    await wrapper.find('[data-testid="favorite-btn"]').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('toggleFavorite')
    expect(wrapper.emitted('toggleFavorite')[0]).toEqual([1])
  })

  // Feature: indonesian-learning-app, Property 50: 点击已掌握图标应触发 toggleMemorized 事件
  it('emits toggleMemorized event when memorized icon clicked', async () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20, isMemorized: false }
    })
    await wrapper.find('[data-testid="memorized-btn"]').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('toggleMemorized')
    expect(wrapper.emitted('toggleMemorized')[0]).toEqual([1])
  })

  // Feature: indonesian-learning-app, Property 51: 应正确显示当前进度（如 "1 / 20"）
  it('displays current progress correctly', () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 5, totalCount: 20 }
    })
    expect(wrapper.text()).toContain('5 / 20')
  })

  // Feature: indonesian-learning-app, Property 52: 已收藏状态应正确显示
  it('shows favorited state when isFavorited is true', () => {
    const wrapper = mount(FlashCard, {
      props: { word: mockWord, currentIndex: 1, totalCount: 20, isFavorited: true }
    })
    const favBtn = wrapper.find('[data-testid="favorite-btn"]')
    expect(favBtn.classes()).toContain('favorited')
  })
})

import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request.js'

/**
 * 用户状态管理
 * 封装用户资料、设置、收藏、错题本与签到日历
 */
export const useUserStore = defineStore('user', () => {
  const profile = ref(null)
  const favorites = ref([])
  const mistakes = ref([])
  const memorized = ref([])
  const calendar = ref({ year: 0, month: 0, checkinDates: [], streak: 0 })

  async function fetchProfile() {
    const { data } = await request.get('/users/me')
    profile.value = data
  }

  async function updateSettings(settings) {
    await request.put('/users/me/settings', settings)
    await fetchProfile()
  }

  async function fetchCalendar(year, month) {
    const { data } = await request.get('/users/me/checkin-calendar', { params: { year, month } })
    calendar.value = data
  }

  async function toggleFavorite(wordId) {
    await request.post(`/users/me/favorites/${wordId}`)
    await fetchFavorites()
  }

  async function fetchFavorites() {
    const { data } = await request.get('/users/me/favorites')
    favorites.value = data
  }

  async function fetchMistakes() {
    const { data } = await request.get('/users/me/mistakes')
    mistakes.value = data
  }

  async function fetchMemorized() {
    const { data } = await request.get('/users/me/memorized')
    memorized.value = data
  }

  async function markMemorized(wordId) {
    await request.post(`/users/me/memorized/${wordId}`)
  }

  return {
    profile, favorites, mistakes, memorized, calendar,
    fetchProfile, updateSettings, fetchCalendar,
    toggleFavorite, fetchFavorites, fetchMistakes, fetchMemorized, markMemorized,
  }
})

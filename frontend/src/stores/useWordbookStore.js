import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request.js'

/**
 * 词书状态管理
 * 封装词书列表浏览与激活状态
 */
export const useWordbookStore = defineStore('wordbook', () => {
  const allWordbooks = ref([])
  const activeIds = ref([])

  async function fetchAll() {
    const { data } = await request.get('/wordbooks')
    allWordbooks.value = data
  }

  async function fetchActive() {
    const { data } = await request.get('/wordbooks/active')
    activeIds.value = data
  }

  async function activate(wordbookId) {
    await request.post(`/wordbooks/${wordbookId}/activate`)
    await fetchActive()
  }

  async function deactivate(wordbookId) {
    await request.post(`/wordbooks/${wordbookId}/deactivate`)
    await fetchActive()
  }

  return {
    allWordbooks, activeIds,
    fetchAll, fetchActive, activate, deactivate,
  }
})

import { ref } from 'vue'

const AUTO_SPEAK_KEY = 'autoSpeak'
const SPEECH_RATE_KEY = 'speechRate'
const SPEECH_LANG_KEY = 'speechLang'

const autoSpeak = ref(localStorage.getItem(AUTO_SPEAK_KEY) !== 'false')
const speechRate = ref(parseFloat(localStorage.getItem(SPEECH_RATE_KEY)) || 0.9)
const speechLang = ref(localStorage.getItem(SPEECH_LANG_KEY) || 'id-ID')
const isSpeaking = ref(false)

let voiceCache = {}
let initialized = false

function initVoices() {
  if (initialized || !('speechSynthesis' in window)) return
  initialized = true

  function loadVoices() {
    const voices = window.speechSynthesis.getVoices()
    voiceCache = {
      'id-ID': voices.find(v => v.lang === 'id-ID') ||
               voices.find(v => v.lang.startsWith('id')) ||
               voices.find(v => v.lang === 'ms-MY') || null,
      'zh-CN': voices.find(v => v.lang === 'zh-CN') ||
               voices.find(v => v.lang.startsWith('zh')) || null,
      'en-US': voices.find(v => v.lang === 'en-US') ||
               voices.find(v => v.lang.startsWith('en')) || null,
    }
  }

  loadVoices()
  window.speechSynthesis.onvoiceschanged = loadVoices
}

function speak(text, lang) {
  if (!('speechSynthesis' in window)) return Promise.reject(new Error('不支持语音合成'))

  initVoices()
  window.speechSynthesis.cancel()

  const useLang = lang || speechLang.value
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = useLang
  utterance.rate = speechRate.value
  utterance.pitch = 1

  const voice = voiceCache[useLang]
  if (voice) utterance.voice = voice

  return new Promise((resolve, reject) => {
    isSpeaking.value = true
    utterance.onend = () => { isSpeaking.value = false; resolve() }
    utterance.onerror = (e) => {
      isSpeaking.value = false
      if (e.error !== 'canceled' && e.error !== 'interrupted') reject(new Error('语音播放失败'))
      else resolve()
    }
    window.speechSynthesis.speak(utterance)
  })
}

function stop() {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
  }
}

function toggleAutoSpeak(value) {
  autoSpeak.value = typeof value === 'boolean' ? value : !autoSpeak.value
  localStorage.setItem(AUTO_SPEAK_KEY, String(autoSpeak.value))
}

function setSpeechRate(rate) {
  speechRate.value = rate
  localStorage.setItem(SPEECH_RATE_KEY, String(rate))
}

function setSpeechLang(lang) {
  speechLang.value = lang
  localStorage.setItem(SPEECH_LANG_KEY, lang)
}

export function useSpeech() {
  initVoices()
  return { autoSpeak, speechRate, speechLang, isSpeaking, speak, stop, toggleAutoSpeak, setSpeechRate, setSpeechLang }
}

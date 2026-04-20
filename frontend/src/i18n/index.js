import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN.json'
import enUS from './locales/en-US.json'
import idID from './locales/id-ID.json'

/**
 * 国际化配置
 * 支持简体中文、英文、印尼语切换
 */
const i18n = createI18n({
  locale: localStorage.getItem('locale') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
    'id-ID': idID,
  },
})

export default i18n

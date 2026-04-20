import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

/**
 * Vite 配置文件
 * 配置 Vue 插件、PWA、Vitest 测试环境与开发服务器 API 代理
 */
export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: '印尼语学习',
        short_name: '印尼语',
        description: '面向中文用户的印尼语词汇学习应用',
        theme_color: '#18a058',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: '/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        // 缓存静态资源
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        // API 请求不缓存
        navigateFallbackDenylist: [/^\/api/]
      }
    })
  ],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.test.js']
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('代理请求错误', err.message)
          })
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('代理请求:', req.method, req.url, '->', options.target + proxyReq.path)
          })
        }
      }
    }
  }
})

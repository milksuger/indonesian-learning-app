<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  NButton, NCard, NInput, NSpace, NUpload, NList, NListItem, NTag,
  NCollapse, NCollapseItem, useMessage
} from 'naive-ui'

const message = useMessage()
const jsonText = ref('')
const importing = ref(false)
const result = ref(null)
const wordbooks = ref([])
const wordbookDetails = ref({})
const loadingWordbooks = ref(false)

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

/* ===== 导入 ===== */
const sampleJson = JSON.stringify({
  name: '基础印尼语',
  nameEn: 'Basic Indonesian',
  level: 'A1',
  words: [
    { indonesian: 'apa', chinese: '什么', english: 'what', exampleIndonesian: 'Apa ini?', exampleZh: '这是什么？', exampleEn: 'What is this?' },
    { indonesian: 'saya', chinese: '我', english: 'I', exampleIndonesian: 'Saya suka Indonesia.', exampleZh: '我喜欢印尼。', exampleEn: 'I like Indonesia.' },
    { indonesian: 'kamu', chinese: '你', english: 'you', exampleIndonesian: 'Kamu siapa?', exampleZh: '你是谁？', exampleEn: 'Who are you?' },
    { indonesian: 'dia', chinese: '他/她', english: 'he/she', exampleIndonesian: 'Dia guru.', exampleZh: '他/她是老师。', exampleEn: 'He/She is a teacher.' },
    { indonesian: 'ini', chinese: '这个', english: 'this', exampleIndonesian: 'Ini buku.', exampleZh: '这是书。', exampleEn: 'This is a book.' }
  ]
}, null, 2)

function fillSample() {
  jsonText.value = sampleJson
}

async function handleImport() {
  if (!jsonText.value.trim()) {
    message.warning('请先粘贴 JSON 数据')
    return
  }

  let blob
  try {
    const data = JSON.parse(jsonText.value)
    blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
  } catch (e) {
    message.error('JSON 格式错误：' + e.message)
    return
  }

  importing.value = true
  result.value = null

  try {
    const formData = new FormData()
    formData.append('file', blob, 'words.json')

    const { data } = await api.post('/admin/import', formData)

    result.value = data
    if (data.success) {
      message.success(`成功导入 ${data.imported} 个单词`)
      await fetchWordbooks()
    }
  } catch (err) {
    const msg = err.response?.data?.error || err.message || '导入失败'
    message.error(msg)
    result.value = { error: msg }
  } finally {
    importing.value = false
  }
}

function handleFileUpload({ file }) {
  const reader = new FileReader()
  reader.onload = (e) => {
    jsonText.value = e.target.result
    message.success('文件已读取，请点击导入')
  }
  reader.onerror = () => message.error('文件读取失败')
  reader.readAsText(file.file)
}

/* ===== 词书列表 ===== */
async function fetchWordbooks() {
  loadingWordbooks.value = true
  try {
    const { data } = await api.get('/admin/wordbooks')
    wordbooks.value = data
  } catch (err) {
    message.error('获取词书列表失败')
  } finally {
    loadingWordbooks.value = false
  }
}

async function viewWordbook(id) {
  if (wordbookDetails.value[id]) {
    // 已加载则切换显示状态
    return
  }
  try {
    const { data } = await api.get(`/admin/wordbooks/${id}/export`)
    wordbookDetails.value[id] = data
  } catch (err) {
    message.error('获取单词详情失败')
  }
}

function exportWordbook(id, name) {
  const data = wordbookDetails.value[id]
  if (!data) {
    message.warning('请先点击“查看”加载数据，再导出')
    return
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${name}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  message.success(`已导出：${name}.json`)
}

onMounted(() => {
  fetchWordbooks()
})
</script>

<template>
  <div class="admin-wrapper">
    <!-- 导入区域 -->
    <n-card title="批量导入词书" class="admin-card" style="margin-bottom: 24px">
      <n-space vertical size="large">
        <div class="intro">
          <p>在此批量导入词书与单词数据。JSON 格式如下：</p>
          <pre>{
  "name": "词书名称",
  "nameEn": "英文名称",
  "level": "A1",
  "words": [
    {
      "indonesian": "印尼语单词",
      "chinese": "中文释义",
      "english": "英文释义",
      "exampleIndonesian": "印尼语例句",
      "exampleZh": "中文翻译",
      "exampleEn": "英文翻译"
    }
  ]
}</pre>
        </div>

        <n-button text type="primary" @click="fillSample">
          填充示例 JSON
        </n-button>

        <n-input
          v-model:value="jsonText"
          type="textarea"
          placeholder="在此粘贴 JSON 数据..."
          :rows="16"
        />

        <n-upload
          accept=".json"
          :show-file-list="false"
          @change="handleFileUpload"
        >
          <n-button>或选择 JSON 文件上传</n-button>
        </n-upload>

        <n-button
          type="primary"
          size="large"
          block
          :loading="importing"
          @click="handleImport"
        >
          开始导入
        </n-button>

        <div v-if="result" class="result-box">
          <p v-if="result.success" class="success-text">
            导入成功！共导入 {{ result.imported }} 个单词
          </p>
          <p v-if="result.error" class="error-text">
            导入失败：{{ result.error }}
          </p>
        </div>
      </n-space>
    </n-card>

    <!-- 词书管理区域 -->
    <n-card title="词书列表与导出" class="admin-card">
      <n-list v-if="wordbooks.length > 0">
        <n-list-item v-for="wb in wordbooks" :key="wb.id">
          <n-collapse>
            <n-collapse-item :name="String(wb.id)">
              <template #header>
                <div class="wordbook-header">
                  <span class="wordbook-name">{{ wb.name }}</span>
                  <n-tag size="small" type="info">{{ wb.level }}</n-tag>
                  <span class="wordbook-count">{{ wb.wordCount }} 词</span>
                </div>
              </template>

              <n-space style="margin-bottom: 12px">
                <n-button size="small" @click="viewWordbook(wb.id)">
                  查看单词
                </n-button>
                <n-button size="small" type="primary" @click="exportWordbook(wb.id, wb.name)">
                  导出 JSON
                </n-button>
              </n-space>

              <div v-if="wordbookDetails[wb.id]" class="word-list">
                <table class="word-table">
                  <thead>
                    <tr>
                      <th>印尼语</th>
                      <th>中文</th>
                      <th>英文</th>
                      <th>例句</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(w, idx) in wordbookDetails[wb.id].words" :key="idx">
                      <td><strong>{{ w.indonesian }}</strong></td>
                      <td>{{ w.chinese }}</td>
                      <td>{{ w.english || '-' }}</td>
                      <td class="example-cell">
                        <div v-if="w.exampleIndonesian">{{ w.exampleIndonesian }}</div>
                        <div v-if="w.exampleZh" class="example-zh">{{ w.exampleZh }}</div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="word-list-placeholder">
                点击“查看单词”加载数据
              </div>
            </n-collapse-item>
          </n-collapse>
        </n-list-item>
      </n-list>

      <div v-else class="empty-text">
        暂无词书，请先导入数据
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.admin-wrapper {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 16px;
}

.admin-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.intro {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 8px;
}

.intro p {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.intro pre {
  font-size: 12px;
  color: #333;
  overflow-x: auto;
}

.result-box {
  padding: 16px;
  border-radius: 8px;
  background: #f8f8f8;
}

.success-text {
  color: #18a058;
  font-weight: 500;
  font-size: 16px;
}

.error-text {
  color: #d03050;
  font-weight: 500;
}

.wordbook-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.wordbook-name {
  font-size: 16px;
  font-weight: 500;
}

.wordbook-count {
  color: #999;
  font-size: 13px;
}

.word-list {
  overflow-x: auto;
}

.word-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.word-table th,
.word-table td {
  border: 1px solid #e8e8e8;
  padding: 10px 12px;
  text-align: left;
}

.word-table th {
  background: #f5f5f5;
  font-weight: 500;
}

.word-table tr:nth-child(even) {
  background: #fafafa;
}

.example-cell {
  max-width: 300px;
}

.example-zh {
  color: #666;
  font-size: 13px;
  margin-top: 4px;
}

.word-list-placeholder {
  padding: 16px;
  color: #999;
  text-align: center;
  background: #fafafa;
  border-radius: 6px;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>

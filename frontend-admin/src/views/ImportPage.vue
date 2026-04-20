<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  NButton, NCard, NInput, NSpace, NUpload, NRadio, NRadioGroup,
  NSelect, useMessage
} from 'naive-ui'

const message = useMessage()
const jsonText = ref('')
const importing = ref(false)
const result = ref(null)
const wordbooks = ref([])
const loadingWordbooks = ref(false)

const targetType = ref('new')
const selectedWordbookId = ref(null)

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

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

const sampleExistingJson = JSON.stringify({
  words: [
    { indonesian: 'apa', chinese: '什么', english: 'what', exampleIndonesian: 'Apa ini?', exampleZh: '这是什么？', exampleEn: 'What is this?' },
    { indonesian: 'saya', chinese: '我', english: 'I', exampleIndonesian: 'Saya suka Indonesia.', exampleZh: '我喜欢印尼。', exampleEn: 'I like Indonesia.' }
  ]
}, null, 2)

const wordbookOptions = ref([])

async function fetchWordbooks() {
  loadingWordbooks.value = true
  try {
    const { data } = await api.get('/admin/wordbooks')
    wordbooks.value = data
    wordbookOptions.value = data.map(wb => ({
      label: `${wb.name}（${wb.level}）`,
      value: wb.id
    }))
    if (data.length > 0 && !selectedWordbookId.value) {
      selectedWordbookId.value = data[0].id
    }
  } catch (err) {
    message.error('获取词书列表失败')
  } finally {
    loadingWordbooks.value = false
  }
}

function fillSample() {
  if (targetType.value === 'new') {
    jsonText.value = sampleJson
  } else {
    jsonText.value = sampleExistingJson
  }
}

async function handleImport() {
  if (!jsonText.value.trim()) {
    message.warning('请先粘贴 JSON 数据')
    return
  }

  if (targetType.value === 'existing' && !selectedWordbookId.value) {
    message.warning('请选择要导入的词书')
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
    if (targetType.value === 'existing') {
      formData.append('wordbookId', selectedWordbookId.value)
    }

    const { data } = await api.post('/admin/import', formData)

    result.value = data
    if (data.success) {
      message.success(`成功导入 ${data.imported} 个单词`)
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

onMounted(() => {
  fetchWordbooks()
})
</script>

<template>
  <div class="page-wrapper">
    <n-card title="批量导入单词" class="admin-card">
      <div class="import-layout">
        <!-- 左侧：导入表单 -->
        <div class="import-form">
          <div class="intro">
            <p>在此批量导入单词数据。JSON 格式如下：</p>
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

          <div class="target-section">
            <div class="section-title">导入目标</div>
            <n-radio-group v-model:value="targetType">
              <n-space>
                <n-radio value="new">创建新词书</n-radio>
                <n-radio value="existing">添加到已有词书</n-radio>
              </n-space>
            </n-radio-group>

            <div v-if="targetType === 'existing'" class="select-wrapper">
              <n-select
                v-model:value="selectedWordbookId"
                :options="wordbookOptions"
                placeholder="请选择词书"
                style="width: 320px"
              />
            </div>
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
        </div>

        <!-- 右侧：导入结果 -->
        <div class="import-result">
          <div v-if="result" class="result-box">
            <p v-if="result.success" class="success-text">
              导入成功！共导入 {{ result.imported }} 个单词
            </p>
            <p v-if="result.error" class="error-text">
              导入失败：{{ result.error }}
            </p>
          </div>
          <div v-else class="empty-result">
            <p>导入结果将显示在这里...</p>
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.page-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}

.admin-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.import-layout {
  display: flex;
  gap: 24px;
}

.import-form {
  flex: 1;
  min-width: 0;
}

.import-result {
  flex: 0 0 350px;
}

.intro {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
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

.target-section {
  background: #f0f7ff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.select-wrapper {
  margin-top: 12px;
}

.result-box {
  padding: 16px;
  border-radius: 8px;
  background: #f8f8f8;
  height: fit-content;
  margin-bottom: 24px;
}

.empty-result {
  padding: 16px;
  border-radius: 8px;
  background: #f8f8f8;
  color: #999;
  font-style: italic;
  text-align: center;
  margin-bottom: 24px;
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-text {
  color: #18a058;
  font-weight: 500;
  font-size: 16px;
}

.error-text {
  color: #d03050;
  font-weight: 500;
  font-size: 16px;
}

/* 响应式设计：在小屏幕上堆叠垂直布局 */
@media (max-width: 768px) {
  .import-layout {
    flex-direction: column;
  }
  
  .import-result {
    flex: 0 0 auto;
    width: 100%;
  }
}
</style>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  NButton, NCard, NList, NListItem, NTag,
  NCollapse, NCollapseItem, NSpace, useMessage,
  NModal, NForm, NFormItem, NInput, NPopconfirm,
  NPagination
} from 'naive-ui'

const message = useMessage()
const wordbooks = ref([])
const wordbookDetails = ref({})
const loadingWordbooks = ref(false)
const currentViewingWordbookId = ref(null)

// Pagination state for each wordbook
const paginationStates = ref({})

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

/* ===== 词书 CRUD ===== */
const showWordbookModal = ref(false)
const wordbookModalTitle = ref('新建词书')
const editingWordbook = ref(null)
const wordbookForm = ref({
  name: '',
  nameEn: '',
  level: ''
})

function openCreateWordbook() {
  wordbookModalTitle.value = '新建词书'
  editingWordbook.value = null
  wordbookForm.value = { name: '', nameEn: '', level: '' }
  showWordbookModal.value = true
}

function openEditWordbook(wb) {
  wordbookModalTitle.value = '编辑词书'
  editingWordbook.value = wb
  wordbookForm.value = {
    name: wb.name,
    nameEn: wb.nameEn || '',
    level: wb.level
  }
  showWordbookModal.value = true
}

async function saveWordbook() {
  if (!wordbookForm.value.name.trim() || !wordbookForm.value.level.trim()) {
    message.warning('词书名称和等级不能为空')
    return
  }
  try {
    if (editingWordbook.value) {
      await api.put(`/admin/wordbooks/${editingWordbook.value.id}`, wordbookForm.value)
      message.success('词书更新成功')
    } else {
      await api.post('/admin/wordbooks', wordbookForm.value)
      message.success('词书创建成功')
    }
    showWordbookModal.value = false
    await fetchWordbooks()
  } catch (err) {
    message.error(err.response?.data?.error || '操作失败')
  }
}

async function removeWordbook(id) {
  try {
    await api.delete(`/admin/wordbooks/${id}`)
    message.success('词书删除成功')
    delete wordbookDetails.value[id]
    delete paginationStates.value[id]
    await fetchWordbooks()
  } catch (err) {
    message.error(err.response?.data?.error || '删除失败')
  }
}

/* ===== 单词 CRUD ===== */
const showWordModal = ref(false)
const wordModalTitle = ref('添加单词')
const currentWordbookId = ref(null)
const editingWord = ref(null)
const wordForm = ref({
  indonesian: '',
  chinese: '',
  english: '',
  exampleIndonesian: '',
  exampleZh: '',
  exampleEn: ''
})

function openAddWord(wordbookId) {
  wordModalTitle.value = '添加单词'
  currentWordbookId.value = wordbookId
  editingWord.value = null
  wordForm.value = {
    indonesian: '', chinese: '', english: '',
    exampleIndonesian: '', exampleZh: '', exampleEn: ''
  }
  showWordModal.value = true
}

function openEditWord(w, wordbookId) {
  if (!w || !w.id) {
    message.error('单词数据异常，缺少 ID，请重新加载页面')
    return
  }
  wordModalTitle.value = '编辑单词'
  currentWordbookId.value = wordbookId
  editingWord.value = w
  wordForm.value = {
    indonesian: w.indonesian,
    chinese: w.chinese,
    english: w.english || '',
    exampleIndosian: w.exampleIndosian || '',
    exampleZh: w.exampleZh || '',
    exampleEn: w.exampleEn || ''
  }
  showWordModal.value = true
}

async function saveWord() {
  if (!wordForm.value.indonesian.trim() || !wordForm.value.chinese.trim()) {
    message.warning('印尼语和中文释义不能为空')
    return
  }
  if (editingWord.value && !editingWord.value.id) {
    message.error('单词 ID 无效，请刷新页面后重试')
    return
  }
  try {
    if (editingWord.value) {
      await api.put(`/admin/words/${editingWord.value.id}`, wordForm.value)
      message.success('单词更新成功')
    } else {
      await api.post(`/admin/wordbooks/${currentWordbookId.value}/words`, wordForm.value)
      message.success('单词添加成功')
    }
    showWordModal.value = false
    await viewWordbook(currentWordbookId.value)
    await fetchWordbooks()
  } catch (err) {
    message.error(err.response?.data?.error || '操作失败')
  }
}

async function removeWord(wordId, wordbookId) {
  try {
    await api.delete(`/admin/words/${wordId}`)
    message.success('单词删除成功')
    await viewWordbook(wordbookId)
    await fetchWordbooks()
  } catch (err) {
    message.error(err.response?.data?.error || '删除失败')
  }
}

/* ===== 列表与导出 ===== */
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
  currentViewingWordbookId.value = id
  try {
    // Initialize pagination state for this wordbook if not exists
    if (!paginationStates.value[id]) {
      paginationStates.value[id] = {
        page: 0,
        size: 20,
        total: 0
      }
    }
    
    const { data } = await api.get(`/admin/wordbooks/${id}/words`, {
      params: {
        page: paginationStates.value[id].page,
        size: paginationStates.value[id].size
      }
    })
    
    // Update pagination state with total count
    paginationStates.value[id].total = data.total || 0
    wordbookDetails.value[id] = data.words || []
  } catch (err) {
    message.error('获取单词详情失败')
  }
}

function handlePageChange(wordbookId, page) {
  if (paginationStates.value[wordbookId]) {
    paginationStates.value[wordbookId].page = page
    viewWordbook(wordbookId)
  }
}

function handleSizeChange(wordbookId, size) {
  if (paginationStates.value[wordbookId]) {
    paginationStates.value[wordbookId].size = size
    paginationStates.value[wordbookId].page = 0 // Reset to first page
    viewWordbook(wordbookId)
  }
}

function exportWordbook(id, name) {
  const data = wordbookDetails.value[id]
  if (!data) {
    message.warning('请先点击“查看”加载数据，再导出')
    return
  }
  const fullData = {
    name: name,
    words: data
  }
  const blob = new Blob([JSON.stringify(fullData, null, 2)], { type: 'application/json' })
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
  <div class="page-wrapper">
    <n-card title="词书列表与导出" class="admin-card">
      <div class="toolbar">
        <n-button type="primary" @click="openCreateWordbook">+ 新建词书</n-button>
      </div>

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
                <n-button size="small" @click="openEditWordbook(wb)">
                  编辑词书
                </n-button>
                <n-popconfirm @positive-click="removeWordbook(wb.id)">
                  <template #trigger>
                    <n-button size="small" type="error">删除词书</n-button>
                  </template>
                  确定删除该词书及其所有单词吗？
                </n-popconfirm>
                <n-button size="small" type="info" @click="openAddWord(wb.id)">
                  + 添加单词
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
                       <th style="width: 120px">操作</th>
                     </tr>
                   </thead>
                   <tbody>
                     <tr v-for="w in wordbookDetails[wb.id]" :key="w.id">
                       <td><strong>{{ w.indonesian }}</strong></td>
                       <td>{{ w.chinese }}</td>
                       <td>{{ w.english || '-' }}</td>
                       <td class="example-cell">
                         <div v-if="w.exampleIndonesian">{{ w.exampleIndonesian }}</div>
                         <div v-if="w.exampleZh" class="example-zh">{{ w.exampleZh }}</div>
                       </td>
                       <td>
                         <n-space size="small">
                           <n-button size="tiny" @click="openEditWord(w, wb.id)">编辑</n-button>
                           <n-popconfirm @positive-click="removeWord(w.id, wb.id)">
                             <template #trigger>
                               <n-button size="tiny" type="error">删除</n-button>
                             </template>
                             确定删除该单词吗？
                           </n-popconfirm>
                         </n-space>
                       </td>
                     </tr>
                   </tbody>
                 </table>
                 
                 <!-- 分页条 -->
                 <div v-if="paginationStates[wb.id]" class="pagination-container">
                   <n-pagination
                     :page-count="Math.ceil(paginationStates[wb.id].total / paginationStates[wb.id].size)"
                     :current-page="paginationStates[wb.id].page + 1"
                     :simple="false"
                     @update:page="handlePageChange(wb.id, $event - 1)"
                     :page-size-options="[10, 20, 50, 100]"
                     :show-size-dropdown="true"
                     :show-quick-jumper="true"
                     :show-total="`共 ${paginationStates[wb.id].total} 条`"
                     :default-page-size="paginationStates[wb.id].size"
                     @update:page-size="handleSizeChange(wb.id, $event)"
                   />
                 </div>
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

    <!-- 词书弹窗 -->
    <n-modal v-model:show="showWordbookModal" :title="wordbookModalTitle" preset="card" style="width: 420px">
      <n-form :model="wordbookForm" label-placement="left" label-width="80">
        <n-form-item label="名称" required>
          <n-input v-model:value="wordbookForm.name" placeholder="词书名称" />
        </n-form-item>
        <n-form-item label="英文名称">
          <n-input v-model:value="wordbookForm.nameEn" placeholder="英文名称" />
        </n-form-item>
        <n-form-item label="等级" required>
          <n-input v-model:value="wordbookForm.level" placeholder="如 A1 / A2 / B1" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showWordbookModal = false">取消</n-button>
          <n-button type="primary" @click="saveWordbook">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 单词弹窗 -->
    <n-modal v-model:show="showWordModal" :title="wordModalTitle" preset="card" style="width: 480px">
      <n-form :model="wordForm" label-placement="left" label-width="100">
        <n-form-item label="印尼语" required>
          <n-input v-model:value="wordForm.indonesian" placeholder="印尼语单词" />
        </n-form-item>
        <n-form-item label="中文释义" required>
          <n-input v-model:value="wordForm.chinese" placeholder="中文释义" />
        </n-form-item>
        <n-form-item label="英文释义">
          <n-input v-model:value="wordForm.english" placeholder="英文释义" />
        </n-form-item>
        <n-form-item label="印尼语例句">
          <n-input v-model:value="wordForm.exampleIndonesian" type="textarea" :rows="2" placeholder="印尼语例句" />
        </n-form-item>
        <n-form-item label="中文翻译">
          <n-input v-model:value="wordForm.exampleZh" type="textarea" :rows="2" placeholder="中文翻译" />
        </n-form-item>
        <n-form-item label="英文翻译">
          <n-input v-model:value="wordForm.exampleEn" type="textarea" :rows="2" placeholder="英文翻译" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showWordModal = false">取消</n-button>
          <n-button type="primary" @click="saveWord">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.admin-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.toolbar {
  margin-bottom: 16px;
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

.pagination-container {
  margin-top: 16px;
  text-align: center;
}

.word-table tr:nth-child(even) {
  background: #fafafa;
}

.example-cell {
  max-width: 260px;
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

<script setup>
import { ref, onMounted, h } from 'vue'
import axios from 'axios'
import {
  NCard, NInput, NSelect, NButton, NDataTable, NSpace,
  NPagination, NSpin, NTag, NModal, NForm, NFormItem, NPopconfirm,
  useMessage
} from 'naive-ui'

/**
 * 单词全局搜索页
 * 跨词书模糊搜索（印尼语/中文/英文），支持快速编辑和删除
 */
const api = axios.create({ baseURL: '/api/v1', timeout: 15000 })
const message = useMessage()

// ---- 搜索状态 ----
const keyword = ref('')
const selectedWordbookId = ref(null)
const wordbookOptions = ref([{ label: '全部词书', value: null }])
const loading = ref(false)
const words = ref([])
const total = ref(0)
const currentPage = ref(1)
const PAGE_SIZE = 20

// ---- 编辑弹窗 ----
const showModal = ref(false)
const editingWord = ref(null)
const wordForm = ref({
  indonesian: '', chinese: '', english: '',
  exampleIndonesian: '', exampleZh: '', exampleEn: ''
})
const saving = ref(false)

// ---- 表格列 ----
const columns = [
  {
    title: '词书',
    key: 'wordbookName',
    width: 110,
    render: (row) => h(NTag, { size: 'small', type: 'info' }, { default: () => row.wordbookName })
  },
  {
    title: '印尼语',
    key: 'indonesian',
    width: 130,
    render: (row) => h('strong', row.indonesian)
  },
  {
    title: '中文',
    key: 'chinese',
    width: 120
  },
  {
    title: '英文',
    key: 'english',
    width: 120,
    render: (row) => row.english || '-'
  },
  {
    title: '例句',
    key: 'example',
    ellipsis: { tooltip: true },
    render: (row) => row.exampleIndonesian || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => h(NSpace, { size: 'small' }, {
      default: () => [
        h(NButton, {
          size: 'tiny',
          onClick: () => openEdit(row)
        }, { default: () => '编辑' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row)
        }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确定删除该单词吗？'
        })
      ]
    })
  }
]

// ---- 方法 ----
async function fetchWordbooks() {
  try {
    const { data } = await api.get('/admin/wordbooks')
    wordbookOptions.value = [
      { label: '全部词书', value: null },
      ...data.map(wb => ({ label: `${wb.name}（${wb.level}）`, value: wb.id }))
    ]
  } catch {
    message.error('获取词书列表失败')
  }
}

async function search(resetPage = true) {
  if (resetPage) currentPage.value = 1
  loading.value = true
  try {
    const { data } = await api.get('/admin/words/search', {
      params: {
        q: keyword.value.trim(),
        wordbookId: selectedWordbookId.value || undefined,
        page: currentPage.value - 1,
        size: PAGE_SIZE
      }
    })
    words.value = data.words
    total.value = data.total
  } catch {
    message.error('搜索失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  search(false)
}

// 回车触发搜索
function handleKeydown(e) {
  if (e.key === 'Enter') search()
}

function openEdit(row) {
  editingWord.value = row
  wordForm.value = {
    indonesian: row.indonesian,
    chinese: row.chinese,
    english: row.english || '',
    exampleIndonesian: row.exampleIndonesian || '',
    exampleZh: row.exampleZh || '',
    exampleEn: row.exampleEn || ''
  }
  showModal.value = true
}

async function handleSave() {
  if (!wordForm.value.indonesian.trim() || !wordForm.value.chinese.trim()) {
    message.warning('印尼语和中文释义不能为空')
    return
  }
  saving.value = true
  try {
    await api.put(`/admin/words/${editingWord.value.id}`, wordForm.value)
    message.success('保存成功')
    showModal.value = false
    // 更新本地数据，避免重新请求
    const idx = words.value.findIndex(w => w.id === editingWord.value.id)
    if (idx !== -1) {
      words.value[idx] = { ...words.value[idx], ...wordForm.value }
    }
  } catch (err) {
    message.error(err.response?.data?.error || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await api.delete(`/admin/words/${row.id}`)
    message.success('删除成功')
    words.value = words.value.filter(w => w.id !== row.id)
    total.value = Math.max(0, total.value - 1)
  } catch (err) {
    message.error(err.response?.data?.error || '删除失败')
  }
}

onMounted(async () => {
  await fetchWordbooks()
  search()
})
</script>

<template>
  <div class="page-wrapper">
    <n-card title="单词全局搜索">
      <!-- 搜索栏 -->
      <n-space align="center" style="margin-bottom: 16px" wrap>
        <n-input
          v-model:value="keyword"
          placeholder="搜索印尼语 / 中文 / 英文..."
          clearable
          style="width: 280px"
          @keydown="handleKeydown"
          @clear="search"
        />
        <n-select
          v-model:value="selectedWordbookId"
          :options="wordbookOptions"
          style="width: 200px"
          @update:value="search"
        />
        <n-button type="primary" @click="search">搜索</n-button>
        <span class="total-hint" v-if="!loading">共 {{ total }} 条结果</span>
      </n-space>

      <!-- 结果表格 -->
      <n-spin :show="loading">
        <n-data-table
          :columns="columns"
          :data="words"
          :bordered="false"
          :pagination="false"
          striped
          size="small"
          scroll-x="800"
        />
      </n-spin>

      <!-- 分页 -->
      <div v-if="total > PAGE_SIZE" class="pagination-bar">
        <n-pagination
          v-model:page="currentPage"
          :page-count="Math.ceil(total / PAGE_SIZE)"
          :page-slot="7"
          @update:page="handlePageChange"
        />
      </div>
    </n-card>

    <!-- 编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      title="编辑单词"
      preset="card"
      style="width: 500px"
    >
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
          <n-input v-model:value="wordForm.exampleIndonesian" type="textarea" :rows="2" />
        </n-form-item>
        <n-form-item label="中文翻译">
          <n-input v-model:value="wordForm.exampleZh" type="textarea" :rows="2" />
        </n-form-item>
        <n-form-item label="英文翻译">
          <n-input v-model:value="wordForm.exampleEn" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px;
}

.total-hint {
  font-size: 13px;
  color: #999;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

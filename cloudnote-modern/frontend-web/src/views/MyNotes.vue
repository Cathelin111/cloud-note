<template>
  <div>
    <AppNav />
    <div class="page">
      <el-row :gutter="10" style="min-height:72vh">
        <!-- 笔记本列 -->
        <el-col :xs="24" :sm="7" :md="5">
          <div class="card" style="min-height:72vh">
            <div style="display:flex;justify-content:space-between">
              <b>{{ bookTitle }}</b>
              <el-button size="small" type="primary" :icon="Plus" @click="createBookDlg = true" />
            </div>
            <el-menu :default-active="bookId || ''" @select="selectBook" style="border-right:none">
              <el-menu-item v-for="b in normalBooks" :key="b.cn_notebook_id" :index="b.cn_notebook_id">
                <el-icon><Folder /></el-icon><span>{{ b.cn_notebook_name }}</span>
              </el-menu-item>
            </el-menu>
            <el-divider style="margin:8px 0" />
            <el-menu :default-active="panelMode" @select="selectPanel">
              <el-menu-item index="normal"><el-icon><Notebook /></el-icon>全部笔记本</el-menu-item>
              <el-menu-item index="favorites"><el-icon><Star /></el-icon>收藏笔记本</el-menu-item>
              <el-menu-item index="recycle"><el-icon><Delete /></el-icon>回收站</el-menu-item>
              <el-menu-item index="action"><el-icon><User /></el-icon>活动投稿</el-menu-item>
            </el-menu>
            <template v-if="panelMode === 'normal' && currentBook">
              <el-divider style="margin:8px 0" />
              <el-button size="small" @click="renameDlg = true">重命名笔记本</el-button>
              <el-button size="small" type="danger" plain @click="removeBook">删除笔记本</el-button>
            </template>
          </div>
        </el-col>

        <!-- 笔记列 -->
        <el-col :xs="24" :sm="8" :md="6">
          <div class="card" style="min-height:72vh">
            <div style="display:flex;justify-content:space-between">
              <b>笔记</b>
              <el-button v-if="bookId" size="small" type="primary" :icon="Plus" @click="createNoteDlg = true" />
            </div>
            <el-menu :default-active="noteId || ''" @select="selectNote" style="border-right:none">
              <el-menu-item v-for="n in notes" :key="n.cn_note_id" :index="n.cn_note_id">
                <el-icon><Document /></el-icon><span>{{ n.cn_note_title || '(无标题)' }}</span>
                <template #title>
                  <span class="note-actions">
                    <el-button v-if="panelMode === 'recycle'" link type="primary" size="small" @click.stop="openRestore(n)">恢复</el-button>
                    <el-button v-if="panelMode === 'recycle'" link type="danger" size="small" @click.stop="permanent(n)">彻底删除</el-button>
                    <el-button v-if="panelMode === 'favorites'" link type="danger" size="small" @click.stop="unlike(n)">取消收藏</el-button>
                    <el-button v-if="panelMode === 'normal'" link type="danger" size="small" @click.stop="softDelete(n)">删除</el-button>
                  </span>
                </template>
              </el-menu-item>
            </el-menu>
            <el-empty v-if="!notes.length" description="暂无笔记" :image-size="60" />
          </div>
        </el-col>

        <!-- 编辑列 -->
        <el-col :xs="24" :sm="9" :md="13">
          <div class="card" style="min-height:72vh">
            <div style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap">
              <el-input v-model="editorTitle" placeholder="笔记标题..." style="max-width:300px" />
              <el-button type="primary" :disabled="!noteId" @click="save">保存笔记</el-button>
              <el-button :disabled="!noteId || panelMode !== 'normal'" @click="shareNote">分享该笔记</el-button>
            </div>
            <el-input v-model="editorBody" type="textarea" :rows="18" placeholder="笔记内容（支持HTML片段）..." />
            <div class="muted" style="margin-top:6px">提示：内容将以HTML富文本保存，可粘贴 <code>&lt;p&gt;…&lt;/p&gt;</code> 片段</div>
          </div>
        </el-col>
      </el-row>

      <!-- 对话框 -->
      <el-dialog v-model="createBookDlg" title="新建笔记本" width="360px">
        <el-input v-model="bookName" placeholder="笔记本名称" />
        <template #footer>
          <el-button @click="createBookDlg = false">取消</el-button>
          <el-button type="primary" @click="createBook">创建</el-button>
        </template>
      </el-dialog>
      <el-dialog v-model="renameDlg" title="重命名笔记本" width="360px">
        <el-input v-model="bookName" :placeholder="currentBook?.cn_notebook_name" />
        <template #footer>
          <el-button @click="renameDlg = false">取消</el-button>
          <el-button type="primary" @click="renameBook">保存</el-button>
        </template>
      </el-dialog>
      <el-dialog v-model="createNoteDlg" title="新建笔记" width="360px">
        <el-input v-model="noteTitle" placeholder="笔记标题" />
        <template #footer>
          <el-button @click="createNoteDlg = false">取消</el-button>
          <el-button type="primary" @click="createNote">创建</el-button>
        </template>
      </el-dialog>
      <el-dialog v-model="restoreDlg" title="恢复到哪个笔记本" width="360px">
        <el-select v-model="restoreTarget" style="width:100%" placeholder="选择笔记本">
          <el-option v-for="b in normalBooks" :key="b.cn_notebook_id" :label="b.cn_notebook_name" :value="b.cn_notebook_id" />
        </el-select>
        <template #footer>
          <el-button @click="restoreDlg = false">取消</el-button>
          <el-button type="primary" :disabled="!restoreTarget" @click="restoreNote">恢复</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder, Notebook, Star, Delete, User, Document } from '@element-plus/icons-vue'
import AppNav from '@/components/AppNav.vue'
import { notebookApi, noteApi, shareApi } from '@/api'

const normalBooks = ref([])
const special = ref({})
const bookId = ref('')
const panelMode = ref('normal')
const notes = ref([])
const noteId = ref('')
const editorTitle = ref('')
const editorBody = ref('')
const currentBook = computed(() => normalBooks.value.find((b) => b.cn_notebook_id === bookId.value) || null)
const bookTitle = computed(() => (panelMode.value === 'normal' ? (currentBook.value?.cn_notebook_name || '全部笔记本') : ''))

const createBookDlg = ref(false)
const bookName = ref('')
const renameDlg = ref(false)
const createNoteDlg = ref(false)
const noteTitle = ref('')
const restoreDlg = ref(false)
const restoreTarget = ref('')
let restoreNoteId = ''

async function loadBooks() {
  const res = await notebookApi.list()
  if (res.status === 0) normalBooks.value = res.data || []
}

async function loadSpecial() {
  const res = await notebookApi.special()
  if (res.status === 0) special.value = res.data || {}
}

async function selectBook(id) {
  panelMode.value = 'normal'
  bookId.value = id
  notes.value = []
  noteId.value = ''
  await loadNotes()
}

async function selectPanel(mode) {
  panelMode.value = mode
  if (mode === 'normal') {
    bookId.value = normalBooks.value[0]?.cn_notebook_id || ''
    await loadNotes()
    return
  }
  bookId.value = special.value[mode]?.cn_notebook_id || ''
  notes.value = []
  noteId.value = ''
  if (bookId.value) await loadNotes()
}

async function loadNotes() {
  if (!bookId.value) {
    notes.value = []
    return
  }
  const res = await noteApi.listByBook(bookId.value)
  if (res.status === 0) notes.value = res.data || []
  noteId.value = ''
}

async function selectNote(id) {
  noteId.value = id
  const res = await noteApi.detail(id)
  if (res.status === 0) {
    editorTitle.value = res.data.cn_note_title || ''
    editorBody.value = res.data.cn_note_body || ''
  }
}

async function createBook() {
  if (!bookName.value.trim()) return ElMessage.warning('请输入名称')
  const res = await notebookApi.create(bookName.value.trim())
  if (res.status === 0) {
    ElMessage.success(res.msg)
    bookName.value = ''
    createBookDlg.value = false
    await loadBooks()
    await selectBook(res.data.cn_notebook_id)
  }
}

async function renameBook() {
  if (!bookName.value.trim()) return
  await notebookApi.rename(bookId.value, bookName.value.trim())
  ElMessage.success('重命名成功')
  renameDlg.value = false
  bookName.value = ''
  await loadBooks()
}

async function removeBook() {
  await ElMessageBox.confirm(`确定删除笔记本「${currentBook.value.cn_notebook_name}」？`, '提示', { type: 'warning' })
  const res = await notebookApi.remove(bookId.value)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    bookId.value = ''
    await loadBooks()
    if (normalBooks.value.length) await selectBook(normalBooks.value[0].cn_notebook_id)
  }
}

async function createNote() {
  if (!noteTitle.value.trim()) return ElMessage.warning('请输入标题')
  const res = await noteApi.create(bookId.value, noteTitle.value.trim())
  if (res.status === 0) {
    ElMessage.success(res.msg)
    noteTitle.value = ''
    createNoteDlg.value = false
    await loadNotes()
  }
}

async function save() {
  if (!noteId.value) return ElMessage.warning('请先选择一条笔记')
  const res = await noteApi.update(noteId.value, editorTitle.value.trim() || '(无标题)', editorBody.value)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    await loadNotes()
  }
}

async function shareNote() {
  const res = await shareApi.shareMine(noteId.value)
  if (res.status === 0) ElMessage.success(res.msg)
}

async function softDelete(n) {
  await ElMessageBox.confirm('删除后进入回收站，可恢复。确定？', '提示', { type: 'warning' })
  const res = await noteApi.softDelete(n.cn_note_id)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    await loadNotes()
  }
}

function openRestore(n) {
  restoreNoteId = n.cn_note_id
  restoreTarget.value = normalBooks.value[0]?.cn_notebook_id || ''
  restoreDlg.value = true
}

async function restoreNote() {
  const res = await noteApi.move(restoreNoteId, restoreTarget.value)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    restoreDlg.value = false
    await loadNotes()
  }
}

async function permanent(n) {
  await ElMessageBox.confirm('彻底删除不可恢复！确定？', '警告', { type: 'error' })
  const res = await noteApi.permanent(n.cn_note_id)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    await loadNotes()
  }
}

async function unlike(n) {
  const res = await noteApi.softDelete(n.cn_note_id)
  if (res.status === 0) {
    ElMessage.success('已移出收藏')
    await loadNotes()
  }
}

onMounted(async () => {
  await loadBooks()
  await loadSpecial()
  if (normalBooks.value.length) await selectBook(normalBooks.value[0].cn_notebook_id)
})
</script>

<style scoped>
.note-actions { margin-left: 6px; display: inline-flex; gap: 2px; }
</style>

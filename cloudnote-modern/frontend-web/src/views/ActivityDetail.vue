<template>
  <div>
    <AppNav />
    <div class="page">
      <el-card>
        <template #header>
          <div style="display:flex;justify-content:space-between;flex-wrap:wrap;gap:6px">
            <b>活动 #{{ id }} 的投稿</b>
            <el-button type="primary" size="small" @click="onJoinClick">参加活动</el-button>
          </div>
        </template>
        <el-row :gutter="10">
          <el-col :xs="24" :md="8">
            <el-table :data="rows" highlight-current-row @current-change="pick">
              <el-table-column prop="cn_note_activity_title" label="投稿标题" show-overflow-tooltip />
            </el-table>
            <el-pagination v-if="total > size" small layout="prev,pager,next" :total="total" :page-size="size"
                           :current-page="page" @current-change="load(page)" style="margin-top:8px" />
          </el-col>
          <el-col :xs="24" :md="16">
            <template v-if="detail">
              <h3>{{ detail.cn_note_activity_title }}</h3>
              <div class="muted" style="margin-bottom:8px">
                顶 {{ detail.cn_note_activity_up }} / 踩 {{ detail.cn_note_activity_down }}
              </div>
              <div class="rich-body card" v-html="detail.cn_note_activity_body || '<p class=\'muted\'>暂无内容</p>'"></div>
              <div style="margin-top:10px;display:flex;gap:8px;flex-wrap:wrap">
                <el-button size="small" type="success" @click="vote(true)"><el-icon><Top /></el-icon> 顶</el-button>
                <el-button size="small" type="danger" @click="vote(false)"><el-icon><Bottom /></el-icon> 踩</el-button>
                <el-button size="small" @click="collect">收藏该投稿</el-button>
              </div>
            </template>
            <el-empty v-else description="点击左侧投稿查看详情" />
          </el-col>
        </el-row>
      </el-card>

      <el-dialog v-model="joinDlg" title="选择要投稿的笔记" width="90%" style="max-width:520px">
        <el-select v-model="myNotebookId" placeholder="先选择笔记本" style="width:100%" @change="loadMyNotes">
          <el-option v-for="b in myNotebooks" :key="b.cn_notebook_id" :label="b.cn_notebook_name" :value="b.cn_notebook_id" />
        </el-select>
        <el-select v-model="myNoteId" placeholder="再选择笔记" style="width:100%;margin-top:8px" filterable>
          <el-option v-for="n in myNotes" :key="n.cn_note_id" :label="n.cn_note_title" :value="n.cn_note_id" />
        </el-select>
        <template #footer>
          <el-button @click="joinDlg = false">取消</el-button>
          <el-button type="primary" :disabled="!myNoteId" @click="join">投稿</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Top, Bottom } from '@element-plus/icons-vue'
import AppNav from '@/components/AppNav.vue'
import { activityApi, notebookApi, noteApi } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const id = route.params.id
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const detail = ref(null)
const joinDlg = ref(false)
const myNotebooks = ref([])
const myNotebookId = ref('')
const myNotes = ref([])
const myNoteId = ref('')

function needLogin() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function load(p) {
  page.value = p || 1
  const res = await activityApi.submissions(id, page.value)
  if (res.status === 0) {
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  }
}

async function pick(row) {
  const res = await activityApi.submissionDetail(row.cn_note_activity_id)
  if (res.status === 0) detail.value = res.data
}

async function vote(up) {
  if (!needLogin() || !detail.value) return
  const res = up ? await activityApi.up(detail.value.cn_note_activity_id) : await activityApi.down(detail.value.cn_note_activity_id)
  if (res.status === 0) {
    ElMessage.success(res.msg)
    await pick({ cn_note_activity_id: detail.value.cn_note_activity_id })
  }
}

async function collect() {
  if (!needLogin() || !detail.value) return
  const res = await activityApi.collectSubmission(detail.value.cn_note_activity_id)
  if (res.status === 0) ElMessage.success(res.msg)
}

function onJoinClick() {
  // 游客点击"参加活动"才引导登录
  if (!needLogin()) return
  joinDlg.value = true
  openJoin()
}

async function openJoin() {
  // 打开"参加活动"弹窗: 游客不预载数据, 由按钮点击处引导登录
  if (!userStore.isLogin) return
  const res = await notebookApi.list()
  if (res.status === 0) {
    myNotebooks.value = res.data || []
    myNotebookId.value = ''
    myNoteId.value = ''
  }
}

async function loadMyNotes() {
  myNoteId.value = ''
  const res = await noteApi.listByBook(myNotebookId.value)
  if (res.status === 0) myNotes.value = res.data || []
}

async function join() {
  const res = await activityApi.join(id, myNoteId.value)
  if (res.status === 0) {
    ElMessage.success('投稿成功')
    joinDlg.value = false
    detail.value = null
    await load(1)
  }
}

onMounted(async () => {
  await load(1)
  await openJoin()
})
</script>

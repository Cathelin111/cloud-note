<template>
  <div>
    <AppNav />
    <div class="page">
      <el-tabs v-model="tab">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <div style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="userKw" placeholder="按用户名/昵称搜索" clearable style="max-width:260px" @keyup.enter="loadUsers(1)" />
            <el-button type="primary" @click="loadUsers(1)">搜索</el-button>
            <span class="muted">共 {{ userTotal }} 位普通用户</span>
          </div>
          <el-table :data="users" border>
            <el-table-column prop="cn_user_name" label="用户名" />
            <el-table-column prop="cn_user_nick" label="昵称" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.cn_user_status === 'disabled' ? 'danger' : 'success'">
                  {{ row.cn_user_status === 'disabled' ? '已停用' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button size="small" @click="toggleUser(row)">
                  {{ row.cn_user_status === 'disabled' ? '启用' : '停用' }}
                </el-button>
                <el-button size="small" type="warning" plain @click="promote(row)">设为管理员</el-button>
                <el-button size="small" type="danger" plain @click="delUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="userTotal > 10" layout="prev,pager,next" :total="userTotal" :page-size="10"
                         @current-change="loadUsers" style="margin-top:8px" small />
        </el-tab-pane>

        <!-- 管理员管理 -->
        <el-tab-pane label="管理员管理" name="admins">
          <div style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="adminForm.username" placeholder="用户名" style="max-width:160px" />
            <el-input v-model="adminForm.password" type="password" placeholder="密码(≥6位)" style="max-width:160px" />
            <el-input v-model="adminForm.nick" placeholder="昵称(选填)" style="max-width:160px" />
            <el-button type="primary" @click="createAdmin">新建管理员</el-button>
          </div>
          <el-table :data="admins" border>
            <el-table-column prop="cn_user_name" label="用户名" />
            <el-table-column prop="cn_user_nick" label="昵称" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.cn_user_status === 'disabled' ? 'danger' : 'success'">
                  {{ row.cn_user_status === 'disabled' ? '已停用' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 分享管理 -->
        <el-tab-pane label="分享管理" name="shares">
          <div style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="shareKw" placeholder="按标题搜索" clearable style="max-width:260px" @keyup.enter="loadShares(1)" />
            <el-button type="primary" @click="loadShares(1)">搜索</el-button>
            <span class="muted">共 {{ shareTotal }} 条分享</span>
          </div>
          <el-table :data="shares" border>
            <el-table-column prop="cn_share_title" label="标题" show-overflow-tooltip />
            <el-table-column prop="cn_share_author" label="分享人" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.cn_share_status === 'disabled' ? 'info' : 'success'">
                  {{ row.cn_share_status === 'disabled' ? '已下架' : '公开' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button size="small" @click="toggleShare(row)">
                  {{ row.cn_share_status === 'disabled' ? '上架' : '下架' }}
                </el-button>
                <el-button size="small" type="danger" plain @click="delShare(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="shareTotal > 10" layout="prev,pager,next" :total="shareTotal" :page-size="10"
                         @current-change="loadShares" style="margin-top:8px" small />
        </el-tab-pane>

        <!-- 活动管理 -->
        <el-tab-pane label="活动管理" name="activities">
          <div style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="actForm.title" placeholder="活动标题" style="max-width:200px" />
            <el-input v-model="actForm.body" placeholder="活动介绍" style="max-width:260px" />
            <el-button type="primary" @click="saveActivity">发布活动</el-button>
            <span class="muted">活动总数：{{ activities.length }}</span>
          </div>
          <el-table :data="activities" border>
            <el-table-column prop="cn_activity_title" label="标题" width="160" />
            <el-table-column prop="cn_activity_body" label="介绍" show-overflow-tooltip />
            <el-table-column label="结束时间" width="140">
              <template #default="{ row }">{{ row.cn_activity_end_time ? fmt(row.cn_activity_end_time) : '长期有效' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170">
              <template #default="{ row }">
                <el-button size="small" @click="editActivity(row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="delActivity(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 系统 -->
        <el-tab-pane label="数据备份" name="system">
          <el-alert type="info" :closable="false"
                    title="每日启动自动备份逻辑由运维脚本承担；此处调用 mysqldump 立即生成 SQL 快照。" />
          <el-button type="warning" style="margin-top:10px" :loading="backing" @click="backup">立即备份数据库</el-button>
          <div v-if="lastBackup" class="muted" style="margin-top:8px">最近备份：{{ lastBackup }}</div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppNav from '@/components/AppNav.vue'
import { adminApi } from '@/api'

const tab = ref('users')
const users = ref([])
const userTotal = ref(0)
const userKw = ref('')
const admins = ref([])
const adminForm = reactive({ username: '', password: '', nick: '' })
const shares = ref([])
const shareTotal = ref(0)
const shareKw = ref('')
const activities = ref([])
const actForm = reactive({ title: '', body: '', activityId: '' })
const backing = ref(false)
const lastBackup = ref('')

const fmt = (ms) => {
  const d = new Date(Number(ms))
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

async function loadUsers(p = 1) {
  const res = await adminApi.users({ keyword: userKw.value.trim(), page: p })
  if (res.status === 0) {
    users.value = res.data.rows || []
    userTotal.value = res.data.total || 0
  }
}

async function toggleUser(row) {
  const next = row.cn_user_status === 'disabled' ? 'normal' : 'disabled'
  const res = await adminApi.userStatus(row.cn_user_id, next)
  if (res.status === 0) { ElMessage.success(res.msg); await loadUsers() }
}

async function promote(row) {
  await ElMessageBox.confirm(`将「${row.cn_user_name}」提升为系统管理员？`, '提示', { type: 'warning' })
  const res = await adminApi.promote(row.cn_user_id)
  if (res.status === 0) { ElMessage.success(res.msg); await loadUsers() }
}

async function delUser(row) {
  await ElMessageBox.confirm(`删除用户「${row.cn_user_name}」及其全部数据？`, '警告', { type: 'error' })
  const res = await adminApi.deleteUser(row.cn_user_id)
  if (res.status === 0) { ElMessage.success(res.msg); await loadUsers() }
}

async function loadAdmins() {
  const res = await adminApi.admins()
  if (res.status === 0) admins.value = res.data || []
}

async function createAdmin() {
  if (!adminForm.username || adminForm.password.length < 6) {
    return ElMessage.warning('请填写用户名和不少于6位的密码')
  }
  const res = await adminApi.createAdmin({ ...adminForm })
  if (res.status === 0) {
    ElMessage.success(res.msg)
    adminForm.username = ''; adminForm.password = ''; adminForm.nick = ''
    await loadAdmins()
  }
}

async function loadShares(p = 1) {
  const res = await adminApi.shares({ keyword: shareKw.value.trim(), page: p })
  if (res.status === 0) {
    shares.value = res.data.rows || []
    shareTotal.value = res.data.total || 0
  }
}

async function toggleShare(row) {
  const next = row.cn_share_status === 'disabled' ? 'normal' : 'disabled'
  const res = await adminApi.shareStatus(row.cn_share_id, next)
  if (res.status === 0) { ElMessage.success(res.msg); await loadShares() }
}

async function delShare(row) {
  await ElMessageBox.confirm('删除该分享？前台将不再展示。', '提示', { type: 'warning' })
  const res = await adminApi.deleteShare(row.cn_share_id)
  if (res.status === 0) { ElMessage.success(res.msg); await loadShares() }
}

async function loadActivities() {
  const res = await adminApi.activities()
  if (res.status === 0) activities.value = res.data || []
}

function editActivity(row) {
  actForm.activityId = row.cn_activity_id
  actForm.title = row.cn_activity_title
  actForm.body = row.cn_activity_body || ''
}

async function saveActivity() {
  if (!actForm.title.trim()) return ElMessage.warning('请填写活动标题')
  const res = await adminApi.saveActivity({
    activityId: actForm.activityId || '',
    title: actForm.title.trim(),
    body: actForm.body.trim(),
    endTime: null
  })
  if (res.status === 0) {
    ElMessage.success(res.msg)
    actForm.activityId = ''; actForm.title = ''; actForm.body = ''
    await loadActivities()
  }
}

async function delActivity(row) {
  await ElMessageBox.confirm('删除活动将一并清理其投稿，确定？', '警告', { type: 'error' })
  const res = await adminApi.deleteActivity(row.cn_activity_id)
  if (res.status === 0) { ElMessage.success(res.msg); await loadActivities() }
}

async function backup() {
  backing.value = true
  try {
    const res = await adminApi.backup()
    if (res.status === 0) {
      lastBackup.value = res.data
      ElMessage.success('备份完成')
    }
  } finally {
    backing.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadAdmins(), loadShares(), loadActivities()])
})
</script>

<template>
  <div>
    <AppNav />
    <div class="page">
      <div class="card">
        <div style="display:flex;gap:8px;margin-bottom:10px">
          <el-input v-model="keyword" placeholder="搜索公开分享（标题或内容）" clearable @keyup.enter="search(1)" style="max-width:360px" />
          <el-button type="primary" @click="search(1)">搜索</el-button>
          <span class="muted" style="align-self:center">共 {{ total }} 条分享</span>
        </div>

        <el-row :gutter="10">
          <el-col :xs="24" :sm="24" :md="10" :lg="9">
            <el-table :data="rows" highlight-current-row @current-change="pickShare" style="cursor:pointer">
              <el-table-column prop="cn_share_title" label="标题" min-width="140" show-overflow-tooltip />
              <el-table-column prop="cn_share_author" label="分享者" width="100" />
            </el-table>
            <el-pagination v-if="total > size" layout="prev, pager, next" :total="total" :page-size="size"
                           :current-page="page" @current-change="search" style="margin-top:8px" small />
          </el-col>

          <el-col :xs="24" :sm="24" :md="14" :lg="15">
            <template v-if="detail">
              <h3>{{ detail.cn_share_title }}</h3>
              <div class="muted" style="margin-bottom:10px">分享者：{{ detail.cn_share_author || '佚名' }}</div>
              <div class="rich-body card" v-html="detail.cn_share_body || '<p class=\'muted\'>该分享暂无内容</p>'"></div>
              <div style="margin-top:10px">
                <el-button type="primary" :disabled="collected" @click="collect">
                  {{ collected ? '已收藏' : '收藏该分享' }}
                </el-button>
                <span class="muted" style="margin-left:8px">{{ userStore.isLogin ? '收藏后将保存到我的「收藏笔记本」' : '登录后可收藏该分享' }}</span>
              </div>
            </template>
            <el-empty v-else description="点击左侧分享查看详情" />
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppNav from '@/components/AppNav.vue'
import { shareApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const detail = ref(null)
const collected = ref(false)

async function search(p) {
  page.value = p || 1
  const res = await shareApi.publicPage({ keyword: keyword.value.trim(), page: page.value })
  if (res.status === 0) {
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  }
}

async function pickShare(row) {
  collected.value = false
  const res = await shareApi.publicDetail(row.cn_share_id)
  if (res.status === 0) detail.value = res.data
}

async function collect() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录或注册后再收藏')
    router.push({ path: '/login', query: { redirect: '/shares' } })
    return
  }
  const res = await shareApi.collect(detail.value.cn_share_id)
  if (res.status === 0) {
    collected.value = true
    ElMessage.success(res.msg)
  }
}

onMounted(() => search(1))
</script>

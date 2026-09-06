<template>
  <div>
    <AppNav />
    <div class="page">
      <h3><el-icon><Trophy /></el-icon> 社区活动（{{ activities.length }}）</h3>
      <el-row :gutter="12">
        <el-col v-for="a in activities" :key="a.cn_activity_id" :xs="12" :sm="8" :md="6" style="margin-bottom:12px">
          <el-card shadow="hover" class="pointer" @click="open(a)">
            <div style="font-weight:bold">{{ a.cn_activity_title }}</div>
            <div class="muted" style="margin-top:6px">{{ a.cn_activity_body || '（暂无介绍）' }}</div>
            <div class="muted" style="margin-top:8px">
              {{ a.cn_activity_end_time ? '截止 ' + fmt(a.cn_activity_end_time) : '长期有效' }}
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!activities.length" description="暂无活动" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Trophy } from '@element-plus/icons-vue'
import AppNav from '@/components/AppNav.vue'
import { activityApi } from '@/api'

const router = useRouter()
const activities = ref([])

function fmt(ms) {
  const d = new Date(Number(ms))
  const p = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

function open(a) {
  router.push(`/activities/${a.cn_activity_id}`)
}

onMounted(async () => {
  const res = await activityApi.list()
  if (res.status === 0) activities.value = res.data || []
})
</script>

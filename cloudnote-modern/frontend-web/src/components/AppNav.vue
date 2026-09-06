<template>
  <el-menu mode="horizontal" :default-active="active" router class="nav-bar" :ellipsis="false">
    <el-menu-item index="/shares" route="/shares">
      <el-icon><Notebook /></el-icon>&nbsp;云笔记
    </el-menu-item>
    <el-menu-item index="/shares" route="/shares">分享广场</el-menu-item>
    <el-menu-item index="/activities" route="/activities">社区活动</el-menu-item>
    <el-menu-item v-if="userStore.isLogin" index="/notes" route="/notes">我的笔记</el-menu-item>
    <el-menu-item v-if="userStore.isAdmin" index="/admin" route="/admin">后台管理</el-menu-item>
    <div class="nav-right">
      <template v-if="userStore.isLogin">
        <span class="muted">你好，{{ userStore.user?.cn_user_nick || userStore.user?.cn_user_name }}</span>
        <el-button size="small" @click="doLogout">退出登录</el-button>
      </template>
      <template v-else>
        <el-button size="small" type="primary" @click="$router.push('/login')">登录</el-button>
        <el-button size="small" @click="$router.push('/register')">注册</el-button>
      </template>
    </div>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Notebook } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const active = computed(() => {
  if (route.path.startsWith('/activities')) return '/activities'
  if (route.path.startsWith('/notes')) return '/notes'
  if (route.path.startsWith('/admin')) return '/admin'
  return '/shares'
})
function doLogout() {
  userStore.logout()
  router.push('/shares')
}
</script>

<style scoped>
.nav-bar { display: flex; align-items: center; }
.nav-right { margin-left: auto; display: flex; align-items: center; gap: 6px; padding-right: 10px; }
</style>

<template>
  <div class="nav-bar">
    <div class="nav-brand pointer" @click="$router.push('/shares')">
      <el-icon :size="18"><Notebook /></el-icon><b>&nbsp;云笔记</b>
    </div>

    <!-- 桌面端导航 -->
    <el-menu class="nav-links only-desktop" mode="horizontal" :default-active="active" router :ellipsis="false">
      <el-menu-item index="/shares" route="/shares">分享广场</el-menu-item>
      <el-menu-item index="/activities" route="/activities">社区活动</el-menu-item>
      <el-menu-item v-if="userStore.isLogin" index="/notes" route="/notes">我的笔记</el-menu-item>
      <el-menu-item v-if="userStore.isAdmin" index="/admin" route="/admin">后台管理</el-menu-item>
    </el-menu>

    <div class="nav-right">
      <template v-if="userStore.isLogin">
        <span class="muted only-desktop">你好，{{ userStore.user?.cn_user_nick || userStore.user?.cn_user_name }}</span>
        <el-button size="small" @click="doLogout">退出</el-button>
      </template>
      <template v-else>
        <el-button size="small" type="primary" @click="$router.push('/login')">登录</el-button>
        <el-button class="only-desktop" size="small" @click="$router.push('/register')">注册</el-button>
      </template>
      <!-- 移动端汉堡菜单 -->
      <el-button class="only-mobile" size="small" :icon="Menu" circle @click="drawer = true" />
    </div>

    <!-- 移动端抽屉导航 -->
    <el-drawer v-model="drawer" direction="ltr" size="220px" title="云笔记">
      <el-menu :default-active="active" @select="onDrawerSelect">
        <el-menu-item index="/shares">分享广场</el-menu-item>
        <el-menu-item index="/activities">社区活动</el-menu-item>
        <el-menu-item v-if="userStore.isLogin" index="/notes">我的笔记</el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin">后台管理</el-menu-item>
        <el-menu-item v-if="!userStore.isLogin" index="/register">注册</el-menu-item>
      </el-menu>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Notebook, Menu } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const drawer = ref(false)
const active = computed(() => {
  if (route.path.startsWith('/activities')) return '/activities'
  if (route.path.startsWith('/notes')) return '/notes'
  if (route.path.startsWith('/admin')) return '/admin'
  return '/shares'
})
function onDrawerSelect(index) {
  drawer.value = false
  if (['/shares', '/activities', '/notes', '/admin', '/register'].includes(index)) {
    router.push(index)
  }
}
function doLogout() {
  userStore.logout()
  router.push('/shares')
}
</script>

<style scoped>
.nav-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e7ecf0;
  padding: 0 12px;
  min-height: 50px;
  gap: 8px;
}
.nav-brand { display: flex; align-items: center; color: #0e7d76; white-space: nowrap; }
.nav-links { flex: 1; border-bottom: none !important; }
.nav-right { margin-left: auto; display: flex; align-items: center; gap: 6px; }

/* 默认(移动优先): 只显示汉堡与品牌/关键按钮 */
.only-desktop { display: none !important; }
.only-mobile { display: inline-flex !important; }

@media (min-width: 768px) {
  .only-desktop { display: inline-flex !important; }
  .only-mobile { display: none !important; }
}
</style>

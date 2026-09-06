<template>
  <el-container class="full">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-title">云笔记 · {{ isAdminMode ? '管理员登录' : '用户登录' }}</div>
      </template>
      <el-form @submit.prevent="submit" label-width="70px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
            {{ isAdminMode ? '管理员登录' : '登录' }}
          </el-button>
        </el-form-item>
        <el-form-item v-if="!isAdminMode">
          <el-button link type="primary" @click="$router.push('/register')">没有账号？立即注册</el-button>
          <el-button link @click="isAdminMode = true">管理员登录</el-button>
        </el-form-item>
        <el-form-item v-else>
          <el-button link @click="isAdminMode = false">返回用户登录</el-button>
        </el-form-item>
        <div class="muted" style="text-align:center">
          游客无需登录，可直接到「分享广场 / 社区活动」浏览
        </div>
      </el-form>
    </el-card>
  </el-container>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const isAdminMode = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const user = await userStore.login(form.username, form.password)
    if (isAdminMode.value && user.cn_user_role !== 'admin') {
      ElMessage.warning('该账号不是系统管理员')
      userStore.logout()
      return
    }
    ElMessage.success('登录成功')
    const target = user.cn_user_role === 'admin' ? '/admin' : (route.query.redirect || '/notes')
    router.push(target)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.full { height: 100vh; display: flex; align-items: center; justify-content: center; background: #eef3f7; }
.auth-card { width: 380px; max-width: 92vw; }
.auth-title { font-weight: bold; text-align: center; font-size: 18px; }
</style>

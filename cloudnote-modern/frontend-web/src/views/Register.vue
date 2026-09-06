<template>
  <el-container class="full">
    <el-card class="auth-card">
      <template #header><div class="auth-title">注册云笔记账号</div></template>
      <el-form label-width="70px">
        <el-form-item label="用户名"><el-input v-model="form.username" placeholder="2-50个字符" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nick" placeholder="选填" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="至少6位" /></el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="submit">注 册</el-button>
        </el-form-item>
        <div style="text-align:center">
          <el-button link type="primary" @click="$router.push('/login')">已有账号？去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </el-container>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', nick: '', password: '', confirm: '' })

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码长度不能小于6位')
    return
  }
  if (form.password !== form.confirm) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await userStore.register({ username: form.username, password: form.password, nick: form.nick })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.full { height: 100vh; display: flex; align-items: center; justify-content: center; background: #eef3f7; }
.auth-card { width: 400px; max-width: 92vw; }
.auth-title { font-weight: bold; text-align: center; font-size: 18px; }
</style>

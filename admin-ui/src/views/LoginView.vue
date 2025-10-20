<template>
  <section class="login">
    <div class="card">
      <h2>管理端登录</h2>
      <form @submit.prevent="onSubmit">
        <el-input v-model="tenantId" placeholder="租户ID" type="number" class="mb" />
        <el-input v-model="username" placeholder="用户名" class="mb" />
        <el-input v-model="password" placeholder="密码" show-password class="mb" />
        <el-button type="primary" native-type="submit" :loading="loading">登录</el-button>
      </form>
      <p v-if="error" class="err">{{ error }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const tenantId = ref<number | null>(1);
const username = ref('platform-admin');
const password = ref('Admin@123');
const loading = ref(false);
const error = ref('');

async function onSubmit() {
  loading.value = true;
  error.value = '';
  try {
    const resp = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tenantId: tenantId.value, username: username.value, password: password.value })
    });
    const data = await resp.json();
    if (!resp.ok || data.code !== 'OK') {
      throw new Error(data.message || '登录失败');
    }
    const token = data.data.accessToken;
    localStorage.setItem('access_token', token);
    router.replace('/');
  } catch (e:any) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login { display:flex; align-items:center; justify-content:center; min-height: 100vh; }
.card { width: 360px; padding: 24px; border-radius: 12px; background: #fff; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.mb { margin-bottom: 12px; }
.err { color: #e74c3c; margin-top: 8px; }
</style>


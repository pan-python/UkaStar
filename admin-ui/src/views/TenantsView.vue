<template>
  <div>
    <h2>租户管理</h2>
    <el-table :data="tenants" style="width:100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="tenant_code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const tenants = ref<any[]>([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    const resp = await fetch('/api/tenants', { headers: authHeaders() });
    const data = await resp.json();
    tenants.value = Array.isArray(data) ? data : (data?.data || []);
  } finally {
    loading.value = false;
  }
});

function authHeaders() {
  const t = localStorage.getItem('access_token') || '';
  return { 'Authorization': `Bearer ${t}` };
}
</script>

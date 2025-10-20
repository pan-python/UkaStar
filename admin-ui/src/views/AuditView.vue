<template>
  <div>
    <h2>审计日志</h2>
    <div class="toolbar">
      <el-input v-model.number="tenantId" type="number" placeholder="租户ID" class="mr"/>
      <el-input v-model="eventType" placeholder="事件类型（可选）" class="mr"/>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="rows" style="width:100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="eventType" label="类型" width="160"/>
      <el-table-column prop="actor" label="操作者" width="160"/>
      <el-table-column prop="target" label="目标"/>
      <el-table-column prop="summary" label="摘要"/>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { get } from '../api';

const tenantId = ref<number>(1);
const eventType = ref('');
const rows = ref<any[]>([]);
const loading = ref(false);

async function load(){
  loading.value=true;
  try{
    if (eventType.value) {
      rows.value = await get(`/api/audit/type/${encodeURIComponent(eventType.value)}`) || [];
    } else {
      rows.value = await get(`/api/audit?tenantId=${tenantId.value}`) || [];
    }
  } finally { loading.value=false; }
}
</script>

<style scoped>
.toolbar { display:flex; gap:8px; margin-bottom: 12px; }
.mr { width: 180px; }
</style>


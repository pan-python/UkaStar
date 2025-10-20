<template>
  <div>
    <h2>积分记录与导出</h2>
    <div class="toolbar">
      <el-input v-model.number="tenantId" type="number" placeholder="租户ID" class="mr"/>
      <el-input v-model.number="childId" type="number" placeholder="孩子ID（可选）" class="mr"/>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="exportCsv" v-perm="'PERM_EXPORT_EXECUTE'">导出CSV</el-button>
    </div>
    <section class="summary">
      <el-card class="card">
        <div class="metric"><span>今日记录数</span><b>{{ stats.todayCount }}</b></div>
        <div class="metric"><span>今日净分</span><b>{{ stats.todayNetScore }}</b></div>
        <div class="metric"><span>孩子数</span><b>{{ stats.totalChildren }}</b></div>
        <div class="metric"><span>累计积分</span><b>{{ stats.totalPoints }}</b></div>
      </el-card>
      <el-card class="card"><div id="chart" style="height:300px;"/></el-card>
    </section>
    <el-table :data="records" style="width:100%" v-loading="loading" @selection-change="onSelection">
      <el-table-column type="selection" width="40"/>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="childId" label="孩子ID" width="100"/>
      <el-table-column prop="actionType" label="类型" width="120"/>
      <el-table-column prop="amount" label="分值" width="100"/>
      <el-table-column prop="balanceAfter" label="余额" width="100"/>
      <el-table-column prop="occurredAt" label="时间"/>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { get, post } from '../api';

const tenantId = ref<number>(1);
const childId = ref<number | null>(null);
const records = ref<any[]>([]);
const loading = ref(false);
const selected = ref<any[]>([]);
const stats = ref<any>({ todayCount:0, todayNetScore:0, totalChildren:0, totalPoints:0, weeklyNetScore:0 });

function onSelection(rows:any[]){ selected.value = rows; }

async function load(){
  loading.value = true;
  try {
    const qs = childId.value ? `childId=${childId.value}` : `tenantId=${tenantId.value}`;
    const rs = await get(`/api/points/records?${qs}`);
    records.value = rs || [];
    // 统计
    stats.value = await get(`/api/points/statistics?tenantId=${tenantId.value}`) || stats.value;
    // 图表
    drawChart();
  } finally { loading.value=false; }
}

async function exportCsv(){
  const resp = await post('/api/export/points', { tenantId: tenantId.value });
  const { data } = resp || {};
  if (!data) return;
  const link = document.createElement('a');
  link.href = 'data:text/csv;base64,' + data.base64;
  link.download = data.filename || 'export.csv';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function drawChart(){
  const el = document.getElementById('chart');
  if (!el || !(window as any).echarts) return;
  const chart = (window as any).echarts.init(el);
  // 过去7天的净分趋势（从 records 聚合）
  const now = new Date();
  const days:string[] = [];
  const map:Record<string, number> = {};
  for (let i=6;i>=0;i--){
    const d = new Date(now.getTime()-i*24*3600*1000);
    const k = d.toISOString().slice(0,10);
    days.push(k);
    map[k] = 0;
  }
  for (const r of records.value){
    const k = (r.occurredAt || '').slice(0,10);
    if (k in map){
      const sign = (r.actionType === 'AWARD') ? 1 : -1;
      map[k] += sign * (r.amount || 0);
    }
  }
  const series = days.map(d => map[d] || 0);
  chart.setOption({
    tooltip:{},
    xAxis:{ type:'category', data: days },
    yAxis:{ type:'value' },
    series:[{ type:'bar', data: series, itemStyle:{ color:'#4e73df' } }]
  });
}

onMounted(()=>{ watch([tenantId, childId], ()=>{}, { immediate:false }); });
</script>

<style scoped>
.toolbar { display:flex; gap:8px; margin-bottom: 12px; }
.mr { width: 180px; }
.summary { display:grid; grid-template-columns: 1fr; gap:12px; margin-bottom:12px; }
.card { }
.metric { display:flex; justify-content:space-between; padding:4px 0; }
</style>

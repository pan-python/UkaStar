<template>
  <div>
    <h2>兑换项</h2>
    <div class="toolbar">
      <el-input v-model.number="tenantId" type="number" placeholder="租户ID" class="mr"/>
      <el-button type="primary" @click="load">加载</el按钮>
      <el-button @click="openCreate" v-perm="'PERM_CATALOG_MANAGE'">新建兑换项</el按钮>
    </div>
    <el-table :data="rewards" style="width:100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="name" label="名称"/>
      <el-table-column prop="cost" label="积分" width="120"/>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="小" @click="edit(row)" v-perm="'PERM_CATALOG_MANAGE'">编辑</el按钮>
          <el-button size="小" type="danger" @click="remove(row)" v-perm="'PERM_CATALOG_MANAGE'">删除</el按钮>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" title="兑换项" width="420px">
      <el-form label-width="96px">
        <el-form-item label="名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="积分"><el-input v-model.number="form.cost" type="number"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { get, post, put, del } from '../api';

const tenantId = ref<number>(1);
const rewards = ref<any[]>([]);
const loading = ref(false);
const dlg = ref(false);
const form = ref<any>({ id:null, name:'', cost:0 });

async function load(){ loading.value=true; try { rewards.value = await get(`/api/catalog/rewards?tenantId=${tenantId.value}`) || []; } finally { loading.value=false; } }
function openCreate(){ form.value={ id:null, name:'', cost:0}; dlg.value=true; }
function edit(row:any){ form.value = { id: row.id, name: row.name, cost: row.cost }; dlg.value=true; }
async function save(){ if(!form.value.id){ await post('/api/catalog/rewards', { tenantId: tenantId.value, name: form.value.name, cost: form.value.cost, systemDefault:false }); } else { await put(`/api/catalog/rewards/${form.value.id}`, { name: form.value.name, cost: form.value.cost }); } dlg.value=false; await load(); }
async function remove(row:any){ await del(`/api/catalog/rewards/${row.id}`); await load(); }
</script>

<style scoped>
.toolbar { display:flex; gap:8px; margin-bottom: 12px; }
.mr { width: 160px; }
</style>

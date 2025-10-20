<template>
  <div>
    <h2>账号与角色</h2>
    <section class="toolbar">
      <el-input v-model="search" placeholder="搜索用户名" class="mr" clearable />
      <el-button type="primary" @click="openCreate" v-perm="'PERM_ACCOUNT_MANAGE'">新建账号</el-button>
    </section>
    <el-table :data="filtered" style="width:100%" v-loading="loading" @selection-change="onSelection">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="tenantId" label="租户ID" width="100" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="active" label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.active" @change="(v:any)=>toggle(row,v)" v-perm="'PERM_ACCOUNT_MANAGE'" />
        </template>
      </el-table-column>
      <el-table-column label="角色">
        <template #default="{ row }">
          <el-select v-model="row.roleCodes" multiple filterable @change="(v:any)=>updateRoles(row)" v-perm="'PERM_ACCOUNT_MANAGE'">
            <el-option v-for="r in roles" :key="r.code" :label="r.name" :value="r.code" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreate" title="新建账号" width="400px">
      <el-form label-width="96px">
        <el-form-item label="租户ID"><el-input v-model.number="form.tenantId" type="number"/></el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username"/></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" show-password/></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCodes" multiple filterable>
            <el-option v-for="r in roles" :key="r.code" :label="r.name" :value="r.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue';
import { get, post, put } from '../api';

const loading = ref(false);
const accounts = ref<any[]>([]);
const roles = ref<any[]>([]);
const selected = ref<any[]>([]);
const search = ref('');
const showCreate = ref(false);
const form = ref({ tenantId: 1, username: '', password: '', roleCodes: [] as string[] });

const filtered = computed(()=> accounts.value.filter(a => !search.value || a.username?.includes(search.value)));

function onSelection(rows:any[]) { selected.value = rows; }
function openCreate(){ showCreate.value = true; }

async function load(){
  loading.value = true;
  try {
    const acc = await get('/api/accounts');
    accounts.value = acc || [];
    roles.value = await get('/api/accounts/roles');
  } finally { loading.value = false; }
}

async function toggle(row:any, active:boolean){
  await put(`/api/accounts/${row.id}/toggle`, { active });
  row.active = active;
}

async function updateRoles(row:any){
  await put(`/api/accounts/${row.id}/roles`, { roleCodes: row.roleCodes });
}

async function create(){
  await post('/api/accounts', form.value);
  showCreate.value = false;
  await load();
}

onMounted(load);
</script>

<style scoped>
.toolbar { display:flex; gap:8px; margin-bottom: 12px; }
.mr { width: 240px; }
</style>

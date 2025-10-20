<template>
  <div>
    <h2>类别与项目</h2>
    <div class="toolbar">
      <el-input v-model.number="tenantId" type="number" placeholder="租户ID" class="mr"/>
      <el-button type="primary" @click="loadAll">加载</el按钮>
      <el-button @click="openCreateCategory" v-perm="'PERM_CATALOG_MANAGE'">新建类别</el-button>
      <el-button @click="openCreateItem" v-perm="'PERM_CATALOG_MANAGE'">新建项目</el-button>
    </div>
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card>
          <h3>类别</h3>
          <el-table :data="categories" style="width:100%" v-loading="loadingCat">
            <el-table-column prop="id" label="ID" width="80"/>
            <el-table-column prop="name" label="名称"/>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <h3>项目</h3>
          <el-table :data="items" style="width:100%" v-loading="loadingItem">
            <el-table-column prop="id" label="ID" width="80"/>
            <el-table-column prop="categoryId" label="类别ID" width="100"/>
            <el-table-column prop="name" label="名称"/>
            <el-table-column prop="score" label="分值" width="100"/>
            <el-table-column prop="positive" label="正向" width="100"/>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="editItem(row)" v-perm="'PERM_CATALOG_MANAGE'">编辑</el-button>
                <el-button size="small" type="danger" @click="removeItem(row)" v-perm="'PERM_CATALOG_MANAGE'">删除</el按钮>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dlgCat" title="新建类别" width="400px">
      <el-form label-width="96px">
        <el-form-item label="名称"><el-input v-model="catForm.name"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgCat=false">取消</el-button>
        <el-button type="primary" @click="createCategory">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dlgItem" title="项目" width="500px">
      <el-form label-width="96px">
        <el-form-item label="类别ID"><el-input v-model.number="itemForm.categoryId" type="number"/></el-form-item>
        <el-form-item label="名称"><el-input v-model="itemForm.name"/></el-form-item>
        <el-form-item label="分值"><el-input v-model.number="itemForm.score" type="number"/></el-form-item>
        <el-form-item label="正向"><el-switch v-model="itemForm.positive"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgItem=false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { get, post, put, del } from '../api';

const tenantId = ref<number>(1);
const categories = ref<any[]>([]);
const items = ref<any[]>([]);
const loadingCat = ref(false);
const loadingItem = ref(false);

const dlgCat = ref(false);
const catForm = ref({ name: '', systemDefault: false });

const dlgItem = ref(false);
const itemForm = ref<any>({ id: null, categoryId: null, name: '', score: 0, positive: true });

async function loadAll() { await Promise.all([loadCategories(), loadItems()]); }
async function loadCategories(){ loadingCat.value=true; try { categories.value = await get(`/api/catalog/categories?tenantId=${tenantId.value}`) || []; } finally { loadingCat.value=false; } }
async function loadItems(){ loadingItem.value=true; try { items.value = await get(`/api/catalog/items?tenantId=${tenantId.value}`) || []; } finally { loadingItem.value=false; } }

function openCreateCategory(){ catForm.value = { name: '', systemDefault: false }; dlgCat.value=true; }
async function createCategory(){ await post('/api/catalog/categories', { tenantId: tenantId.value, name: catForm.value.name, systemDefault: false}); dlgCat.value=false; await loadCategories(); }

function openCreateItem(){ itemForm.value = { id:null, categoryId: null, name:'', score:0, positive:true}; dlgItem.value=true; }
function editItem(row:any){ itemForm.value = { id: row.id, categoryId: row.categoryId, name: row.name, score: row.score, positive: row.positive }; dlgItem.value=true; }
async function saveItem(){
  if (!itemForm.value.id) {
    await post('/api/catalog/items', { tenantId: tenantId.value, categoryId: itemForm.value.categoryId, name: itemForm.value.name, score: itemForm.value.score, positive: itemForm.value.positive, systemDefault:false });
  } else {
    await put(`/api/catalog/items/${itemForm.value.id}`, { name: itemForm.value.name, score: itemForm.value.score, positive: itemForm.value.positive });
  }
  dlgItem.value=false; await loadItems();
}
async function removeItem(row:any){ await del(`/api/catalog/items/${row.id}`); await loadItems(); }
</script>

<style scoped>
.toolbar { display:flex; gap:8px; margin-bottom: 12px; }
.mr { width: 160px; }
</style>

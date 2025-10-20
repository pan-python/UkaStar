<template>
  <el-config-provider :size="size" :locale="locale">
    <div class="layout">
      <aside class="sider">
        <h2 class="brand">UkaStar</h2>
        <el-menu :default-active="active" @select="onSelect">
          <el-menu-item v-for="m in menuItems" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
        </el-menu>
      </aside>
      <main class="content">
        <header class="toolbar">
          <div />
          <el-button type="danger" size="small" @click="logout">退出登录</el-button>
        </header>
        <section class="page">
          <router-view />
        </section>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
import { useAppStore } from "./stores/app";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from './stores/auth';

const store = useAppStore();
const router = useRouter();

const size = computed(() => store.size);
const locale = zhCn;

const route = useRoute();
const auth = useAuthStore();
onMounted(()=> auth.fetchProfile());

const menuItems = computed(()=> filterMenu([
  { label:'首页', path:'/', perm:'MENU_DASHBOARD_OVERVIEW' },
  { label:'租户管理', path:'/tenants', perm:'PERM_TENANT_MANAGE' },
  { label:'账号与角色', path:'/accounts', perm:'PERM_ACCOUNT_MANAGE' },
  { label:'类别与项目', path:'/catalog', perm:'PERM_CATALOG_MANAGE' },
  { label:'兑换项', path:'/rewards', perm:'PERM_CATALOG_MANAGE' },
  { label:'积分记录', path:'/points', perm:'PERM_POINTS_VIEW' },
  { label:'审计日志', path:'/audit', perm:'PERM_AUDIT_VIEW' },
  { label:'系统配置', path:'/config', perm:'MENU_SYSTEM_CENTER' }
]));

function filterMenu(items: any[]){
  if (!auth.profile) return items; // 未取到画像时全部展示
  return items.filter(i => !i.perm || auth.hasPerm(i.perm));
}

const active = computed(() => route.path);
function onSelect(index: string) { router.push(index); }
function logout() {
  localStorage.removeItem('access_token');
  router.replace('/login');
}
</script>

<style scoped>
.layout { display: flex; height: 100vh; }
.sider { width: 220px; background: #0f172a; color: #fff; padding: 12px; }
.brand { color: #fff; margin: 8px 0 12px; }
.content { flex:1; display: flex; flex-direction: column; }
.toolbar { height: 48px; display:flex; align-items:center; justify-content: flex-end; padding: 0 12px; border-bottom: 1px solid #eee; }
.page { padding: 16px; overflow:auto; }
</style>

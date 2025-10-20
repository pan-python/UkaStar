import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import HelloView from "../views/HelloView.vue";
import LoginView from "../views/LoginView.vue";
import TenantsView from "../views/TenantsView.vue";
import AccountsView from "../views/AccountsView.vue";
import CatalogView from "../views/CatalogView.vue";
import RewardsView from "../views/RewardsView.vue";
import PointsView from "../views/PointsView.vue";
import AuditView from "../views/AuditView.vue";
import ConfigView from "../views/ConfigView.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: HelloView
  },
  {
    path: "/login",
    name: "login",
    component: LoginView
  },
  {
    path: "/tenants",
    name: "tenants",
    component: TenantsView
  },
  { path: "/accounts", name: "accounts", component: AccountsView },
  { path: "/catalog", name: "catalog", component: CatalogView },
  { path: "/rewards", name: "rewards", component: RewardsView },
  { path: "/points", name: "points", component: PointsView },
  { path: "/audit", name: "audit", component: AuditView },
  { path: "/config", name: "config", component: ConfigView },
  {
    path: "/hello",
    name: "hello",
    component: HelloView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 简单路由守卫：未登录跳转登录页
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('access_token');
  if (to.name !== 'login' && !token) {
    next({ name: 'login' });
  } else if (to.name === 'login' && token) {
    next({ name: 'home' });
  } else {
    next();
  }
});

export default router;

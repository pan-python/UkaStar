import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import HelloView from "../views/HelloView.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: HelloView
  },
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

export default router;

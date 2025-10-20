<template>
  <el-config-provider :size="size" :locale="locale">
    <main class="container">
      <el-card shadow="hover">
        <h1>UkaStar 管理端</h1>
        <p>这是一个可运行的 Vite + Vue3 骨架页面。</p>
        <el-button type="primary" @click="goHello">访问 Hello 页面</el-button>
      </el-card>
      <router-view />
    </main>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from "vue";
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
import { useAppStore } from "./stores/app";
import { useRouter } from "vue-router";

const store = useAppStore();
const router = useRouter();

const size = computed(() => store.size);
const locale = zhCn;

const goHello = () => {
  router.push({ name: "hello" });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24px;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1f2937, #111827);
  color: #fff;
}
</style>

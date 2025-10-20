<template>
  <div>
    <h2>系统配置（成长树阈值）</h2>
    <el-alert type="info" show-icon title="后端尚未提供配置接口，暂仅本地演示。" class="mb"/>
    <el-form label-width="140px" class="form">
      <el-form-item label="bronze"><el-input v-model.number="cfg.bronze" type="number"/></el-form-item>
      <el-form-item label="silver"><el-input v-model.number="cfg.silver" type="number"/></el-form-item>
      <el-form-item label="gold"><el-input v-model.number="cfg.gold" type="number"/></el-form-item>
      <el-button type="primary" @click="saveLocal">保存到本地</el-button>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
const cfg = ref({ bronze: 100, silver: 300, gold: 600 });
onMounted(()=>{
  const raw = localStorage.getItem('growth_cfg');
  if (raw) { try { cfg.value = JSON.parse(raw);} catch {} }
});
function saveLocal(){ localStorage.setItem('growth_cfg', JSON.stringify(cfg.value)); }
</script>

<style scoped>
.mb{ margin-bottom: 12px; }
.form{ max-width: 420px; }
</style>


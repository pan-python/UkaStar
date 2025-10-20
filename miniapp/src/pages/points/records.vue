<template>
  <view class="page">
    <view class="toolbar">
      <input class="ipt" type="number" v-model.number="childId" placeholder="孩子ID"/>
      <button type="primary" @click="load">查询</button>
    </view>
    <view v-for="r in rows" :key="r.id" class="item">
      <text class="time">{{ r.occurredAt }}</text>
      <text class="text">{{ r.actionType }} {{ r.amount }} 分，余额 {{ r.balanceAfter }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { get } from '../../utils/api';

const childId = ref<number | null>(null);
const rows = ref<any[]>([]);

async function load(){
  const qs = childId.value ? `childId=${childId.value}` : '';
  const data:any = await get(`/api/points/records?${qs}`);
  rows.value = Array.isArray(data) ? data : (data||[]);
}
</script>

<style scoped>
.page{ padding:24rpx; }
.toolbar{ display:flex; gap: 12rpx; margin-bottom: 12rpx; }
.ipt{ background:#fff; padding:16rpx; border-radius:12rpx; }
.item{ background:#0b1220; color:#fff; padding:12rpx; border-radius:12rpx; margin-bottom:8rpx; display:flex; justify-content:space-between; }
.time{ opacity:.7; }
</style>


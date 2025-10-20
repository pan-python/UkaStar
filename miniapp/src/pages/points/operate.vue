<template>
  <view class="page">
    <view class="form">
      <input class="ipt" type="number" v-model.number="childId" placeholder="孩子ID"/>
      <input class="ipt" type="number" v-model.number="amount" placeholder="分值"/>
      <input class="ipt" v-model="reason" placeholder="备注"/>
      <view class="row">
        <button type="primary" @click="doOp('award')">加分</button>
        <button @click="doOp('deduct')">减分</button>
        <button @click="doOp('redeem')">兑换</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { post } from '../../utils/api';

const childId = ref<number | null>(null);
const amount = ref<number>(10);
const reason = ref<string>('小程序操作');

async function doOp(kind:'award'|'deduct'|'redeem'){
  if (!childId.value || amount.value<=0){ uni.showToast({ title:'请完善信息', icon:'none' }); return; }
  const payload:any = { childId: childId.value, operatorAccountId: 0, amount: amount.value, reason: reason.value };
  try{
    const resp:any = await post(`/api/points/${kind}`, payload);
    if (resp && resp.code === 'OK'){ uni.showToast({ title:'成功', icon:'success' }); }
    else { uni.showToast({ title:'失败', icon:'error' }); }
  }catch{ uni.showToast({ title:'异常', icon:'error' }); }
}
</script>

<style scoped>
.page{ padding:24rpx; }
.form{ display:flex; flex-direction:column; gap: 12rpx; }
.ipt{ background:#fff; padding:16rpx; border-radius:12rpx; }
.row{ display:flex; gap: 12rpx; }
</style>


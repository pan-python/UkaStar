<template>
  <view class="page">
    <view class="row">
      <view class="card"><text>今日记录</text><text class="num">{{ stats.todayCount }}</text></view>
      <view class="card"><text>今日净分</text><text class="num">{{ stats.todayNetScore }}</text></view>
    </view>
    <view class="row">
      <view class="card"><text>孩子数</text><text class="num">{{ stats.totalChildren }}</text></view>
      <view class="card"><text>累计积分</text><text class="num">{{ stats.totalPoints }}</text></view>
    </view>
    <view class="row">
      <input class="ipt" type="number" v-model.number="childId" placeholder="孩子ID"/>
      <button type="primary" @click="loadBalance">加载成长</button>
    </view>
    <view v-if="growth" class="grow">
      <text>当前阶段：{{ growth.stage }}（{{ growth.progress }}%）</text>
      <view class="bar"><view class="fill" :style="{ width: growth.progress + '%' }"/></view>
      <text>下一阶段目标：{{ growth.nextLabel }}</text>
    </view>
    <button type="primary" @click="goOperate">加减/兑换</button>
    <button @click="goRecords">查看记录</button>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { get } from '../../utils/api';

const stats = ref<any>({ todayCount:0, todayNetScore:0, totalChildren:0, totalPoints:0 });
const childId = ref<number | null>(Number(uni.getStorageSync('child_id')||0) || null);
const growth = ref<any|null>(null);

onMounted(load);

async function load(){
  const tenantId = 1;
  const data:any = await get(`/api/points/statistics?tenantId=${tenantId}`);
  if (data) stats.value = data;
}

function goOperate(){ uni.navigateTo({ url:'/pages/points/operate' }); }
function goRecords(){ uni.navigateTo({ url:'/pages/points/records' }); }

async function loadBalance(){
  if (!childId.value){ uni.showToast({ title:'请输入孩子ID', icon:'none' }); return; }
  uni.setStorageSync('child_id', childId.value);
  const bal:any = await get(`/api/points/balance?childId=${childId.value}`);
  const cfg:any = await get(`/api/configs/growth?tenantId=1`);
  const thresholds = (cfg && cfg.milestone_thresholds) || { bronze:100, silver:300, gold:600 };
  const pts = (bal?.data?.balance) ?? 0;
  // 简单阶段计算：bronze -> silver -> gold -> MAX
  const order = [ ['bronze', thresholds.bronze], ['silver', thresholds.silver], ['gold', thresholds.gold] ] as Array<[string,number]>;
  let stage = '新手', nextLabel='bronze', max = order[0][1], min = 0;
  for (let i=0;i<order.length;i++){
    const [label, val] = order[i];
    if (pts < val){ nextLabel = label; max = val; min = i>0? order[i-1][1]:0; break; }
    stage = label;
    if (i===order.length-1){ nextLabel='MAX'; min = val; max = val; }
  }
  const progress = nextLabel==='MAX' ? 100 : Math.max(0, Math.min(100, Math.round(((pts-min)/(max-min))*100)));
  growth.value = { pts, stage, nextLabel, progress };
}
</script>

<style scoped>
.page{ padding:24rpx; }
.row{ display:flex; gap: 16rpx; margin-bottom: 16rpx; }
.card{ flex:1; background:#0b1220; color:#fff; padding:16rpx; border-radius:12rpx; display:flex; flex-direction:column; gap:8rpx; align-items:center; }
.num{ font-size: 40rpx; font-weight:600; }
.ipt{ background:#fff; padding:16rpx; border-radius:12rpx; flex:1; }
.grow{ background:#0b1220; color:#fff; padding:16rpx; border-radius:12rpx; margin-bottom: 16rpx; }
.bar{ height: 16rpx; background:#111827; border-radius:8rpx; overflow:hidden; margin-top:8rpx; }
.fill{ height:100%; background:#1cc88a; }
</style>

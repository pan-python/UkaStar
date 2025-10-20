<template>
  <view class="page">
    <text class="title">优卡星小程序</text>
    <view v-if="!token" class="box">
      <button type="primary" @click="login">微信一键登录</button>
    </view>
    <view v-else class="box">
      <text>已登录，openid：{{ openid }}</text>
    </view>
  </view>
  
</template>

<script setup lang="ts">
import { ref } from 'vue';

const token = ref<string | null>(uni.getStorageSync('access_token') || '');
const openid = ref<string | null>(uni.getStorageSync('openid') || '');

async function login(){
  uni.login({
    provider: 'weixin',
    success: async (res) => {
      try{
        const { code } = res as any;
        const resp = await uni.request({
          url: '/api/miniapp/login',
          method: 'POST',
          data: { code, tenantId: 1 },
          header: { 'Content-Type': 'application/json' }
        });
        const data:any = resp.data as any;
        if (data && data.code === 'OK'){
          const { accessToken, refreshToken, openid:oid } = data.data;
          uni.setStorageSync('access_token', accessToken);
          uni.setStorageSync('refresh_token', refreshToken);
          uni.setStorageSync('openid', oid);
          token.value = accessToken; openid.value = oid;
          uni.showToast({ title: '登录成功', icon: 'success' });
        } else {
          uni.showToast({ title: '登录失败', icon: 'error' });
        }
      } catch (e){
        uni.showToast({ title: '请求异常', icon: 'error' });
      }
    },
    fail: () => uni.showToast({ title: 'wx.login 失败', icon: 'error' })
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  display: flex;
  gap: 16rpx;
  background-color: #0f172a;
  color: #ffffff;
}

.box { margin-top: 24rpx; }
.title {
  font-size: 48rpx;
  font-weight: 600;
}
</style>

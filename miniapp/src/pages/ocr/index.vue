<template>
  <view class="page">
    <button type="primary" @click="choose">选择图片</button>
    <image v-if="img" :src="img" mode="widthFix" style="width:100%; margin-top:12rpx;"/>
    <button v-if="img" @click="recognize">识别文字</button>
    <view v-if="text" class="result">{{ text }}</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const img = ref<string>('');
const text = ref<string>('');

function choose(){
  uni.chooseImage({ count:1, success: (res:any)=>{
    img.value = res.tempFilePaths[0];
  } });
}

async function recognize(){
  if (!img.value) return;
  const fs = uni.getFileSystemManager();
  fs.readFile({ filePath: img.value, encoding: 'base64', success: async (r:any)=>{
    try{
      const payload = { image: 'data:image/jpeg;base64,' + r.data };
      const token = uni.getStorageSync('access_token') || '';
      const [err, resp] = await uni.request({ url:'/api/ocr/base64', method:'POST', data: payload, header: { 'Content-Type':'application/json', 'Authorization': `Bearer ${token}` } }) as any;
      if (err) throw err;
      const data:any = resp.data;
      text.value = data?.text || '无结果';
    }catch{ uni.showToast({ title:'识别失败', icon:'error' }); }
  } });
}
</script>

<style scoped>
.page{ padding: 16rpx; }
.result{ margin-top: 12rpx; white-space: pre-wrap; }
</style>

<template>
  <view class="page">
    <view class="topbar">
      <button size="mini" @click="newSession">新会话</button>
      <button size="mini" @click="toggleList">会话列表</button>
      <text v-if="limit">上下文上限：{{ limit }} 条</text>
    </view>
    <view v-if="showList" class="sess">
      <view v-for="s in sessions" :key="s.sessionCode" class="item" @click="selectSession(s.sessionCode)">{{ s.title }} ({{ s.sessionCode.slice(0,8) }})</view>
    </view>
    <scroll-view class="list" scroll-y="true">
      <view v-for="(m,i) in msgs" :key="i" :class="['msg', m.role]">
        <text>{{ m.content }}</text>
      </view>
    </scroll-view>
    <view class="bar">
      <input class="ipt" v-model="text" placeholder="说点什么..."/>
      <button type="primary" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const msgs = ref<Array<{role:'user'|'assistant', content:string}>>([]);
const text = ref('');
let socket:any = null; let reconnectTimer:any=null;
const limit = ref<number|undefined>(undefined);
const sessions = ref<Array<{sessionCode:string; title:string}>>([]);
const showList = ref(false);

onMounted(()=> { loadHistory(); connect(); });
onUnmounted(()=>{ if (socket){ socket.close(); socket=null; } if (reconnectTimer){ clearTimeout(reconnectTimer);} });

function connect(){
  const token = uni.getStorageSync('access_token');
  const session = uni.getStorageSync('chat_session') || '';
  const host = '';
  const url = `/ws?token=${encodeURIComponent(token)}&session=${encodeURIComponent(session)}`;
  socket = uni.connectSocket({ url, protocols: [], success: ()=>{} });
  socket.onOpen(()=>{});
  socket.onMessage((e:any)=> { msgs.value.push({ role:'assistant', content: String(e.data||'') }); });
  socket.onClose(()=>{ reconnectTimer = setTimeout(()=> connect(), 3000); });
}

function send(){
  if (!text.value || !socket) return;
  socket.send({ data: text.value });
  msgs.value.push({ role:'user', content: text.value });
  text.value='';
}

async function loadHistory(){
  try{
    const token = uni.getStorageSync('access_token');
    const session = uni.getStorageSync('chat_session') || '';
    const [err, resp] = await uni.request({ url:`/api/chat/history?session=${encodeURIComponent(session)}`, method:'GET', header:{ 'Authorization': `Bearer ${token}` } }) as any;
    if (err) return;
    const data:any = resp.data;
    msgs.value = (data?.messages || []).map((m:any)=>({ role: (m.role||'USER').toLowerCase(), content: m.content }));
    if (typeof data?.maxContext === 'number') limit.value = data.maxContext;
  }catch{}
}

async function newSession(){
  try{
    const token = uni.getStorageSync('access_token');
    const [err, resp] = await uni.request({ url:'/api/chat/sessions', method:'POST', data:{ title:'小程序会话' }, header:{ 'Authorization': `Bearer ${token}`, 'Content-Type':'application/json' } }) as any;
    if (err) return;
    const data:any = resp.data;
    if (data && data.sessionCode){
      uni.setStorageSync('chat_session', data.sessionCode);
      msgs.value = [];
      if (socket){ socket.close(); socket=null; }
      connect();
    }
  }catch{}
}

function toggleList(){ showList.value = !showList.value; if (showList.value) loadSessions(); }
async function loadSessions(){
  try{
    const token = uni.getStorageSync('access_token');
    const [err, resp] = await uni.request({ url:'/api/chat/sessions', method:'GET', header:{ 'Authorization': `Bearer ${token}` } }) as any;
    if (err) return;
    const data:any = resp.data;
    sessions.value = data?.sessions || [];
  }catch{}
}
function selectSession(code:string){
  uni.setStorageSync('chat_session', code);
  msgs.value = [];
  if (socket){ socket.close(); socket=null; }
  connect();
  showList.value=false;
}
</script>

<style scoped>
.page{ padding:0; display:flex; flex-direction:column; height:100vh; }
.topbar{ display:flex; justify-content: space-between; padding: 8rpx 12rpx; border-bottom: 1px solid #eee; }
.list{ flex:1; padding: 16rpx; }
.sess{ position:absolute; top:60rpx; left:0; right:0; background:#fff; border:1px solid #eee; z-index:2; }
.sess .item{ padding: 12rpx; border-bottom: 1px solid #f0f0f0; }
.msg{ margin-bottom: 8rpx; padding: 12rpx; border-radius: 8rpx; }
.msg.user{ background:#1e293b; color:#fff; align-self:flex-end; }
.msg.assistant{ background:#e2e8f0; color:#111827; align-self:flex-start; }
.bar{ display:flex; gap: 12rpx; padding: 12rpx; border-top: 1px solid #eee; }
.ipt{ flex:1; background:#fff; padding: 12rpx; border-radius: 8rpx; }
</style>

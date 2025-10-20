# miniapp 模块

uni-app x 微信小程序骨架，提供示例首页展示文本。

## 快速开始

> 需安装 `@dcloudio/uni-app` CLI，可使用 `pnpm dlx @dcloudio/uni-app`。

```bash
cd miniapp
pnpm install
pnpm run dev:mp-weixin
```

构建成功后可在微信开发者工具中导入 `dist/dev/mp-weixin` 目录预览。

## 目录结构

- `pages.json`：页面路由配置。
- `src/pages/index/index.vue`：示例首页。
- `src/main.ts`：应用入口，注册 Pinia。

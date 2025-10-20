# admin-ui 模块

基于 Vite + Vue3 + Element Plus 的管理端骨架，包含演示页面和路由。

## 快速开始

```bash
cd admin-ui
pnpm install   # 或 npm/yarn 安装依赖
pnpm dev
```

访问 `http://localhost:5173` 可看到示例页面，点击按钮可跳转到 Hello 路由。

## 目录结构

- `src/main.ts`：应用入口，挂载 Element Plus、Pinia 与路由。
- `src/router`：示例路由配置。
- `src/views/HelloView.vue`：演示页面。

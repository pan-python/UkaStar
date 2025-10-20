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

## 联调说明

1. 先在登录页输入租户ID/用户名/密码（默认可用：租户ID=1，用户名=`platform-admin`，密码=`Admin@123`）。
2. 已提供页面：租户、账号与角色、类别与项目、兑换项、积分记录与导出、审计日志、系统配置（本地演示）。
3. 请求默认走同域 `/api/*`，需由反向代理（如 Traefik/Nginx）转发到后端。

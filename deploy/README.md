# deploy 模块

提供基于 Docker Compose + Traefik 的一键启动样例（含 HTTPS/WSS 自动证书配置示例）。

注意：自动签发证书需要公共可解析域名与 80/443 端口放通；本样例用于结构说明，实际域名请在 `.env` 内替换。

## 快速开始

```bash
cd deploy
cp .env.example .env
# 编辑 .env 中的 ACME_EMAIL、BASE_DOMAIN、BACKEND_DOMAIN、OCR_DOMAIN 等

# 初始化 Traefik ACME 存储文件权限
mkdir -p traefik && touch traefik/acme.json && chmod 600 traefik/acme.json

docker compose up -d --build
```

服务说明：
- Traefik：80/443 入口；自动证书（Let’s Encrypt，HTTP Challenge）。
- backend-service：Spring Boot WebFlux 服务，通过 `https://$BACKEND_DOMAIN` 暴露。
- ocr-service：FastAPI OCR 服务，通过 `https://$OCR_DOMAIN` 暴露，需携带服务间 Token。
- mysql：开发用 MySQL 8，数据持久化到 `deploy/data/mysql`。

挂载目录：
- `deploy/traefik/acme.json`：证书存储（需 `chmod 600`）。
- `deploy/data/ocr-tmp`：OCR 临时文件（宿主机可查看问题样例）。

## 常见问题
- 本地开发环境无公网与域名，ACME 无法签发证书，可临时只用 80 端口（web）进行联调或通过自签名证书方案。
- 生产环境请限制 Traefik Dashboard 暴露或加鉴权。

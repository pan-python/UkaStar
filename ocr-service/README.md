# ocr-service 模块

基于 FastAPI 的 OCR 独立服务，集成 EasyOCR，支持 base64 图片识别，临时文件落宿主机目录（便于排障），支持服务间鉴权 Token。

## 快速开始（本地）

```bash
cd ocr-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

export OCR_SERVICE_TOKEN=dev-token-please-change
export OCR_TMP_DIR=/tmp/ukastar-ocr
uvicorn app.main:app --host 0.0.0.0 --port 9000
```

接口：
- 健康检查：`GET /healthz`
- OCR 识别：`POST /ocr/base64`

请求体：
```json
{
  "image_base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."
}
```

鉴权方式（二选一）：
- `Authorization: Bearer <OCR_SERVICE_TOKEN>`
- `X-Internal-Token: <OCR_SERVICE_TOKEN>`

响应体（成功）：
```json
{
  "text": "识别出的文本...",
  "elapsed_ms": 123,
  "filename": "1739999999999_abcdef.jpg"
}
```

错误结构（示例）：
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "invalid token"
  }
}
```

## Docker 运行

```bash
docker build -t ukastar/ocr-service:dev ./ocr-service
docker run --rm -p 9000:9000 \
  -e OCR_SERVICE_TOKEN=dev-token-please-change \
  -e OCR_TMP_DIR=/data/ocr-tmp \
  -v $(pwd)/.data/ocr-tmp:/data/ocr-tmp \
  ukastar/ocr-service:dev
```

## 目录结构

- `app/main.py`：FastAPI 应用入口与路由定义（含 EasyOCR 集成与鉴权）。
- `requirements.txt`：依赖列表。
- `Dockerfile`：容器镜像构建文件（安装 EasyOCR 运行依赖）。

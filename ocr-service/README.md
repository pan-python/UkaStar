# ocr-service 模块

FastAPI 骨架服务，预留 OCR 推理接口。

## 快速开始

```bash
cd ocr-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

服务默认监听 `http://0.0.0.0:9000`。
- 健康检查：`GET /healthz`
- 示例 OCR 接口：`POST /ocr/base64` 返回固定占位文本

## 目录结构

- `app/main.py`：FastAPI 应用入口与路由定义。
- `requirements.txt`：依赖列表。

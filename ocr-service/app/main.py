import base64
import io
import logging
import os
import time
import uuid
from dataclasses import dataclass
from typing import Optional

import easyocr
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel

app = FastAPI(title="UkaStar OCR Service", version="0.2.0")


# -------------------- 配置与日志 --------------------
@dataclass
class Settings:
    service_token: Optional[str] = os.getenv("OCR_SERVICE_TOKEN")
    tmp_dir: str = os.getenv("OCR_TMP_DIR", "/tmp/ukastar-ocr")
    languages: tuple[str, ...] = tuple(os.getenv("OCR_LANGS", "ch_sim,en").split(","))


settings = Settings()
os.makedirs(settings.tmp_dir, exist_ok=True)

logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s %(name)s - %(message)s",
)
logger = logging.getLogger("ocr-service")


# 懒加载 EasyOCR 阅读器（避免首次启动阻塞）
_reader: Optional[easyocr.Reader] = None


def get_reader() -> easyocr.Reader:
    global _reader
    if _reader is None:
        logger.info("Initializing EasyOCR reader with languages=%s", settings.languages)
        _reader = easyocr.Reader(list(settings.languages))
    return _reader


# -------------------- 模型与响应 --------------------
class OCRRequest(BaseModel):
    image_base64: str


class OCRResponse(BaseModel):
    text: str
    elapsed_ms: int
    filename: str


class ErrorResponse(BaseModel):
    error: dict


def _require_token(authz: Optional[str], x_token: Optional[str]):
    """校验服务间 Token（Authorization: Bearer ... 或 X-Internal-Token）。"""
    expected = settings.service_token
    if not expected:
        # 未配置 Token 时不校验，便于本地开发
        return
    token: Optional[str] = None
    if x_token:
        token = x_token.strip()
    elif authz and authz.lower().startswith("bearer "):
        token = authz.split(" ", 1)[1].strip()
    if token != expected:
        logger.warning("Auth failed: invalid token")
        raise HTTPException(status_code=401, detail={"code": "UNAUTHORIZED", "message": "invalid token"})


@app.get("/healthz")
def healthz() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/ocr/base64", response_model=OCRResponse, responses={401: {"model": ErrorResponse}, 400: {"model": ErrorResponse}, 500: {"model": ErrorResponse}})
def ocr_base64(
    payload: OCRRequest,
    authorization: Optional[str] = Header(None),
    x_internal_token: Optional[str] = Header(None, alias="X-Internal-Token"),
):
    start = time.time()
    _require_token(authorization, x_internal_token)

    # 基础校验
    if not payload.image_base64:
        raise HTTPException(status_code=400, detail={"code": "BAD_REQUEST", "message": "image_base64 is required"})

    # 去除前缀 data:image/...;base64,
    b64 = payload.image_base64
    if "," in b64:
        b64 = b64.split(",", 1)[1]

    # 解码并落盘
    binary: bytes
    try:
        binary = base64.b64decode(b64)
    except Exception:
        raise HTTPException(status_code=400, detail={"code": "BAD_BASE64", "message": "invalid base64 data"})

    filename = f"{int(time.time()*1000)}_{uuid.uuid4().hex}.jpg"
    file_path = os.path.join(settings.tmp_dir, filename)
    try:
        with open(file_path, "wb") as f:
            f.write(binary)
    except Exception as e:
        logger.error("Write temp file failed: %s", e)
        raise HTTPException(status_code=500, detail={"code": "IO_ERROR", "message": "write temp file failed"})

    # OCR 识别
    try:
        reader = get_reader()
        result = reader.readtext(file_path, detail=0)
        text = "\n".join(result)
        elapsed_ms = int((time.time() - start) * 1000)
        logger.info(
            "OCR done file=%s size=%s bytes elapsed_ms=%s lines=%s", filename, len(binary), elapsed_ms, len(result)
        )
        return OCRResponse(text=text, elapsed_ms=elapsed_ms, filename=filename)
    except HTTPException:
        raise
    except Exception as e:
        logger.exception("OCR failed for file=%s", filename)
        raise HTTPException(status_code=500, detail={"code": "OCR_ERROR", "message": str(e)})

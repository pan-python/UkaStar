from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="UkaStar OCR Service", version="0.1.0")


class OCRRequest(BaseModel):
    image_base64: str


class OCRResponse(BaseModel):
    text: str


@app.get("/healthz")
def healthz() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/ocr/base64", response_model=OCRResponse)
def ocr_base64(_: OCRRequest) -> OCRResponse:
    # 占位实现：后续集成 EasyOCR
    return OCRResponse(text="Hello from ocr-service")

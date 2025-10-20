package com.ukastar.api.export.vo;

/**
 * 导出响应，包含文件名与 Base64 内容。
 */
public record ExportResponse(
        String fileName,
        String contentBase64
) {
}

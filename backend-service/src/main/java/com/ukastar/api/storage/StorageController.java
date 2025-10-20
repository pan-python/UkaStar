package com.ukastar.api.storage;

import com.ukastar.common.config.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true")
public class StorageController {

    private final MinioProperties props;
    private final MinioClient minioClient;

    public StorageController(MinioProperties props, MinioClient minioClient) {
        this.props = props;
        this.minioClient = minioClient;
    }

    @GetMapping("/presign/upload")
    public Object presignUpload(@RequestParam String key, @RequestParam(defaultValue = "300") int ttl) throws Exception {
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(props.bucket())
                        .object(key)
                        .method(Method.PUT)
                        .expiry(Math.min(Math.max(ttl, 60), 7*24*3600))
                        .build()
        );
        Map<String, Object> resp = new HashMap<>();
        resp.put("method", "PUT");
        resp.put("url", url);
        resp.put("headers", Map.of("Content-Type", "application/octet-stream"));
        return resp;
    }

    @GetMapping("/presign/download")
    public Object presignDownload(@RequestParam String key, @RequestParam(defaultValue = "900") int ttl) throws Exception {
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(props.bucket())
                        .object(key)
                        .method(Method.GET)
                        .expiry(Math.min(Math.max(ttl, 60), 7*24*3600))
                        .build()
        );
        return Map.of("method", "GET", "url", url);
    }
}

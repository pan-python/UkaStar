package com.ukastar.api.ocr;

import com.ukastar.common.config.properties.OcrProxyProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@ConditionalOnProperty(prefix = "ocr.proxy", name = "enabled", havingValue = "true")
public class OcrProxyController {

    record OcrReq(String image) {}

    private final OcrProxyProperties props;
    private final WebClient webClient;

    public OcrProxyController(OcrProxyProperties props, WebClient.Builder builder) {
        this.props = props;
        WebClient.Builder b = builder.baseUrl(props.baseUrl());
        if (props.token() != null && !props.token().isBlank()) {
            b = b.defaultHeader(props.tokenHeader(), props.token());
        }
        this.webClient = b.build();
    }

    @PostMapping(value = "/base64", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map> base64(@RequestBody Mono<OcrReq> body) {
        return body.flatMap(req -> webClient.post()
                .uri("/ocr/base64")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("image", req.image()))
                .retrieve()
                .bodyToMono(Map.class)
        );
    }
}


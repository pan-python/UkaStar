package com.ukastar.api.config;

import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class ConfigControllerTest extends WebTestClientSupport {

    @Test
    void growthThresholdsShouldReturnDefaultsOrConfigured() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());
        client.get().uri(uriBuilder -> uriBuilder.path("/api/configs/growth").queryParam("tenantId", token.tenantId()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.milestone_thresholds.bronze").exists();
    }
}


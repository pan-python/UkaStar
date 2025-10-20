package com.ukastar.api.miniapp;

import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class ChatSessionControllerTest extends WebTestClientSupport {

    @Test
    void createAndListSessionsShouldWork() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());

        client.post().uri("/api/chat/sessions").bodyValue(java.util.Map.of("title","Test Chat"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.sessionCode").exists();

        client.get().uri("/api/chat/sessions")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.sessions").exists();
    }
}

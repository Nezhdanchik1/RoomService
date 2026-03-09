package org.example.roomservice.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ContentClient {

    private final WebClient webClient;

    public ContentClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8083/api/posts").build();
    }

    public Mono<Map<Long, Long>> getPostsCount(List<Long> roomIds) {
        return webClient.post()
                .uri("/rooms/count")
                .bodyValue(roomIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}
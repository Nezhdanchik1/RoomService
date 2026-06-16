package org.example.roomservice.client;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.dto.PresenceRequest;
import org.example.roomservice.dto.PresenceResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserPresenceClient {

    private final WebClient userServiceWebClient;

    @org.springframework.beans.factory.annotation.Value("${INTERNAL_API_TOKEN:room-service-secret}")
    private String internalToken;

    public PresenceResponse getUsersPresence(List<Long> userIds) {

        PresenceRequest request = PresenceRequest.builder()
                .user_ids(userIds)
                .build();

        return userServiceWebClient.post()
                .uri("/internal/presence/users")
                .bodyValue(request)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .bodyToMono(PresenceResponse.class)
                .block(); // для MVC приложения допустимо
    }

}
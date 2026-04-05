package org.example.roomservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomservice.config.RabbitConfig;
import org.example.roomservice.dto.event.UserJoinedRoomEvent;
import org.example.roomservice.model.RoomRole;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserJoinedEvent(Long userId, Long roomId, RoomRole role) {
        UserJoinedRoomEvent event = UserJoinedRoomEvent.builder()
                .userId(userId)
                .roomId(roomId)
                .userRole(role != null ? role.name() : RoomRole.MEMBER.name())
                .joinedAt(LocalDateTime.now())
                .build();

        log.info("Sending user joined event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_JOIN, event);
    }
}

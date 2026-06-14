package org.example.roomservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomservice.dto.event.UserActionEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EducationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserActionEvent(UserActionEvent event) {
        String exchange = "statistics-exchange";
        String routingKey = "user.action." + event.getType().name().toLowerCase();
        try {
            log.info("Sending user action event to RabbitMQ: exchange={}, routingKey={}, event={}", exchange, routingKey, event);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to send user action event to RabbitMQ", e);
        }
    }
}

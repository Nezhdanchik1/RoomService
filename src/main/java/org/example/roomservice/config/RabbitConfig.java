package org.example.roomservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "room-events-exchange";
    public static final String QUEUE_JOIN = "room-join-queue";
    public static final String QUEUE_LEAVE = "room-leave-queue";
    public static final String ROUTING_KEY_JOIN = "room.user.joined";
    public static final String ROUTING_KEY_LEAVE = "room.user.left";

    @Bean
    public Queue joinQueue() {
        return new Queue(QUEUE_JOIN);
    }

    @Bean
    public Queue leaveQueue() {
        return new Queue(QUEUE_LEAVE);
    }

    @Bean
    public TopicExchange roomExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingJoin(Queue joinQueue, TopicExchange roomExchange) {
        return BindingBuilder.bind(joinQueue).to(roomExchange).with(ROUTING_KEY_JOIN);
    }

    @Bean
    public Binding bindingLeave(Queue leaveQueue, TopicExchange roomExchange) {
        return BindingBuilder.bind(leaveQueue).to(roomExchange).with(ROUTING_KEY_LEAVE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

package org.example.roomservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomservice.config.RabbitConfig;
import org.example.roomservice.dto.event.UserJoinedRoomEvent;
import org.example.roomservice.dto.event.UserLeftRoomEvent;
import org.example.roomservice.repository.RoomRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomParticipationListener {

    private final RoomRepository roomRepository;

    @Transactional
    @RabbitListener(queues = RabbitConfig.QUEUE_JOIN)
    public void handleUserJoined(UserJoinedRoomEvent event) {
        log.info("Async: User {} joined room {}, incrementing membersCount", 
                 event.getUserId(), event.getRoomId());
        
        roomRepository.incrementMembersCount(event.getRoomId());
    }

    @Transactional
    @RabbitListener(queues = RabbitConfig.QUEUE_LEAVE)
    public void handleUserLeft(UserLeftRoomEvent event) {
        log.info("Async: User {} left room {}, decrementing membersCount", 
                 event.getUserId(), event.getRoomId());
        
        roomRepository.decrementMembersCount(event.getRoomId());
    }
}

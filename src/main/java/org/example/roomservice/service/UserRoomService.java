package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.model.UserRoomId;
import org.example.roomservice.repository.UserRoomRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoomService {

    private final UserRoomRepository userRoomRepository;
    private final RoomService roomService;
    private final ApplicationEventPublisher eventPublisher;

    // Вступление пользователя
    public UserRoom joinRoom(Long userId, Long roomId, RoomRole role) {
        Room room = roomService.getRoomById(roomId);
        UserRoomId id = new UserRoomId(userId, roomId);

        if (userRoomRepository.existsById(id)) {
            throw new AlreadyExistsException("User already in room");
        }

        UserRoom userRoom = UserRoom.builder()
                .id(id)
                .room(room)
                .roomRole(role != null ? role : RoomRole.MEMBER)
                .build();

        UserRoom saved = userRoomRepository.save(userRoom);

//        eventPublisher.publishEvent(new UserJoinedRoomEvent(this, userId, roomId));

        return saved;
    }

    // Выйти из комнаты
    public void leaveRoom(Long userId, Long roomId) {
        UserRoomId id = new UserRoomId(userId, roomId);
        UserRoom userRoom = userRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not in room"));

        userRoomRepository.delete(userRoom);
//        eventPublisher.publishEvent(new UserLeftRoomEvent(this, userId, roomId));
    }

    // Получить участников комнаты
    public List<UserRoom> getMembers(Long roomId) {
        Room room = roomService.getRoomById(roomId);
        return userRoomRepository.findByRoom(room);
    }

    // Получить комнаты пользователя
    public List<Room> getUserRooms(Long userId) {
        List<UserRoom> userRooms = userRoomRepository.findById_UserId(userId);
        return userRooms.stream()
                .map(UserRoom::getRoom)
                .collect(Collectors.toList());
    }
}
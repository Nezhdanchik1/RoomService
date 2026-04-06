package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.model.UserRoomId;
import org.example.roomservice.repository.RoomRepository;
import org.example.roomservice.repository.UserRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoomService {

    private final UserRoomRepository userRoomRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final org.example.roomservice.producer.RoomEventProducer roomEventProducer;

    // Вступление пользователя
    public UserRoom joinRoom(Long userId, String roomSlug, RoomRole role) {
        Room room = roomService.getRoom(roomSlug);
        UserRoomId id = new UserRoomId(userId, room.getId());

        if (userRoomRepository.existsById(id)) {
            throw new AlreadyExistsException("User already in room");
        }

        UserRoom userRoom = UserRoom.builder()
                .id(id)
                .room(room)
                .roomRole(role != null ? role : RoomRole.MEMBER)
                .build();

        UserRoom saved = userRoomRepository.save(userRoom);

        // Синхронное обновление счетчика для надежности
        roomRepository.incrementMembersCount(room.getId());

        roomEventProducer.sendUserJoinedEvent(userId, room.getId(), role);

        return saved;
    }

    // Выйти из комнаты
    public void leaveRoom(Long userId, String roomSlug) {
        Room room = roomService.getRoom(roomSlug);
        UserRoomId id = new UserRoomId(userId, room.getId());
        UserRoom userRoom = userRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not in room"));

        userRoomRepository.delete(userRoom);

        // Синхронное обновление счетчика
        roomRepository.decrementMembersCount(room.getId());

        roomEventProducer.sendUserLeftEvent(userId, room.getId());
    }

    // Получить участников комнаты
    public List<UserRoom> getMembers(String roomSlug) {
        Room room = roomService.getRoom(roomSlug);
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
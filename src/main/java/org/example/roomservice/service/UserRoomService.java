
package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.*;
import org.example.roomservice.repository.RoomRepository;
import org.example.roomservice.repository.UserDirectionRepository;
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
    private final UserDirectionRepository userDirectionRepository;
    private final RoomRepository roomRepository;
    private final org.example.roomservice.producer.RoomEventProducer roomEventProducer;

    // Вступление пользователя
    public UserRoom joinRoom(Long userId, Room room, RoomRole role) {
        UserRoomId id = new UserRoomId(userId, room.getId());

        if (userRoomRepository.existsById(id)) {
            throw new AlreadyExistsException("User already in room");
        }

        UserRoom userRoom = UserRoom.builder()
                .id(id)
                .room(room)
                .roomRole(role != null ? role : RoomRole.STUDENT)
                .build();

        UserRoom saved = userRoomRepository.save(userRoom);

        // Синхронное обновление счетчика для надежности
        roomRepository.incrementMembersCount(room.getId());

        roomEventProducer.sendUserJoinedEvent(userId, room.getId(), role);

        return saved;
    }

    // Выйти из комнаты
    public void leaveRoom(Long userId, Room room) {
        UserRoomId id = new UserRoomId(userId, room.getId());
        UserRoom userRoom = userRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not in room"));

        userRoomRepository.delete(userRoom);

        // Синхронное обновление счетчика
        roomRepository.decrementMembersCount(room.getId());

        roomEventProducer.sendUserLeftEvent(userId, room.getId());
    }

    // Получить участников комнаты
    public List<UserRoom> getMembers(Room room) {
        return userRoomRepository.findByRoom(room);
    }

    // Получить комнаты пользователя
    public List<Room> getUserRooms(Long userId) {
        List<UserRoom> userRooms = userRoomRepository.findById_UserId(userId);
        return userRooms.stream()
                .map(UserRoom::getRoom)
                .collect(Collectors.toList());
    }

    // Получить эффективную роль пользователя в комнате
    public RoomRole getEffectiveRole(Long userId, Room room) {
        UserRoom userRoom = userRoomRepository.findById(new UserRoomId(userId, room.getId())).orElse(null);

        // 1. Если пользователь ROOM_ADMIN (Уровень 4)
        if (userRoom != null && userRoom.getRoomRole() == RoomRole.ROOM_ADMIN) {
            return RoomRole.ROOM_ADMIN;
        }

        // 2. Если пользователь эксперт в направлении (Уровень 3)
        if (userDirectionRepository.existsById(new UserDirectionId(userId, room.getDirection().getId()))) {
            return RoomRole.EXPERT;
        }

        // 3. Остальные роли из UserRoom (MODERATOR, STUDENT)
        return userRoom != null ? userRoom.getRoomRole() : null;
    }

    // Обновить роль пользователя в комнате
    public UserRoom updateUserRole(Long userId, Room room, RoomRole newRole) {
        UserRoomId id = new UserRoomId(userId, room.getId());
        UserRoom userRoom = userRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not in room"));

        userRoom.setRoomRole(newRole);
        return userRoomRepository.save(userRoom);
    }
}

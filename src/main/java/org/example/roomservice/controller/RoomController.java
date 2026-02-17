package org.example.roomservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.dto.RoomDto;
import org.example.roomservice.dto.UserRoomDto;
import org.example.roomservice.mapper.RoomMapper;
import org.example.roomservice.mapper.UserRoomMapper;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.service.RoomService;
import org.example.roomservice.service.UserRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserRoomService userRoomService;
    private final RoomMapper roomMapper;
    private final UserRoomMapper userRoomMapper;

    // Создать комнату
    @PostMapping
    public RoomDto createRoom(@RequestBody RoomDto dto) {
        Room room = roomService.createRoom(
                dto.getDirectionId(),
                dto.getName(),
                dto.getDescription(),
                dto.getIsPrivate()
        );
        return roomMapper.toDto(room);
    }

    // Получить комнату по id
    @GetMapping("/{id}")
    public RoomDto getRoom(@PathVariable Long id) {
        return roomMapper.toDto(roomService.getRoomById(id));
    }

    // Получить комнаты направления
    @GetMapping("/direction/{directionId}")
    public List<RoomDto> getRoomsByDirection(@PathVariable Long directionId) {
        return roomService.getRoomsByDirection(directionId)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    // Удалить комнату
    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }

    // Вступить в комнату
    @PostMapping("/{roomId}/join")
    public UserRoomDto joinRoom(
            @PathVariable Long roomId,
            @RequestParam Long userId,
            @RequestParam(required = false) RoomRole role
    ) {
        UserRoom userRoom = userRoomService.joinRoom(userId, roomId, role);
        return userRoomMapper.toDto(userRoom);
    }

    // Выйти из комнаты
    @PostMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        userRoomService.leaveRoom(userId, roomId);
    }

    // Получить участников комнаты
    @GetMapping("/{roomId}/members")
    public List<UserRoomDto> getMembers(@PathVariable Long roomId) {
        return userRoomService.getMembers(roomId)
                .stream()
                .map(userRoomMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}")
    public List<RoomDto> getUserRooms(@PathVariable Long userId) {
        return userRoomService.getUserRooms(userId)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

}

package org.example.roomservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.dto.OneRoomDto;
import org.example.roomservice.dto.RoomListDto;
import org.example.roomservice.dto.UserRoomDto;
import org.example.roomservice.mapper.RoomMapper;
import org.example.roomservice.mapper.UserRoomMapper;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.service.RoomService;
import org.example.roomservice.service.UserRoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
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
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public RoomListDto createRoom(@RequestBody RoomListDto dto, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Room room = roomService.createRoom(
                userId,
                dto.getDirectionSlug(),
                dto.getName(),
                dto.getSlug(),
                dto.getDescription(),
                dto.getIsPrivate(),
                dto.getTags()
        );
        return roomMapper.toDto(room);
    }

    // Поиск по тегу
    @GetMapping("/tag/{tagName}")
    public List<RoomListDto> getRoomsByTag(@PathVariable String tagName) {
        return roomService.findRoomsByTag(tagName);
    }

    // Получить комнату по слагу
    @GetMapping("/{slug}")
    public OneRoomDto getRoom(@PathVariable String slug) {
        return roomService.getRoomBySlug(slug);
    }

    // Получить комнаты направления
    @GetMapping("/direction/{directionSlug}")
    public Mono<List<RoomListDto>> getRoomsByDirection(@PathVariable String directionSlug) {
        return roomService.getRoomsByDirection(directionSlug);
    }

    // Удалить комнату
    @DeleteMapping("/{slug}")
    @PreAuthorize("hasRole('MODERATOR')")
    public void deleteRoom(@PathVariable String slug) {
        roomService.deleteRoom(slug);
    }

    // Вступить в комнату
    @PostMapping("/{roomSlug}/join")
    @PreAuthorize("hasRole('USER')")
    public UserRoomDto joinRoom(
            @PathVariable String roomSlug,
            @RequestParam(required = false) RoomRole role,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        Room room = roomService.getRoom(roomSlug);
        UserRoom userRoom = userRoomService.joinRoom(userId, room, role);
        return userRoomMapper.toDto(userRoom);
    }

    // Выйти из комнаты
    @PostMapping("/{roomSlug}/leave")
    @PreAuthorize("hasRole('USER')")
    public void leaveRoom(@PathVariable String roomSlug, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Room room = roomService.getRoom(roomSlug);
        userRoomService.leaveRoom(userId, room);
    }

    // Получить участников комнаты
    @GetMapping("/{roomSlug}/members")
    public List<UserRoomDto> getMembers(@PathVariable String roomSlug) {
        Room room = roomService.getRoom(roomSlug);
        return userRoomService.getMembers(room)
                .stream()
                .map(userRoomMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}")
    public List<RoomListDto> getUserRooms(@PathVariable Long userId) {
        return userRoomService.getUserRooms(userId)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

}

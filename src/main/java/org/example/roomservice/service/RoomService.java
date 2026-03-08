package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.client.UserPresenceClient;
import org.example.roomservice.dto.PresenceResponse;
import org.example.roomservice.dto.RoomDto;
import org.example.roomservice.dto.UserPresenceDto;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
import org.example.roomservice.repository.RoomRepository;
import org.example.roomservice.repository.UserRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final DirectionService directionService;
    private final UserPresenceClient userPresenceClient;

    public Room createRoom(Long directionId, String name, String description, boolean isPrivate) {
        Direction direction = directionService.getDirectionById(directionId);

        if (roomRepository.existsByDirectionAndName(direction, name)) {
            throw new AlreadyExistsException("Room with name '" + name + "' already exists in this direction");
        }

        Room room = Room.builder()
                .direction(direction)
                .name(name)
                .description(description)
                .isPrivate(isPrivate)
                .build();

        return roomRepository.save(room);
    }

    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));

        List<Long> userIds = userRoomRepository.findUserIdsByRoomId(id);
        PresenceResponse presence = userPresenceClient.getUsersPresence(userIds);

        Map<Long, Boolean> onlineMap =
                presence.getUsers()
                        .stream()
                        .collect(Collectors.toMap(
                                UserPresenceDto::getUser_id,
                                UserPresenceDto::getOnline
                        ));

        List<UserPresenceDto> members = userIds.stream()
                .map(userId -> UserPresenceDto.builder()
                        .user_id(userId)
                        .online(onlineMap.getOrDefault(userId, false))
                        .build())
                .toList();

        return RoomDto.builder()
                .id(room.getId())
                .directionId(room.getDirection().getId())
                .name(room.getName())
                .description(room.getDescription())
                .isPrivate(room.getIsPrivate())
                .participantsCount((long) userIds.size())
                .onlineCount((long) presence.getOnline_count())
                .members(members)
                .build();
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByDirection(Long directionId) {
        Direction direction = directionService.getDirectionById(directionId);
        return roomRepository.findByDirection(direction);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}

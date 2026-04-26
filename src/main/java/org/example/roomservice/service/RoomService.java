package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.client.ContentClient;
import org.example.roomservice.client.UserPresenceClient;
import org.example.roomservice.dto.OneRoomDto;
import org.example.roomservice.dto.PresenceResponse;
import org.example.roomservice.dto.RoomListDto;
import org.example.roomservice.dto.UserPresenceDto;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.mapper.RoomMapper;
import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.repository.DirectionRepository;
import org.example.roomservice.repository.RoomRepository;
import org.example.roomservice.repository.TagRepository;
import org.example.roomservice.repository.UserRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    private final ContentClient contentClient;
    private final DirectionRepository directionRepository;
    private final TagRepository tagRepository;
    private final RoomMapper roomMapper;
    private final UserRoomService userRoomService;

    public Room createRoom(Long userId, String directionSlug, String name, String slug, String description, boolean isPrivate, java.util.Set<String> tagNames) {
        Direction direction = directionService.getDirectionBySlug(directionSlug);

        if (roomRepository.existsByDirectionAndName(direction, name)) {
            throw new AlreadyExistsException("Room with name '" + name + "' already exists in this direction");
        }

        java.util.Set<org.example.roomservice.model.Tag> tags = new java.util.HashSet<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                org.example.roomservice.model.Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(org.example.roomservice.model.Tag.builder().name(tagName).build()));
                tags.add(tag);
            }
        }

        Room room = Room.builder()
                .direction(direction)
                .name(name)
                .slug(slug)
                .description(description)
                .isPrivate(isPrivate)
                .tags(tags)
                .build();

        Room savedRoom = roomRepository.save(room);

        // Создатель автоматически становится ROOM_ADMIN
        userRoomService.joinRoom(userId, savedRoom, RoomRole.ROOM_ADMIN);

        return savedRoom;
    }

    public OneRoomDto getRoomBySlug(String slug) {
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Room not found with slug: " + slug));

        List<Long> userIds = userRoomRepository.findUserIdsByRoomId(room.getId());
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

        OneRoomDto dto = roomMapper.toOneRoomDto(room);
        dto.setParticipantsCount((long) room.getMembersCount());
        dto.setOnlineCount((long) presence.getOnline_count());
        dto.setMembers(members);
        return dto;
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
    }

    public Room getRoom(String slug) {
        return roomRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Room not found with slug: " + slug));
    }

    public Mono<List<RoomListDto>> getRoomsByDirection(String directionSlug) {
        Direction direction = directionService.getDirectionBySlug(directionSlug);

        List<Room> rooms = roomRepository.findRoomsByDirection(direction);

        List<Long> ids = rooms.stream()
                .map(Room::getId)
                .toList();

        Mono<Map<Long, Long>> postsMono = contentClient.getPostsCount(ids);
        return postsMono.map(posts -> {

            return rooms.stream().map(room -> {

                RoomListDto dto = roomMapper.toDto(room);
                dto.setParticipantsCount((long) room.getMembersCount());
                dto.setPostsCount(posts.getOrDefault(room.getId(), 0L));

                return dto;

            }).toList();
        });
    }

    public List<RoomListDto> findRoomsByTag(String tagName) {
        org.example.roomservice.model.Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new NotFoundException("Tag not found: " + tagName));

        return tag.getRooms().stream()
                .map(roomMapper::toDto)
                .toList();
    }

    public void deleteRoom(String slug) {
        Room room = getRoom(slug);
        roomRepository.delete(room);
    }
}

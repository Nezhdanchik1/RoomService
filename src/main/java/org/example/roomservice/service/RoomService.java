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
import org.example.roomservice.mapper.RoomMapperImpl;
import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
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

    public Room createRoom(Long directionId, String name, String description, boolean isPrivate, java.util.Set<String> tagNames) {
        Direction direction = directionService.getDirectionById(directionId);

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
                .description(description)
                .isPrivate(isPrivate)
                .tags(tags)
                .build();

        return roomRepository.save(room);
    }

    public OneRoomDto getRoomById(Long id) {
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

        return OneRoomDto.builder()
                .id(room.getId())
                .directionId(room.getDirection().getId())
                .name(room.getName())
                .description(room.getDescription())
                .isPrivate(room.getIsPrivate())
                .participantsCount((long) userIds.size())
                .onlineCount((long) presence.getOnline_count())
                .members(members)
                .tags(room.getTags().stream().map(org.example.roomservice.model.Tag::getName).collect(java.util.stream.Collectors.toSet()))
                .build();
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
    }

    public Mono<List<RoomListDto>> getRoomsByDirection(Long directionId) {
        Direction direction = directionRepository.getReferenceById(directionId);

        List<Room> rooms = roomRepository.findRoomsByDirection(direction);

        List<Long> ids = rooms.stream()
                .map(Room::getId)
                .toList();

        Map<Long, Long> participants = userRoomRepository
                .countParticipants(ids)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        Mono<Map<Long, Long>> postsMono = contentClient.getPostsCount(ids);
        return postsMono.map(posts -> {

            return rooms.stream().map(room -> {

                RoomListDto dto = new RoomListDto();

                dto.setId(room.getId());
                dto.setName(room.getName());
                dto.setDescription(room.getDescription());
                dto.setDirectionId(room.getDirection().getId());
                dto.setIsPrivate(room.getIsPrivate());
                dto.setTags(room.getTags().stream().map(org.example.roomservice.model.Tag::getName).collect(java.util.stream.Collectors.toSet()));

                dto.setParticipantsCount(
                        participants.getOrDefault(room.getId(), 0L)
                );

                dto.setPostsCount(
                        posts.getOrDefault(room.getId(), 0L)
                );

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

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}

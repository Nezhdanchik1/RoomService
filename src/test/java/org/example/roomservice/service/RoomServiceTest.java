package org.example.roomservice.service;

import org.example.roomservice.model.*;
import org.example.roomservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private DirectionService directionService;
    @Mock
    private UserRoomService userRoomService;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private RoomService roomService;

    private Direction direction;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        direction = Direction.builder().id(10L).slug("java").name("Java").build();
    }

    @Test
    void createRoom_ShouldAssignCreatorAsRoomAdmin() {
        // Arrange
        String roomName = "Spring Boot";
        String roomSlug = "spring-boot";
        
        when(directionService.getDirectionBySlug("java")).thenReturn(direction);
        when(roomRepository.existsByDirectionAndName(any(), anyString())).thenReturn(false);
        
        Room savedRoom = Room.builder()
                .id(100L)
                .name(roomName)
                .slug(roomSlug)
                .direction(direction)
                .build();
        
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // Act
        roomService.createRoom(userId, "java", roomName, roomSlug, "Desc", false, Set.of());

        // Assert
        verify(roomRepository).save(any(Room.class));
        verify(userRoomService).joinRoom(eq(userId), eq(savedRoom), eq(RoomRole.ROOM_ADMIN));
    }
}

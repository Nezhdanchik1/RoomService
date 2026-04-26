package org.example.roomservice.service;

import org.example.roomservice.model.*;
import org.example.roomservice.repository.UserDirectionRepository;
import org.example.roomservice.repository.UserRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRoomServiceTest {

    @Mock
    private UserRoomRepository userRoomRepository;
    @Mock
    private UserDirectionRepository userDirectionRepository;

    @InjectMocks
    private UserRoomService userRoomService;

    @Test
    void getEffectiveRole_ShouldReturnRoomAdmin_WhenUserIsRoomAdmin() {
        // Arrange
        Long userId = 1L;
        Room room = Room.builder().id(100L).direction(Direction.builder().id(10L).build()).build();
        UserRoom userRoom = UserRoom.builder().roomRole(RoomRole.ROOM_ADMIN).build();

        when(userRoomRepository.findById(any(UserRoomId.class))).thenReturn(Optional.of(userRoom));

        // Act
        RoomRole role = userRoomService.getEffectiveRole(userId, room);

        // Assert
        assertEquals(RoomRole.ROOM_ADMIN, role);
    }

    @Test
    void getEffectiveRole_ShouldReturnExpert_WhenUserIsDirectionExpertAndNotRoomAdmin() {
        // Arrange
        Long userId = 1L;
        Room room = Room.builder().id(100L).direction(Direction.builder().id(10L).build()).build();
        UserRoom userRoom = UserRoom.builder().roomRole(RoomRole.STUDENT).build();

        when(userRoomRepository.findById(any(UserRoomId.class))).thenReturn(Optional.of(userRoom));
        when(userDirectionRepository.existsById(any(UserDirectionId.class))).thenReturn(true);

        // Act
        RoomRole role = userRoomService.getEffectiveRole(userId, room);

        // Assert
        assertEquals(RoomRole.EXPERT, role);
    }

    @Test
    void getEffectiveRole_ShouldReturnModerator_WhenUserIsModeratorAndNotExpert() {
        // Arrange
        Long userId = 1L;
        Room room = Room.builder().id(100L).direction(Direction.builder().id(10L).build()).build();
        UserRoom userRoom = UserRoom.builder().roomRole(RoomRole.MODERATOR).build();

        when(userRoomRepository.findById(any(UserRoomId.class))).thenReturn(Optional.of(userRoom));
        when(userDirectionRepository.existsById(any(UserDirectionId.class))).thenReturn(false);

        // Act
        RoomRole role = userRoomService.getEffectiveRole(userId, room);

        // Assert
        assertEquals(RoomRole.MODERATOR, role);
    }
}

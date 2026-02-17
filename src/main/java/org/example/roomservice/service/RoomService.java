package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
import org.example.roomservice.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final DirectionService directionService;

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

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByDirection(Long directionId) {
        Direction direction = directionService.getDirectionById(directionId);
        return roomRepository.findByDirection(direction);
    }

    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }
}

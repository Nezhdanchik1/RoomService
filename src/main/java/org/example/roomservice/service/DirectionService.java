package org.example.roomservice.service;


import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.ForbiddenException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Direction;
import org.example.roomservice.repository.DirectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DirectionService {

    private final DirectionRepository directionRepository;

    public Direction createDirection(String name, String description) {
        if (directionRepository.existsByName(name)) {
            throw new AlreadyExistsException("Direction with name " + name + " already exists");
        }
        Direction direction = Direction.builder()
                .name(name)
                .description(description)
                .build();
        return directionRepository.save(direction);
    }

    public List<Direction> getAllDirections() {
        return directionRepository.findAll();
    }

    public Direction getDirectionById(Long id) {
        return directionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Direction not found with id: " + id));
    }

    public void deleteDirection(Long id) {
        Direction direction = getDirectionById(id);
        if (!direction.getRooms().isEmpty()) {
            throw new ForbiddenException("Cannot delete direction with existing rooms");
        }
        directionRepository.delete(direction);
    }
}

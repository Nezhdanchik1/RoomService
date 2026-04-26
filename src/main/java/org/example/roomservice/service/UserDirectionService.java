package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.Direction;
import org.example.roomservice.model.UserDirection;
import org.example.roomservice.model.UserDirectionId;
import org.example.roomservice.repository.DirectionRepository;
import org.example.roomservice.repository.UserDirectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDirectionService {

    private final UserDirectionRepository userDirectionRepository;
    private final DirectionRepository directionRepository;

    public UserDirection assignExpert(Long userId, String directionSlug) {
        Direction direction = directionRepository.findBySlug(directionSlug)
                .orElseThrow(() -> new NotFoundException("Direction not found: " + directionSlug));

        UserDirectionId id = new UserDirectionId(userId, direction.getId());

        if (userDirectionRepository.existsById(id)) {
            throw new AlreadyExistsException("User is already an expert in this direction");
        }

        UserDirection userDirection = UserDirection.builder()
                .id(id)
                .direction(direction)
                .build();

        return userDirectionRepository.save(userDirection);
    }

    public void removeExpert(Long userId, String directionSlug) {
        Direction direction = directionRepository.findBySlug(directionSlug)
                .orElseThrow(() -> new NotFoundException("Direction not found: " + directionSlug));

        UserDirectionId id = new UserDirectionId(userId, direction.getId());
        UserDirection userDirection = userDirectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expert record not found"));

        userDirectionRepository.delete(userDirection);
    }

    public boolean isExpert(Long userId, Long directionId) {
        return userDirectionRepository.existsById(new UserDirectionId(userId, directionId));
    }

    public List<UserDirection> getExpertsByDirection(String directionSlug) {
        Direction direction = directionRepository.findBySlug(directionSlug)
                .orElseThrow(() -> new NotFoundException("Direction not found: " + directionSlug));
        return userDirectionRepository.findByDirection(direction);
    }
}

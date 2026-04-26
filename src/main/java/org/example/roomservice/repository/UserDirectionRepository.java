package org.example.roomservice.repository;

import org.example.roomservice.model.UserDirection;
import org.example.roomservice.model.UserDirectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDirectionRepository extends JpaRepository<UserDirection, UserDirectionId> {
    List<UserDirection> findById_UserId(Long userId);
    Optional<UserDirection> findById_UserIdAndId_DirectionId(Long userId, Long directionId);
    List<UserDirection> findByDirection(org.example.roomservice.model.Direction direction);
}

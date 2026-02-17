package org.example.roomservice.repository;


import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Все комнаты для конкретного направления
    List<Room> findByDirection(Direction direction);

    // Найти комнату по name в рамках направления
    Optional<Room> findByDirectionAndName(Direction direction, String name);

    // Проверка существования комнаты с таким именем в направлении
    boolean existsByDirectionAndName(Direction direction, String name);
}

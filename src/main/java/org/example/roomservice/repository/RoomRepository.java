package org.example.roomservice.repository;


import org.example.roomservice.model.Direction;
import org.example.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Modifying
    @Query("UPDATE Room r SET r.membersCount = r.membersCount + 1 WHERE r.id = :id")
    void incrementMembersCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Room r SET r.membersCount = r.membersCount - 1 WHERE r.id = :id AND r.membersCount > 0")
    void decrementMembersCount(@Param("id") Long id);

    Optional<Room> findBySlug(String slug);

    // Все комнаты для конкретного направления
    List<Room> findRoomsByDirection(Direction direction);

    // Найти комнату по name в рамках направления
    Optional<Room> findByDirectionAndName(Direction direction, String name);

    // Проверка существования комнаты с таким именем в направлении
    boolean existsByDirectionAndName(Direction direction, String name);
}

package org.example.roomservice.repository;

import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.model.UserRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, UserRoomId> {

    // Список участников комнаты
    List<UserRoom> findByRoom(Room room);

    // Найти конкретного пользователя в комнате
    Optional<UserRoom> findById_UserIdAndRoom(Long userId, Room room);

    // Список комнат пользователя
    List<UserRoom> findById_UserId(Long userId);

    // Проверка роли пользователя
    Optional<UserRoom> findById_UserIdAndRoomAndRoomRole(Long userId, Room room, RoomRole role);
}
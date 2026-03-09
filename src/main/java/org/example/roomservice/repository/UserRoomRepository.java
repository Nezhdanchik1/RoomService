package org.example.roomservice.repository;

import org.example.roomservice.model.Room;
import org.example.roomservice.model.RoomRole;
import org.example.roomservice.model.UserRoom;
import org.example.roomservice.model.UserRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, UserRoomId> {

    @Query("""
        SELECT ur.id.userId
        FROM UserRoom ur
        WHERE ur.id.roomId = :roomId
    """)
    List<Long> findUserIdsByRoomId(Long roomId);

    @Query("""
        SELECT ur.id.roomId, COUNT(ur.id.userId)
        FROM UserRoom ur
        WHERE ur.id.roomId IN :roomIds
        GROUP BY ur.id.roomId
    """)
    List<Object[]> countParticipants(List<Long> roomIds);

    // Список участников комнаты
    List<UserRoom> findByRoom(Room room);

    // Список комнат пользователя
    List<UserRoom> findById_UserId(Long userId);

}
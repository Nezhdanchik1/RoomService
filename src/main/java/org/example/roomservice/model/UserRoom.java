package org.example.roomservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoom {

    @EmbeddedId
    private UserRoomId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomRole roomRole;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    // Удобный геттер для userId
    public Long getUserId() {
        return id.getUserId();
    }

    // Удобный геттер для roomId
    public Long getRoomId() {
        return id.getRoomId();
    }
}

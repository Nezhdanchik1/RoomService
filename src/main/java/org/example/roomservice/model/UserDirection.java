package org.example.roomservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_directions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDirection {

    @EmbeddedId
    private UserDirectionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("directionId")
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }
}

package org.example.roomservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "directions")
@Builder
public class Direction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "direction", cascade = CascadeType.ALL)
    private List<Room> rooms;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

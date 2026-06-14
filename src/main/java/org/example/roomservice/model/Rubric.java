package org.example.roomservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rubrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "criterion_name", nullable = false)
    private String criterionName;

    @Column(name = "max_points", nullable = false)
    private Integer maxPoints;

    @Column
    private String description;
}

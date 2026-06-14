package org.example.roomservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewGradeDto {
    private Long rubricId;
    private Integer score;
    private String comment;
}

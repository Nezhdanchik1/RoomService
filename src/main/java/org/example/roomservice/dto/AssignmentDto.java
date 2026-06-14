package org.example.roomservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDto {
    private Long id;
    private Long skillId;
    private String title;
    private String description;
    private List<RubricDto> rubrics;
    private LocalDateTime createdAt;
}

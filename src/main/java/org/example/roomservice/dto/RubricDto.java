package org.example.roomservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RubricDto {
    private Long id;
    private String criterionName;
    private Integer maxPoints;
    private String description;
}

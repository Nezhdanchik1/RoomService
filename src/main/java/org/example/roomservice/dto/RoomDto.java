package org.example.roomservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private Long directionId;
    private String name;
    private String description;

    private Boolean isPrivate = false;
}

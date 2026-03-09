package org.example.roomservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomListDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isPrivate = false;
    private Long directionId;
    private Long participantsCount;
    private Long postsCount;
}

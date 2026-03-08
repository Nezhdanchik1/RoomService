package org.example.roomservice.dto;

import lombok.*;

import java.util.List;

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
    private Long participantsCount;
    private Long onlineCount;
    private List<UserPresenceDto> members;
}

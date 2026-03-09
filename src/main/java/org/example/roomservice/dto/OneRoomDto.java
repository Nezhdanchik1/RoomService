package org.example.roomservice.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneRoomDto {
    private Long id;
    private Long directionId;
    private String name;
    private String description;

    private Boolean isPrivate = false;
    private Long participantsCount;
    private Long onlineCount;
    private List<UserPresenceDto> members;
    private Set<String> tags;
}

package org.example.roomservice.dto;

import lombok.*;

import java.util.Set;
import org.example.roomservice.model.RoomRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomListDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean isPrivate = false;
    private Long directionId;
    private String directionSlug;
    private Long participantsCount;
    private Long postsCount;
    private Set<String> tags;
    private RoomRole userRoomRole;
}

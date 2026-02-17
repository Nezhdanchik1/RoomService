package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.RoomRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoomDto {
    private Long userId;
    private Long roomId;
    private RoomRole roomRole;
}

package org.example.roomservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinedRoomEvent {
    private Long userId;
    private Long roomId;
    private String userRole;
    private LocalDateTime joinedAt;
}

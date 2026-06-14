package org.example.roomservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionEvent {
    private String eventId;
    private Long userId;
    private AchievementEventType type;
    private Long targetId;
    private Long directionId;
}

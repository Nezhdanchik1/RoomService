package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.UserSkillStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDto {
    private Long id;
    private Long roomId;
    private String name;
    private String description;
    private Boolean requiresExpertValidation;
    private LocalDateTime createdAt;
    private UserSkillStatus userStatus;
}

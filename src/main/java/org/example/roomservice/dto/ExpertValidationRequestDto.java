package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.ExpertValidationStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertValidationRequestDto {
    private Long id;
    private Long submissionId;
    private Long studentId;
    private String solutionText;
    private String fileUrl;
    private String assignmentTitle;
    private String assignmentDescription;
    private String skillName;
    private ExpertValidationStatus status;
    private String comment;
    private LocalDateTime createdAt;
}

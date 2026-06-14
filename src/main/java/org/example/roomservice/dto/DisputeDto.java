package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.DisputeStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeDto {
    private Long id;
    private Long submissionId;
    private Long studentId;
    private String solutionText;
    private String fileUrl;
    private String assignmentTitle;
    private String assignmentDescription;
    private String reason;
    private String resolutionComment;
    private DisputeStatus status;
    private Long moderatorId;
    private LocalDateTime createdAt;
}

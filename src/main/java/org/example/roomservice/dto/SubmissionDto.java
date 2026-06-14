package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.SubmissionStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String solutionText;
    private String fileUrl;
    private SubmissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

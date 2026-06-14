package org.example.roomservice.dto;

import lombok.*;
import org.example.roomservice.model.ReviewStatus;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private ReviewStatus status;
    private String comment;
    private List<ReviewGradeDto> grades;
    private LocalDateTime createdAt;
    
    // Дополнительные поля для рецензента
    private String solutionText;
    private String fileUrl;
    private String assignmentTitle;
    private String assignmentDescription;
    private List<RubricDto> rubrics;
}

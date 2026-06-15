package org.example.roomservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.roomservice.dto.*;
import org.example.roomservice.service.EducationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    // 1. Создать новый навык (доступно модераторам и админам, а также ROOM_ADMIN и EXPERT комнаты)
    @PostMapping("/skills")
    @PreAuthorize("hasRole('USER')")
    public SkillDto createSkill(
            @RequestParam Long roomId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean requiresExpertValidation,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        return educationService.createSkill(roomId, name, description, requiresExpertValidation, userId);
    }

    // 2. Получить список навыков для конкретной комнаты (доступно всем пользователям)
    @GetMapping("/rooms/{roomSlug}/skills")
    public List<SkillDto> getSkillsByRoom(@PathVariable String roomSlug, Principal principal) {
        Long userId = (principal != null) ? Long.valueOf(principal.getName()) : null;
        return educationService.getSkillsByRoom(roomSlug, userId);
    }

    // 2a. Получить все навыки пользователя (доступно авторизованным пользователям)
    @GetMapping("/skills/user/{userId}")
    public List<SkillDto> getUserSkills(@PathVariable Long userId) {
        return educationService.getUserSkills(userId);
    }

    // 3. Создать новое задание (доступно модераторам и админам, а также ROOM_ADMIN и EXPERT комнаты)
    @PostMapping("/assignments")
    @PreAuthorize("hasRole('USER')")
    public AssignmentDto createAssignment(@RequestBody AssignmentDto dto, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return educationService.createAssignment(dto.getSkillId(), dto.getTitle(), dto.getDescription(), dto.getRubrics(), userId);
    }

    // 4. Получить задание по ID навыка (доступно всем пользователям)
    @GetMapping("/skills/{skillId}/assignments")
    public AssignmentDto getAssignmentBySkill(@PathVariable Long skillId) {
        return educationService.getAssignmentBySkill(skillId);
    }

    // 5. Отправить решение на задание (доступно пользователям)
    @PostMapping("/submissions")
    @PreAuthorize("hasRole('USER')")
    public SubmissionDto submitSolution(@RequestBody SubmissionDto dto, Principal principal) {
        Long studentId = Long.valueOf(principal.getName());
        return educationService.submitSolution(dto.getAssignmentId(), studentId, dto.getSolutionText(), dto.getFileUrl());
    }

    // 6. Получить мои решения (доступно пользователям)
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('USER')")
    public List<SubmissionDto> getMySubmissions(Principal principal) {
        Long studentId = Long.valueOf(principal.getName());
        return educationService.getMySubmissions(studentId);
    }

    // 7. Получить назначенные мне работы на рецензирование (доступно пользователям)
    @GetMapping("/reviews/assigned")
    @PreAuthorize("hasRole('USER')")
    public List<ReviewDto> getAssignedReviews(Principal principal) {
        Long reviewerId = Long.valueOf(principal.getName());
        return educationService.getAssignedReviews(reviewerId);
    }

    // 7a. Получить мой баланс кредитов проверок
    @GetMapping("/credits/my")
    @PreAuthorize("hasRole('USER')")
    public int getMyCredits(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return educationService.getReviewCredits(userId);
    }

    // 7b. Запросить работу на проверку из пула
    @PostMapping("/reviews/request")
    @PreAuthorize("hasRole('USER')")
    public ReviewDto requestReviewAssignment(
            @RequestParam Long assignmentId,
            Principal principal
    ) {
        Long reviewerId = Long.valueOf(principal.getName());
        return educationService.requestReviewAssignment(assignmentId, reviewerId);
    }

    // 8. Отправить рецензию (доступно пользователям)
    @PostMapping("/reviews/{reviewId}/submit")
    @PreAuthorize("hasRole('USER')")
    public ReviewDto submitReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDto dto,
            Principal principal
    ) {
        Long reviewerId = Long.valueOf(principal.getName());
        return educationService.submitReview(reviewId, reviewerId, dto.getComment(), dto.getGrades());
    }

    // 9. Получить отзывы по конкретному решению (доступно пользователям)
    @GetMapping("/submissions/{submissionId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public List<ReviewDto> getReviewsForSubmission(@PathVariable Long submissionId) {
        return educationService.getReviewsForSubmission(submissionId);
    }

    // 10. Получить ожидающие проверки работы для эксперта
    @GetMapping("/experts/pending-validations")
    @PreAuthorize("hasRole('USER')")
    public List<ExpertValidationRequestDto> getPendingValidationsForExpert(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return educationService.getPendingValidationsForExpert(userId);
    }

    // 11. Вынести решение по экспертной валидации
    @PostMapping("/experts/validations/{requestId}/resolve")
    @PreAuthorize("hasRole('USER')")
    public void resolveValidationRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment,
            Principal principal
    ) {
        Long expertId = Long.valueOf(principal.getName());
        educationService.resolveValidationRequest(requestId, expertId, approved, comment);
    }

    // 12. Оспорить оценку Peer Review (Апелляция)
    @PostMapping("/submissions/{submissionId}/dispute")
    @PreAuthorize("hasRole('USER')")
    public DisputeDto createDispute(
            @PathVariable Long submissionId,
            @RequestBody DisputeRequest request,
            Principal principal
    ) {
        Long studentId = Long.valueOf(principal.getName());
        return educationService.createDispute(submissionId, studentId, request.getReason());
    }

    // 12a. Получить споры по решению
    @GetMapping("/submissions/{submissionId}/disputes")
    @PreAuthorize("hasRole('USER')")
    public List<DisputeDto> getDisputesForSubmission(@PathVariable Long submissionId) {
        return educationService.getDisputesForSubmission(submissionId);
    }

    // 13. Получить список споров для модератора
    @GetMapping("/moderators/disputes")
    @PreAuthorize("hasRole('USER')")
    public List<DisputeDto> getPendingDisputes(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return educationService.getPendingDisputes(userId);
    }

    // 14. Разрешить спор модератором
    @PostMapping("/moderators/disputes/{disputeId}/resolve")
    @PreAuthorize("hasRole('USER')")
    public void resolveDispute(
            @PathVariable Long disputeId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment,
            Principal principal
    ) {
        Long moderatorId = Long.valueOf(principal.getName());
        educationService.resolveDispute(disputeId, moderatorId, approved, comment);
    }

    public static class DisputeRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

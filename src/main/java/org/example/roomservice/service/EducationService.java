package org.example.roomservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomservice.dto.*;
import org.example.roomservice.dto.event.AchievementEventType;
import org.example.roomservice.dto.event.UserActionEvent;
import org.example.roomservice.exception.AlreadyExistsException;
import org.example.roomservice.exception.NotFoundException;
import org.example.roomservice.model.*;
import org.example.roomservice.producer.EducationEventProducer;
import org.example.roomservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EducationService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final AssignmentRepository assignmentRepository;
    private final RubricRepository rubricRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewGradeRepository reviewGradeRepository;
    private final RoomRepository roomRepository;
    private final EducationEventProducer eventProducer;
    private final ExpertValidationRequestRepository expertValidationRequestRepository;
    private final DisputeRepository disputeRepository;
    private final UserDirectionRepository userDirectionRepository;
    private final UserRoomService userRoomService;

    // 1. Создать навык
    public SkillDto createSkill(Long roomId, String name, String description, boolean requiresExpertValidation, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + roomId));

        // Проверка прав: глобальный ADMIN/MODERATOR, либо локальный ROOM_ADMIN/EXPERT
        RoomRole callerRole = userRoomService.getEffectiveRole(userId, room);
        boolean isGlobalPrivileged = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isGlobalPrivileged && callerRole != RoomRole.ROOM_ADMIN && callerRole != RoomRole.EXPERT) {
            throw new org.springframework.security.access.AccessDeniedException("Only room administrators, experts, or global moderators can create skills");
        }

        Skill skill = Skill.builder()
                .room(room)
                .name(name)
                .description(description)
                .requiresExpertValidation(requiresExpertValidation)
                .build();

        Skill saved = skillRepository.save(skill);
        return mapToSkillDto(saved, null);
    }

    // 2. Получить навыки комнаты
    public List<SkillDto> getSkillsByRoom(String roomSlug, Long userId) {
        Room room = roomRepository.findBySlug(roomSlug)
                .orElseThrow(() -> new NotFoundException("Room not found with slug: " + roomSlug));

        List<Skill> skills = skillRepository.findByRoomId(room.getId());
        
        Map<Long, UserSkillStatus> userSkillMap = new HashMap<>();
        if (userId != null) {
            List<UserSkill> userSkills = userSkillRepository.findByIdUserId(userId);
            userSkills.forEach(us -> userSkillMap.put(us.getId().getSkillId(), us.getStatus()));
        }

        return skills.stream()
                .map(s -> mapToSkillDto(s, userSkillMap.get(s.getId())))
                .collect(Collectors.toList());
    }

    // 2a. Получить все навыки пользователя
    public List<SkillDto> getUserSkills(Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByIdUserId(userId);
        return userSkills.stream()
                .map(us -> mapToSkillDto(us.getSkill(), us.getStatus()))
                .collect(Collectors.toList());
    }

    // 3. Создать задание
    public AssignmentDto createAssignment(Long skillId, String title, String description, List<RubricDto> rubricsDto, Long userId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found with id: " + skillId));

        // Проверка прав: глобальный ADMIN/MODERATOR, либо локальный ROOM_ADMIN/EXPERT
        Room room = skill.getRoom();
        RoomRole callerRole = userRoomService.getEffectiveRole(userId, room);
        boolean isGlobalPrivileged = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isGlobalPrivileged && callerRole != RoomRole.ROOM_ADMIN && callerRole != RoomRole.EXPERT) {
            throw new org.springframework.security.access.AccessDeniedException("Only room administrators, experts, or global moderators can create assignments");
        }

        Assignment assignment = Assignment.builder()
                .skill(skill)
                .title(title)
                .description(description)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);

        List<Rubric> rubrics = new ArrayList<>();
        if (rubricsDto != null) {
            for (RubricDto rd : rubricsDto) {
                Rubric rubric = Rubric.builder()
                        .assignment(savedAssignment)
                        .criterionName(rd.getCriterionName())
                        .maxPoints(rd.getMaxPoints())
                        .description(rd.getDescription())
                        .build();
                rubrics.add(rubricRepository.save(rubric));
            }
        }
        savedAssignment.setRubrics(rubrics);

        return mapToAssignmentDto(savedAssignment);
    }

    // 4. Получить задание по навыку
    public AssignmentDto getAssignmentBySkill(Long skillId) {
        List<Assignment> assignments = assignmentRepository.findBySkillId(skillId);
        if (assignments.isEmpty()) {
            throw new NotFoundException("Assignment not found for skill: " + skillId);
        }
        return mapToAssignmentDto(assignments.get(0));
    }

    // 5. Загрузить решение и запустить Task Dispatcher
    public SubmissionDto submitSolution(Long assignmentId, Long studentId, String solutionText, String fileUrl) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found with id: " + assignmentId));

        // Проверяем, есть ли уже решение от этого студента
        List<Submission> existingSubmissions = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        Submission submission;

        if (!existingSubmissions.isEmpty()) {
            submission = existingSubmissions.get(0);
            submission.setSolutionText(solutionText);
            submission.setFileUrl(fileUrl);
            submission.setStatus(SubmissionStatus.PENDING);
            submission = submissionRepository.save(submission);
            log.info("Updated existing submission ID {} for student {}", submission.getId(), studentId);
        } else {
            submission = Submission.builder()
                    .assignment(assignment)
                    .studentId(studentId)
                    .solutionText(solutionText)
                    .fileUrl(fileUrl)
                    .status(SubmissionStatus.PENDING)
                    .build();
            submission = submissionRepository.save(submission);
            log.info("Created new submission ID {} for student {}", submission.getId(), studentId);
        }

        // Переводим статус навыка в LEARNING, если нет записи
        Skill skill = assignment.getSkill();
        UserSkillId usId = new UserSkillId(studentId, skill.getId());
        if (!userSkillRepository.existsById(usId)) {
            UserSkill userSkill = UserSkill.builder()
                    .id(usId)
                    .skill(skill)
                    .status(UserSkillStatus.LEARNING)
                    .build();
            userSkillRepository.save(userSkill);
        }

        // --- Алгоритм Task Dispatcher ---
        runTaskDispatcher(submission, studentId, assignmentId);

        publishSubmissionSubmittedEvent(studentId, submission);

        return mapToSubmissionDto(submission);
    }

    private void runTaskDispatcher(Submission submission, Long studentId, Long assignmentId) {
        // Ищем другие сданные работы, у которых число рецензий меньше 2
        List<Submission> pendingCandidates = submissionRepository.findCandidateSubmissionsForReview(assignmentId, studentId, SubmissionStatus.PENDING);
        List<Submission> underReviewCandidates = submissionRepository.findCandidateSubmissionsForReview(assignmentId, studentId, SubmissionStatus.UNDER_REVIEW);

        List<Submission> candidates = new ArrayList<>();
        candidates.addAll(pendingCandidates);
        candidates.addAll(underReviewCandidates);

        // Назначаем до 2-х работ на проверку текущему студенту
        int assignedCount = 0;
        for (Submission candidate : candidates) {
            if (assignedCount >= 2) break;

            // Проверяем, не назначена ли уже эта работа этому студенту
            List<Review> existingReviews = reviewRepository.findBySubmissionId(candidate.getId());
            boolean alreadyAssigned = existingReviews.stream()
                    .anyMatch(r -> r.getReviewerId().equals(studentId));

            if (!alreadyAssigned) {
                Review review = Review.builder()
                        .submission(candidate)
                        .reviewerId(studentId)
                        .status(ReviewStatus.DRAFT)
                        .build();
                reviewRepository.save(review);

                // Обновляем статус работы кандидата на UNDER_REVIEW
                if (candidate.getStatus() == SubmissionStatus.PENDING) {
                    candidate.setStatus(SubmissionStatus.UNDER_REVIEW);
                    submissionRepository.save(candidate);
                }

                assignedCount++;
                log.info("Task Dispatcher: Assigned submission ID {} to reviewer {}", candidate.getId(), studentId);
            }
        }
    }

    // 6. Получить мои решения
    public List<SubmissionDto> getMySubmissions(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::mapToSubmissionDto)
                .collect(Collectors.toList());
    }

    // 7. Получить назначенные мне на проверку работы (DRAFT)
    public List<ReviewDto> getAssignedReviews(Long reviewerId) {
        return reviewRepository.findByReviewerIdAndStatus(reviewerId, ReviewStatus.DRAFT).stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsForSubmission(Long submissionId) {
        return reviewRepository.findBySubmissionIdAndStatus(submissionId, ReviewStatus.SUBMITTED).stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
    }

    // 8. Отправить рецензию (Peer Review) и запустить Skill Validator
    public ReviewDto submitReview(Long reviewId, Long reviewerId, String comment, List<ReviewGradeDto> gradesDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review assignment not found with id: " + reviewId));

        if (!review.getReviewerId().equals(reviewerId)) {
            throw new org.example.roomservice.exception.ForbiddenException("You are not authorized to submit this review");
        }

        if (review.getStatus() != ReviewStatus.DRAFT) {
            throw new AlreadyExistsException("Review has already been submitted");
        }

        // Сохраняем оценки по критериям
        if (review.getGrades() == null) {
            review.setGrades(new ArrayList<>());
        } else {
            review.getGrades().clear();
        }

        int totalScore = 0;
        if (gradesDto != null) {
            for (ReviewGradeDto gd : gradesDto) {
                Rubric rubric = rubricRepository.findById(gd.getRubricId())
                        .orElseThrow(() -> new NotFoundException("Rubric criterion not found: " + gd.getRubricId()));

                ReviewGrade grade = ReviewGrade.builder()
                        .review(review)
                        .rubric(rubric)
                        .score(Math.min(gd.getScore(), rubric.getMaxPoints())) // Ограничиваем максимальным баллом
                        .comment(gd.getComment())
                        .build();

                review.getGrades().add(grade);
                totalScore += grade.getScore();
            }
        }
        review.setComment(comment);
        review.setStatus(ReviewStatus.SUBMITTED);
        Review savedReview = reviewRepository.save(review);

        log.info("Review ID {} submitted by reviewer {}. Total score: {}", reviewId, reviewerId, totalScore);

        // Публикация события в RabbitMQ о выполнении ревью
        publishReviewSubmittedEvent(reviewerId, review.getSubmission());

        // --- Алгоритм Skill Validator ---
        runSkillValidator(review.getSubmission());

        return mapToReviewDto(savedReview);
    }

    private void runSkillValidator(Submission submission) {
        List<Review> submittedReviews = reviewRepository.findBySubmissionIdAndStatus(submission.getId(), ReviewStatus.SUBMITTED);

        // Проверяем, набралось ли минимум 2 завершенные рецензии
        if (submittedReviews.size() >= 2) {
            log.info("Skill Validator: Submission ID {} has {} submitted reviews. Evaluating score...", submission.getId(), submittedReviews.size());
            
            // Рассчитываем сумму баллов и максимальный возможный балл
            int totalEarned = 0;
            int totalMax = 0;

            for (Review r : submittedReviews) {
                List<ReviewGrade> grades = reviewGradeRepository.findByReviewId(r.getId());
                for (ReviewGrade g : grades) {
                    totalEarned += g.getScore();
                    totalMax += g.getRubric().getMaxPoints();
                }
            }

            double percent = totalMax > 0 ? (double) totalEarned / totalMax : 0.0;
            log.info("Submission ID {} score evaluation: {} / {} ({})", submission.getId(), totalEarned, totalMax, percent);

            if (percent >= 0.70) {
                submission.setStatus(SubmissionStatus.COMPLETED);
                submissionRepository.save(submission);
                log.info("Submission ID {} successfully COMPLETED!", submission.getId());

                // Обновляем статус навыка студента
                Skill skill = submission.getAssignment().getSkill();
                UserSkillId usId = new UserSkillId(submission.getStudentId(), skill.getId());
                UserSkill userSkill = userSkillRepository.findById(usId)
                        .orElseGet(() -> UserSkill.builder().id(usId).skill(skill).build());

                if (skill.getRequiresExpertValidation()) {
                    userSkill.setStatus(UserSkillStatus.COMPLETED);
                    log.info("Skill {} requires expert validation. UserSkill status set to COMPLETED for user {}", skill.getName(), submission.getStudentId());
                    
                    if (!expertValidationRequestRepository.existsBySubmissionIdAndStatus(submission.getId(), ExpertValidationStatus.PENDING)) {
                        ExpertValidationRequest request = ExpertValidationRequest.builder()
                                .submission(submission)
                                .status(ExpertValidationStatus.PENDING)
                                .build();
                        expertValidationRequestRepository.save(request);
                        log.info("Created ExpertValidationRequest for submission ID {}", submission.getId());
                    }
                } else {
                    userSkill.setStatus(UserSkillStatus.CONFIRMED);
                    log.info("Skill {} CONFIRMED (Растолған) for user {}", skill.getName(), submission.getStudentId());
                    
                    // Публикуем событие подтверждения навыка в RabbitMQ
                    publishSkillValidatedEvent(submission.getStudentId(), skill);
                }
                userSkillRepository.save(userSkill);
            } else {
                submission.setStatus(SubmissionStatus.NEEDS_REVISION);
                submissionRepository.save(submission);
                log.info("Submission ID {} score too low (less than 70%). Status set to NEEDS_REVISION", submission.getId());
            }
        }
    }

    private void publishReviewSubmittedEvent(Long reviewerId, Submission submission) {
        String eventId = UUID.randomUUID().toString();
        UserActionEvent event = UserActionEvent.builder()
                .eventId(eventId)
                .userId(reviewerId)
                .type(AchievementEventType.PEER_REVIEW_SUBMITTED)
                .targetId(submission.getId())
                .directionId(submission.getAssignment().getSkill().getRoom().getDirection().getId())
                .build();
        eventProducer.sendUserActionEvent(event);
    }

    private void publishSkillValidatedEvent(Long studentId, Skill skill) {
        String eventId = UUID.randomUUID().toString();
        UserActionEvent event = UserActionEvent.builder()
                .eventId(eventId)
                .userId(studentId)
                .type(AchievementEventType.SKILL_VALIDATED)
                .targetId(skill.getId())
                .directionId(skill.getRoom().getDirection().getId())
                .build();
        eventProducer.sendUserActionEvent(event);
    }

    private void publishSubmissionSubmittedEvent(Long studentId, Submission submission) {
        String eventId = UUID.randomUUID().toString();
        UserActionEvent event = UserActionEvent.builder()
                .eventId(eventId)
                .userId(studentId)
                .type(AchievementEventType.SUBMISSION_SUBMITTED)
                .targetId(submission.getId())
                .directionId(submission.getAssignment().getSkill().getRoom().getDirection().getId())
                .build();
        eventProducer.sendUserActionEvent(event);
    }

    // --- Мапперы ---
    private SkillDto mapToSkillDto(Skill skill, UserSkillStatus status) {
        return SkillDto.builder()
                .id(skill.getId())
                .roomId(skill.getRoom().getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .requiresExpertValidation(skill.getRequiresExpertValidation())
                .createdAt(skill.getCreatedAt())
                .userStatus(status)
                .build();
    }

    private AssignmentDto mapToAssignmentDto(Assignment assignment) {
        List<RubricDto> rubrics = assignment.getRubrics() != null ?
                assignment.getRubrics().stream()
                        .map(r -> RubricDto.builder()
                                .id(r.getId())
                                .criterionName(r.getCriterionName())
                                .maxPoints(r.getMaxPoints())
                                .description(r.getDescription())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        return AssignmentDto.builder()
                .id(assignment.getId())
                .skillId(assignment.getSkill().getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .rubrics(rubrics)
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    private SubmissionDto mapToSubmissionDto(Submission submission) {
        return SubmissionDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .studentId(submission.getStudentId())
                .solutionText(submission.getSolutionText())
                .fileUrl(submission.getFileUrl())
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    private ReviewDto mapToReviewDto(Review review) {
        List<ReviewGradeDto> grades = review.getGrades() != null ?
                review.getGrades().stream()
                        .map(g -> ReviewGradeDto.builder()
                                .rubricId(g.getRubric().getId())
                                .score(g.getScore())
                                .comment(g.getComment())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<RubricDto> rubrics = review.getSubmission().getAssignment().getRubrics() != null ?
                review.getSubmission().getAssignment().getRubrics().stream()
                        .map(r -> RubricDto.builder()
                                .id(r.getId())
                                .criterionName(r.getCriterionName())
                                .maxPoints(r.getMaxPoints())
                                .description(r.getDescription())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        return ReviewDto.builder()
                .id(review.getId())
                .submissionId(review.getSubmission().getId())
                .reviewerId(review.getReviewerId())
                .status(review.getStatus())
                .comment(review.getComment())
                .grades(grades)
                .createdAt(review.getCreatedAt())
                .solutionText(review.getSubmission().getSolutionText())
                .fileUrl(review.getSubmission().getFileUrl())
                .assignmentTitle(review.getSubmission().getAssignment().getTitle())
                .assignmentDescription(review.getSubmission().getAssignment().getDescription())
                .rubrics(rubrics)
                .build();
    }

    // Получить ожидающие проверки работы для эксперта
    public List<ExpertValidationRequestDto> getPendingValidationsForExpert(Long userId) {
        List<UserDirection> directions = userDirectionRepository.findById_UserId(userId);
        if (directions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> directionIds = directions.stream().map(d -> d.getId().getDirectionId()).collect(Collectors.toList());
        
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(r -> directionIds.contains(r.getDirection().getId()))
                .collect(Collectors.toList());
        if (rooms.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());

        return expertValidationRequestRepository.findBySubmissionAssignmentSkillRoomIdInAndStatus(roomIds, ExpertValidationStatus.PENDING).stream()
                .map(this::mapToExpertValidationDto)
                .collect(Collectors.toList());
    }

    // Решить запрос на валидацию экспертом
    public void resolveValidationRequest(Long requestId, Long expertId, boolean approved, String comment) {
        ExpertValidationRequest request = expertValidationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Validation request not found: " + requestId));

        if (request.getStatus() != ExpertValidationStatus.PENDING) {
            throw new AlreadyExistsException("Request has already been processed");
        }

        Long directionId = request.getSubmission().getAssignment().getSkill().getRoom().getDirection().getId();
        boolean isExpert = userDirectionRepository.existsById(new UserDirectionId(expertId, directionId));
        if (!isExpert) {
            throw new org.example.roomservice.exception.ForbiddenException("You are not an expert for this direction");
        }

        request.setExpertId(expertId);
        request.setStatus(approved ? ExpertValidationStatus.APPROVED : ExpertValidationStatus.REJECTED);
        request.setComment(comment);
        expertValidationRequestRepository.save(request);

        Submission submission = request.getSubmission();
        Skill skill = submission.getAssignment().getSkill();
        UserSkillId usId = new UserSkillId(submission.getStudentId(), skill.getId());
        UserSkill userSkill = userSkillRepository.findById(usId)
                .orElseThrow(() -> new NotFoundException("UserSkill not found"));

        if (approved) {
            userSkill.setStatus(UserSkillStatus.CONFIRMED);
            submission.setStatus(SubmissionStatus.COMPLETED);
            userSkillRepository.save(userSkill);
            submissionRepository.save(submission);
            publishSkillValidatedEvent(submission.getStudentId(), skill);
            log.info("Expert approved validation request {}. Skill confirmed.", requestId);
        } else {
            userSkill.setStatus(UserSkillStatus.LEARNING);
            submission.setStatus(SubmissionStatus.NEEDS_REVISION);
            userSkillRepository.save(userSkill);
            submissionRepository.save(submission);
            log.info("Expert rejected validation request {}. Submission sent to revision.", requestId);
        }
    }

    // Подать спор (апелляцию)
    public DisputeDto createDispute(Long submissionId, Long studentId, String reason) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found: " + submissionId));

        if (!submission.getStudentId().equals(studentId)) {
            throw new org.example.roomservice.exception.ForbiddenException("You can only dispute your own submissions");
        }

        if (submission.getStatus() != SubmissionStatus.NEEDS_REVISION) {
            throw new AlreadyExistsException("You can only dispute submissions that require revision");
        }

        if (disputeRepository.existsBySubmissionIdAndStatus(submissionId, DisputeStatus.PENDING)) {
            throw new AlreadyExistsException("There is already a pending dispute for this submission");
        }

        Dispute dispute = Dispute.builder()
                .submission(submission)
                .studentId(studentId)
                .status(DisputeStatus.PENDING)
                .reason(reason)
                .build();

        Dispute saved = disputeRepository.save(dispute);
        log.info("Dispute created for submission {} by student {}", submissionId, studentId);
        return mapToDisputeDto(saved);
    }

    public List<DisputeDto> getDisputesForSubmission(Long submissionId) {
        return disputeRepository.findBySubmissionId(submissionId).stream()
                .map(this::mapToDisputeDto)
                .collect(Collectors.toList());
    }

    public boolean isModeratorOfRoom(Long userId, Room room) {
        RoomRole role = userRoomService.getEffectiveRole(userId, room);
        return role == RoomRole.ROOM_ADMIN || role == RoomRole.EXPERT || role == RoomRole.MODERATOR;
    }

    // Получить список всех споров (для модератора)
    public List<DisputeDto> getPendingDisputes(Long userId) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isGlobalAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Dispute> allPending = disputeRepository.findByStatus(DisputeStatus.PENDING);

        if (isGlobalAdmin) {
            return allPending.stream()
                    .map(this::mapToDisputeDto)
                    .collect(Collectors.toList());
        }

        return allPending.stream()
                .filter(dispute -> {
                    Room room = dispute.getSubmission().getAssignment().getSkill().getRoom();
                    return isModeratorOfRoom(userId, room);
                })
                .map(this::mapToDisputeDto)
                .collect(Collectors.toList());
    }

    // Разрешить спор модератором
    public void resolveDispute(Long disputeId, Long moderatorId, boolean approved, String comment) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("Dispute not found: " + disputeId));

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new AlreadyExistsException("Dispute has already been resolved");
        }

        // Проверка прав: глобальный ADMIN, либо локальный ROOM_ADMIN/EXPERT/MODERATOR этой комнаты
        Room room = dispute.getSubmission().getAssignment().getSkill().getRoom();
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isGlobalAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isGlobalAdmin && !isModeratorOfRoom(moderatorId, room)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to resolve disputes in this room");
        }

        dispute.setModeratorId(moderatorId);
        dispute.setStatus(approved ? DisputeStatus.RESOLVED_APPROVED : DisputeStatus.RESOLVED_REJECTED);
        dispute.setResolutionComment(comment);
        disputeRepository.save(dispute);

        Submission submission = dispute.getSubmission();
        if (approved) {
            submission.setStatus(SubmissionStatus.COMPLETED);
            submissionRepository.save(submission);
            log.info("Moderator resolved dispute {}. Submission approved.", disputeId);

            Skill skill = submission.getAssignment().getSkill();
            UserSkillId usId = new UserSkillId(submission.getStudentId(), skill.getId());
            UserSkill userSkill = userSkillRepository.findById(usId)
                    .orElseGet(() -> UserSkill.builder().id(usId).skill(skill).build());

            if (skill.getRequiresExpertValidation()) {
                userSkill.setStatus(UserSkillStatus.COMPLETED);
                userSkillRepository.save(userSkill);
                
                if (!expertValidationRequestRepository.existsBySubmissionIdAndStatus(submission.getId(), ExpertValidationStatus.PENDING)) {
                    ExpertValidationRequest request = ExpertValidationRequest.builder()
                            .submission(submission)
                            .status(ExpertValidationStatus.PENDING)
                            .build();
                    expertValidationRequestRepository.save(request);
                }
            } else {
                userSkill.setStatus(UserSkillStatus.CONFIRMED);
                userSkillRepository.save(userSkill);
                publishSkillValidatedEvent(submission.getStudentId(), skill);
            }
        } else {
            submission.setStatus(SubmissionStatus.NEEDS_REVISION);
            submissionRepository.save(submission);
            log.info("Moderator resolved dispute {}. Submission kept in NEEDS_REVISION.", disputeId);
        }
    }

    private ExpertValidationRequestDto mapToExpertValidationDto(ExpertValidationRequest request) {
        Submission sub = request.getSubmission();
        return ExpertValidationRequestDto.builder()
                .id(request.getId())
                .submissionId(sub.getId())
                .studentId(sub.getStudentId())
                .solutionText(sub.getSolutionText())
                .fileUrl(sub.getFileUrl())
                .assignmentTitle(sub.getAssignment().getTitle())
                .assignmentDescription(sub.getAssignment().getDescription())
                .skillName(sub.getAssignment().getSkill().getName())
                .status(request.getStatus())
                .comment(request.getComment())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private DisputeDto mapToDisputeDto(Dispute dispute) {
        Submission sub = dispute.getSubmission();
        return DisputeDto.builder()
                .id(dispute.getId())
                .submissionId(sub.getId())
                .studentId(sub.getStudentId())
                .solutionText(sub.getSolutionText())
                .fileUrl(sub.getFileUrl())
                .assignmentTitle(sub.getAssignment().getTitle())
                .assignmentDescription(sub.getAssignment().getDescription())
                .reason(dispute.getReason())
                .resolutionComment(dispute.getResolutionComment())
                .status(dispute.getStatus())
                .moderatorId(dispute.getModeratorId())
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}

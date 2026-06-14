package org.example.roomservice.service;

import org.example.roomservice.dto.*;
import org.example.roomservice.model.*;
import org.example.roomservice.producer.EducationEventProducer;
import org.example.roomservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EducationServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ExpertValidationRequestRepository expertValidationRequestRepository;
    @Mock
    private DisputeRepository disputeRepository;
    @Mock
    private UserDirectionRepository userDirectionRepository;
    @Mock
    private EducationEventProducer eventProducer;
    @Mock
    private UserRoomService userRoomService;

    @InjectMocks
    private EducationService educationService;

    @Test
    void testExpertValidation_ApprovedShouldConfirmSkill() {
        // Arrange
        Long requestId = 1L;
        Long expertId = 2L;
        Long directionId = 10L;
        Long studentId = 3L;
        Long skillId = 4L;

        Direction direction = Direction.builder().id(directionId).build();
        Room room = Room.builder().id(5L).direction(direction).build();
        Skill skill = Skill.builder().id(skillId).room(room).requiresExpertValidation(true).build();
        Assignment assignment = Assignment.builder().id(6L).skill(skill).build();
        Submission submission = Submission.builder().id(7L).studentId(studentId).assignment(assignment).status(SubmissionStatus.UNDER_REVIEW).build();
        
        ExpertValidationRequest request = ExpertValidationRequest.builder()
                .id(requestId)
                .submission(submission)
                .status(ExpertValidationStatus.PENDING)
                .build();

        UserSkill userSkill = UserSkill.builder()
                .id(new UserSkillId(studentId, skillId))
                .skill(skill)
                .status(UserSkillStatus.LEARNING)
                .build();

        when(expertValidationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userDirectionRepository.existsById(new UserDirectionId(expertId, directionId))).thenReturn(true);
        when(userSkillRepository.findById(new UserSkillId(studentId, skillId))).thenReturn(Optional.of(userSkill));

        // Act
        educationService.resolveValidationRequest(requestId, expertId, true, "Looks great!");

        // Assert
        assertEquals(ExpertValidationStatus.APPROVED, request.getStatus());
        assertEquals("Looks great!", request.getComment());
        assertEquals(expertId, request.getExpertId());
        assertEquals(UserSkillStatus.CONFIRMED, userSkill.getStatus());
        assertEquals(SubmissionStatus.COMPLETED, submission.getStatus());

        verify(expertValidationRequestRepository).save(request);
        verify(userSkillRepository).save(userSkill);
        verify(submissionRepository).save(submission);
        verify(eventProducer).sendUserActionEvent(any());
    }

    @Test
    void testDisputeResolution_ApprovedShouldCompleteSubmission() {
        // Arrange
        Long disputeId = 1L;
        Long moderatorId = 2L;
        Long studentId = 3L;
        Long skillId = 4L;
        Long directionId = 10L;

        Direction direction = Direction.builder().id(directionId).build();
        Room room = Room.builder().id(5L).direction(direction).build();
        Skill skill = Skill.builder().id(skillId).room(room).requiresExpertValidation(false).build();
        Assignment assignment = Assignment.builder().id(5L).skill(skill).build();
        Submission submission = Submission.builder().id(6L).studentId(studentId).assignment(assignment).status(SubmissionStatus.NEEDS_REVISION).build();

        Dispute dispute = Dispute.builder()
                .id(disputeId)
                .submission(submission)
                .studentId(studentId)
                .status(DisputeStatus.PENDING)
                .reason("Peer reviews were unfair")
                .build();

        UserSkill userSkill = UserSkill.builder()
                .id(new UserSkillId(studentId, skillId))
                .skill(skill)
                .status(UserSkillStatus.LEARNING)
                .build();

        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(dispute));
        when(userSkillRepository.findById(new UserSkillId(studentId, skillId))).thenReturn(Optional.of(userSkill));
        when(userRoomService.getEffectiveRole(moderatorId, room)).thenReturn(RoomRole.MODERATOR);

        // Act
        educationService.resolveDispute(disputeId, moderatorId, true, "Appealed successfully");

        // Assert
        assertEquals(DisputeStatus.RESOLVED_APPROVED, dispute.getStatus());
        assertEquals("Appealed successfully", dispute.getResolutionComment());
        assertEquals(moderatorId, dispute.getModeratorId());
        assertEquals(SubmissionStatus.COMPLETED, submission.getStatus());
        assertEquals(UserSkillStatus.CONFIRMED, userSkill.getStatus());

        verify(disputeRepository).save(dispute);
        verify(submissionRepository).save(submission);
        verify(userSkillRepository).save(userSkill);
        verify(eventProducer).sendUserActionEvent(any());
    }
}

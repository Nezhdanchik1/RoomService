package org.example.roomservice.repository;

import org.example.roomservice.model.ExpertValidationRequest;
import org.example.roomservice.model.ExpertValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExpertValidationRequestRepository extends JpaRepository<ExpertValidationRequest, Long> {
    List<ExpertValidationRequest> findBySubmissionAssignmentSkillRoomIdInAndStatus(Collection<Long> roomIds, ExpertValidationStatus status);
    List<ExpertValidationRequest> findByStatus(ExpertValidationStatus status);
    boolean existsBySubmissionIdAndStatus(Long submissionId, ExpertValidationStatus status);
}

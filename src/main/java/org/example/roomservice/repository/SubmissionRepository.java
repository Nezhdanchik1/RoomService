package org.example.roomservice.repository;

import org.example.roomservice.model.Submission;
import org.example.roomservice.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<Submission> findByStudentId(Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.studentId != :studentId " +
           "AND s.status = :status AND (SELECT COUNT(r) FROM Review r WHERE r.submission.id = s.id) < 2")
    List<Submission> findCandidateSubmissionsForReview(@Param("assignmentId") Long assignmentId, 
                                                       @Param("studentId") Long studentId, 
                                                       @Param("status") SubmissionStatus status);
}

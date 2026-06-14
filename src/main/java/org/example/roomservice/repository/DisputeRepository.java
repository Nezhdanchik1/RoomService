package org.example.roomservice.repository;

import org.example.roomservice.model.Dispute;
import org.example.roomservice.model.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByStatus(DisputeStatus status);
    List<Dispute> findBySubmissionId(Long submissionId);
    boolean existsBySubmissionIdAndStatus(Long submissionId, DisputeStatus status);
}

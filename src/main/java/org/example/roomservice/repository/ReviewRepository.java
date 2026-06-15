package org.example.roomservice.repository;

import org.example.roomservice.model.Review;
import org.example.roomservice.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewerIdAndStatus(Long reviewerId, ReviewStatus status);
    List<Review> findBySubmissionId(Long submissionId);
    List<Review> findBySubmissionIdAndStatus(Long submissionId, ReviewStatus status);
    long countByReviewerIdAndStatus(Long reviewerId, ReviewStatus status);
}

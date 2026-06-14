package org.example.roomservice.repository;

import org.example.roomservice.model.ReviewGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewGradeRepository extends JpaRepository<ReviewGrade, Long> {
    List<ReviewGrade> findByReviewId(Long reviewId);
}

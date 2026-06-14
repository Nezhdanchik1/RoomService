package org.example.roomservice.repository;

import org.example.roomservice.model.UserSkill;
import org.example.roomservice.model.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    List<UserSkill> findByIdUserId(Long userId);
    Optional<UserSkill> findByIdUserIdAndIdSkillId(Long userId, Long skillId);
}

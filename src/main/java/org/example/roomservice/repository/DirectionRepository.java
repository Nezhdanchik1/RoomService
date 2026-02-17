package org.example.roomservice.repository;

import org.example.roomservice.model.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long> {

//    Optional<Direction> findByName(String name);

    boolean existsByName(String name);
}

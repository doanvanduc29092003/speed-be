package org.example.speeded.Repository;

import org.example.speeded.Entity.Run;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunRepository extends JpaRepository<Run, Long> {
    List<Run> findByUserId(Long userId);
}

package org.example.diploma.repository;

import org.example.diploma.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByModuleId(Long moduleId);
    void deleteByModuleId(Long moduleId);
}

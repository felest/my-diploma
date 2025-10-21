package org.example.diploma.repository;

import org.example.diploma.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByTeacherId(Long teacherId);

    @Query("SELECT g FROM Group g JOIN g.assignedModules m WHERE m.id = :moduleId")
    List<Group> findGroupsByModuleId(Long moduleId);

    boolean existsByNameAndTeacherId(String name, Long teacherId);
}

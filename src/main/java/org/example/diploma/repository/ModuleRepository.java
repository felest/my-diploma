package org.example.diploma.repository;

import org.example.diploma.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByUserId(Long userId);
    boolean existsByTitleAndUserId(String title, Long userId);
}
